#makedat.m is not used for this. m-file with CCM.

set terminal postscript portrait "Helvetica" 8 monochrome dashed dashlength 0.1 linewidth 0.7 size 7cm,7cm
#set terminal postscript portrait "Helvetica" 8 monochrome linewidth 0.4 size 7cm,7cm
#set style fill pattern border
#set style fill pattern 

set style fill solid border
###set style fill solid border -1

set output 'contacthistNEW.ps'
set boxwidth 0.7 absolute
set style histogram rowstacked
set style data histogram
set xtic out nomirror 
set ytic out nomirror 100
#set key off
set border 3
set xlabel "Duration of contact [min]"
set ylabel "Total number of contacts"
set bmargin 4 #bottom margin
plot '/home/tbudev3/javaproj/ev/util2/cellContactMap/newdurhist.dat' using 1:xtic(2) w histograms fs empty notitle

 
