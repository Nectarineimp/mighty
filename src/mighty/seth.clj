(ns tree.core
  (:require [clojure.pprint :refer [pprint]]
            [clojure.set :refer [index]])
)

(def transaction-list [[{:name "alpha" :n 7} {:name "beta" :n 6} {:name "delta" :n 5} {:name "gamma" :n 4}]
                       [{:name "alpha" :n 4} {:name "beta" :n 3} {:name "theta" :n 2} {:name "zeta" :n 1}]
                       [{:name "beta" :n 4} {:name "delta" :n 3} {:name "theta" :n 1}]
                       [{:name "epsilon" :n 2} {:name "phi" :n 1}]])

(declare process-transaction)

(defn update-node [tree main children]
  (let [tree (update-in tree [(:name main) :entity]
                        (fn [e]
                          (assoc e :count (inc (:count e)))))]
    (assoc tree
           (:name main)
           {:entity (:entity (get tree (:name main)))
            :children (if (not (empty? children))
                        (process-transaction (:children (get tree (:name main)))
                                             (first children)
                                             (rest children))
                        {})})))

(defn process-transaction [tree main children]
  (if (contains? tree (:name main))
    (update-node tree main children)
    (assoc tree
           (:name main)
           {:entity (assoc main :count 1)
            :children (if (not (empty? children))
                        (process-transaction {}
                                             (first children)
                                             (rest children))
                        {})})))

(defn run []
  (reduce
    (fn [tree transaction]
      (process-transaction tree
                           (first transaction)
                           (rest transaction)))
    {}
    transaction-list))

(def tree (run))

(keys tree)

;; keys get you nodes. :children get you branches. :entity gets you the current node or nil if you are at root.
;; So a top level word with keys word1 and word2 represents a branch situation

(keys tree)
(:children (get tree "alpha"))
(-> (get tree "alpha")
    :children
    keys)

(def new-tree (-> (get tree "alpha")
                  :children))
(:entity new-tree)
new-tree

(pprint new-tree)
(pprint tree)

(-> (get new-tree "beta")
    :children
    keys)

;; parent-list is a meta list of all the elements and the roots of the tree's they belong to.
;; If an element is it's own root you can ignore it. Example, since "alpha" is the only root
;; for "alpha" there is no need to process it at all. "beta" has two roots but one of them is
;; itself so ignore that one and search the "alpha" root only.
(def parent-list (sort-by first '(("alpha" ("alpha"))
                  ("beta" ("alpha" "beta"))
                  ("epsilon" ("epsilon"))
                  ("delta" ("alpha" "beta"))
                  ("phi" ("epsilon"))
                  ("theta" ("alpha" "beta"))
                  ("gamma" ("alpha"))
                  ("zeta" ("alpha")))))

parent-list


;; How many parents does an entity have? The more parents, the broader the meaning.
(def count-second (fn [x] (count (second x))))

(def name-count (juxt first count-second))

(doall (map name-count parent-list))

;; Process transaction and make a map of term and parent set.
;;first transaction
;;:name t
;;for each rest t
;; assoc name name

(def m {"theta" '("alpha" "beta") "delta" '("alpha" "beta")})
(get m "theta")
(assoc m "theta" (into (get m "theta") (list "acme")))

(defn add-to-mapping
  [m entity association]
  (assoc m entity (into (get m entity) (list association))))

(add-to-mapping '{"test" ("winning")} "test" "tigerblood")

(defn recur-into-parent-map
  (letfn into-parent-map
    ([parent-map transaction])
    ([parent-map transaction parent])
    (if (nil? parent)
      (let [parent (-> transaction
                       first
                       :name)
            children (->> transaction
                         rest
                         (map :name))]
        (recur (add-to-mapping parent-map (first children) parent) (rest children) parent)
      )
    )
  )
)

(def sample-transaction (first transaction-list))
(first sample-transaction)
(rest sample-transaction)

(into-parent-map {} (first transaction-list))

(defn make-parent-map
  [transaction-list]
  (let [parent-map {}]
    (map #(into-parent-map parent-map %) transaction-list)
  )
)

(make-parent-map transaction-list)


