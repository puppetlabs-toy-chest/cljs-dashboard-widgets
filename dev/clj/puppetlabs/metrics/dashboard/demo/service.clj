(ns puppetlabs.metrics.dashboard.demo.service
  (:require [puppetlabs.metrics.dashboard.demo.handler :refer [app]]
            [puppetlabs.trapperkeeper.core :refer [defservice]]))


(defservice metrics-service
  [[:WebserverService add-ring-handler]]
  (init [this context]
        (add-ring-handler app "/")
        context))


