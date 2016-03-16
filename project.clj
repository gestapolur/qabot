(defproject mockersanswer "0.1.0-SNAPSHOT"
  :description "Frontend of a neural network based chat bot."
  :url "https://twitter.com/mockersanswer"
  :license {:name "GNU GENERAL PUBLIC LICENSE"
            :url "http://fsf.org"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [http-kit "2.1.19"]
                 [org.clojure/data.json "0.2.6"]
                 [twitter-api "0.7.8"]]
  :main ^:skip-aot mockersanswer.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
