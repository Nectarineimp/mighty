(ns mighty.arrtree
  (:require [clojure.pprint :refer [pprint]]))

 (comment
 arrtree
 example implementation of the Array Tree method
 for Frequent Pattern Analysis.

 FP-Growth (aka FP-Tree) requires the following

 PREPARATION
 1. Find all unique W in D with counts
 2. Determine minimum support. It can be based
    on a number of factors such as minimum count,
    maximum of desired W, a minimum d/D for w,
    and so forth. There are many options for this
    limit.
 3. (optional) Winnow W to W1 based on 2.
    (alternate) Leave it be, winnow on search only.
    this allows for the ability to add documents
    later on
 4. Build a node-index from W.
   a. one entry per unique entity label.
   b. contains :first and :last node addresses.
   c. contains :node-count
   d. can be saved / loaded to/from disk or datomic
   e. kept in memory always
 5. Build a root-index
   a. kept in memory always
   b. can be saved / loaded to/from disk or datomic
   c. ONLY contains root nodes
   d. Unique entity label (optional: not-unique is sharding will help)
   e. Reference to a node-array

 BUILDING
 1. Considerations
   a. Leaf to Root has a perplexity of 0.
   b. Root to Leaf has a perplexity of 0 to infinity.
   c. Potential size of this system is billions of
      nodes, possibly hundreds of billions on very
      large and complex corpora. Sharding is a
      design requirement.
   d.
 4. Iterate on each transaction in T.
    Order t of T by frequency count.
    insert into tree:
    If Tree-Root does not contain t0,
      create new element in Tree-Root and new Tree.
    If Tree has a node t increment node-count by 1.
      If not create a new node with count 1 and set
      last node as parent. Root elements have nil parent.
    If Parent is not nil, update child sequence with
    this node's address {:root :nth}.
)

 ;; array tests
 (def ma-arr (make-array Integer/TYPE 10000 4))
 (pprint (take 10 ma-arr))

 (def ia-arr (int-array [9 8 7 6 5 4 3 2 1 0]))
 (def ia-arr2 (int-array 10000 4)) ;; single dimension only, 2nd value is initialization

 (pprint ia-arr)
 (pprint (take 10 ia-arr2))

 (def to-arr (to-array "Hello Homies!"))
 (alength to-arr)
 (pprint "hello")
 (pprint (aget to-arr 0))
 (apply str (into [] to-arr))

 (def first-array-atom (atom (make-array Integer/TYPE 100 4)))


 (defn grow-array! [a s]
   (let [a1 (atom a)
         a2 (make-array Integer/TYPE (+ (length a) s) 4)]
     (into a2 a)
     (swap! a1 a2)
   ))


