#START
set title "TITLE"
set view 35,55,1,1
set hidden3d
unset colorbox
set border 127+256+512

set xr [0:19]
set yr [0:100]


#If we want to name x-axis
unset xtics
set xtics ("Anterior" 0, "Posterior" 19)
unset ztics



set ylabel "Time [s]"
splot "#INFILE" notitle w lines palette

