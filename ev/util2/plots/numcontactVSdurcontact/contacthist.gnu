#set terminal postscript portrait "Helvetica" 8 monochrome dashed dashlength 0.2 linewidth 0.7 size 7cm,7cm
set terminal postscript portrait "Helvetica" 8 color linewidth 0.4 size 7cm,7cm
#set style fill pattern border
#set style fill pattern 

#set style fill solid border
set style fill solid border -1

set output 'contacthist.ps'
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
plot 'series.dat' using 1:xtic(3) title 'Complete lifetime', '' using 2 title 'Partial lifetime'

#time of contact VS number of contacts
#column 1: contact num based on start of contact
#column 2: contact num based on end of contact
#column 3: time of contact. midvalue of bar in histogram
 