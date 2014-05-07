;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.

(defn recur-fibo [n]
  (letfn [(fib
           [current next n]
           (if (zero? n)
             current
             (recur next (+ current next) (dec n))))]
    (fib 0N 1N n)))

(recur-fibo 100)

(def transaction-list [[{:name "alpha" :n 7} {:name "beta" :n 6} {:name "delta" :n 5} {:name "gamma" :n 4}]
                       [{:name "alpha" :n 4} {:name "beta" :n 3} {:name "theta" :n 2} {:name "zeta" :n 1}]
                       [{:name "beta" :n 4} {:name "delta" :n 3} {:name "theta" :n 1}]
                       [{:name "epsilon" :n 2} {:name "phi" :n 1}]])

(defn add-to-mapping
  [m entity association]
  (assoc m entity (into (get m entity) (list association))))

(add-to-mapping '{"test" ("winning")} "test" "tigerblood")

(defn recur-into-parent-map
([transaction])
([parent-map transaction])
  (letfn [(into-parent-map
          ([parent-map transaction])
          ([parent-map transaction parent])
           (if (and (nil? parent) (< 0 (count transaction)))
             (let [parent (-> transaction
                              first
                              name)
                   children (->> transaction
                                 rest
                                 (map :name))]
               (recur (add-to-mapping parent-map (first children) parent) (rest children) parent))
             (if (< 0 (count transaction))
               (let [children (rest transaction)]
                 (recur (add-to-mapping parent-map (first children) parent) (rest children) parent)))
           ))]
      (into-parent-map {} transaction)
    )
)

(comment (defn recur-into-parent-map
  [transaction]
  (letfn [into-parent-map
    ([parent-map transaction])
    ([parent-map transaction parent])
    (if (nil? parent)
      (let [parent (-> transaction
                       first
                       :name)
            children (->> transaction
                         rest
                         (map :name))]
          (recur (add-to-mapping parent-map (first children) parent) (rest children) parent)))])
  (into-parent-map {} transaction)
))
