(ns leiningen.new.smartcard
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files year]]
            [leiningen.core.main :as main]))

(def render (renderer "smartcard"))

(defn smartcard
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)
              :year (year)}]
    (main/info "Generating fresh 'lein new' smartcard project.")
    (->files data
             [".gitignore" (render "gitignore" data)]
             ["project.clj" (render "project.clj" data)]
             ["README.md" (render "README.md" data)]
             ["src/java/{{sanitized}}/Sokoban.java" (render "Sokoban.java" data)]
             ["src/clojure/{{sanitized}}/sokoban.clj" (render "sokoban.clj" data)]
             ["src/clojure/{{sanitized}}/hello_world.clj" (render "hello_world.clj" data)])))
