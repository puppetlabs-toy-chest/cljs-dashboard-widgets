(ns puppetlabs.metrics.dashboard.metrics-box
  (:require [goog.string :as gstring]
            [goog.string.format]
            [cljs-time.core :as cljs-time]
            [reagent.core :as reagent]))

(def next-box-id (atom 0))


(def width 400)
(def height 60)
(def margin {:top 10
             :right 0
             :bottom 10
             :left 50})

(def w (-> width
           (- (:left margin))
           (- (:right margin))))

(def h (-> height
           (- (:top margin))
           (- (:bottom margin))))

(defn date-subtract
  [date millis]
  (cljs-time/minus date (cljs-time/millis millis)))

(defn metrics-box-dom
  [box-id description addendum]
  [:tr {:class "counterbox" :id box-id}
   [:td {:class "counterlabel"}
    [:div {:class "counterdesc"} description]
    [:div {:class "counteraddendum"} addendum]]
   [:td {:class "countertext"} "??"]
   [:td {:class "countersparkline"}
    [:svg {:width width :height height}
     [:g {:class "countersvg"
          :transform (gstring/format "translate(%s,%s)"
                          (:left margin)
                          (:top margin))}
      [:g {:class "y_axis"
           :transform "translate(-1,0)"}]
      [:defs
       [:clipPath {:id "clip"}
        [:rect {:width  w
                :height h}]]]
      [:g {:class "line_g"}
       [:path {:class "line"}]]]]]])

(defn initialize-counterbox
  [x-axis-domain data-fn counterbox-selection]
  (let [x   (.. js/d3.time
                scale
                (domain x-axis-domain)
                (range (array 0 w)))
        y   (.. js/d3.scale
                linear
                (range (array h 1)))
        line (.. js/d3.svg
                 area
                 (interpolate "linear")
                 (x #(+ 1 (x (.-time %))))
                 (y1 (+ h 1))
                 (y0 #(y (.-value %))))
        axis  (.. js/d3.svg
                  axis
                  (scale y)
                  (orient "left")
                  (ticks 3))
        svg-line-g (.select counterbox-selection ".line_g")
        svg-line-path (.select counterbox-selection ".line")]
    (.attr svg-line-g "clip-path" "url(#clip)")
    (.attr svg-line-path "d" (line (array)))
    (set! (.-axis y) axis)
    {:data-fn data-fn
     :x       x
     :y       y
     :line    line}))

(defn metrics-box
  [description addendum data-fn]
  (let [box-id (str "counterbox"
                    (swap! next-box-id inc))]
    {:description description
     :box-id      box-id
     :addendum    addendum
     :data-fn     data-fn
     :reagent-fn  (fn []
                    [metrics-box-dom box-id description addendum])}))

(defn redraw-box!
  [num-historical-data-points polling-interval box {:keys [data-fn x y line] :as state}]
  (let [box-node box
        format #((.format js/d3 ",.1f") %)
        data (data-fn)
        datavals (map #(.-value %) data)
        now (cljs-time/now)
        startx (date-subtract
                 now
                 (* polling-interval
                    (- num-historical-data-points 2)))]
    (.domain x
             (if (> (count data) 1)
               (array startx
                      (.-time (nth data (- (count data) 2))))
               (array startx
                      (- (.-time (last data)) polling-interval))))

    (let [y-domain (array (apply min datavals)
                          (apply max datavals))]
      (.. y
          (domain y-domain)
          nice))

    (.. box-node
        (select ".countertext")
        (html (format (last datavals))))

    (let [translate (gstring/format "translate(%s)"
                                    (x
                                      (date-subtract
                                        now
                                        (* (- num-historical-data-points 1)
                                           polling-interval))))]
      (.. box-node
          (select ".line")
          (attr "d" (line data))
          (attr "transform" nil)
          transition
          (duration polling-interval)
          (ease "linear")
          (attr "transform" translate)
          (each "end" (fn [] (redraw-box!
                               num-historical-data-points
                               polling-interval
                               box
                               state)))))

    (let [axis-fn (.. y
                      -axis
                      (ticks 3)
                      (tickSize 6 0 0)
                      (tickFormat format))]
      (.. box-node
          (select ".y_axis")
          transition
          (call axis-fn)))))

(defn metrics-table-mounted
  [num-historical-data-points polling-interval boxes]
  (fn [metrics-table]
    (let [table-dom-node (reagent/dom-node metrics-table)]
      (doseq [box boxes]
        (let [dom-node (-> (.select js/d3 table-dom-node)
                           (.select (str "#" (:box-id box))))
              now (cljs-time/now)
              x-axis-domain (array (date-subtract now (* num-historical-data-points
                                                         polling-interval))
                                   now)
              state (initialize-counterbox
                      x-axis-domain
                      (:data-fn box)
                      dom-node)]
          (js/setTimeout #(redraw-box!
                           num-historical-data-points
                           polling-interval
                           dom-node
                           state)
                         (.ceil js/Math
                                (* polling-interval
                                   (.random js/Math)))))))))

(defn metrics-table-dom
  [& boxes]
  (let [result [:table
                (vec (concat [:tbody {:class "metrics"}]
                             (map (fn [box] [(:reagent-fn box)])
                                  boxes)))]]
    result))

(defn metrics-table
  [num-historical-data-points polling-interval & boxes]
  (vec (concat [(with-meta
                  metrics-table-dom
                  {:component-did-mount
                   (metrics-table-mounted
                     num-historical-data-points
                     polling-interval
                     boxes)})]
               boxes)))
