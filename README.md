# Smartcard

[![Clojars](https://img.shields.io/clojars/v/smartcard.svg)](https://clojars.org/smartcard)
[![Travis](https://img.shields.io/travis/profitware/smartcard.svg)](https://travis-ci.org/profitware/smartcard)
[![Dependencies Status](https://versions.deps.co/profitware/smartcard/status.svg)](https://versions.deps.co/profitware/smartcard)

The `Smartcard` library is a simple wrapper around Java Smartcard library.

## Installation

Prerequisites include installed `pcsc-lite` and `pcsc-tools` software and `pscsd` service up and running.

To install, add the following to your project `:dependencies`:

    [smartcard "0.1.0"]

Or use the [Leiningen](https://leiningen.org/) template to build a new application from scratch:

    lein new smartcard my-smartcard-application

## Usage

```clj
(use 'smartcard.core)

(def applet-id [0x4f 0x46 0x46 0x5a 0x4f 0x4e 0x45 0x32 0x10 0x01])

(defsmartcard applet-function applet-id
  (let [line (->> (.transmit card [0x10 0x30 0x00 0x00 0x00])
                  .getData
                  bytes-to-str)]
    (println line)))

(applet-function)
```

## License

Copyright © 2018 Sergey Sobko

Distributed under the MIT License. See LICENSE.

All the libraries and systems are licensed and remain the property of their respective owners.
