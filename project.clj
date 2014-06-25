(defproject cav/asyncflow "0.1.0"
  :description "A future/promise model async flow based on core.async."
  :url "https://github.com/sethyuan/cav.asyncflow"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cav/cljcav "0.1.1"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]]
  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-2202"]]}}
  :plugins [[com.keminglabs/cljx "0.4.0"]
            [lein-cljsbuild "1.0.3"]
            [com.cemerick/clojurescript.test "0.3.1"]
            [codox "0.8.9"]]
  :hooks [cljx.hooks]
  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["test/clj" "target/tests"]
  :codox {:language :clojure
          :src-dir-uri "https://github.com/sethyuan/cav.asyncflow/blob/master/"
          :src-linenum-anchor-prefix "L"
          :src-uri-mapping {#"target/classes" #(str "src/" % "x")}}
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}
                  {:source-paths ["test/cljx"]
                   :output-path "target/tests"
                   :rules :clj}
                  {:source-paths ["test/cljx"]
                   :output-path "target/tests"
                   :rules :cljs}]}
  :cljsbuild {:builds [{:id "asyncflow-test"
                        :source-paths ["src/cljs" "test/cljs" "target/classes"]
                        :compiler {:output-to "target/asyncflow-test.js"
                                   :output-dir "target/asyncflow-test-out"
                                   :source-map "target/asyncflow-test.js.map"
                                   :optimizations :none
                                   :target :nodejs
                                   :pretty-print true}}]
              :test-commands {"unit-tests" ["node" "test/node_runner.js"
                                            "test/run.js"]}} 
  :aliases {"cljstest" ["do" ["cljx"] ["cljsbuild" "test"]]})
