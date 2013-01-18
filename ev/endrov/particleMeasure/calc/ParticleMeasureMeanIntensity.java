/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.particleMeasure.calc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.imageset.EvStack;
import endrov.particleMeasure.ParticleMeasure;
import endrov.util.ProgressHandle;

/**
 * Measure: mean intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureMeanIntensity implements MeasurePropertyType 
	{
	private static String propertyName="meanI";

	public void analyze(ProgressHandle progh, EvStack stackValue, EvStack stackMask, ParticleMeasure.Frame info)
		{
		HashMap<Integer,Double> sum=new HashMap<Integer, Double>();
		HashMap<Integer,Double> vol=new HashMap<Integer, Double>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		for(int az=0;az<stackValue.getDepth();az++)
			{
			double[] arrValue=stackValue.getInt(az).getPixels(progh).convertToDouble(true).getArrayDouble();
			int[] arrID=stackMask.getInt(az).getPixels(progh).convertToInt(true).getArrayInt();
			
			for(int i=0;i<arrValue.length;i++)
				{
				double v=arrValue[i];
				int id=arrID[i];
	
				//TODO should 0 be ignored? sounds good.
				//Can know that it should be ignored by scanning which IDs there are first, and then exclude 0.
				//costs a bit more but convenience makes it worth it. no if needed here then.
//				if(m==0)
				if(id!=0)
					{
					Double lastSum=sum.get(id);
					if(lastSum==null)
						lastSum=0.0;
					sum.put(id, lastSum+v);
					
					Double lastVol=vol.get(id);
					if(lastVol==null)
						lastVol=0.0;
					vol.put(id, lastVol+1);

					}
				
				}
			
			
			}
		
		//Write into particles
		for(int id:sum.keySet())
			{
			HashMap<String, Object> p=info.getCreateParticle(id);
			p.put(propertyName, sum.get(id)/vol.get(id));
			}
		}

	public String getDesc()
		{
		return "Mean intensity of any pixel";
		}

	public Set<String> getColumns()
		{
		return Collections.singleton(propertyName);
		}

	
	
	
	}
