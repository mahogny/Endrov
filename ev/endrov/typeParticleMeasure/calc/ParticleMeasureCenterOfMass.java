/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.calc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Vector3d;

import endrov.typeImageset.EvStack;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.util.ProgressHandle;

/**
 * Measure: center of mass
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureCenterOfMass implements MeasurePropertyType 
	{
	private static String propertyName="com";

	public void analyze(ProgressHandle progh, EvStack stackValue, EvStack stackMask, ParticleMeasure.Frame info)
		{
		//TODO should thickness be taken into account? world or pixel coordinates?
		
		
		HashMap<Integer,Vector3d> sum=new HashMap<Integer, Vector3d>();
		HashMap<Integer,Double> vol=new HashMap<Integer, Double>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		for(int az=0;az<stackValue.getDepth();az++)
			{
			double[] arrValue=stackValue.getPlane(az).getPixels(progh).convertToDouble(true).getArrayDouble();
			int[] arrID=stackMask.getPlane(az).getPixels(progh).convertToInt(true).getArrayInt();
			
			int w=stackValue.getWidth();
			int h=stackValue.getHeight();

			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					int index=ay*w+ax;

					double v=arrValue[index];
					int id=arrID[index];
		
					if(id!=0)
						{
						Vector3d lastSum=sum.get(id);
						if(lastSum==null)
							sum.put(id,lastSum=new Vector3d());
						lastSum.add(new Vector3d(ax*v,ay*v,az*v));
						
						
						Double lastVol=vol.get(id);
						if(lastVol==null)
							lastVol=0.0;
						vol.put(id, lastVol+v);
						}

					
					}
			
			}
		
		//Write into particles
		for(int id:sum.keySet())
			{
			ParticleMeasure.ColumnSet p=info.getCreateParticle(id);
			Vector3d s=sum.get(id);
			double v=vol.get(id);
			p.put(propertyName+"X", s.x/v);
			p.put(propertyName+"Y", s.y/v);
			p.put(propertyName+"Z", s.z/v);
			}
		}

	public String getDesc()
		{
		return "Center of mass (takes intensity into account)";
		}

	public Set<String> getColumns()
		{
		HashSet<String> set=new HashSet<String>();
		set.add(propertyName+"X");
		set.add(propertyName+"Y");
		set.add(propertyName+"Z");
		return set;
		}

	
	
	
	}
