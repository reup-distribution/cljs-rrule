# cljs-rrule

`[cljs-rrule "0.1.0"]`

A ClojureScript wrapper for [rrule.js](https://github.com/jkbrzt/rrule), with common Clojure semantics for `RRule` and `RRuleSet`, conforming to [RFC 2445](https://tools.ietf.org/html/rfc2445) ([with some specific exceptions](https://github.com/jkbrzt/rrule#differences-from-icalendar-rfc)).

## Usage

### RRule

```clojure
(ns my-ns
  (:require [cljs-rrule.core :as rrule]))

(def dtstart
  (Date. 2016 6 6))

(def an-rrule
  (rrule/rrule
    {:freq :weekly
     :byweekday :we
     :dtstart dtstart}))

;; RRules access recurrences as a seq

(first an-rrule)
; => #inst "2016-07-06T07:00:00.000-00:00"

(take 4 an-rrule)
; => [#inst "2016-07-06T07:00:00.000-00:00"
;     #inst "2016-07-13T07:00:00.000-00:00"
;     #inst "2016-07-20T07:00:00.000-00:00"
;     #inst "2016-07-27T07:00:00.000-00:00"]
```

### RRuleSet

```clojure
(def rules
  [{:freq :weekly
    :byweekday :we
    :dtstart dtstart}
   {:freq :weekly
    :byweekday :fri
    :dtstart dtstart}])

(def an-rrule-set
  (rrule/rrule-set rules))

(first an-rrule-set)
; => #inst "2016-07-06T07:00:00.000-00:00"

(take 4 an-rrule-set)
; => [#inst "2016-07-06T07:00:00.000-00:00"
;     #inst "2016-07-08T07:00:00.000-00:00"
;     #inst "2016-07-13T07:00:00.000-00:00"
;     #inst "2016-07-15T07:00:00.000-00:00"]
```

## Releases

- 0.1.0 - Initial release

## License

Copyright (c) 2016, ReUp Distribution Inc
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
