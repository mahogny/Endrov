package endrov.flowMeasure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.imageset.EvStack;

/**
 * Measure: maximum intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureMaxIntensity implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="maxI";
	
	
	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		HashMap<Integer,Double> max=new HashMap<Integer, Double>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		//Find maximas
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
					Double curmax=max.get(id);
					if(curmax==null || curmax<v)
						max.put(id, v);
					}
				
				}
			
			
			}
		
		//Write into particles
		for(int id:max.keySet())
			{
			HashMap<String, Object> p=info.getCreate(id);
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
