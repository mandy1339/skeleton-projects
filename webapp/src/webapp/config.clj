(ns webapp.config
  (:require [com.stuartsierra.component :as component]))

;; File-based config data
(def ^:dynamic configfile-data {})

(def base-log-config
  (if-not (empty? (System/getProperty "catalina.base"))
    {:name "catalina"
     :level :info
     :out (org.apache.log4j.FileAppender.
           (org.apache.log4j.PatternLayout.
            "%d{HH:mm:ss} %-5p %22.22t %-22.22c{2} %m%n")
           (str (. System getProperty "catalina.base")
                "/logs/tail_catalina.log")
           true)}
    {:name "console"
     :level :info
     :out (org.apache.log4j.ConsoleAppender.
           (org.apache.log4j.PatternLayout.
            "%d{HH:mm:ss} %-5p %22.22t %-22.22c{2} %m%n"))}))

(defn- get-config-value
  [key & [default]]
  (or (System/getenv key)
      (System/getProperty key)
      default))

(defn app-config []
  {:dev         {:logging base-log-config
                 :port 3000
                 :hub "192.168.1.54"
                 :hub-user "EnWJmi6oA0wbyFd5KaRDFpAHtC0ssJdUHX3it1Fq"
                 :env :dev}
   :test        {:logging base-log-config
                 :port 3000
                 :hub "192.168.1.54"
                 :hub-user "EnWJmi6oA0wbyFd5KaRDFpAHtC0ssJdUHX3it1Fq"
                 :env :test}
   :staging     {:logging base-log-config
                 :port (get-config-value "PORT")
                 :hub (get-config-value "HUB")
                 :hub-user (get-config-value "HUB_USER")
                 :env :staging}
   :integration {:logging base-log-config
                 :port (get-config-value "PORT")
                 :hub (get-config-value "HUB")
                 :hub-user (get-config-value "HUB_USER")
                 :env :integration}
   :production  {:logging base-log-config
                 :port (get-config-value "PORT")
                 :hub (get-config-value "HUB")
                 :hub-user (get-config-value "HUB_USER")
                 :env :production}})

(defn lookup []
  (let [env (keyword (get-config-value "ENV" "dev"))]
    (env (app-config))))

(defrecord Config [config-file]
  component/Lifecycle
  (start [component]
    (when config-file
      (let [data (-> config-file slurp read-string)]
        (alter-var-root #'configfile-data (constantly data))))
    (let [m (lookup)]
      (if ((:env m) #{:production :integration})
        (alter-var-root #'*warn-on-reflection* (constantly false))
        (alter-var-root #'*warn-on-reflection* (constantly true)))
      (merge component m)))
  (stop
    [component]
    component))
