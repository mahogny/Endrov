#made this one myself

set terminal postscript "Helvetica" 8 monochrome dashed dashlength 0.2 linewidth 0.7 size 7cm,7cm

set output 'traveldisthist.ps'
set boxwidth 0.9 absolute #1.5
set style fill solid border -1
set title "Travel Distances"
set xtic out nomirror 2
set ytic out nomirror 50 #500
set key off
set border 3
set xlabel "Travel distance [micrometer]"
set ylabel "Number of cells"
plot 'traveldisthist.dat' using 2:1 with boxes

# 10 bins in ju orig, 0-12
# 2 col: #, mid-of-bin
