^{:nextjournal.clerk/visibility :hide-ns}
(ns ^:nextjournal.clerk/no-cache youtube
  (:require [common-utils :refer [assert=]]
            [nextjournal.clerk :as clerk]
            [clojure.string :refer [split]]
            [hickory.core :refer [parse as-hiccup]]
            [hiccup.core :refer [html]]))

; # YouTube Activity

; ## Get Data
; Taken from Google Takeout's YouTube activity export.

(def raw-hiccup (as-hiccup (parse (slurp "notebooks/youtube-activity.html"))))
#_(def raw-hiccup (as-hiccup (parse (slurp "notebooks/youtube-activity-sample.html"))))
(def entries
  (->> raw-hiccup
   last   ; html
   last   ; body
   last   ; mdl-grid
   (drop 2)))
(count entries)

#_(def sample-entries
    [:html [:body [:div (take 20 entries)]]])
#_(spit "notebooks/youtube-activity-sample.html" (html sample-entries))

; ## Make a Parser

(defn insert
  [coll values idx]
  (let [[before after] (split-at idx coll)]
    (vec (concat before values after))))
(defn parse-entry
  [entry]
  (let [vid-data (-> entry (nth 2) (nth 3))
        vid-data-size (count vid-data)
        [_ _ _ video _ channel _ datetime]
        (cond
          (= 8 vid-data-size)
          vid-data
          (contains? (set vid-data) "Watched a video that has been removed")
          ["" "" "" [:a {:href nil} "removed"] "" [:a {:href nil} "none"] "" (last vid-data)]
          (contains? (set vid-data) "Visited YouTube Music")
          ["" "" "" [:a {:href nil} "YouTube music"] "" [:a {:href nil} "none"] "" (last vid-data)]
          (contains? (set vid-data) "Answered survey question")
          ["" "" "" [:a {:href nil} "Survey"] "" [:a {:href nil} \m] "" (last vid-data)]
          :else
          (insert vid-data ["" "none"] 4))]
    {:video (last video)
     :video-link (:href (nth video 1))
     :channel (last channel)
     :channel-link (:href (nth video 1))
     :datetime datetime
     :count 1
     :month (first (split datetime #","))}))
     
; Example for one entry:
^{:nextjournal.clerk/visibility :fold}
(def example
 [:div
   {:class "outer-cell mdl-cell mdl-cell--12-col mdl-shadow--2dp"}
   [:div
    {:class "mdl-grid"}
    [:div {:class "header-cell mdl-cell mdl-cell--12-col"}
     [:p {:class "mdl-typography--title"} "YouTube" [:br {}]]]
    [:div {:class "content-cell mdl-cell mdl-cell--6-col mdl-typography--body-1"}
     "Watched "
     [:a
      {:href "https://www.youtube.com/watch?v=Rfq3tTGKcNs"}
      "Bile Acids: The Next Frontier In Longevity?"]
     [:br {}]
     [:a
      {:href "https://www.youtube.com/channel/UCT1UMLpZ_CrQ_8I431K0b-g"}
      "Conquer Aging Or Die Trying! "]
     [:br {}]
     "Jul 25, 2022, 6:11:56 PM PDT"]
    [:div
     {:class
      "content-cell mdl-cell mdl-cell--6-col mdl-typography--body-1 mdl-typography--text-right"}]
    [:div
     {:class
      "content-cell mdl-cell mdl-cell--12-col mdl-typography--caption"}
     [:b {} "Products:"]
     [:br {}]
     " YouTube"
     [:br {}]
     [:b {} "Why is this here?"]
     [:br {}]
     " This activity was saved to your Google Account because the following settings were on: YouTube watch history. You can control these settings  "
     [:a {:href "https://myaccount.google.com/activitycontrols"} "here"]
     "."]]])
#_(def example-no-channel
    [:div
     {:class "outer-cell mdl-cell mdl-cell--12-col mdl-shadow--2dp"}
     [:div
      {:class "mdl-grid"}
      [:div {:class "header-cell mdl-cell mdl-cell--12-col"}
       [:p {:class "mdl-typography--title"} "YouTube" [:br {}]]]
      [:div {:class "content-cell mdl-cell mdl-cell--6-col mdl-typography--body-1"}
       "Watched "
       [:a {:href "https://www.youtube.com/watch?v=BQ2RiTx4hsw"} "The Umbrella Academy Season 3 | Trailer"]
       [:br {}]
       "Jun 23, 2022, 4:57:47 PM PDT"]
      [:div {:class "content-cell mdl-cell mdl-cell--6-col mdl-typography--body-1 mdl-typography--text-right"}]
      [:div {:class "content-cell mdl-cell mdl-cell--12-col mdl-typography--caption"}
       [:b {} "Products:"]
       [:br {}]
       " YouTube"
       [:br {}]
       [:b {} "Details:"]
       [:br {}]
       " From Google Ads"
       [:br {}]
       [:b {} "Why is this here?"]
       [:br {}]
       " This activity was saved to your Google Account because the following settings were on: YouTube watch history. You can control these settings  "
       [:a {:href "https://myaccount.google.com/activitycontrols"} "here"]
       "."]]])
#_(def example-removed
    [:div {:class "outer-cell mdl-cell mdl-cell--12-col mdl-shadow--2dp"}
     [:div {:class "mdl-grid"} [:div {:class "header-cell mdl-cell mdl-cell--12-col"} [:p {:class "mdl-typography--title"} "YouTube" [:br {}]]]
      [:div {:class "content-cell mdl-cell mdl-cell--6-col mdl-typography--body-1"}
       "Watched a video that has been removed" [:br {}] "Apr 26, 2022, 5:53:11 PM PDT"] [:div {:class "content-cell mdl-cell mdl-cell--6-col mdl-typography--body-1 mdl-typography--text-right"}] [:div {:class "content-cell mdl-cell mdl-cell--12-col mdl-typography--caption"} [:b {} "Products:"] [:br {}] " YouTube" [:br {}] [:b {} "Why is this here?"] [:br {}] " This activity was saved to your Google Account because the following settings were on: YouTube watch history. You can control these settings  " [:a {:href "https://myaccount.google.com/activitycontrols"} "here"] "."]]])
(def parsed-example
  (parse-entry example))


; ## Run it!

; All data:

(def parsed-entries
  (filter
    #(and
       ()
       (not (= \m (:channel %))))  ; remove ads
    (map parse-entry entries)))
#_(for [entry entries]
    (:video (parse-entry entry)))
(count parsed-entries)

#_(group-by :channel parsed-entries)

; Channels by videos watched:

(reverse (sort-by val (frequencies (map :channel parsed-entries))))


; Videos watched by month:

(clerk/vl 
  {:width 3000 :height 400 
   :data {:name "data"
          :values parsed-entries}
   :mark "bar"
   :encoding {:x {:field "month"}
              :y {:aggregate "sum" :field "count"}
              :color {:field "channel"}}})
               
