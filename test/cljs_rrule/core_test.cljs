(ns cljs-rrule.core-test
  (:require-macros [speclj.core :refer [describe it should should-not run-specs]])
  (:require [speclj.core]))

(describe "Truth"

  (it "is true"
    (should true))

  (it "is not false"
    (should-not false)))

(run-specs)
