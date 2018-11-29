(defproject {{name}} "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [jline "0.9.94"]
                 [smartcard "0.1.0"]]
  :jvm-opts ["-Dsun.security.smartcardio.library=/usr/lib64/libpcsclite.so.1"]
  :main ^:skip-aot {{name}}.sokoban
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
