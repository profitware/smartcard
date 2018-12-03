(ns smartcard.core
  (:import (java.security Security)
           (javax.smartcardio TerminalFactory
                              CommandAPDU
                              CardException)
           (com.licel.jcardsim.smartcardio JCardSimProvider)
           (javacard.framework ISO7816)))


(defprotocol SmartCardProto
  (disconnect [card])
  (transmit [card data])
  (select-applet [card aid]))


(def ^:dynamic *select-cmd* [ISO7816/CLA_ISO7816 ISO7816/INS_SELECT 0x04 0x00])


(defrecord SmartCard [conn channel]
  SmartCardProto
  (disconnect [card] (.disconnect conn false))
  (transmit [card data] (.transmit channel (-> data
                                               byte-array
                                               (CommandAPDU.))))
  (select-applet [card aid] (let [select-management *select-cmd*
                                  data (concat select-management
                                               [(count aid)]
                                               aid)]
                              (.transmit card data))))


(defn- set-simulator-applets! [simulator-applets]
  (doall (map #(let [[index [aid classname]] %
                     str-aid (apply str
                                    (map (partial format "%02x")
                                         aid))
                     str-aid-property (str "com.licel.jcardsim.card.applet." index ".AID")
                     class-property (str "com.licel.jcardsim.card.applet." index ".Class")]
                 (System/setProperty str-aid-property str-aid)
                 (System/setProperty class-property classname))
              (map-indexed vector simulator-applets))))


(defn- install-simulator-applets! [card simulator-applets]
  (doall (map #(let [[index [aid classname]] %
                     data (concat [(count aid)]
                                  aid
                                  [5 0 0 2 0x0f 0x0f])]
                 (assert (= (.getSW (.transmit card (concat [0x80 0xb8 0 0]
                                                            [(count data)]
                                                            data)))
                            0x9000)))
              (map-indexed vector simulator-applets))))


(defn- unset-simulator-applets! [simulator-applets]
  (doall (map #(let [index %
                     aid-property (str "com.licel.jcardsim.card.applet." index ".AID")
                     class-property (str "com.licel.jcardsim.card.applet." index ".Class")]
                 (System/clearProperty aid-property)
                 (System/clearProperty class-property))
              (range (count simulator-applets)))))


(defn get-smartcard [& [terminal-filter-fn simulator-applets]]
  (when-not (Security/getProvider "jCardSim")
    (Security/addProvider (JCardSimProvider.)))
  (when simulator-applets
    (set-simulator-applets! simulator-applets))
  (let [terminal-factories [(TerminalFactory/getDefault)
                            (TerminalFactory/getInstance "jCardSim" nil)]
        terminals (apply concat
                         (map #(try
                                 (-> %
                                     .terminals
                                     .list)
                                 (catch CardException _
                                   nil))
                              terminal-factories))
        terminal (first (filter (or terminal-filter-fn
                                    #(.isCardPresent %))
                                terminals))
        conn (.connect terminal "*")
        channel (.getBasicChannel conn)
        card (->SmartCard conn channel)]
    (when simulator-applets
      (->> simulator-applets
           (install-simulator-applets! card)
           (unset-simulator-applets!)))
    card))


(defn bytes-to-str [data]
  (apply str (map char data)))


(def ^:dynamic *simulator-applets* nil)
(def ^:dynamic *terminal-filter-fn* nil)


(defmacro with-terminal-filter-fn [terminal-filter-fn & body]
  `(binding [*terminal-filter-fn* ~terminal-filter-fn]
     ~@body))


(defmacro with-simulator-applets [simulator-applets & body]
  `(binding [*simulator-applets* ~simulator-applets]
     ~@body))


(defmacro defsmartcard [function-name applet-id & body]
  `(let [terminal-filter-fn# *terminal-filter-fn*
         simulator-applets# *simulator-applets*]
     (defn ~function-name [& [function#]]
       (let [~'card (get-smartcard terminal-filter-fn#
                                   simulator-applets#)
             result# (atom nil)]
         (.select-applet ~'card ~applet-id)
         (when function#
           (reset! result# (function# ~'card)))
         ~@body
         (.disconnect ~'card)
         @result#))))
