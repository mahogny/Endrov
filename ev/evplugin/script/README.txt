EV Script
======================================================================

functions with fixed amount of parameters?
is (a,b) a Vector? what about (a)? should not be a vector.

when is a function evaluated?



Basic Functions:

map :: (a->b) -> [a] -> [b]
filter :: (a->Boolean) -> [a] -> [a]
? :: Boolean -> a -> a -> a




Maybe later, special syntax:

if Boolean then a else a

a;b;c...? = execute a,b, return

-------

parallel :: Array CalcThread -> [a]
seq :: Array (a->CalcThread or CalcThread)