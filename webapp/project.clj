(defproject webapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.apache.commons/commons-daemon "1.0.9"]
                 [commons-io/commons-io "2.4"]
                 [clj-logging-config "1.9.12"]
                 [clj-time "0.13.0"]
                 [compojure "1.6.0"]
                 [http-kit "2.2.0"]
                 [ring-middleware-format "0.7.2" :exclusions [commons-codec]]
                 [ring/ring-core "1.6.1" :exclusions [joda-time]]
                 [com.stuartsierra/component "0.3.2"]
                 [javax.servlet/servlet-api "2.5"]]
  :main ^:skip-aot webapp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
