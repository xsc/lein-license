(ns leiningen.license.update-license
  (:require [leiningen.license.render :refer [render-license]]
            [clojure.java.io :as io]))

(defn update-license-file!
  "Write out LICENSE file."
  [{:keys [root]} {values :license-values} license]
  (with-open [out (io/output-stream (io/file root "LICENSE"))]
    (.write out (.getBytes (render-license license values) "UTF-8"))))
