(defproject cljs-rrule "0.1.0"
  :license {:name "BSD 2-clause \"Simplified\" License"
            :url "http://opensource.org/licenses/BSD-2-Clause"
            :year 2016
            :key "bsd-2-clause"}
  :description "ClojureScript rrule.js wrapper"
  :url "https://github.com/reup-distribution/cljs-rrule"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228" :scope "provided"]
                 [cljsjs/rrule "2.1.0-838226d-0"]]
  :aliases {"test" ["with-profile" "dev" "cljsbuild" "test"]
            "cljsbuild" ["with-profile" "dev" "cljsbuild"]}
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]
                   :plugins [[lein-cljsbuild "1.1.2"]]
                   :clean-targets ^{:protect false} [:target-path "target/cljsbuild"]
                   :cljsbuild {:builds [{:id "advanced"
                                         :source-paths ["src" "test"]
                                         :compiler {:output-to "target/advanced/build.js"
                                                    :output-dir "target/advanced"
                                                    :optimizations :advanced
                                                    :source-map "target/advanced/build.js.map"}}
                                        {:id "whitespace"
                                         :source-paths ["src" "test"]
                                         :compiler {:output-to "target/whitespace/build.js"
                                                    :output-dir "target/whitespace"
                                                    :optimizations :whitespace
                                                    :source-map "target/whitespace/build.js.map"}}]
                               :test-commands {"advanced" ["phantomjs"
                                                           "test/runner"
                                                           "target/advanced/build.js"]
                                               "whitespace" ["phantomjs"
                                                             "test/runner"
                                                             "target/whitespace/build.js"]}}}})
