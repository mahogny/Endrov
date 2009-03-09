set terminal postscript portrait "Helvetica" 8 monochrome dashed dashlength 0.2 linewidth 0.7 size 7cm,7cm
set output 'plot.ps'

unset xtics 
unset ytics 
unset border

set xrange [-1.05:1.05]
set yrange [-1.05:1.05]

set key center

#set grid polar
#set xtics 1.05
#set ytics 1

plot 'ne.txt' using 1:2 w points lc 'b' ps 0.7 title 'E', \
 'nems.txt' using 1:2 w points lc 'b' ps 0.5 title 'EMS', \
 'nvenc.txt' using 1:2 w points lc 'b' pt 4 ps 0.5 title 'Ventral Enc.'
