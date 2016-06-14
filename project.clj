(defproject cljs-rrule "0.1.0"
  :license {:name "BSD 2-clause \"Simplified\" License"
            :url "http://opensource.org/licenses/BSD-2-Clause"
            :year 2016
            :key "bsd-2-clause"}
  :description "ClojureScript rrule.js wrapper"
  :url "https://github.com/reup-distribution/cljs-rrule"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228" :scope "provided"]]
  :aliases {"test" ["with-profile" "dev" "cljsbuild" "test"]
            "cljsbuild" ["with-profile" "dev" "cljsbuild"]}
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]
                   :plugins [[lein-cljsbuild "1.1.2"]]
                   :clean-targets ^{:protect false} [:target-path "target/cljsbuild"]
                   :cljsbuild {:builds [{:id "whitespace"
                                         :source-paths ["src" "test"]
                                         :compiler {:output-to "target/cljsbuild/build.js"
                                                    :output-dir "target/cljsbuild"
                                                    :optimizations :whitespace
                                                    :source-map "target/cljsbuild/build.js.map"}}]
                               :test-commands {"whitespace" ["phantomjs"
                                                             "test/runner"
                                                             "target/cljsbuild/build.js"]}}}})
