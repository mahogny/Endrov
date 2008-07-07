#!/bin/csh
#
F90 -c -g geompack3_prb.f90 >& compiler.txt
if ( $status != 0 ) then
  echo "Errors occurred while compiling geompack3_prb.f90"
  exit
endif
rm compiler.txt
#
F90 geompack3_prb.o -L$HOME/lib/$ARCH -lgeompack3
if ( $status != 0 ) then
  echo "Errors occurred while linking and loading geompack3_prb.o"
  exit
endif
rm geompack3_prb.o
#
mv a.out geompack3_prb
./geompack3_prb > geompack3_prb_output.txt
if ( $status != 0 ) then
  echo "An error occurred while executing geompack3_prb."
  exit
endif
rm geompack3_prb
#
echo "Program output written to geompack3_prb_output.txt"
