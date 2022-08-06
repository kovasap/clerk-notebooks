^{:nextjournal.clerk/visibility :hide-ns}
(ns ^:nextjournal.clerk/no-cache stocks
  (:require [common-utils :refer [slurp-csv convert-csv-dates]]
            [nextjournal.clerk :as clerk]))

;; # Messing with Stocks!

;; ### 📈 S&P 500
(def sp500
  (into [] (convert-csv-dates (slurp-csv "notebooks/SP500.csv"))))

(clerk/vl 
  {:width 650 :height 400 
   :data {:values sp500}
   :mark "line"
   :encoding {:x {:field :DATE :type "temporal"},
              :y {:field :SP500 :type "quantitative"}}})
