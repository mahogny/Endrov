package endrov.flowMeasure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import endrov.imageset.EvStack;

/**
 * Measure: surface area (pixels open in any of 6 directions)
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureSurfaceArea implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="surfaceArea";

	
	
	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		//TODO should thickness be taken into account? world or pixel coordinates?
		
		
		HashMap<Integer,Integer> surfaceArea=new HashMap<Integer, Integer>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash
		
		int[][] arrIDs=stackMask.getReadOnlyArraysInt();

		int d=arrIDs.length;
		for(int az=0;az<stackValue.getDepth();az++)
			{
			int[] arrID=arrIDs[az];//stackValue.getInt(az).getPixels().convertToInt(true).getArrayInt();
			
			int w=stackValue.getWidth();
			int h=stackValue.getHeight();

			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					int index=ay*w+ax;

					int id=arrID[index];
		
					if(id!=0)
						{
						if(
								//Boundary pixels are always open
								ax==0 || ax==w-1 ||
								ay==0 || ay==h-1 ||
								az==0 || az==d-1 || //Can optimize away this
								
								//Check neighbours
								arrID[ay*w+ax-1]!=id ||
								arrID[ay*w+ax+1]!=id ||
								arrID[(ay-1)*w+ax]!=id ||
								arrID[(ay+1)*w+ax]!=id ||
								arrIDs[az-1][ay*w+ax]!=id ||
								arrIDs[az+1][ay*w+ax]!=id)
							{
							Integer lastSurf=surfaceArea.get(id);
							if(lastSurf==null)
								lastSurf=0;
							surfaceArea.put(id, lastSurf+1);
							}
						}
					}
			
			}
		
		//Write into particles
		for(int id:surfaceArea.keySet())
			{
			HashMap<String, Object> p=info.getCreate(id);
			double v=surfaceArea.get(id);
			p.put(propertyName, v);
			}
		}

	public String getDesc()
		{
		return "Surface area (borders pixels, and pixels with opening in any of 6 directions)";
		}

	public Set<String> getColumns()
		{
		HashSet<String> set=new HashSet<String>();
		set.add(propertyName);
		return set;
		}

	
	
	
	}
