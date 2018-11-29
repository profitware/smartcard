(ns smartcard.core
  (:import (javax.smartcardio TerminalFactory
                              CommandAPDU)))


(defprotocol SmartCardProto
  (disconnect [card])
  (transmit [card data])
  (select-applet [card aid]))


(defrecord SmartCard [conn channel]
  SmartCardProto
  (disconnect [card] (.disconnect conn false))
  (transmit [card data] (.transmit channel (-> data
                                               byte-array
                                               (CommandAPDU.))))
  (select-applet [card aid] (let [select-management [0x00 0xA4 0x04 0x00]
                                  data (concat select-management
                                               [(count aid)]
                                               aid)]
                              (.transmit card data))))


(defn get-smartcard []
  (let [terminal-factory (TerminalFactory/getDefault)
        terminal (-> terminal-factory
                     .terminals
                     .list
                     first)
        conn (.connect terminal "*")
        channel (.getBasicChannel conn)]
    (->SmartCard conn channel)))


(defn bytes-to-str [data]
  (apply str (map char data)))


(defmacro defsmartcard [function-name applet-id & body]
  `(defn ~function-name [& [function#]]
     (let [~'card (get-smartcard)
           result# (atom nil)]
       (.select-applet ~'card ~applet-id)
       (when function#
         (reset! result# (function# ~'card)))
       ~@body
       (.disconnect ~'card)
       @result#)))
