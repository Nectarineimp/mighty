(ns mighty.dcoref
  (:require [clojure.string :as str :refer (join)]
            [clojure.pprint :refer (pprint)]
            [mighty.config :refer (config)]
            [incanter.core :as incanter]
            [incanter.stats :as stats]
            [incanter.distributions :as dist])
  (:import (edu.stanford.nlp.pipeline StanfordCoreNLP
                                      Annotation)
           (edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation
                                  CoreAnnotations$TokensAnnotation
                                  CoreAnnotations$MentionTokenAnnotation)
           (edu.stanford.nlp.dcoref CorefCoreAnnotations
                                    CorefCoreAnnotations$CorefChainAnnotation
                                    CorefCoreAnnotations$CorefGraphAnnotation)
           edu.stanford.nlp.trees.tregex.TregexPattern
           edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon
           edu.stanford.nlp.trees.TreeCoreAnnotations$TreeAnnotation
           (java.lang.reflect.Method))
  )

(def ^:private props
  ;; props are properties used by the Stanford CoreNLP system. We establish a pipeline of annotation this way.
  (doto (java.util.Properties.)
    (.put "annotators" "tokenize, ssplit, pos, lemma, ner, parse, dcoref")
    (.put "parse.maxlen" (str (-> config :nlp :max-sentence-length)))
    (.put "pos.maxlen" (str (-> config :nlp :max-sentence-length)))))

(def ^:private pipeline
  ;; pipeline is the NLP stack we use to process our text.
  (StanfordCoreNLP. props))

(defn- annotated-doc [s]
  ;; This function take our text and passes it through the NLP pipeline.
  (.process pipeline s))


;; Examples using data from a document about quantum teleportation.
(def raw-document (slurp "universe.txt"))
(def annotated-document (annotated-doc raw-document))
(def dcoref-chains (.get annotated-document CorefCoreAnnotations$CorefChainAnnotation))
(def chain (map #(.getValue %) dcoref-chains))
(def representative-mentions (map #(.getRepresentativeMention %) chain))
(def annotated-sentences (.get annotated-document CoreAnnotations$SentencesAnnotation))

(defn- tokens-sentence
  ;; Takes the processed document and recovers the token annotations from it.
  [annotated-sentences sentNum]
  (.get (nth annotated-sentences sentNum) CoreAnnotations$TokensAnnotation))

(defn- sentence-2-tokens
  ;; takes a single sentence and produces the token objects.
  [sentence]
  (.get sentence CoreAnnotations$TokensAnnotation))

(defn- rep-mention-coordinates
  ;; Takes a representitive mention and pulls the coordinates for it as a vector of [sentence number, start index and end index]. These values are corrected to 0-start array coordinates.
  ;; For example the first sentence is 0 not 1.
  [mention]
  (let [sentNum    (dec (.sentNum mention))
        startIndex (dec (.startIndex mention))
        endIndex   (dec (.endIndex   mention))]
    (vector sentNum startIndex endIndex))
)

(defn- select-tokens
  ;; This takes the token list and the start and end coordinates and produces a list of the token objects.
  [tokens startIndex endIndex]
  (->> tokens
     (split-at startIndex)
     second
     (split-at (- endIndex startIndex))
     first))

(defn- coord-to-tokens
  ;; This takes annotated sentences and coordinates in a vector (as generated by rep-mention-coordinates for example) and produces token objects.
  [annotated-sentences [sentNum startIndex endIndex]]
  (let [tokens (.get (nth annotated-sentences sentNum) CoreAnnotations$TokensAnnotation)]
    (select-tokens tokens startIndex endIndex)))

(defn hash-tokens
  ;; Takes token coordinates and produces token maps.
  ;; as annotated sentences
  ;; tc token coordinates
  [as tc]
  (map #(hash-map :word (.originalText %) :ner (.ner %) :tag (.tag %)) (coord-to-tokens as tc)))

(def noun-tags '("NN" "NNS" "NNP" "NNPS"))
(def good-body-tags '("DT" "IN" "RP" "TO" "JJ" "JJR" "JJS" "NN" "NNS" "NNP" "NNPS"))
(def good-first-tags '("NN" "NNS" "NNP" "NNPS"))

(defn noun-tag? [tag]
  (some #(= tag %) noun-tags))

(defn good-body-tags? [token]
  (some #(= (:tag token) %) good-body-tags))

(defn good-first-tag? [token]
  (some #(= (:tag token) %) good-first-tags))

(defn just-text
  ;; ht hash tokens
  [ht]
  (map :word ht))

(defn text-tag
  ;; ht hash tokens
  [ht]
  (map #(str (:word %) " - " (:tag %)) ht))

(defn get-rm-text [rm as]
  (let [ht (->> rm
       rep-mention-coordinates
       (hash-tokens as))]
    (if (good-first-tag? (first ht))
      (flatten (list (:word (first ht)) (map :word (filter good-body-tags? (rest ht)))))
      (map :word (filter good-body-tags? (rest ht)))
    )
  )
)
(first representative-mentions)
(class annotated-sentences)
(class representative-mentions)
(class chain)
(first chain)
(class dcoref-chains)
(first dcoref-chains)
(class annotated-document)
(.size annotated-document)

(get-rm-text (first representative-mentions) annotated-sentences)
(:tag (get-rm-text (first representative-mentions) annotated-sentences))

;; example
(map #(get-rm-text % annotated-sentences) representative-mentions)

(map #(.tag %) (sentence-2-tokens (nth annotated-sentences 3)))
(map #(.originalText %) (sentence-2-tokens (nth annotated-sentences 3)))
(.getMentionMap (nth chain 3))
(.getSource (first (.keySet (.getMentionMap (nth chain 3)))))
(.getTarget (first (.keySet (.getMentionMap (nth chain 3)))))
(defn filter-chain [chain]
  (->> chain
  .getMentionMap
  count
  (< 1)))

(filter-chain (second chain))

(map #(get-rm-text (.getRepresentativeMention %) annotated-sentences) (filter filter-chain chain))

(defn get-rm-mm [chain annotated-sentences]
  (let [rm (.getRepresentativeMention chain)
        mm (.getMentionMap chain)]
       ;; process goes here
  ))

(nth (tokens-sentence annotated-sentences 0) 7)

Sorting with depuplication
(def unsorted-seq (stats/sample (range 99) :size 10))
unsorted-seq

(defn dedup-sort [coll]
  (->> coll
       sort
       distinct))
(dedup-sort unsorted-seq)

