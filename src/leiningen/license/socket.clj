(ns leiningen.license.socket
  (:require [clojure.string :as string])
  (:import [java.net Authenticator
                     InetSocketAddress
                     PasswordAuthentication
                     Proxy
                     Proxy$Type
                     URL]))

(defn socket-stream [url]
  "If System has proxy-config, return socket having proxy, else return normal socket."
  (letfn [(get-env [env-name]
            (let [env-result (System/getenv (string/lower-case env-name))
                  ENV-RESULT (System/getenv (string/upper-case env-name))]
              (some #(when (and (not= nil %) (not= "" %)) %) [env-result ENV-RESULT])))]

    (let [proxy-str (some identity [(get-env "http_proxy") (get-env "socks_proxy")])]
      (if (nil? proxy-str) url
        (let [proxy-list (seq (.split proxy-str "[:/@]+"))
              proxy-list-amount (count proxy-list)
              proxy-protocol (first proxy-list)
              [proxy-host proxy-port] (drop (case proxy-list-amount 3 1 3) proxy-list)]
          (when (= 5 proxy-list-amount)
            (System/setProperty "jdk.http.auth.tunneling.disabledSchemes" "")
            (let [[proxy-user proxy-pass] (take 2 (drop 1 proxy-list))]
              (Authenticator/setDefault (proxy [Authenticator] []
                                          (getPasswordAuthentication []
                                            (PasswordAuthentication. proxy-user (char-array proxy-pass)))))))
          (let [urlconnection (. (URL. url)
                              (openConnection
                                (Proxy.
                                  (Proxy$Type/valueOf (string/upper-case proxy-protocol))
                                  (InetSocketAddress. proxy-host (Integer/parseInt proxy-port)))))]
            (.getInputStream urlconnection)))))))
