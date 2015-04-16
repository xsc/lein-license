(defproject lein-license "0.1.0-SNAPSHOT"
  :description "Project-Level License Management."
  :url "https://github.com/xsc/lein-license"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"
            :key "mit"
            :year 2015}
  :dependencies [[org.yaml/snakeyaml "1.15"]
                 [rewrite-clj "0.4.12"]]
  :eval-in :leiningen
  :pedantic? :abort)
