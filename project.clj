(defproject smartcard "0.1.3"
  :description "Simple library for Smartcards"
  :url "http://github.com/profitware/smartcard"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :plugins [[jonase/eastwood "0.3.4"]
            [lein-cljfmt "0.6.2"]
            [lein-bump-version "0.1.6"]]
  :cljfmt {:remove-consecutive-blank-lines? false}
  :aliases {"lint" ["do" ["cljfmt" "check"] ["eastwood"]]
            "test" ["lint"]}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.licel/jcardsim "2.2.2"]]
  :profiles {:1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}})
