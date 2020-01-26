(ns mutant-types.core)

(defprotocol Mutable
  (mut! [this, key, new-val] "Mutates value of field `key` to be new-val"))

(defn is-mutable? [x]
  (isa? Mutable x))

(def primitive-type-hint-fn?
  '#{int float double long short byte ints bytes shorts #_"TODO: More?"})

(defn get-raw-fields
  "Returns the same list `fields` of names provided without its metadata"
  [fields]
  (map #(with-meta % nil) fields))

(defn make-fields-mutable
  "Given a list `fields` of field names, returns the same list with all
  names annotated as unsynchronized-mutable"
  [fields]
  (map #(vary-meta % assoc :unsynchronized-mutable true) fields))

(defmacro defmutable
  {:style/indent 2}
  [name, fields & remaining-protocols]
  (let [raw-fields (get-raw-fields fields)]
    `(deftype ~name
         [~@(make-fields-mutable fields)]
       clojure.lang.ILookup
       (valAt [_ key#]
         (case key#
           ~@(mapcat (fn [k] [(keyword k) k]) raw-fields)
           nil))
       (valAt [_ key# not-found#]
         (case key#
           ~@(mapcat (fn [k] [(keyword k) k]) raw-fields)
           not-found#))
       Mutable
       ~(let [new-val-sym (gensym "new-val")
              type-hints (map #(-> % meta :tag) fields)]
          `(mut! [_, key#, ~new-val-sym]
                 (case key#
                   ~@(mapcat (fn [k h] [(keyword k)
                                       `(set! ~k ~(if (primitive-type-hint-fn? h)
                                                    `(~h ~new-val-sym)
                                                    new-val-sym))])
                             raw-fields
                             type-hints))))
       ~@remaining-protocols)))
