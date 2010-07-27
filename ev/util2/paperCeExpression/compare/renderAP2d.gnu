#START
set title "TITLE"
unset colorbox
set border 127+256+512

set xr [0:19]
set yr [0:100]


#If we want to name x-axis
unset xtics
set xtics ("Anterior" 0, "Posterior" 19)
#unset ztics


set pm3d map
set palette

unset ytics #space issue?


set ylabel "Time [s]"
#splot "#FILE" notitle w lines palette
splot "#INFILE" notitle palette