(ns mighty.testnlp
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
    (.put "annotators" "tokenize, ssplit, pos, lemma, parse")
    (.put "parse.maxlen" (str (-> config :nlp :max-sentence-length)))
    (.put "pos.maxlen" (str (-> config :nlp :max-sentence-length)))))

;;(.put "annotators" "tokenize, ssplit, pos, lemma, ner, parse, dcoref")

(def ^:private pipeline (StanfordCoreNLP. props))

(defn- annotated-doc [s]
  (.process pipeline s))

(def sample "Scientists in the Netherlands have achieved a breakthrough in quantum teleportation that could quash Albert Einstein’s objection to the notion of quantum entanglement.")
(def sample2 (slurp "/home/peter/Documents/test-text/spooky-action.txt"))
(def sample4 "Scientists in the Netherlands have achieved a breakthrough in quantum teleportation that could quash Albert Einstein’s objection to the notion of quantum entanglement.\nEinstein could not be reached for comment.")
(def sample3 "Scientists in the Netherlands have achieved a breakthrough in quantum teleportation that could quash Albert Einstein’s objection to the notion of quantum entanglement, which he famously labelled “spooky action at a distance”.")
sample3
(annotated-doc sample)
(annotated-doc sample3)
(count sample2)
(annotated-doc sample4)
(class (str (doall (take 3886 sample2))))
(annotated-doc (str (doall (take 3886 sample2))))
