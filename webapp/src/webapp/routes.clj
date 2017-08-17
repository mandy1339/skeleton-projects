(ns webapp.routes
  (:import [java.util UUID])
  (:require [clj-time.core :refer [before? after? now] :as t]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [compojure.core :refer [routes GET PUT HEAD POST DELETE ANY context defroutes] :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [webapp.system :refer [current-system]]
            [webapp.controllers.hue :as hue]))

(defroutes api-routes
  (context "/v1" []
           (GET "/lights" [] (hue/get-light-ids current-system))
           (PUT "/lights/:id" [id hue] (hue/change-light-color current-system id hue))
           (POST "/lights/:id" [id state] (hue/change-light-state current-system id state))))


(defroutes all-routes
  (GET "/health-check" [] (str (-> current-system :config :env)))
  api-routes)


(defn wrap-stacktrace
  "ring.middleware.stacktrace only catches exception, not Throwable, so we replace it here."
  [handler]
  (fn [request]
    (try (handler request)
         (catch Throwable t
           (log/error t :request request)
           {:status 500
            :headers {"Content-Type" "text/plain; charset=UTF-8"}
            :body (with-out-str
                    (binding [*err* *out*]
                      (println "\n\nREQUEST:\n")
                      (pprint request)))}))))

(defn wrap-token
  "Add a unique token identifier to each request for easy debugging."
  [handler]
  (fn [request]
    (let [request-token (str (UUID/randomUUID))
          tokenized-request (assoc request :token request-token)
          log-fn (if (= :dev (-> current-system :config :env))
                   (fn [s] (log/info s))
                   (fn [s] (log/debug s)))]
      (log-fn (format "\n Start: %s \n Time: %s \n Request: \n %s"
                      request-token (t/now) request))
      (let [response (handler tokenized-request)]
        (log-fn (format "\n End: %s \n Time: %s \n Response: \n %s"
                        request-token (t/now) response))
        response))))

(defn app []
  (-> all-routes
      (wrap-restful-format :formats [:json-kw :edn])
      wrap-keyword-params
      wrap-params
      wrap-token
      wrap-stacktrace
      wrap-content-type))
