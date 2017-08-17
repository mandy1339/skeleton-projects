;;(clojure.core/use '[clojure.repl :only (doc)])
(ns comproject.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [toledohue.hue :as hue])
  (:use [toledohue.hue])
  (:use [ring.middleware.reload])
  (:use [ring.adapter.jetty]))



(defn turn-all-off
  []
  (hue/turn-off 1 (hue/get-user))
  (hue/turn-off 2 (hue/get-user))
  (hue/turn-off 3 (hue/get-user)))


(defn turn-all-on
  []
  (hue/turn-on 1 (hue/get-user))
  (hue/turn-on 2 (hue/get-user))
  (hue/turn-on 3 (hue/get-user)))

(defn make-all-red
  []
  (hue/hue 1 0 (hue/get-user))
  (hue/hue 2 0 (hue/get-user))
  (hue/hue 3 0 (hue/get-user)))


(defn slack-alert
  "makes lights flash to alert of a slack message"
  []
  (if (:on (:state (:1 (:lights (hue/get-sys-info (hue/get-user))))))
    (do (make-all-red)
        (hue/flash 1 (hue/get-user)) ;;"true"
        (hue/flash 2 (hue/get-user))
        (hue/flash 3 (hue/get-user)))
    (do (hue/turn-on 1 (hue/get-user)) ;;"false"
        (hue/turn-on 2 (hue/get-user))
        (hue/turn-on 3 (hue/get-user))
        (make-all-red)
        (hue/flash 1 (hue/get-user))
        (hue/flash 2 (hue/get-user))
        (hue/flash 3 (hue/get-user))
        (hue/turn-off 1 (hue/get-user))
        (hue/turn-off 2 (hue/get-user))
        (hue/turn-off 3 (hue/get-user)))))


;;HANDLERS ARE HERE. CALLING APP-ROUTES CALLS USES THEM ALL
;;-------------------------------------------------------------------------------------
(defroutes app-routes
  
  ;;PRACTICE ROUTES
  (GET "/user/:id" [id] (str "<h1>Hello World Armando Toledo " id "</h1>")) ;;compoj destr
  (GET "/user/:id" [id greeting] (str "<h1>" greeting " user " id "</h1>")) ;;compj destr
  (GET "/user/Armando/the3" [x y z] (str x ", " y ", " z))
  ;;(GET "/" request (str request)) ;;return request map
  
  (GET "/user" {{:keys [user-id]}:session} (str "The current user is " user-id)) ;;clJ destr

  ;;SAMPLE LINK
  (GET "/user/menu" [] (str "<a href=\"lights/1/flash\">CLICK</a>"))
  (GET "/user/lights/1/flash" [] (hue/flash 1 (hue/get-user)) (str "ye boy"))

  ;;DESTR PRACTICE BINDING
  (GET "/foobar/2" [x y :as r] (str "x is " x " y is " y "\nr is \n" r))

  (GET "/foobar/2" [x y :as {u :uri rm :request-method}]       
       (str "'x' is \"" x "\"\n"
            "'y' is \"" y "\"\n"
            "The request URI was \"" u "\"\n"
            "The request method was \"" rm "\""))

  ;;FOR SLACK (IT HANDLES A POST AND RESPONDS WITH FLASHING)
  (POST "/" request (slack-alert) )
 
  (route/not-found "Not Found"))
;;END DEFROUTES
;;-------------------------------------------------------------------------------------


;;ENTRY POINT (INCLUDES MIDDLEWARE) USE JETTY WITH THIS ON SOME PORT
(def app
  (wrap-reload (wrap-defaults app-routes api-defaults)))



(comment
  (str "{\"text\":\"African or European?\"}"))
