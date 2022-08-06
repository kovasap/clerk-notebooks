(ns common-utils
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :refer [split trim]]))


(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
    (rest csv-data)))


(defn slurp-csv
  [filename]
  (with-open [reader (io/reader filename)]
    (doall
      (csv-data->maps (csv/read-csv reader)))))

(def Date :string)
(def Timestamp [:and :int [:>= 0]])
; Outputs dates in format https://vega.github.io/vega-lite/docs/datetime.html
(def VegaDate [:map [:month :int]
                    [:date :int]
                    [:year :int]])

(defn parse-date
  {:malli/schema [:=> [:cat Date] [:or :nil VegaDate]]}
  [date-string]
  (let [split (split (trim date-string) #"-")]
    (prn split)
    (if (not (= 3 (count split)))
      nil
      (let [[year month day] split]
        {:month (Integer/parseInt month)
         :date (Integer/parseInt day)
         :year (Integer/parseInt (case (count year)
                                   2 (str "20" year)
                                   4 year
                                   nil))}))))

(defn parse-price
  [s]
  (if (= s ".")
    nil
    (Float/parseFloat s)))

(defn convert-csv-dates
  [csv-data]
  (for [row csv-data]
    (assoc row
           ; :DATE (parse-date (:DATE row))
           :SP500 (parse-price (:SP500 row)))))
