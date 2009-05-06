set terminal postscript portrait "Helvetica" 8 monochrome dashed dashlength 0.2 linewidth 0.7 size 7cm,7cm

set output 'traveldisthist.ps'
set boxwidth 0.9 absolute #1.5
set style fill solid border -1
set xtic out nomirror 2
set ytic out nomirror 20 #500
set bmargin 4 #bottom margin
#set key off
set border 3
set xlabel "Travel distance [micrometer]"
set ylabel "Number of cells"

set xrange [ -0.5 : 14] noreverse nowriteback
set yrange [ 0 : 80] noreverse nowriteback


set style line 1 linewidth 0.3

set style data histogram
set style histogram cluster gap 1
set style fill solid border -1
set boxwidth 0.9
plot 'traveldisthist.dat' using 1 ls 1 title 'MS-ME', '' using 2 fs pattern 0 ls 1 title 'MS-DS'
#plot 'traveldisthist.dat' using 1 ls 1 title 'Straight', '' using 2 fs pattern 2 ls 1 title '+child'
#:xticlabels(3)


#plot 'traveldisthist.dat' using 2:1 with boxes



# 10 bins in ju orig, 0-12
# 2 col: #, mid-of-bin
