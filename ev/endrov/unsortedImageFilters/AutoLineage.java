package endrov.unsortedImageFilters;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
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
		
		TreeMap<EvDecimal,EvImage> slices=imageset.getChannel(channelName).imageLoader.get(frame);
		
		
		List<EvPixels> pixels=new LinkedList<EvPixels>();
		
		for(Map.Entry<EvDecimal, EvImage> e:slices.entrySet())
			pixels.add(e.getValue().getPixels());
		
		
		
		
		
		
		Partitioning<Vector3i> clustersi=SpotCluster.exec3d(pixels);
		
		//TODO convert to lineage, from vector3i. use resolution information
		
		//public static Partitioning<Vector3i> exec3d(List<EvPixels> in)
		
		
		
		}
	}
