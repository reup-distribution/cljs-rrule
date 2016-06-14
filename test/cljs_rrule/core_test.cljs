(ns cljs-rrule.core-test
  (:require-macros [speclj.core :refer [describe it should should-not run-specs]])
  (:require [speclj.core]
            [cljs-rrule.core :refer [RRule]]))

(describe "RRule is available"
  (it "has access to js/RRule"
    (should-not (nil? RRule))))

(run-specs)
