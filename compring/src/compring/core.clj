(ns compring.core
   (:require [clj-http.client :as http]
             [clojure.data.json :as json]
             [clojure.java.io :as io]
			 [ring.middleware.reload :refer [wrap-reload]]
             [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
			 [ring.util.response :as response]
			 [ring.util.http-response :as response2]
			 [compojure.core :refer :all]
             [compojure.route :as route]
			 [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout poll! dropping-buffer sliding-buffer]])

  (:use [clojure.pprint])
  (:use [ring.adapter.jetty])
  (:use [toledohue.hue]))



(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
