(ns cljs-rrule.core-test
  (:require-macros [speclj.core :refer [describe it should=]])
  (:require [speclj.core]
            [cljs-rrule.core :as rrule]))

(describe "RRule"
  (it "supports looking up fields by keyword"
    (let [rrule (rrule/rrule {:byhour [1 3]})]
      (should= [1 3] (:byhour rrule))))

  (it "accepts keywords for constants"
    (let [rrule (rrule/rrule {:freq :weekly})]
      (should= :weekly (:freq rrule))))

  (it "provides the first date"
    (let [d (js/Date. 2006 6 6)
          rrule (rrule/rrule {:freq :weekly :dtstart d})]
      (should= d (first rrule))))

  (it "provides the next date"
    (let [d (js/Date. 2006 6 6)
          rrule (rrule/rrule {:freq :weekly :dtstart d})
          rest-rrule (rest rrule)]
      (should= (js/Date. 2006 6 13) (first rest-rrule))))

  (it "gets a lazy sequence of dates"
    (let [d (js/Date. 2006 6 6)
          rrule (rrule/rrule {:freq :weekly :dtstart d})
          ds [(js/Date. 2006 6 6)
              (js/Date. 2006 6 13)
              (js/Date. 2006 6 20)
              (js/Date. 2006 6 27)]]
      (should= ds (take 4 rrule))))

  (it "supports IAssoc assoc"
    (let [d (js/Date. 2006 6 6)
          rrule (rrule/rrule {:freq :weekly :dtstart d})
          with-hour (assoc rrule :byhour 12)]
      (should= (js/Date. 2006 6 6 12) (first with-hour))))

  (it "supports IMap dissoc"
    (let [d (js/Date. 2006 6 6)
          rrule (rrule/rrule {:freq :weekly :byhour 12 :dtstart d})
          without-hour (dissoc rrule :byhour)]
      (should= (js/Date. 2006 6 6) (first without-hour)))))

(describe "RRuleSet"
  (it "provides the first date"
    (let [d (js/Date. 2006 6 6)
          rrule0 {:freq :weekly :byweekday :th :dtstart d}
          rrule1 {:freq :weekly :byweekday :sa :dtstart d}
          rrules (rrule/rrule-set [rrule0 rrule1])]
      (should= d (first rrules))))

  (it "provides the next date"
    (let [d (js/Date. 2006 6 6)
          rrule0 {:freq :weekly :byweekday :th :dtstart d}
          rrule1 {:freq :weekly :byweekday :sa :dtstart d}
          rrules (rrule/rrule-set [rrule0 rrule1])
          rest-rrules (rest rrules)]
      (should= (js/Date. 2006 6 8) (first rest-rrules))))

  (it "gets a lazy sequence of dates"
    (let [d (js/Date. 2006 6 6)
          rrule0 {:freq :weekly :byweekday :th :dtstart d}
          rrule1 {:freq :weekly :byweekday :sa :dtstart d}
          rrules (rrule/rrule-set [rrule0 rrule1])
          ds [(js/Date. 2006 6 6)
              (js/Date. 2006 6 8)
              (js/Date. 2006 6 13)
              (js/Date. 2006 6 15)]]
      (should= ds (take 4 rrules))))

  (it "supports ICollection conj"
    (let [d (js/Date. 2006 6 6)
          rrule0 {:freq :weekly :byweekday :th :dtstart d}
          rrule1 {:freq :weekly :byweekday :sa :dtstart d}
          rrules (rrule/rrule-set [rrule0])
          with-rrule1 (conj rrules rrule1)]
      (should= (js/Date. 2006 6 8) (second with-rrule1))))

  (it "supports ISet disj"
    (let [d (js/Date. 2006 6 6)
          rrule0 {:freq :weekly :byweekday :th :dtstart d}
          rrule1 {:freq :weekly :byweekday :sa :dtstart d}
          rrules (rrule/rrule-set [rrule0 rrule1])
          without-rrule0 (disj rrules rrule0)]
      (should= (js/Date. 2006 6 8) (first without-rrule0)))))
