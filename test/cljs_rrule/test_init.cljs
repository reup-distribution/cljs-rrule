(ns cljs-rrule.test-init
  (:require [speclj.report.progress :refer [new-progress-reporter]]
            [speclj.run.standard :as speclj]))

(defn ^:export init []
  (set! speclj/armed true)
  (speclj/run-specs :color true :reporters [(new-progress-reporter)]))
