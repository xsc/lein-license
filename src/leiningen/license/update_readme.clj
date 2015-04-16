(ns leiningen.license.update-readme
  (:require [leiningen.license.render
             :refer [render-license copyright-data]]
            [clojure.string :as string]
            [clojure.java.io :as io]))

;; ## Markdown

(defn- copyright-markdown
  [values]
  (let [{:keys [name email year]} (copyright-data values)]
    (when name
      (str "&copy; " year " " name))))

(defn- license-info-markdown
  [{:keys [title source]} values]
  (let [preposition (if (re-find #"(?i)^the" title)
                      ""
                      "the ")]
    (str
      "This project is licensed under "
      preposition
      (if source
        (format "[%s][license]" title)
        title)
      "."
      (when source
        (str "\n\n[license]: " source)))))

(defn license-and-copyright-markdown
  [license values]
  (->> (vector
         (copyright-markdown values)
         (license-info-markdown license values))
       (filter identity)
       (string/join "\n\n")))

(defn- license-markdown
  "Render short copyright notice."
  [{:keys [text key] :as license} values]
  (cond (= (name key) "no-license")
        (copyright-markdown values)

        (< (count text) 1280)
        (format "%s%n%n```%n%s%n```"
                (license-and-copyright-markdown license values)
                (render-license license values))

        :else
        (license-and-copyright-markdown license values)))

(defn- update-readme-markdown!
  [root {values :license-values} license filename]
  (let [f (io/file root filename)]
    (when (.isFile f)
      (let [contents (slurp f :encoding "UTF-8")
            idx (.indexOf contents "## License\n")]
        (when-not (neg? idx)
          (let [end-idx (.indexOf contents "\n#" (inc idx))
                end-idx (if (neg? end-idx) (count contents) end-idx)]
            (spit
              f
              (str (subs contents 0 (+ idx 12))
                   (license-markdown license values)
                   (subs contents (dec end-idx)))
              :encoding "UTF-8")))))))

;; ## README

(defn update-readme-file!
  [{:keys [root]} options license]
  (or (update-readme-markdown! root options license "README.md")
      (update-readme-markdown! root options license "README.markdown")))
