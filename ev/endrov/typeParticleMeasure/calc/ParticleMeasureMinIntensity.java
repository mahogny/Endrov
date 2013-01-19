/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.calc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.typeImageset.EvStack;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.util.ProgressHandle;

/**
 * Measure: minimum intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureMinIntensity implements MeasurePropertyType 
	{
	private static String propertyName="minI";
	
	
	public void analyze(ProgressHandle progh, EvStack stackValue, EvStack stackMask, ParticleMeasure.Frame info)
		{
		HashMap<Integer,Double> min=new HashMap<Integer, Double>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		for(int az=0;az<stackValue.getDepth();az++)
			{
			double[] arrValue=stackValue.getPlane(az).getPixels(progh).convertToDouble(true).getArrayDouble();
			int[] arrID=stackMask.getPlane(az).getPixels(progh).convertToInt(true).getArrayInt();
			
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
			ParticleMeasure.Particle p=info.getCreateParticle(id);
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
