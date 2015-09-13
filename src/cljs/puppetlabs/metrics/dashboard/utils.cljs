(ns puppetlabs.metrics.dashboard.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Private
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; abstracting the JS data structure into functions in case we decide
;; to use clojurescript structures instead in the future
(defn metrics-data-map
  []
  (js-obj))

(defn data-item
  [value]
  (let [item (js-obj)
        now (js/Date.)]
    (set! (.-time item) now)
    (set! (.-value item) value)
    item))

(declare get-values-for-metric)


(defn add-metric-value
  [num-historical-data-points metrics-data metric-id value]
  (let [metric-data (get-values-for-metric metrics-data metric-id)]
    (.push metric-data (data-item value))
    (when (> (.-length metric-data) num-historical-data-points)
      (.shift metric-data))))

(defn update-metrics-data
  [num-historical-data-points metrics-data latest-data-fn]
  (let [channel (async/chan)]
    (latest-data-fn channel)
    (go
      (let [latest-data (async/<! channel)]
        (doseq [[k v] latest-data]
          (add-metric-value num-historical-data-points metrics-data k v))
        (async/close! channel)))))

(defn update-metrics-and-reschedule!
  [polling-interval num-historical-data-points metrics-data latest-data-fn]
  (go
    (async/<!
      (update-metrics-data
        num-historical-data-points
        metrics-data
        latest-data-fn))
    (js/setTimeout (partial update-metrics-and-reschedule!
                     polling-interval
                     num-historical-data-points
                     metrics-data
                     latest-data-fn)
      polling-interval)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn begin-metrics-loop
  [polling-interval num-historical-data-points latest-data-fn]
  (let [metrics-data (metrics-data-map)]
    (update-metrics-and-reschedule!
      polling-interval
      num-historical-data-points
      metrics-data
      latest-data-fn)
    metrics-data))

(defn get-values-for-metric
  [metrics-data metric-id]
  (if-let [vals (aget metrics-data metric-id)]
    vals
    (do
      (aset metrics-data metric-id (array))
      (aget metrics-data metric-id))))