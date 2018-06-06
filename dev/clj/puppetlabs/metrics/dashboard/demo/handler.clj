(ns puppetlabs.metrics.dashboard.demo.handler
  (:require [puppetlabs.comidi :as comidi]
            [ring.util.response :refer [resource-response]]
            [cheshire.core :as json]))

(def resource-root "puppetlabs/metrics/dashboard/public")

(defn random-metrics-box-data
  [ks]
  (reduce (fn [acc k]
            (assoc acc k (* 10.0 (rand))))
    {}
    ks))

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
                   double
                   Math/round
                   (/ 100))]
    (-> metric
      (assoc :count new-count)
      (assoc :mean new-mean)
      (assoc :aggregate new-agg))))

(defn update-timer-metrics
  [timer-metrics-atom]
  (let [metric-to-update (rand-nth (vals @timer-metrics-atom))]
    (swap! timer-metrics-atom
      assoc (:id metric-to-update) (update-timer-metric metric-to-update))))

(def app
  (let [requests-atom (atom
                        (generate-empty-timer-metrics ["/foo1" "/foo2" "/foo3"
                                                       "/bar1" "/bar2" "/bar3"
                                                       "/baz1" "/baz2" "/baz3"
                                                       "/bam"]))
        functions-atom (atom
                         (generate-empty-timer-metrics ["hiera1" "hiera2" "hiera3"
                                                        "yaml1" "yaml2" "yaml3"
                                                        "foo1" "foo2" "foo3"
                                                        "create_resources"]))]
    (comidi/routes->handler
      (comidi/routes
        (comidi/GET ["/random-data"] []
          (update-timer-metrics requests-atom)
          (update-timer-metrics functions-atom)
          {:status 200
           :headers {"Content-Type" "application/json" "Access-Control-Allow-Origin" "http://localhost:3449" "Access-Control-Allow-Credentials" "true"}
           :body (json/generate-string
                   {:metrics-boxes (random-metrics-box-data ["metric1" "metric2" "metric3" "metric4" "metric5"])
                    :requests (vals @requests-atom)
                    :functions (vals @functions-atom)})})
        (comidi/GET ["/"] [] (resource-response "metrics.html" {:root resource-root}))
        (comidi/resources "/" {:root resource-root})
        (comidi/not-found "Not Found")))))