(ns mutant-types.core-test
  (:require [mutant.core :as mut]
            [clojure.test :as t]))

(t/deftest basic-functionality-test
  (t/testing "Creating a mutant"
    (t/is (mut/defmutable TestType []))
    (t/is (mut/defmutable TestType2 [a]))
    (t/is (mut/defmutable TestType3 [a b c]))
    (t/is (mut/defmutable TestType4 [^int a ^float b c]))
    (t/is (mut/defmutable TestType5 [^double a ^long b ^String c])))

  (t/testing "Getting fields"
    (mut/defmutable TestType6 [^int a ^long b ^short c])
    (t/is (= 1 (:a (->TestType6 1 2 3))))
    (t/is (= 2 (:b (->TestType6 1 2 3))))
    (t/is (= 3 (:c (->TestType6 1 2 3))))

    (mut/defmutable TestType7 [^ints a, ^String s, ^Object ob])

    (let [m (->TestType7 (int-array [1 2 3]) "123" (Object.))]
      (t/is (= "class [I" (-> m :a type str)))
      (t/is (= "class java.lang.String" (-> m :s type str)))
      (t/is (= "class java.lang.Object" (-> m :ob type str)))))

  (t/testing "Setting fields"
    (mut/defmutable TestType8 [^int a, ^ints b, ^Integer c])
    (let [m (->TestType8 42, (int-array [1 2 3 4 5]),
                         {:some "arbitrary" :object "here"})]
      (t/is (do (mut/mut! m :a 23)
                (= (:a m) 23))
            "A primitive field can be mut! to an element of its type")
      (t/is (do (mut/mut! m :a 23.0)
                (= (:a m) 23))
            "A cast is automatically performed for comptaible types")
      (t/is (try (do (mut/mut! m :a "hello") false)
                 (catch java.lang.ClassCastException _ true))
            "But a non-compatible type will throw on mut!")
      (t/is (try (do (mut/mut! m :a "hello") false)
                 (catch java.lang.ClassCastException _ true))
            "But a non-compatible type will throw on mut!")
      (t/is (try (do (mut/mut! m :b "hello") false)
                 (catch java.lang.ClassCastException _ true))
            "Same holds for primitive arrays")
      (t/is (mut/mut! m :c "hello")
            "But type hints for other kinds of objects don't work the same"))))
