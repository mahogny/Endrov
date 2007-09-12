AceTree Import
======================================================================


Coordinates:
============

./
nuclei/
t###-nuclei
starting from 001. 

x, x, x, .... (also comma in the end)

11 columns:
==============
* line# in file, starting from 1
* 1 if name assigned otherwise 0? fails. some named nuc have both 0 and 1 in multiple frames

* line# in last frame for this nuc, or line of parent in last frame. if missing in some frame in between, -1 (!)
* line# in next frame for this nuc, or line of one of the childs

* -1 normally, otherwise line# of other child
**** x,y,z,r in plane/res coordinates
* Name of nucleus or nill if unknown
* ???? (score metric? not the same for all frames for some nuc)



Parameters:
===========

./
parameters/
081505_L1-parameters

time_interval ###
=frametime/60

xy_res ###
z_res ###
[um] per pixel? looks that way


Loading strategy: 
=================
because of (!), best to take name in all frames and connect.
ignore line# reference other than for connecting parents (look at last line#)