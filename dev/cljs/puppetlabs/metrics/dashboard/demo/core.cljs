(ns puppetlabs.metrics.dashboard.demo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [puppetlabs.metrics.dashboard.metrics-box :as metrics-box]
            [puppetlabs.metrics.dashboard.sortable-table :as sortable-table]
            [puppetlabs.metrics.dashboard.utils :as metrics-utils]
            [cljs.core.async :as async]
            [cljs-http.client :as http]))

(def num-historical-data-points 60)
(def polling-interval 2000)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Local - sample data generator fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn generate-empty-timer-metrics
  [ids]
  (reduce
    (fn [acc id]
      (assoc acc id {:id id :count 0 :mean 0 :aggregate 0}))
    {}
    ids))

(defn update-timer-metric
  [metric]
  (let [rand-ms (rand-int 1000)
        new-count (inc (:count metric))
        new-agg (+ rand-ms (:aggregate metric))
        new-mean (-> (/ new-agg new-count)
                   (* 100)
                   (Math/round)
                   (/ 100))]
    (-> metric
      (assoc :count new-count)
      (assoc :mean new-mean)
      (assoc :aggregate new-agg))))

(defn update-timer-metrics!
  [timer-metrics-atom]
  (let [metric-to-update (rand-nth (vals @timer-metrics-atom))]
    (swap! timer-metrics-atom
      assoc (:id metric-to-update) (update-timer-metric metric-to-update))))

(defn local-random-data-fn
  [requests-reagent-atom functions-reagent-atom]
  (let [requests-map (atom (generate-empty-timer-metrics ["/foo1" "/foo2" "/foo3"
                                                          "/bar1" "/bar2" "/bar3"
                                                          "/baz1" "/baz2" "/baz3"
                                                          "/bam"]))
        functions-map (atom (generate-empty-timer-metrics ["hiera1" "hiera2" "hiera3"
                                                           "yaml1" "yaml2" "yaml3"
                                                           "foo1" "foo2" "foo3"
                                                           "create_resources"]))]
    (reset! requests-reagent-atom (vals @requests-map))
    (reset! functions-reagent-atom (vals @functions-map))
    (fn [channel]
      (update-timer-metrics! requests-map)
      (update-timer-metrics! functions-map)
      (reset! requests-reagent-atom (vals @requests-map))
      (reset! functions-reagent-atom (vals @functions-map))
      (async/put! channel
        (reduce (fn [acc metric-id]
                  (assoc acc metric-id (* 10.0 (rand))))
          {}
          [:metric1 :metric2 :metric3 :metric4 :metric5])))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Server - sample data generator fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn server-random-data-fn
  [requests-atom functions-atom]
  (fn [channel]
    (go
      (let [response (async/<! (http/get "/random-data"))]
        (if-not (= 200 (:status response))
          (do
            ;; TODO: better error handling
            (.log js/console (str "Error issuing HTTP request!: " (:status response) " " (:body response)))
            (async/close! channel))
          (do
            ;; transform HTTP response here if necessary
            ;(println "Got latest data from server:" (:body response))
            (reset! requests-atom (get-in response [:body :requests]))
            (reset! functions-atom (get-in response [:body :functions]))
            (async/put! channel (get-in response [:body :metrics-boxes]))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UI Render Fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn metrics-boxes [metrics-fn]
  (fn []
    [:div
     [metrics-box/metrics-table num-historical-data-points polling-interval
      (metrics-box/metrics-box "My First Metric!" "It's really something."
                               (partial metrics-fn :metric1))
      (metrics-box/metrics-box "My 2nd Metric!" "It's really something."
                               (partial metrics-fn :metric2))
      (metrics-box/metrics-box "My 3rd Metric!" "It's really something."
                               (partial metrics-fn :metric3))
      (metrics-box/metrics-box "My 4th Metric!" "It's really something."
                               (partial metrics-fn :metric4))
      (metrics-box/metrics-box "My 5th Metric!" "It's really something."
                               (partial metrics-fn :metric5))]]))

(defn sortable-tables [requests-atom functions-atom]
  (fn []
   [:div {:class "clearfix"}
    [:div {:class "left table-panel"}
     (sortable-table/sortable-table
       "Top 10 Requests"
       {:id-field :id
        :fields [:id "Route"
                 :count "Count"
                 :mean "Mean"
                 :aggregate "Aggregate"]
        :sort-field :aggregate
        :ascending false}
       requests-atom)]
    [:div {:class "left table-panel"}
     (sortable-table/sortable-table
       "Top 10 Functions"
       {:id-field :id
        :fields [:id "Function"
                 :count "Count"
                 :mean "Mean"
                 :aggregate "Aggregate"]
        :sort-field :aggregate
        :ascending false}
       functions-atom)]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init! []
  (let [requests-atom (reagent/atom {})
        functions-atom (reagent/atom {})
        metrics-data (metrics-utils/begin-metrics-loop
                       polling-interval
                       num-historical-data-points

                       ; uncomment this line to generate random data locally
                       ;(local-random-data-fn requests-atom functions-atom)

                       ;;; or, uncomment this line to get random data from the server
                       (server-random-data-fn requests-atom functions-atom)
                       )]
    (reagent/render-component [(metrics-boxes
                                 #(metrics-utils/get-values-for-metric metrics-data %))]
      (.getElementById js/document "metrics_boxes"))
    (reagent/render-component [(sortable-tables requests-atom functions-atom)]
      (.getElementById js/document "sortable_tables"))))
