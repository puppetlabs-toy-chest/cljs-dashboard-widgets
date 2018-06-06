(ns user
  (:require [puppetlabs.trapperkeeper.services.webserver.jetty9-service :refer [jetty9-service]]
            [puppetlabs.trapperkeeper.core :as tk]
            [puppetlabs.trapperkeeper.app :as tka]
            [puppetlabs.metrics.dashboard.demo.service :refer [metrics-service]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [figwheel-sidecar.repl-api :as ra]))

(defn user-config
  []
  {:webserver {:port 8080}
   :global {:logging-config "./dev/clj/logback-dev.xml"}})

(defn help
  []
  (println "
ClojureScript Dashboard Widgets

Commands:

=> (help)           ;; prints this message
=> (go)             ;; Starts webserver
=> (reset)          ;; Reloads all clj code and restarts webserver
=> (start-figwheel) ;; starts figwheel, to dynamically
                    ;;  recompile cljs code and send it to
                    ;;  the browser
"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Basic system life cycle
(def system nil)
(defn init []
  (alter-var-root #'system
                  (fn [_] (tk/build-app
                            [jetty9-service
                             metrics-service]
                            (user-config))))
  (alter-var-root #'system tka/init)
  (tka/check-for-errors! system))
(defn start []
  (alter-var-root #'system
                  (fn [s] (if s (tka/start s))))
  (tka/check-for-errors! system)
  (println "##### Web server running on port" (get-in (user-config)
                                                      [:webserver :port])))
(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (tka/stop s)))))
(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; CLJS utils

(defn start-figwheel []
    (print "Starting figwheel.\n")
    (ra/start-figwheel!))

(help)
