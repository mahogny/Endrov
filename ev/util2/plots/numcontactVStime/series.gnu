set terminal postscript portrait "Helvetica" 8 monochrome dashed dashlength 0.2 linewidth 0.7 size 7cm,7cm

set output 'series.ps'
#set boxwidth 1.5 absolute
set style fill solid border -1
set key off
set border 3
set xtic out nomirror 50
set ytic out nomirror 2
set xlabel "Developmental time [min]"
set ylabel "Number of contacts per cell"
set yrange [ 0.00000 : 14] noreverse nowriteback
set xrange [ 0.000000 : 300 ] noreverse nowriteback
set bmargin 4 #bottom margin
plot 'series.dat' using 1:2 with lines
# with boxes



#set style histogram clustered gap 1 title  offset character 0, 0, 0
#set datafile missing '-'
#set style data histograms
#set style histogram 
#set style boxes
#set title "Cell Contacts"
#set xtic out nomirror 5
#set ytic out nomirror 500
