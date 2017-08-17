(ns webapp.components
  (:require [com.stuartsierra.component :as component]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clj-logging-config.log4j :as log-config]
            [org.httpkit.server :as server]
            [org.httpkit.client :as http]
            [webapp.config :as config]
            [webapp.routes :as r]))

(defrecord Logging [config]
  component/Lifecycle
  (start [this]
    (log-config/set-logger!
     "webapp"
     :name (-> config :logging :name)
     :level (-> config :logging :level)
     :out (-> config :logging :out))
    (log/logf :info "Environment is %s" (-> config :env))
    this)
  (stop [this]
    this))

(defrecord Router [config logging]
  component/Lifecycle
  (start [this]
    (assoc this :routes (r/app)))
  (stop [this]
    (dissoc this :routes)))

(defrecord Server [port config logging router]
  component/Lifecycle
  (start [this]
    (if (:stop! this)
      this
      (let [server (-> this
                       :router
                       :routes
                       (server/run-server {:port (java.lang.Integer. (or port (-> config :port) 0))}))
            port (-> server meta :local-port)]
        (log/logf :info "Web server running on port %d" port)
        (assoc this :stop! server :port port))))
  (stop [this]
    (when-let [stop! (:stop! this)]
      (stop! :timeout 250))
    (dissoc this :stop! :router :port)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; High Level Application System
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn system
  [{:keys [config-file port] :as options}]
  (component/system-map
   :config    (component/using (config/map->Config options) [])
   :logging   (component/using (map->Logging {}) [:config])
   :router    (component/using (map->Router {}) [:config :logging])
   :server    (component/using (map->Server {:port port}) [:config :logging :router])))
