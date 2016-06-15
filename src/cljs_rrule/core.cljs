(ns cljs-rrule.core
  (:refer-clojure :exclude [frequencies])
  (:require [clojure.set :refer [union]]
            [clojure.string :as string]
            [cljsjs.rrule]))

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

(def constant->keyword*
  (reduce
    (fn [acc [k v]]
      (let [field (if (frequencies k) :frequencies :days)]
        (assoc-in acc [field v] k)))
    {}
    keyword->constant))

(def day-constants
  "Unfortunately, rrule.js uses distinct objects *and* integers for days of the week"
  (let [m (:days constant->keyword*)]
    (reduce
      (fn [acc [const kw]]
        (let [n (.-weekday const)]
          (assoc acc n kw)))
      m
      m)))

(def constant->keyword
  {:freq (:frequencies constant->keyword*)
   :byweekday day-constants
   :wkst day-constants})

(defn multi? [x]
  (or (sequential? x) (array? x)))

(defn replace-kws* [v]
  (if (multi? v)
      (map replace-kws* v)
      (keyword->constant v v)))

(defn replace-kws [m]
  (reduce
    (fn [acc [k v]]
      (assoc acc k (replace-kws* v)))
    {}
    m))

(defn restore-kws* [k v]
  (if (multi? v)
      (map (partial restore-kws* k) v)
      (get-in constant->keyword [k v] v)))

(defn restore-kws [m]
  (reduce
    (fn [acc [k v]]
      (assoc acc k (restore-kws* k v)))
    {}
    m))

(defn rrule-options* [k js-rrule]
  (-> js-rrule
      (aget k)
      (js->clj :keywordize-keys true)
      restore-kws))

(def rrule-options
  (partial rrule-options* "options"))

(def rrule-original-options
  (partial rrule-options* "origOptions"))

(defn date-str [date]
  (-> (.toJSON date)
      ;; Remove fractional seconds
      (string/replace #"\.\d+" "")
      ;; Remove punctuation
      (string/replace #"\W" "")))

(defn ensure-seq [x]
  (if (sequential? x)
      x
      [x]))

(defn upper-case [x]
  (-> x name string/upper-case))

(defn upper-case-all [xs]
  (map upper-case xs))

(def ->js-fns
  {:bysetpos ensure-seq
   :bymonth ensure-seq
   :bymonthday ensure-seq
   :byyearday ensure-seq
   :byweekno ensure-seq
   :byweekday (comp upper-case-all ensure-seq)
   :byhour ensure-seq
   :byminute ensure-seq
   :bysecond ensure-seq
   :byeaster ensure-seq
   :dtstart date-str
   :freq upper-case
   :until date-str
   :wkst upper-case})

(defn ->js [k v]
  (let [f (->js-fns k identity)]
    (f v)))

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
                 (restore-kws* k)
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
      (seq (rest this))))

  IEncodeJS
  (-clj->js [_]
    (let [options (rrule-original-options js-rrule)]
      (clj->js
        (reduce
          (fn [acc [k v]]
            (if (nil? v)
                acc
                (assoc acc k (->js k v))))
          {}
          options)))))

(defn js-rrule [x]
  (cond
    (string? x)  (js/RRule.rrulestr x)
    (map? x)     (js/RRule. (-> x replace-kws clj->js))
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
