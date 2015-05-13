(ns leiningen.license
  (:refer-clojure :exclude [update list])
  (:require [leiningen.license
             [fetch :refer [fetch-license!]]
             [list  :refer [match-licenses]]
             [options :refer [parse-options]]
             [render :refer [render-license]]
             [update :refer [update-license!]]]
            [leiningen.core.main :as main]
            [clojure.java.io :as io]))

;; ## Helpers

(defn-
  ^{:help-arglists '[[project license-name]]}
  render
  "Print a license, filling in user- and project-specific data."
  [project & args]
  (let [{:keys [license-name license-values]} (parse-options project args)
        {:keys [error] :as license} (fetch-license! license-name)]
    (if-not error
      (main/info (render-license license license-values))
      (main/abort error))))

(defn-
  ^{:help-arglists '[[project license-name]
                     [project [options] license-name]]}
  update
  "Update the current license. Available options:

     --license-only        update only the LICENSE file.
     --project-only        update only the project.clj.
     --readme-only         update only the README.
     --no-license          do not update the LICENSE file.
     --no-project          do not update the project.clj.
     --no-readme           do not update the README.

   You can get a list of available licenses using:

     lein license list
     lein license list <prefix>

   "
  [project & args]
  (if (:root project)
    (let [{:keys [license-name] :as options} (parse-options project args)
          {:keys [error] :as license} (fetch-license! license-name)]
      (if-not error
        (update-license! project options license)
        (main/abort error)))
    (main/abort "Not inside a project!")))

(defn-
  ^{:help-arglists '[[project]
                     [project prefix]]}
  list
  "List available licenses, optionally by matching against a prefix."
  [project & [prefix]]
  (let [matches (sort (match-licenses prefix))]
    (if (seq matches)
      (main/info (format "%n  %d License(s) found:%n" (count matches)))
      (main/info "No Licenses found."))
    (doseq [license-name matches]
      (main/info (format "      %-16s (http://choosealicense.com/licenses/%s)"
                         license-name
                         license-name)))
    (when (seq matches)
      (main/info)
      (main/info "  Visit http://choosealicense.com for a comprehensive overview")
      (main/info "  and comparison of these licenses.")
      (main/info))))

;; ## Entry Point

(defn ^:no-project-needed ^{:subtasks [#'list #'render #'update]} license
  "project-level license management."
  [project & args]
  (if-let [[task & rst] (seq args)]
    (case task
      "list"   (apply list project rst)
      "render" (apply render project rst)
      "update" (apply update project rst)
      (main/abort "unknown subtask:" task))
    (main/abort "no subtask given.")))
