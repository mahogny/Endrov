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
 * Measure: integral intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureVolume implements MeasurePropertyType 
	{
	private static String propertyName="volume";

	public void analyze(ProgressHandle progh, EvStack stackValue, EvStack stackMask, ParticleMeasure.Frame info)
		{
		HashMap<Integer,Integer> vol=new HashMap<Integer, Integer>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		//Find maximas
		for(int az=0;az<stackValue.getDepth();az++)
			{
			int[] arrID=stackMask.getInt(az).getPixels(progh).convertToInt(true).getArrayInt();
			
			for(int i=0;i<arrID.length;i++)
				{
				int id=arrID[i];
	
				//TODO should 0 be ignored? sounds good.
				//Can know that it should be ignored by scanning which IDs there are first, and then exclude 0.
				//costs a bit more but convenience makes it worth it. no if needed here then.
//				if(m==0)
				if(id!=0)
					{
					Integer lastVol=vol.get(id);
					if(lastVol==null)
						lastVol=0;
					vol.put(id, lastVol+1);
					}
				
				}
			
			}
		
		//Write into particles
		for(int id:vol.keySet())
			{
			ParticleMeasure.Particle p=info.getCreateParticle(id);
			p.put(propertyName, vol.get(id));
			}
		}

	public String getDesc()
		{
		return "Volume";
		}

	public Set<String> getColumns()
		{
		return Collections.singleton(propertyName);
		}

	
	
	
	}
