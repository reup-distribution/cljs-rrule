(ns cljs-rrule.core
  (:refer-clojure :exclude [frequencies])
  (:require [clojure.set :refer [union]]
            [rrule.js]))

(def frequencies
  #{:secondly
    :minutely
    :hourly
    :daily
    :weekly
    :monthly
    :yearly})

(def days
  #{:su
    :mo
    :tu
    :we
    :th
    :fr
    :sa})

(def constants
  (union frequencies days))

(defn keyword->constant-name [kw]
  (let [s (name kw)]
    (.toUpperCase s)))

(def keyword->constant
  (reduce
    (fn [acc kw]
      (let [s (keyword->constant-name kw)
            constant (aget js/RRule s)]
        (assoc acc kw constant)))
    {}
    constants))

(def constant->keyword
  (reduce
    (fn [acc [k v]]
      (let [field (if (frequencies k) :freq :byweekday)]
        (assoc-in acc [field v] k)))
    {}
    keyword->constant))

(defn replace-constants [m]
  (reduce
    (fn [acc [k v]]
      (assoc acc k (keyword->constant v v)))
    {}
    m))

(defn restore-constant [k v]
  (let [maybe-keyword (get-in constant->keyword [k v])]
    (or maybe-keyword v)))

(defn restore-constants [m]
  (reduce
    (fn [acc [k v]]
      (assoc acc k (restore-constant k v)))
    {}
    m))

(defn rrule-options* [k js-rrule]
  (-> js-rrule
      (aget k)
      (js->clj :keywordize-keys true)
      restore-constants))

(def rrule-options
  (partial rrule-options* "options"))

(def rrule-original-options
  (partial rrule-options* "origOptions"))

(declare rrule)

(deftype RRule [js-rrule prev-start]
  ILookup
  (-lookup [this k]
    (-lookup this k nil))
  (-lookup [_ k not-found]
    (let [s (name k)
          options (aget js-rrule "options")
          v (->> s
                 (aget options)
                 (restore-constant k)
                 js->clj)]
      (or v not-found)))

  IAssociative
  (-assoc [_ k v]
    (let [options (rrule-original-options js-rrule)
          with-k (assoc options k v)]
      (rrule with-k)))

  IMap
  (-dissoc [_ k]
    (let [options (rrule-original-options js-rrule)
          without-k (dissoc options k)]
      (rrule without-k)))

  ISeq
  (-first [_]
    (let [include-start? (nil? prev-start)
          start (or prev-start (aget js-rrule "options" "dtstart"))]
      (.after js-rrule start include-start?)))

  (-rest [this]
    (RRule. js-rrule (first this)))

  ISeqable
  (-seq [this]
    (lazy-cat
      [(first this)]
      (seq (rest this)))))

(defn js-rrule [x]
  (cond
    (string? x)  (js/RRule.rrulestr x)
    (map? x)     (js/RRule. (-> x replace-constants clj->js))
    :else        (throw (ex-info "Invalid RRule argument" {:data x}))))

(defn rrule [x]
  (->RRule (js-rrule x) nil))

(declare rrule-set)

(deftype RRuleSet [js-rrule-set init-start prev-start]
  ISeq
  (-first [_]
    (let [include-start? (nil? prev-start)
          start (or prev-start init-start)]
      (.after js-rrule-set start include-start?)))

  (-rest [this]
    (RRuleSet. js-rrule-set nil (first this)))

  ISeqable
  (-seq [this]
    (lazy-cat
      [(first this)]
      (seq (rest this))))

  ICollection
  (-conj [_ rrule]
    (let [rrules (set (aget js-rrule-set "_rrule"))
          with-v (conj rrules rrule)]
      (rrule-set with-v)))

  ISet
  (-disjoin [this rrule]
    (let [rrules (set (map rrule-original-options (aget js-rrule-set "_rrule")))
          without-v (disj rrules rrule)]
      (rrule-set without-v))))

(defn ->js-rrule [x]
  (cond
    (instance? js/RRule x)
    x

    (instance? RRule x)
    (.-js-rrule x)

    (or (map? x) (string? x))
    (-> x rrule .-js-rrule)

    :else
    (throw (ex-info "Invalid RRule argument" {:data x}))))

(defn earliest [& dates]
  (->> dates
       (sort-by #(.getTime %))
       first))

(defn rrule-set [rrules]
  (let [js-rrule-set (js/RRuleSet.)
        js-rrules (map ->js-rrule rrules)
        init-start (reduce
                     (fn [acc rrule]
                       (let [dtstart (aget rrule "options" "dtstart")]
                         (if acc
                             (earliest acc dtstart)
                             dtstart)))
                     nil
                     js-rrules)]
    (doseq [js-rrule js-rrules]
      (.rrule js-rrule-set js-rrule))
    (->RRuleSet js-rrule-set init-start nil)))
