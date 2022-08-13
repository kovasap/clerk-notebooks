^{:nextjournal.clerk/visibility :hide-ns}
(ns ^:nextjournal.clerk/no-cache youtube
  (:require [common-utils :refer [assert=]]
            [nextjournal.clerk :as clerk]
            [hickory.core :refer [parse as-hiccup]]
            [hiccup.core :refer [html]]))

; # YouTube Activity

; ## Get Data
; Taken from Google Takeout's YouTube activity export.

#_(def raw-hiccup (as-hiccup (parse (slurp "notebooks/youtube-activity.html"))))
(def raw-hiccup (as-hiccup (parse (slurp "notebooks/youtube-activity-sample.html"))))
(def entries
  (->> raw-hiccup
   last   ; html
   last   ; body
   last   ; mdl-grid
   (drop 2)))

#_(def sample-entries
    [:html [:body [:div (take 20 entries)]]])
#_(spit "notebooks/youtube-activity-sample.html" (html sample-entries))

; ## Make a Parser

(defn parse-entry
  [entry]
  (let [[_ _ _ video _ channel _ datetime] (-> entry
                                            (nth 2)
                                            (nth 3))]
    {:video (last video)
     :video-link (:href (nth video 1))
     :channel (last channel)
     :channel-link (:href (nth video 1))
     :datetime datetime}))
     
; Example for one entry:
^{:nextjournal.clerk/visibility :fold}
(def example
 [:div
   {:class "outer-cell mdl-cell mdl-cell--12-col mdl-shadow--2dp"}
   [:div
    {:class "mdl-grid"}
    [:div
     {:class "header-cell mdl-cell mdl-cell--12-col"}
     [:p {:class "mdl-typography--title"} "YouTube" [:br {}]]]
    [:div
     {:class
      "content-cell mdl-cell mdl-cell--6-col mdl-typography--body-1"}
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
(def parsed-example
  (parse-entry example))


; ## Run it!

; All data:

(def parsed-entries
  (map parse-entry entries))
#_(for [entry entries]
    (:video (parse-entry entry)))

#_(group-by :channel parsed-entries)

; Channels by videos watched:

(reverse (sort-by val (frequencies (map :channel parsed-entries))))
