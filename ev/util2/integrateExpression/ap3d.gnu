#START

set ylabel "Time [s]"
#set key top
#set border 4095   #all sides

#Which surrounding lines to show
set border 127+256+512 #backsides

#set pm3d; set palette
set hidden3d

set palette

unset xtics
set xtics ("Anterior" 0, "Posterior" 19)
unset ztics

#Angle of view
set view 35,55,1,1

#set pm3d at s hidden3d 100
#set style line 100 lt 5 lw 0.5


unset colorbox
#unset surface

set title "TITLE"

#splot "#INFILE" matrix notitle "TITLE"
splot "#INFILE" matrix notitle "TITLE" w lines palette
#splot "#INFILE" notitle "TITLE" w lines palette

####TODO time is wrong