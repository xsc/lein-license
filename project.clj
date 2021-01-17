(defproject lein-license "1.0.1-SNAPSHOT"
  :description "Project-Level License Management."
  :url "https://github.com/xsc/lein-license"
  :license {:name "MIT"
            :url "https://choosealicense.com/licenses/mit"
            :key "mit"
            :year 2015
            :comment "MIT License"}
  :dependencies [^:source-dep [org.yaml/snakeyaml "1.17"]
                 ^:source-dep [rewrite-clj "0.6.1" :exclusions [org.clojure/clojure]]
                 ^:source-dep [cheshire "5.8.0"]]
  :plugins [[lein-isolate "0.1.1"]]
  :middleware [leiningen.isolate/middleware]
  :eval-in :leiningen
  :pedantic? :abort)
