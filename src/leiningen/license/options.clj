(ns leiningen.license.options)

;; ## Helpers

(def ^:private default-license
  :mit)

(defn- resolve-license-name
  [{:keys [license]} license-name]
  (if (= (name license-name) "default")
    (or (:key license) default-license)
    license-name))

(defn- resolve-license-values
  [{:keys [license] :as project}]
  (merge
    (select-keys project [:description])
    (select-keys license [:author :email :project])))

(defn- parse-raw-options
  [args]
  (let [[opts rst] (split-with
                     (fn [^String opt]
                       (and (not= opt "--")
                            (.startsWith opt "--")))
                     args)
        options (zipmap
                  (map #(keyword (subs % 2)) opts)
                  (repeat true))
        license-name (or
                       (if (= (first rst) "--")
                         (second rst)
                         (first rst))
                       "default")]
    (assoc options :license-name license-name)))

;; ## Parser

(defn parse-options
  [project args]
  (if (seq args)
    (-> (parse-raw-options args)
        (update-in [:license-name] #(resolve-license-name project %))
        (assoc :license-values (resolve-license-values project)))
    (recur project ["default"])))
