package endrov.flowMeasure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.imageset.EvStack;
import endrov.util.EvListUtil;

/**
 * Measure: modal intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureMedianIntensity implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="medianI";
	
	
	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		HashMap<Integer,ArrayList<Double>> entryList=new HashMap<Integer, ArrayList<Double>>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		//Find entries
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
					ArrayList<Double> entries=entryList.get(id);
					if(entries==null)
						entryList.put(id, entries=new ArrayList<Double>());
					entries.add(v);
					}
				
				}
			
			
			}
		
		//Write into particles
		for(int id:entryList.keySet())
			{
			HashMap<String, Object> p=info.getCreate(id);
			ArrayList<Double> entries=entryList.get(id);
			double modal=EvListUtil.modalValue(EvListUtil.toDoubleArray(entries));
			p.put(propertyName, modal);
			}
		
		}

	public String getDesc()
		{
		return "Modal (most common) intensity of any pixel";
		}

	public Set<String> getColumns()
		{
		return Collections.singleton(propertyName);
		}

	
	
	
	}
