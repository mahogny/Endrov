#!/bin/csh
#
mkdir temp
cd temp
rm *
f90split ../geompack3.f90
#
foreach FILE (`ls -1 *.f90`)
  F90 -c -g $FILE >& compiler.txt
  if ( $status != 0 ) then
   echo "Errors while compiling " $FILE
    exit
  endif
  rm compiler.txt
end
rm *.f90
#
ar qc libgeompack3.a *.o
rm *.o
#
mv libgeompack3.a ~/lib/$ARCH
if ( $status != 0 ) then
  exit
endif
cd ..
rmdir temp
#
echo "Library installed as ~/lib/$ARCH/libgeompack3.a"
