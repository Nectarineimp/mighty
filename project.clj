(defproject mighty "0.1.0-SNAPSHOT"
  :description "An exploration of trees"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.3.1"]
                 [edu.stanford.nlp/stanford-corenlp "3.3.1" :classifier "models"]
                 [incanter/incanter-core "1.5.5"]]
  :jvm-opts ["-Xmx6g"]
  :jar-name "mighty.jar")
