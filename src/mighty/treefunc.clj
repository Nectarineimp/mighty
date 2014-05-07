;; Tree functions

(ns mighty.treefunc
  (:require [clojure.zip :as z]))

(def v [[1 2 [3 4]] [5 6]])

(-> v
    z/vector-zip
    z/node)

(vector? (-> v
    z/vector-zip
    z/down
    z/node))

(-> v
    z/vector-zip
    z/down
    z/node)

(vector? (-> v
    z/vector-zip
    z/down
    z/right
    z/down
    z/node))

(-> v
    z/vector-zip
    z/down
    z/right
    z/down
    z/node)

(def fpg [
          [{:name "alpha" :n 1} {:name "beta" :n 1} {:name "delta" :n 1} {:name "gamma" :n 1}]
          [{:name "epsilon" :n 1} {:name "phi" :n 1}]
         ])

(-> fpg
    z/vector-zip
    z/down
    z/down
    z/rightmost
    z/node)

(-> fpg
    z/vector-zip
    z/down
    (z/append-child {:name "zeta" :n 1})
)

(defn find-first-leaf
  [x]
  (if (z/branch? x)
    (find-first-leaf (z/down x))
    x
  )
)

(-> fpg
    z/vector-zip
    find-first-leaf
    z/node)