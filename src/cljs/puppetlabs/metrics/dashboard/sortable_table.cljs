(ns puppetlabs.metrics.dashboard.sortable-table
  (:require [reagent.core :as reagent]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Private
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn update-sort-value [state new-val]
  (when (= new-val (:sort-field @state))
    (swap! state assoc :ascending (not (:ascending @state))))
  (swap! state assoc :sort-field new-val))

(defn sorted-contents [table-state table-contents]
  (let [table-data-vals @table-contents
        ;; `vals` returns nil on an empty map in cljs, and
        ;; `sort-by` doesn't seem to like nils/empty lists in cljs
        sorted-contents (if (empty? table-data-vals)
                          []
                          (sort-by
                            (:sort-field @table-state)
                            table-data-vals))]
    (if (:ascending @table-state)
      sorted-contents
      (rseq sorted-contents))))


(defn column-header-th
  [table-state [key description]]
  [:th {:on-click #(update-sort-value table-state key)} description])

(defn item-td
  [item field]
  [:td (get item field)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sortable-table
  [title table-structure table-data]
  (let [table-state (reagent/atom {:sort-field (:sort-field table-structure)
                                   :ascending (:ascending table-structure)})
        id-field (:id-field table-structure)
        column-tuples (partition 2 (:fields table-structure))
        column-headers (mapv (partial column-header-th table-state) column-tuples)
        headers-tr (apply conj [:tr] column-headers)]
    [(fn []
     [:div
      [:table
       [:caption {:class "tabletitle"} title]
       [:thead
        headers-tr]
       [:tbody
        (for [item (sorted-contents table-state table-data)]
          (let [tds (mapv (partial item-td item) (map first column-tuples))]
            (with-meta
              (apply conj [:tr] tds)
              {:key (get item id-field)})))]]])]))
