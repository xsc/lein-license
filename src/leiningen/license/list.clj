(ns leiningen.license.list
  (:require [leiningen.core.main :as main]
            [cheshire.core :as json]
            [clojure.java.io :as io]))

;; ## Query

(def ^:dynamic *api-url*
  "https://api.github.com/repos/github/choosealicense.com/contents/_licenses")

(defn- query-licenses!
  []
  (->> (-> (slurp *api-url* :encoding "UTF-8")
           (json/parse-string keyword))
       (keep
         (fn [{:keys [^String name]}]
           (when name
             (when-let [[_ license-name] (re-matches #"(.+)\.(txt|html)" name)]
               license-name))))))

;; ## License List

(defn list-licenses
  []
  (let [home (or (some-> (System/getenv "LEIN_HOME") io/file)
                 (io/file (System/getProperty "user.home") ".lein"))
        cache-file (io/file home ".licenses.edn")
        delta (when (.isFile cache-file)
                (- (System/currentTimeMillis) (.lastModified cache-file)))]
    (or (try
          (when (some-> delta (< 3600000))
            (binding [*read-eval* false]
              (when-let [result (seq (read-string (slurp cache-file :encoding "UTF-8")))]
                result)))
          (catch Exception _
            (main/warn "could not read local license cache.")))
        (let [licenses (query-licenses!)]
          (try
            (spit cache-file (pr-str licenses))
            (catch Exception _
              (main/warn "could not update local license cache.")))
          licenses))))

(defn match-licenses
  [^String prefix]
  (if (seq prefix)
    (filter #(.startsWith ^String % prefix) (list-licenses))
    (list-licenses)))
