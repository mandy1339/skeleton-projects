(ns webapp.controllers.hue
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(defn get-light-ids [config]
  (let [endpoint (format "http://%s/api/%s"
                         (-> config :config :hub)
                         (-> config :config :hub-user))
        response @(http/get endpoint)]
    (if (= 200 (:status response))
      {:status 200
       :body (map name (-> response
                           :body
                           (json/read-str :key-fn keyword)
                           :lights
                           keys))}
      {:status 500
       :body (-> response
                 :body
                 json/read-str)})))


(defn change-light-color [config light-id hue]
  (let [endpoint (format "http://%s/api/%s/lights/%s/state"
                         (-> config :config :hub)
                         (-> config :config :hub-user)
                         light-id)
        response @(http/request {:method :put
                                 :body (json/write-str {:hue hue})
                                 :url endpoint})]
    {:status 200
     :body (-> response
               :body
               json/read-str)}))

(defn change-light-state [config light-id state]
  (let [endpoint (format "http://%s/api/%s/lights/%s/state"
                         (-> config :config :hub)
                         (-> config :config :hub-user)
                         light-id)
        on-off (= "on" state)
        response @(http/request {:method :put
                                 :body (json/write-str {:on on-off})
                                 :url endpoint})]
    {:status 200
     :body (-> response
               :body
               json/read-str)}))
