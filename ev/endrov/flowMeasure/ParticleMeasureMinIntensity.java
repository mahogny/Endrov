/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.imageset.EvStack;

/**
 * Measure: minimum intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureMinIntensity implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="minI";
	
	
	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		HashMap<Integer,Double> min=new HashMap<Integer, Double>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		for(int az=0;az<stackValue.getDepth();az++)
			{
			double[] arrValue=stackValue.getInt(az).getPixels().convertToDouble(true).getArrayDouble();
			int[] arrID=stackMask.getInt(az).getPixels().convertToInt(true).getArrayInt();
			
			for(int i=0;i<arrValue.length;i++)
				{
				double v=arrValue[i];
				int id=arrID[i];
	
				if(id!=0)
					{
					Double curMin=min.get(id);
					if(curMin==null || curMin<v)
						min.put(id, v);
					}
				
				}
			
			
			}
		
		//Write into particles
		for(int id:min.keySet())
			{
			HashMap<String, Object> p=info.getCreate(id);
			p.put(propertyName, min.get(id));
			}
		
		}

	public String getDesc()
		{
		return "Maximum intensity of any pixel";
		}

	public Set<String> getColumns()
		{
		return Collections.singleton(propertyName);
		}

	
	
	
	}
