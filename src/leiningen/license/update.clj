(ns leiningen.license.update
  (:require [leiningen.license
             [update-license :refer [update-license-file!]]
             [update-project :refer [update-project-file!]]
             [update-readme :refer [update-readme-file!]]]
            [leiningen.core.main :as main]))

(defn- desired-files
  [options]
  (cond (:license-only options) #{:license}
        (:readme-only options) #{:readme}
        (:project-only options) #{:project}
        :else (cond-> #{:license :readme :project}
                (:no-license options) (disj :license)
                (:no-readme options)  (disj :readme)
                (:no-project options) (disj :project))))

(defn update-license!
  [project options license]
  (main/info (format "License: %s" (:title license)))
  (let [update? (desired-files options)]
    (when (update? :project)
      (main/info "* updating project.clj ...")
      (update-project-file! project options license))
    (when (update? :license)
      (main/info "* updating LICENSE ...")
      (update-license-file! project options license))
    (when (update? :readme)
      (main/info "* updating README ...")
      (update-readme-file! project options license)))
  (main/info "License updated."))
