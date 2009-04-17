package endrov.unsortedImageFilters;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.Vector3i;

/**
 * Automatic tracking of nuclei from spots
 * 
 * @author Johan Henriksson
 *
 */
public class AutoLineage
	{
	public static void run()
		{
		
		
		
		
		
		//For all image planes
		
		
		
		
		}
	
	public static void run(NucLineage lin, Imageset imageset, String channelName, EvDecimal frame)
		{
		
		EvStack origStack=imageset.getChannel(channelName).imageLoader.get(frame);
		

		//Classify pixels
		EvStack newStack=new EvStack();
		for(Map.Entry<EvDecimal, EvImage> e:origStack.entrySet())
			{
			EvPixels in=e.getValue().getPixels();
			EvPixels average=MiscFilter.movingAverage(in, 30, 30);
			EvImage evim=e.getValue().makeShadowCopy();
			
			
			EvPixels c2=ImageMath.minus(in, average);
			
			EvPixels spotpixels=CompareImage.greater(c2, 2);
			
			/*
			EvPixels binmask=new EvPixels(EvPixels.TYPE_INT,3,3);
			double[] binmaskp=binmask.getArrayDouble();
			binmaskp[1]=binmaskp[0+3]=binmaskp[1+3]=binmaskp[2+3]=binmaskp[1+3*2]=1;
			*/
			
			EvPixels out=CompareImage.greater(MiscFilter.movingSum(spotpixels, 2, 2), 15);
			
			
			
			evim.setPixelsReference(out);
			newStack.put(e.getKey(), evim);
			}
		
		
		//Cluster
		List<EvPixels> pixels=new LinkedList<EvPixels>();
		for(Map.Entry<EvDecimal, EvImage> e:newStack.entrySet())
			pixels.add(e.getValue().getPixels());
		List<Set<Vector3i>> clusters=SpotCluster.exec3d(pixels).getPartitions();

		//Find maximum size
		int maxVolume=0;
		for(Set<Vector3i> s:clusters)
			{
			int v=s.size();
			if(v>maxVolume)
				maxVolume=v;
			}
		
		//Sort by size
		Collections.sort(clusters,new Comparator<Set<Vector3i>>(){
			public int compare(Set<Vector3i> o1, Set<Vector3i> o2)
				{
				return Double.compare(o1.size(), o2.size());
				}
			});
		
		
		//Extract candidates from clusters
		for(Set<Vector3i> c:clusters)
			{
			
			}
		
		
		//TODO convert to lineage, from vector3i. use resolution information
		
		//public static Partitioning<Vector3i> exec3d(List<EvPixels> in)
		
		
		
		}
	}
