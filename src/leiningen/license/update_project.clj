(ns leiningen.license.update-project
  (:require [leiningen.core.main :as main]
            [rewrite-clj.zip :as z]
            [clojure.java.io :as io]))

;; ## Assoc Helper

(defn- assoc-empty
  [zloc k v]
  (-> zloc
      (z/append-child k)
      (z/append-child v)))

(defn- maybe-assoc-existing
  [zloc k v]
  (when-let [kloc (z/find-value zloc k)]
    (-> kloc
        (z/right)
        (z/replace v)
        (z/up))))

(defn- assoc-at-end
  [zloc k v]
  (-> zloc
      (z/rightmost)
      (z/insert-right v)
      (z/insert-right k)
      (z/append-space 11)
      (z/append-newline)
      (z/up)))

(defn- assoc-indented
  [zloc k v]
  (if v
    (if-let [inner (z/down zloc)]
      (or (maybe-assoc-existing inner k v)
          (assoc-at-end inner k v))
      (assoc-empty zloc k v))
    zloc))

(defn- assoc-non-existing
  [zloc k v]
  (if v
    (if-let [inner (z/down zloc)]
      (if-not (z/find-value inner k)
        (assoc-indented zloc k v)
        zloc))
    zloc))

;; ## Assoc/Insert License Data

(defn- current-year
  []
  (-> (java.util.Calendar/getInstance)
      (.get java.util.Calendar/YEAR)))

(defn- assoc-license
  [zloc {:keys [title source key]}]
  (-> zloc
      (assoc-indented :name title)
      (assoc-indented :url (or source "none"))
      (assoc-non-existing :year (current-year))
      (assoc-indented :key (name key))))

(defn- insert-license
  [zloc license]
  (-> zloc
      (z/right)
      (z/right)
      (z/insert-right {})
      (z/right)
      (assoc-license license)
      (z/left)
      (z/insert-right :license)
      (z/append-space)
      (z/append-newline)))

(defn- update-license-node
  [zloc _ license]
  (if-let [zloc (some-> zloc
                        (z/find-value :license)
                        (z/right))]
    (assoc-license zloc license)
    (insert-license zloc license)))

;; ## Project File

(defn update-project-file!
  "Update/Insert `:license` information in `project.clj`."
  [{:keys [root]} options license]
  (try
    (let [f (io/file root "project.clj")
          zloc (some-> (z/of-file f)
                       (z/find-value z/next 'defproject)
                       (update-license-node options license))]
      (if zloc
        (with-open [out (io/writer f :encoding "UTF-8")]
          (z/print-root zloc out))
        (main/warn "invalid project file.")))
    (catch Exception ex
      (main/abort "could not update project file:" ex))))
