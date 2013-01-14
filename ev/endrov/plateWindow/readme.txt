



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



