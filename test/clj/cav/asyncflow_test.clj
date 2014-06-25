(ns cav.asyncflow-test
  (:require [cav.asyncflow :refer [async]]
            [clojure.core.async :refer [<!!]]
            [clojure.test :refer :all]))

(deftest async-test
  (let [f (fn
            ([cb] (future (Thread/sleep 100)
                          (cb (rand-int 100))))
            ([x cb] (future (Thread/sleep 100)
                            (cb x)))
            ([x y cb] (future (Thread/sleep 100)
                              (cb x y)))
            ([x y z cb] (future (Thread/sleep 100)
                                (cb x y z)))
            ([a b c d cb] (throw (Exception. "sync error."))))
        g (fn [cb] (future (Thread/sleep 100) (cb)))]
    (testing "simple one."
      (<!! (async
             (let [res (f ...)]
               (is (= @res @res))))))

    (testing "multiple returns."
      (<!! (async
             (let [res (f 1 2 3 ...)]
               (let [[a b c] @res]
                 (is (= 1 a))
                 (is (= 2 b))
                 (is (= 3 c)))))))

    (testing "zero returns."
      (<!! (async
             (is (nil? @(g ...))))))

    (testing "first as exception argument with 1 other arg."
      (<!! (async
             (let [res (f nil 2 ...)]
               (let [a @res]
                 (is (= 2 a)))))))

    (testing "first as exception argument."
      (<!! (async
             (let [res (f nil 2 3 ...)]
               (let [[a b] @res]
                 (is (= 2 a))
                 (is (= 3 b)))))))

    (testing "sequential."
      (<!! (async
             (let [a (f 1 ...)]
               (let [b (f (+ 2 @a) ...)]
                 (is (= @b 3)))))))

    (testing "parallel."
      (<!! (async
             (let [a (f "a" ...)
                   b (f "b" ...)]
               (is (= @a "a"))
               (is (= @b "b"))))))

    (testing "exception handling when deref."
      (<!! (async
             (is (thrown? Exception
                          (deref (f (Exception. "my custom error.") ...)))))))

    (testing "exception handling when calling."
      (<!! (async
             (is (thrown? Exception (f nil nil nil nil ...))))))
    
    (testing "mappable"
      (<!! (async
             (let [[a b c] (->> [1 2 3]
                                (mapv #(f % ...))
                                (mapv deref))]
               (is (= 1 a))
               (is (= 2 b))
               (is (= 3 c))))))))
