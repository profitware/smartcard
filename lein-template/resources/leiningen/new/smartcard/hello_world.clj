(ns {{name}}.hello-world
  (:require [smartcard.core :refer [defsmartcard
                                    bytes-to-str
                                    with-terminal-filter-fn
                                    with-simulator-applets]])
  (:import (com.licel.jcardsim.smartcardio JCSTerminal)))


(let [aid (range 1 9)
      classname "com.licel.jcardsim.samples.HelloWorldApplet"
      hello-cmd [0x00 0x01 0x00 0x00]]
  (with-terminal-filter-fn (fn [terminal]
                             (identical? (class terminal)
                                         JCSTerminal))
    (with-simulator-applets {aid classname}
      (defsmartcard hello aid
        (.select-applet card aid)
        (println (bytes-to-str (.getData (.transmit card hello-cmd))))))))


(defn -main []
  (hello))
