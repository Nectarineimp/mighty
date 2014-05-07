(ns mighty.fpgrowth
  (:require [clojure.zip :as z]))

;; FP-Growth (aka FP-Tree) requires the following
;; 1. Find all unique W in D with counts
;; 2. Determine minimum support. It can be based
;;    on a number of factors such as minimum count,
;;    maximum of desired W, a minimum d/D for w,
;;    and so forth. There are many options for this
;;    limit.
;; 3. Winnow W to W1 based on 2.
;; 4. Iterate on each transaction in T.
;;    Order t of T by frequency count.
;;    insert-tree(p of P, Tree)
;;    If Tree has a child p increment child by 1.
;;    If not create a new node with count 1.
;;    If P is non-empty call insert-tree(P, Node)

;; Mining an FP-Growth tree means finding paths to
;; your target w. The path of nodes minus the last
;; node (your target) is called the CPB, or
;; Conditional Pattern Base. To find the FP:
;; 1. Create a new FP tree from the CPB.
;; 2. create new W, Ws (Word list from search)
;; 3. Find all paths that lead to target and add w to Ws.
;; 4. Ws yields w {:label :support}, filter Ws on :support

;; T represents our transaction list
;; Each transaction is a document
;; Each entity is listed once
;; We assume that the transactions are
;; in order of frequence decending

(def T
  '(("f" "a" "c" "d" "g" "i" "m" "p")
    ("a" "b" "c" "f" "l" "m" "o")
    ("b" "c" "k" "s" "p")
    ("b" "f" "h" "j" "o")
    ("a" "f" "c" "e" "l" "p" "m" "n")
   )
)

(def W (distinct (sort (flatten T))))

W

(doall (for [x (map keyword W) y '(0)] (vector x y)))

(def fkeyword (comp keyword first))

(apply hash-map (doall (flatten (map (juxt fkeyword count) (partition-by identity (sort (flatten T)))))))

(def part-by-id (fn [c] (partition-by identity c)))
(def make-key-count (fn [c] (map (juxt fkeyword count) c)))
(def make-hash-map (fn [c] (apply hash-map c)))

(defn make-Wc [T]
  (-> T
    flatten
    sort
    part-by-id
    make-key-count
    flatten
    make-hash-map
  )
)

(def Wc (make-Wc T))

;; Wc has each entitys and it's corpus wide frequency. This is
;; used for sorting all transactions and dropping entities
;; that have too low a supporting frequency.
Wc

(defn count-term-inc [termlist term]
  "given a term, return the incremented count."
  (let [term-count ((keyword term) termlist)]
    (if (nil? term-count) nil (inc term-count))
  )
)

(def tl (hash-map :a 1 :b 2 :c 3))

(count-term-inc tl "a")

(:a Wc)


(def tc (map #(vector % ((keyword %) Wc)) (first T)))

(def comp2nd (comparator (fn [x y] (> (second x) (second y)))))

(sort comp2nd tc)

(last (sort comp2nd tc))
(first (sort comp2nd tc))

(sort comp2nd (filter #(> (second %) 1 ) tc))

(def min-support 2)

(defn prepare-transaction [t]
  (let
    [tc (map #(vector % ((keyword %) Wc)) (first T))]
    (map first (sort comp2nd (filter #(>= (second %) min-support ) tc)))))

(prepare-transaction (first T))

(def tree '[])

tree

(defn add-nodes [tree transaction]
  (let [loc (-> tree z/vector-zip)]
    (map #(z/append-child loc {:entity % :count 1}) transaction)
  )
)

(defn in-tree-top [tree transaction]
  (if (vector? (-> tree
      z/down
      z/node))
    "proceed with scan"
    (add-nodes tree transaction))
)

;(in-tree-top tree (prepare-transaction (first T)))

; This is how the tree is supposed to look after the first pass.
(def tree-manual '[{:entity "f", :count 1}{:entity "c", :count 1}{:entity "a", :count 1}{:entity "m", :count 1}{:entity "p", :count 1}])

(add-nodes '[] (prepare-transaction (first T)))
tree
