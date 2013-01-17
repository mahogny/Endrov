



more general system for drawing!

* platewindow contains all the logic for setting up plates
imagewindow would work the same way

* Platewindowview contains how images should be mapped to 2d.
it handles offset, scale and rotation. it should also set up how to draw overlays.
it sets up datasources. it takes user input





* generalimageview contains a general way of drawing 2D images.
this view should be really stupid!!!
at some point it should take over

panning should be implemented at this level! (already is). but dx,dy things should be calculated on a higher level, since
those are anyway needed for tools.


============

how much does it consume to load all plates?
say, 20*20 * 1000*1000*2 = 800 MB 

this excludes the double-counting when converted to bufferedimage. 

scaling down,
20*20 * 100*100*2 = 8 MB
can even have int precision without problem.
co

should store original size, and new size. thus possible to rescale at need. 

use imglib2 to scale down. time to implement the EvImage container! then make ops!
http://pacific.mpi-cbg.de/wiki/index.php/Downsample

should have a thread for loading images. there should be a list of "wanted" images(?).
this list should include wanted resolution.
can have a callback to ask for wanted images. hm. this system should be able to check the status of current images. if too zoomed in, and want an image, then enqueue another round.


===========================================


strategy for ParticleMeasure:

should this become a more general object? like, Table?
only difference from a Table is that it has lazy functionality





