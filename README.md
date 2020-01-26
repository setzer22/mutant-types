# Mutant Types

This small clojure library allows you to define `Mutable`s, a kind of `deftype`
with all of its fields marked as `:unsynchronized-mutable` which allows setting
values from outside and works like a regular map (i.e. Implements ILookup like
`defrecord`).

As with regular `deftype`, type hints work, and primitive type hints internally
create a Java class with primitive types. Similarly, other kinds of type hints
are used by the compiler but will not be enforced at runtime (i.e. you can set a
field hinted as `^Integer` to the value `"Hello"`, but not if it's an `^int`).

The `defmutable` in this repo is inspired by the great
[https://github.com/arcadia-unity/Arcadia](Arcadia) library.

## Usage
 
`Mutable`s follow the same structure as a deftype. Here's an example:

```clojure
(require '[mutant.core :as mut])

(mut/defmutable MyType [^int a, ^String b])

(let [m (->MyType 1 "2")]
  (mut/mut! m :a 25)
  (mut/mut! m :b "Hello")
  (println (:a m)) ; => 25
  (println (:b m)) ; => "Hello"
  
  (mut/mut! m :a "Hello") ; => ClassCastException
  (mut/mut! m :b 3)); => This works
```

## Note

Clojure doesn't have a macro like this for a good reason. Most of the time there
is no need for the kind of `Mutable`s introduced by this project. This should
only be used in very tight loops of the codebase, and always as an
implementation detail, never as part of an API.

Please use with caution and only when really necessary. Also remember to
always measure first before optimizing!
