(ns mighty.nlp
  (:require [clojure.string :as str :refer (join)]
            [clojure.pprint :refer (pprint)]
            [mighty.config :refer (config)])
  (:import (edu.stanford.nlp.pipeline StanfordCoreNLP
                                      Annotation)
           (edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation)
           edu.stanford.nlp.trees.tregex.TregexPattern
           edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon
           edu.stanford.nlp.trees.TreeCoreAnnotations$TreeAnnotation))

(def ^:private props
  (doto (java.util.Properties.)
    (.put "annotators" "tokenize, ssplit, pos, lemma, ner, parse, dcoref")
    (.put "parse.maxlen" (str (-> config :nlp :max-sentence-length)))
    (.put "pos.maxlen" (str (-> config :nlp :max-sentence-length)))))

(def ^:private pipeline (StanfordCoreNLP. props))

(defn- annotated-doc [s]
  (.process pipeline s))

(defn parse-sentences [s]
  (-> s
      annotated-doc
      (.get CoreAnnotations$SentencesAnnotation)
      vec))

(defn sentence->tree [s]
  (.get s TreeCoreAnnotations$TreeAnnotation))

(defn tree->tuple [t]
  (let [leaves (.getLeaves t)
        fix-apos #(str/replace % #" '" "'")]
    (->> leaves
         (map #(.value %))
         (join " ")
         fix-apos)
     ))

(def trees->tuples (partial map tree->tuple))

(defn change-tree! [tree pat-str & op-strs]
  (when tree
    (let [pattern (TregexPattern/compile pat-str)
          operations (->> op-strs
                          (map #(Tsurgeon/parseOperation %))
                          (Tsurgeon/collectOperations))]
      (Tsurgeon/processPattern pattern operations tree))))

(defn find-subtrees [pattern tree]
  (if tree
    (let [matcher (-> pattern
                      (TregexPattern/compile)
                      (.matcher tree))
          find-next-match! #(.findNextMatchingNode matcher)]
      (loop [matches [] has-match (find-next-match!)]
        (if has-match
          (recur (conj matches (.getMatch matcher))
                 (find-next-match!))
          matches)))
    []))

(defn remove-qualifying-phrases! [tree]
  (change-tree! tree "QP=qp" "prune qp"))

(defn remove-leading-determiners! [tree]
  (change-tree! tree "DT=dt >, NP" "prune dt"))

(defn correct-possessives! [tree]
  (change-tree! tree
                "/NN.?/=noun > (NP < (NP=parent < POS))"
                "move noun >-1 parent "))

(defn extract-noun-phrase-trees
  "Return a list of subtrees of t which represent noun phrases."
  [tree]
  (find-subtrees "NP !< NP" tree))

(defn sentence->noun-phrases [s]
  (-> s
      sentence->tree
      remove-leading-determiners!
      remove-qualifying-phrases!
      correct-possessives!
      extract-noun-phrase-trees
      trees->tuples))

(defn text->phrases [text]
  (->> text
       parse-sentences
       (mapcat sentence->noun-phrases)
       ))

(defn -main [& args]
  (time
   (-> args
       first
       slurp
       text->phrases
       pprint)))

(def sentences (->> "/home/peter/Documents/test-text/spooky-action.txt"
                    slurp
                    parse-sentences))
(sentence->noun-phrases (first sentences))


(annotated-doc (slurp "/home/peter/Documents/test-text/spooky-action.txt"))
(class pipeline)
(def document (slurp "/home/peter/Documents/test-text/spooky-action.txt"))
(def long-sentence "The team at Delft's experiments show that we're getting better all the time at creating these \"spooky actions\",
               but their experiments will have to be duplicated over far greater distances to show that signals between entangled particles occur at the speed of light.")
(text->phrases long-sentence)
(sentence->tree (first (parse-sentences long-sentence)))


