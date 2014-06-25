# asyncflow

A future/promise model async flow based on core.async.

Can be used with both Clojure and ClojureScript.

## Installation

[![cav/asyncflow](http://clojars.org/cav/asyncflow/latest-version.svg)](http://clojars.org/cav/asyncflow)

## Example

for Clojure

```clojure
(ns cav.example
  (:require [cav.asyncflow :refer [async]]))

(defn f [x callback]
  (future
    (Thread/sleep 10)
    (callback (inc x))))

;; Sequential.
;; Will print "a = 2, b = 4"
(async
  (let [a (f 1 ...)
        b (f (inc @a) ...)]
    (printf "a = %d, b = %d" @a @b)
    (flush)))

;; Parallel
;; Will print "a = 2, b = 3"
(async
  (let [a (f 1 ...)
        b (f 2 ...)]
    (printf "a = %d, b = %d" @a @b)
    (flush)))
```

for ClojureScript

```clojure
(ns cav.example
  (:require-macros [cav.cljs.asyncflow :refer [async]]))

(defn f [x callback]
  (js/setTimeout
    (fn [] (callback (inc x)))
    10))

;; Sequential.
;; Will print "a = 2, b = 4"
(async
  (let [a (f 1 ...)
        b (f (inc !a) ...)]
    (printf "a = %d, b = %d" !a !b)
    (flush)))

;; Parallel
;; Will print "a = 2, b = 3"
(async
  (let [a (f 1 ...)
        b (f 2 ...)]
    (printf "a = %d, b = %d" !a !b)
    (flush)))
```

If you want to wait for a collection of async calls in ClojureScript, you can use `!!`, like this:

```clojure
(async
  (let [res (map #(f % ...) (range 3))
        [a b c] !!res]
    (printf "a = %d, b = %d, c = %d" a b c)
    (flush)))
```

The same logic can be expressed in Clojure like this:

```clojure
(async
  (let [res (map #(f % ...) (range 3))
        [a b c] (mapv deref res)]
    (printf "a = %d, b = %d, c = %d" a b c)
    (flush)))
```

## License

(The MIT License)

Copyright (c) 2014 Seth Yuan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
