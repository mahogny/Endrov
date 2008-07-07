#!/bin/csh
# Not all modules are necessarily included in this distribution.
# The Makefile fixes this by calling this small script which
# will create an empty file for each nonexisting module.

foreach x ($*)
 if (! -e $x) then
   touch $x
   echo zap: $x
 endif
end

