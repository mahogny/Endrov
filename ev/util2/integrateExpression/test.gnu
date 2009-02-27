set term post eps enh "Helvetica" 24
set output "es3d.eps"
#set size 0.9 ,0.8
#set size 0.7 ,1.0
set size 1.0 ,1.0
#
set parametric
#set zrange [1.0:20.0]
set nokey
set data style line

# View Angle set view x_rot, y_rot, z_scale, 
#set view 60,30,1,0.3
#set view 0,0,1,1
#set view 45,60,1,0.7
set view 45,60,1,1

# Surface
#set nosurface
set surface

# Contours
#set contour both
#set contour base
#set cntrparam bspline
#set cntrparam levels 5
#set cntrparam levels 20
#set cntrparam levels auto 0
#set grid
set hidden3d

set xlabel "AP"
set ylabel "Time [s]" #-1,-0.5
#set xrange [2:17]
#set yrange [2:17]
# Projected to 0+
set cntrparam levels discrete -87., -85.0, -82.5, -80.0, -77.5, -75.0, -72.5, -70.0, -67.5
set zrange [-90:-65]
set title 'title'
splot "es.dat" using 2:3:5 w lines 1
