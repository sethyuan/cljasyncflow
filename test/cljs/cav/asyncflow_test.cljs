(ns cav.asyncflow-test
  (:require [cemerick.cljs.test]
            [cljs.core.async])
  (:require-macros [cemerick.cljs.test :refer [is deftest testing with-test done]]
                   [cav.cljs.asyncflow :refer [async]]))

(defn f
  ([cb] (.nextTick js/process (fn [] (cb (rand-int 100)))))
  ([x cb] (.nextTick js/process (fn [] (cb x))))
  ([x y cb] (.nextTick js/process (fn [] (cb x y))))
  ([x y z cb] (.nextTick js/process (fn [] (cb x y z))))
  ([a b c d cb] (throw (js/Error. "sync error."))))

(defn g [cb]
  (.nextTick js/process (fn [] (cb))))

(deftest ^:async simple-one
  (async
    (let [res (f ...)]
      (is (= !res !res))
      (done))))

(deftest ^:async multiple-returns
  (async
    (let [res (f 1 2 3 ...)]
      (let [[a b c] !res]
        (is (= 1 a))
        (is (= 2 b))
        (is (= 3 c))
        (done)))))

(deftest ^:async zero-returns
  (async
    (is (nil? !(g ...)))
    (done)))

(deftest ^:async first-as-exception-arg-with-1-other-arg
  (async
    (let [res (f nil 2 ...)]
      (let [a !res]
        (is (= 2 a))
        (done)))))

(deftest ^:async first-as-exception-arg
  (async
    (let [res (f nil 2 3 ...)]
      (let [[a b] !res]
        (is (= 2 a))
        (is (= 3 b))
        (done)))))

(deftest ^:async sequential
  (async
    (let [a (f 1 ...)]
      (let [b (f (+ 2 !a) ...)]
        (is (= !b 3))
        (done)))))

(deftest ^:async parallel
  (async
    (let [a (f "a" ...)
          b (f "b" ...)]
      (is (= !a "a"))
      (is (= !b "b"))
      (done))))

(deftest ^:async exception-handling-when-!
  (async
    (is (thrown? js/Error !(f (js/Error. "my custom error.") ...)))
    (done)))

(deftest ^:async exception-handling-when-calling
  (async
    (is (thrown? js/Error (f nil nil nil nil ...)))
    (done)))

(deftest ^:async mappable
  (async
    (let [res (mapv #(f % ...) [1 2 3])
          [a b c] !!res]
      (is (= 1 a))
      (is (= 2 b))
      (is (= 3 c))
      (done))))
