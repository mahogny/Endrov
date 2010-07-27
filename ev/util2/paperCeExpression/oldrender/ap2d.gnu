#START

set ylabel "Time [s]"
#set key top
#set border 4095   #all sides

#Which surrounding lines to show
set border 127+256+512 #backsides

#set pm3d; set palette
set pm3d map

set palette

unset xtics
set xtics ("A" 0, "P" 19)
unset ztics

unset ytics #space issue?

unset colorbox
#unset surface

set title "TITLE"

#splot "#INFILE" matrix notitle "TITLE"
splot "#INFILE" matrix notitle "TITLE" 

####TODO time is wrong