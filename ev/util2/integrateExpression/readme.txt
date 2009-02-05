test on gene5, lineage size increases 40% with expression added. no compression of any type.









then:
* integration of entire image
**** can linearity be adjust by background? grab a few lines around the border
	this could be reused for the more advanced methods

* using start coordinate plane, overlap cube 


expressions as binary blobs. multiple files in blob. 
data.getBlob(EvPath p)=io.getBlob(EvData data, EvPath p)

eek. how to handle resave with blobs? copy recursive? eek. imagesets not compatible. 
* reading partial expression should be possible since many expressions might be analyzed at once
* writing can be more expensive

* cell-level expression, the limitation on lifetime is enough to know how to "cull" a pattern. just need quick access to (frame,exp)
* tissue level and whole level, performance problems. can divide into cells over time but this is very visible. possible to store ranges?
 	(cell, expname) -> [(range,file)]
* shell as subject to lineage? problem of visibility in imview 	

memory use, 2500 frames * ("100.25"=6byte + level=4byte) * 10 parallel cells = 250kb/pattern
100 patterns 25mb
can use cut-offs, eliminate up to 50%. better, can use adaptivity! maybe 90%+ elimination, can be selected. hm. maybe enough with as few as 100 datapoints. 100*6*10=6kb
blobs overkill right now.
* can just try a spacing, calculate interpolation error in between
* does not work well when it starts to move
* start and end point must be conserved
* adjust midpoints to conserve integral?

 	(cell, expname) -> [(range,file range)], allows a single file to be used. allows splitting over a single cell

new saveData() has to also save blob data. avoid saving unchanged data.
* can cache file, read big range when needed
* need to encapsulate write of exp data
* can for now live with having to send 250kb. special mechanism later on, or database, to select data better

integration of pattern: 
 * Code bitmap ROI (which can use blobs, 1-bit png etc)
 * Function to integrate image using ROI
 * ROI from shell
 * ROI from voronoi
 * new object for exp.pattern? sub object in lineage?
 * axis-time needs end coordinates as well. get from shell. do not use the exponential function, apply this later when we analyze 
 
exposure compensation
 * realInteg=integ*exp+C, C is unknown, varies when exp jumps
 * can be shared for all integration systems. work on NucLineage + Imageset 

summary
 * all expressions on one model, re-adjust timing
 * lossy compression

searching



how to compare different levels? superimpose them anyway? several lineage objects? or very very small tissue cells

