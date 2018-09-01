(defproject lein-license "0.1.8"
  :description "Project-Level License Management."
  :url "https://github.com/xsc/lein-license"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :key "mit"
            :year 2015}
  :dependencies [^:source-dep [org.yaml/snakeyaml "1.17"]
                 ^:source-dep [rewrite-clj "0.6.1" :exclusions [org.clojure/clojure]]
                 ^:source-dep [cheshire "5.8.0"]]
  :plugins [[lein-isolate "0.1.1"]]
  :middleware [leiningen.isolate/middleware]
  :eval-in :leiningen
  :pedantic? :abort)
