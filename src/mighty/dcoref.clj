(ns mighty.dcoref
  (:require [clojure.string :as str :refer (join)]
            [clojure.pprint :refer (pprint)]
            [mighty.config :refer (config)])
  (:import (edu.stanford.nlp.pipeline StanfordCoreNLP
                                      Annotation)
           (edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$TokensAnnotation)
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

(def dcoref-chains (.get annotated-document CorefCoreAnnotations$CorefChainAnnotation))

(def chain (map #(.getValue %) dcoref-chains))

(def representative-mentions (map #(.getRepresentativeMention %) chain))

;;TODO get tokens for each sentence
;; CoreAnnotations$SentencesAnnotation

(def annotated-sentences (.get annotated-document CoreAnnotations$SentencesAnnotation))

(defn tokens-sentence [annotated-sentences sentNum]
  (.get (nth annotated-sentences sentNum) CoreAnnotations$TokensAnnotation))

(defn rep-mention-coordinates [mention]
  (let [sentNum    (dec (.sentNum mention))
        startIndex (dec (.startIndex mention))
        endIndex   (dec (.endIndex   mention))]
    (vector sentNum startIndex endIndex))
)

;; ex. (rep-mention-coordinates (nth representative-mentions 5))

(defn select-tokens [tokens startIndex endIndex]
  (->> tokens
     (split-at startIndex)
     second
     (split-at (- endIndex startIndex))
     first))

(defn coord-to-tokens [annotated-sentences [sentNum startIndex endIndex]]
  (let [tokens (.get (nth annotated-sentences sentNum) CoreAnnotations$TokensAnnotation)]
    (select-tokens tokens startIndex endIndex)))

(->> (first representative-mentions)
     rep-mention-coordinates
     (coord-to-tokens annotated-sentences)
     )

(defn badtag? [corelabel]
         (let [badtags '("POS")]
           (some #(= (.tag corelabel) %) badtags)))

(map badtag? (->> (first representative-mentions)
     rep-mention-coordinates
     (coord-to-tokens annotated-sentences)
     ))

(map #(.lemma %) (coord-to-tokens annotated-sentences [0 14 17]))
(map #(.ner %) (coord-to-tokens annotated-sentences [0 14 17]))
(map #(.tag %) (coord-to-tokens annotated-sentences [0 14 17]))

(some #(= "POS" %) '("NNP" "NNP" "POS"))


;;TODO
;; Now match representative-mentions with individual chain mentions
;; Try to weed out non PRP and @ mentions. Instead create new associations.
;; Example. It looked like an airplane. Replace It with representative-mention
;; and if airplane has a more specific mention add that to the associations.
