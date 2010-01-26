://rsbweb.nih.gov/ij/plugins/track/objects.html


This plug-in counts the number of 3D objects in a stack and displays 
the volume, 
the surface, 
the centre of mass and 
the centre of intensity 


for each object. The threshold value can be adjusted using the first slider and user may navigate through the stack using a second slider. Results are presented both as a results table and as new stacks where either the full volume, the edges of each particle, the centre of mass or centre of intensity is represented. On this stack, each structure appears under a different colour. A tick box allows logging a summary of the overall measurement (useful to keep a track of successive counts). 



from analyze particles ====

area
standard deviation
min and max gray value
center of mass
bounding rectangle
circularity
mean gray value
modal gray value
centroid
perimeter
fit ellipse
ferets diameter
limit to threshold


my own suggestions ===

texture statistics
euler number
PCA


====== flow object ====

new MeasureParticle()
.enablePCA(true);
etc

flow object, lots of check boxes

separate plugins, how to combine measurements?
* joinProp (general, works on keys)
* have other measures register into the analyze


==== store objects or decimals? ====

objects -> can do some OOP.
decimals -> easier filtering.



==== next step: filtering measures ===




Object name: ParticleProperty?

more general would be key -> props,
where key can be anything e.g. (frame, id)
later also (trajectory) after identification
etc

how general should it be, when to use normal objects?










============================================

http://imagejdocu.tudor.lu/doku.php?id=plugin:analysis:3d_object_counter:start

   1.
      counts the number of 3D objects in a stack.
   2.
      quantifies for each found object the following parameters:
          *
            Integrated density;
          *
            Mean of the gray values;
          *
            Standard deviation of the gray values;
          *
            Minimum gray value;
          *
            Maximum gray value;
          *
            Median of the gray values;
          *
            Mean distance from the geometrical centre of the object to surface;
          *
            Standard deviation of the distance to surface;
          *
            Median distance to surface;
          *
            Centroid;
          *
            Centre of mass;
          *
            Bounding box.
   3.
      generates results representations such as:
          *
            Objects' map;
          *
            Surface voxels' map;
          *
            Centroids' map;
          *
            Centres of masses' map.





====================================== TODO =================================


standard deviation (intensity?)
bounding rectangle
circularity   http://en.wikipedia.org/wiki/Compactness_measure_of_a_shape   sphericity
modal gray value
perimeter
fit ellipse
ferets diameter
the surface, 
Standard deviation of the gray values;
Median of the gray values;
Mean distance from the geometrical centre of the object to surface;
Standard deviation of the distance to surface;
Median distance to surface;
Bounding box.
texture statistics
euler number
PCA
            