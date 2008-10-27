set terminal postscript portrait "Helvetica" 8 monochrome dashed dashlength 0.2 linewidth 0.7 size 7cm,7cm

set output 'contacthist.ps'
set boxwidth 0.7 absolute
set style fill solid border -1
#set style histogram clustered gap 1 title  offset character 0, 0, 0
#set datafile missing '-'
#set style data histograms
#set style histogram 
#set style boxes
#set title "Cell Contacts"
set xtic out nomirror 10
set ytic out nomirror 250
set key off
set border 3
set xlabel "Duration of contact [min]"
set ylabel "Number of contacts"
#set yrange [ 0.00000 : 70] noreverse nowriteback
set bmargin 4 #bottom margin
plot 'series.dat' using 2:1 with boxes

#time of contact VS number of contacts
#column 1: contact num
#column 2: time of contact. midvalue of bar in histogram
