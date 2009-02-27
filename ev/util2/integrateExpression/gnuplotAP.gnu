set xlabel "AP"
set ylabel "Time [s]"
#set key top
#set border 4095   #all sides
set border 127+256+512 #backsides

set pm3d; set palette
set hidden3d

unset xtics
unset ztics

set view 35,25,1,1

#set pm3d at s hidden3d 100
#set style line 100 lt 5 lw 0.5

unset colorbox
unset surface

splot "/Volumes/TBU_main01/ost4dgood/ceh33_reco2.ost/data/AP20-GFP" matrix notitle

####TODO time is wrong