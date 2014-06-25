(ns cav.asyncflow
  (:require [clojure.walk :refer [prewalk]]
            [clojure.core.async :refer [go chan close! >! <!!]]))

(defmacro async
  "Async flow coordination."
  [& body]
  (cons `go
        (prewalk
          (fn [sexp]
            (if (and (list? sexp) (some #{'...} sexp))
              (let [c (gensym)]
                `(let [~c (chan)]
                   ~(replace {'... `(fn [& xs#] (go (>! ~c (if (nil? xs#) () xs#))
                                                    (close! ~c)))}
                             sexp)
                   (delay (let [[head# & tail# :as res#] (<!! ~c)]
                            (when (instance? Throwable head#) (throw head#))
                            (cond
                              (and (some? head#) (some? tail#)) res#
                              (some? head#) head#
                              (some? tail#) (if (= (count tail#) 1) (first tail#) tail#)
                              :else nil)))))
              sexp))
          body)))
