(ns mighty.dcoref
  (:require [clojure.string :as str :refer (join)]
            [clojure.pprint :refer (pprint)]
            [mighty.config :refer (config)])
  (:import (edu.stanford.nlp.pipeline StanfordCoreNLP
                                      Annotation)
           (edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation)
           (edu.stanford.nlp.dcoref CorefCoreAnnotations
                                    CorefCoreAnnotations$CorefChainAnnotation
                                    CorefCoreAnnotations$CorefGraphAnnotation)
           edu.stanford.nlp.trees.tregex.TregexPattern
           edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon
           edu.stanford.nlp.trees.TreeCoreAnnotations$TreeAnnotation
           (java.lang.reflect.Method))
  )

(def ^:private props
  (doto (java.util.Properties.)
    (.put "annotators" "tokenize, ssplit, pos, lemma, ner, parse, dcoref")
    (.put "parse.maxlen" (str (-> config :nlp :max-sentence-length)))
    (.put "pos.maxlen" (str (-> config :nlp :max-sentence-length)))))

(def ^:private pipeline (StanfordCoreNLP. props))

(defn- annotated-doc [s]
  (.process pipeline s))

(def raw-document (slurp "/home/peter/Documents/test-text/spooky-action.txt"))

(def annotated-document (annotated-doc raw-document))

(def dcoref-chains (.get annotated-document CorefCoreAnnotations$CorefChainannotation))

(def chain (map #(.getValue %) dcoref-chains))