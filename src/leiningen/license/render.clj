(ns leiningen.license.render
  (:require [clojure.string :as string]
            [clojure.java.shell :as sh])
  (:import [java.util Calendar]))

;; ## Helpers

(defn- from-git
  [k]
  (try
    (let [{:keys [exit out]} (sh/sh "git" "config" (name k))]
      (when (= exit 0)
        (string/trim out)))
    (catch Exception _)))

(defn- remote-info
  [^String url data-index]
  (let [[username project] (-> url
                               (subs (inc data-index))
                               (.split "/" 2))]
    {:username username
     :project (if (.endsWith project ".git")
                (subs project 0 (- (count project) 4))
                project)}))

(defn- parse-remote-url
  []
  (when-let [^String url (from-git "remote.origin.url")]
    (cond (or (.startsWith url "git@github.com:")
              (.startsWith url "ssh://git@github.com:")
              (.startsWith url "git@bitbucket.org:")
              (.startsWith url "ssh://git@bitbucket.org:"))
          (remote-info url (.indexOf url ":" 5))

          (or (.startsWith url "https://github.com/")
              (.startsWith url "https://bitbucket.org/"))
          (remote-info url (.indexOf url "/" 9))

          :else nil)))

;; ## Generators

(defn- generate-year
  [{:keys [year]}]
  (let [current-year (-> (Calendar/getInstance)
                         (.get Calendar/YEAR)
                         (str))]
    (if (or (not year)
            (= (str year) current-year))
      current-year
      (str year "-" current-year))))

(defn- generate-email
  [{:keys [email]}]
  (or email (from-git "user.email")))

(defn- generate-name
  [{:keys [author]}]
  (or author (from-git "user.name")))

(defn- generate-project
  [{:keys [project]}]
  (or project (:project (parse-remote-url))))

(defn- generate-login
  [{:keys [username]}]
  (or username (:username (parse-remote-url))))

;; ## Renderer

(def ^:private rendering
  {"[fullname]"    generate-name
   "[name]"        generate-name
   "[email]"       generate-email
   "[login]"       generate-login
   "[project]"     generate-project
   "[description]" #(:description % "")
   "[year]"        generate-year})

(defn render-license
  "Render license text."
  [{:keys [text]} values]
  (reduce
    (fn [text [placeholder f]]
      (if-let [v (f values)]
        (string/replace text placeholder (str v))
        text))
    text rendering))

(defn copyright-data
  [values]
  {:name  (generate-name values)
   :email (generate-email values)
   :year  (generate-year values)})
