/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Measure: surface area (pixels open in any of 6 directions)
 * @author Johan Henriksson
 *
 */
public class ParticleMeasurePerimeter implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="perimeter";

	
	
	public void analyze(ProgressHandle progh, EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		//TODO should thickness be taken into account? world or pixel coordinates?
		
		
		HashMap<Integer,Integer> perimeter=new HashMap<Integer, Integer>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash
		
		int[][] arrIDs=stackMask.getReadOnlyArraysInt(progh);

		//int d=arrIDs.length;
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
								
								//Check neighbours
								arrID[ay*w+ax-1]!=id ||
								arrID[ay*w+ax+1]!=id ||
								arrID[(ay-1)*w+ax]!=id ||
								arrID[(ay+1)*w+ax]!=id)
							{
							Integer lastSurf=perimeter.get(id);
							if(lastSurf==null)
								lastSurf=0;
							perimeter.put(id, lastSurf+1);
							}
						}
					}
			
			}
		
		//Write into particles
		for(int id:perimeter.keySet())
			{
			HashMap<String, Object> p=info.getCreateParticle(id);
			double v=perimeter.get(id);
			p.put(propertyName, v);
			}
		}

	public String getDesc()
		{
		return "Perimeter (border pixels in fact, and sums them over all slices if more than one is selected for a particle)";
		}

	public Set<String> getColumns()
		{
		HashSet<String> set=new HashSet<String>();
		set.add(propertyName);
		return set;
		}

	
	
	
	}
