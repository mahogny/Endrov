#made this one myself

set terminal postscript "Helvetica" 8 monochrome dashed dashlength 0.2 linewidth 0.7 size 7cm,7cm


set output 'plot.ps'
set boxwidth 0.02 absolute #1.5
set style fill solid border -1
set xtic out nomirror 0.1
set ytic out nomirror 20 #500
set xrange [ 0 : 0.4 ]
set bmargin 4 #bottom margin
set key off
set border 3
set xlabel "Relative deviation of mean life time []"
set ylabel "Number of cells"
plot 'divdevhist.dat' using 2:1 with boxes

