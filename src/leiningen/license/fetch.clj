(ns leiningen.license.fetch
  (:require [leiningen.license.list :refer [match-licenses]]
            [leiningen.core.main :as main]
            [clojure.string :as string])
  (:import [org.yaml.snakeyaml Yaml]
           [org.yaml.snakeyaml.scanner ScannerException]))

;; ## URLs

(def ^:dynamic *raw-prefix*
  "https://raw.githubusercontent.com/github/choosealicense.com/gh-pages/_licenses/")

(def ^:private +license-base-url+
  "https://choosealicense.com/licenses/")

(defn- raw-urls
  "Generate URL to raw license data."
  [license-key]
  (let [s (-> (name license-key)
              (string/replace #"\s+|_" "-")
              (string/lower-case))]
    (vector
      (str *raw-prefix* s ".txt")
      (str *raw-prefix* s ".html"))))

;; ## Parser

(def ^:private license-regex
  #"(?s)^---\n(.+)\n---\n(.*)$")

(def ^:private required-keys
  #{:description :title :permalink})

(defn- ensure-required-keys
  [data]
  (if-let [missing (seq (remove (set (keys data)) required-keys))]
    (throw
      (Exception.
        (str "required keys missing: " (pr-str (vec missing)))))
    data))

(def ^:private parse-license-yaml
  "Parse a YAML string into a map. Returns a map with either the single key
   `:value` or `:error`."
  (let [yaml (Yaml.)]
    (fn [^String data license-key]
      (try
        (if-let [value (.load yaml data)]
          (if (or (map? value) (instance? java.util.LinkedHashMap value))
            (->> (for [[k v] value
                       :let [s (string/replace (name k) "_" "-")
                             v' (if (instance? java.util.ArrayList v)
                                  (mapv keyword v)
                                  v)]]
                   [(keyword s) v'])
                 (into
                   {:permalink (format "/_licenses/%s.txt" license-key)})
                 (ensure-required-keys)
                 (hash-map :value))
            {:error "YAML value does not represent a map."})
          {:error "could not read YAML value."})
        (catch ScannerException ex
          {:error (str "invalid YAML: " (.getMessage ex))})
        (catch Exception ex
          {:error (.getMessage ex)})))))

(defn- normalize-template
  [^String s]
  (string/replace s #"(^[\n\r]+|[\n\r]+$)" ""))

(defn- parse-license
  "Parse raw license data, returning a map of either `:error` or the
   desired data."
  [^String data license-key]
  (when-let [[_ info template] (re-find license-regex data)]
    (let [{:keys [value error]} (parse-license-yaml info license-key)]
      (if error
        {:error error}
        (-> value
            (merge
              {:text (normalize-template template)
               :key  license-key})
            (update :source #(or % (str +license-base-url+ license-key))))))))

;; ## License I/O

(defn fetch-license!
  "Fetch license data from the `github/choosealicense.com` repository."
  [license-key & [seen]]
  (if-not (contains? seen license-key)
    (or (let [urls (raw-urls license-key)]
          (main/debug "reading raw license data from:" urls)
          (some-> (some
                    #(try
                       (slurp % :encoding "UTF-8")
                       (catch java.io.FileNotFoundException _))
                    urls)
                  (parse-license license-key)))
        (if-let [matches (seq (match-licenses (name license-key)))]
          (if (next matches)
            {:error (str "No such License. Did you mean: "
                         (string/join ", " matches)
                         "?")}
            (fetch-license! (first matches) (conj (set seen) license-key)))
          {:error "No such License."}))
    {:error "Could not load License."}))
