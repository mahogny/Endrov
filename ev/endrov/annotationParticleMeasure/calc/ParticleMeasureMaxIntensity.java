/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.annotationParticleMeasure.calc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.annotationParticleMeasure.ParticleMeasure;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Measure: maximum intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureMaxIntensity implements MeasurePropertyType 
	{
	private static String propertyName="maxI";
	
	
	public void analyze(ProgressHandle progh, EvStack stackValue, EvStack stackMask, ParticleMeasure.Frame info)
		{
		HashMap<Integer,Double> max=new HashMap<Integer, Double>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		//Find maximas
		for(int az=0;az<stackValue.getDepth();az++)
			{
			double[] arrValue=stackValue.getInt(az).getPixels(progh).convertToDouble(true).getArrayDouble();
			int[] arrID=stackMask.getInt(az).getPixels(progh).convertToInt(true).getArrayInt();
			
			for(int i=0;i<arrValue.length;i++)
				{
				double v=arrValue[i];
				int id=arrID[i];
	
				if(id!=0)
					{
					Double curmax=max.get(id);
					if(curmax==null || curmax<v)
						max.put(id, v);
					}
				
				}
			
			
			}
		
		//Write into particles
		for(int id:max.keySet())
			{
			ParticleMeasure.Particle p=info.getCreateParticle(id);
			p.put(propertyName, max.get(id));
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
