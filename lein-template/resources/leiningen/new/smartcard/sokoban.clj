(ns {{name}}.sokoban
  (:require [smartcard.core :refer [defsmartcard
                                    bytes-to-str]])
  (:import (jline Terminal)))


(defmacro defdirection [function byte]
  `(defn ~function [card#]
     (.transmit card# [0x10 0x20 0x00 0x00 0x01 ~byte])))


(defdirection left 0x61)
(defdirection right 0x64)
(defdirection up 0x77)
(defdirection down 0x73)


(defn reset [card]
  (.transmit card [0x10 0x40 0x00 0x00 0x00]))


(defsmartcard sokoban [0x4f 0x46 0x46 0x5a 0x4f 0x4e 0x45 0x32 0x10 0x01]
  (doall (for [line (->> (.transmit card [0x10 0x30 0x00 0x00 0x00])
                         .getData
                         bytes-to-str
                         (partition 12)
                         (map (partial apply str)))]
           (println line))))


(defn -main [& [steps]]
  (let [term (Terminal/getTerminal)
        remaining-steps (atom steps)]
    (while true
      (print "\033[H\033[2J")
      (sokoban (case (if @remaining-steps
                       (let [[next-step & remaining] @remaining-steps]
                         (reset! remaining-steps remaining)
                         (case next-step
                           \u 105 \U 105
                           \l 106 \L 106
                           \d 107 \D 107
                           \r 108 \R 108))
                       (.readCharacter term System/in))
                 105 up
                 106 left
                 107 down
                 108 right
                 32 reset
                 identity))
      (when @remaining-steps
        (Thread/sleep 100)))))
