(ns cav.cljs.asyncflow
  (:require [clojure.walk :refer [prewalk]]
            [cav.core :refer [rm]]))

(defmacro ^:private wait
  "Await for an async call."
  [x]
  `(let [r# ~x
         [v# c#] (deref r#)]
     (if-not (nil? v#)
       v#
       (let [[head# & tail# :as res#] (cljs.core.async/<! c#)]
         (when (instance? js/Error head#) (throw head#))
         (let [v# (cond
                    (and (not (nil? head#)) (not (nil? tail#))) res#
                    (not (nil? head#)) head#
                    (not (nil? tail#)) (if (= (count tail#) 1) (first tail#) tail#)
                    :else nil)]
           (reset! r# [v#])
           v#)))))

(defn- find-elem
  "Find the element next to ! or !!"
  [sexp bang]
  (loop [exp sexp]
    (when-let [fe (first exp)]
      (if (= bang fe)
        (second exp)
        (recur (rest exp))))))

(defn- !!-handling
  "!! handling."
  [exp]
  `(let [r# ~exp]
     (doseq [x# r#] (wait x#))
     (mapv (comp first deref) r#)))

(defmacro async
  "Async flow coordination."
  [& body]
  (cons `cljs.core.async.macros/go
        (prewalk
          (fn [sexp]
            (cond
              (and (list? sexp) (some #{'...} sexp))
              (let [c (gensym)]
                `(let [~c (cljs.core.async/chan)]
                   ~(replace {'... `(fn [& xs#]
                                      (cljs.core.async.macros/go
                                        (cljs.core.async/>! ~c (if (nil? xs#) [] xs#))
                                        (cljs.core.async/close! ~c)))}
                             sexp)
                   (atom [nil ~c])))
              (and (coll? sexp) (some #{'!!} sexp))
              (let [exp (find-elem sexp '!!)]
                (->> sexp
                     (rm #{'!!})
                     (replace {exp (!!-handling exp)})))
              (and (symbol? sexp) (.startsWith (name sexp) "!!"))
              (!!-handling (-> (name sexp) (subs 2) (symbol)))
              (and (coll? sexp) (some #{'!} sexp))
              (let [exp (find-elem sexp '!)]
                (->> sexp
                     (rm #{'!})
                     (replace {exp `(wait ~exp)})))
              (and (symbol? sexp) (.startsWith (name sexp) "!"))
              `(wait ~(-> (name sexp) (subs 1) (symbol)))
              :else sexp))
          body)))
