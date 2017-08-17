(ns webapp.core
  (:import [org.apache.commons.daemon Daemon DaemonContext])
  (:gen-class :implements [org.apache.commons.daemon.Daemon])
  (:require [com.stuartsierra.component :as c]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [webapp.components :as components]))

(defn init [options]
  (alter-var-root #'webapp.system/current-system (constantly (components/system options))))

(defn start []
  (alter-var-root #'webapp.system/current-system c/start))

(defn stop []
  (alter-var-root #'webapp.system/current-system #(when % (c/stop %) nil)))

(defn go [options]
  (init options)
  (start))

(defn reset [options]
  (stop)
  (go options))

(def cli-options
  [["-p" "--port PORT" "Web server listening on port" :default nil]
   ["-c" "--config-file FILE" "Configuration file name"]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _]
                (update-in m [k] inc))]
   ["-h" "--help"]])

;; Daemon implementation

(def daemon-args (atom nil))

(defn -init [this ^DaemonContext context]
  (reset! daemon-args (.getArguments context)))

(defn -start [this]
  (let [{:keys [options summary errors] :as parsed} (parse-opts @daemon-args cli-options)]
    (go options)))

(defn -stop [this]
    (stop))

;;Entry point

(defn -main
  "lein run entry point"
  [& args]
  (let [{:keys [options summary errors] :as parsed} (parse-opts args cli-options)]
    (go options)))
