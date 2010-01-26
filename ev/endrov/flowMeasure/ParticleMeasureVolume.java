package endrov.flowMeasure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import endrov.imageset.EvStack;

/**
 * Measure: integral intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureVolume implements ParticleMeasure.MeasurePropertyType 
	{
	private static String propertyName="volume";

	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		HashMap<Integer,Integer> vol=new HashMap<Integer, Integer>();
		//TODO: a special map for this case could speed up plenty.
		//also: only accept integer IDs? this would speed up hashing and indexing.
		//can be made even faster as a non-hash

		//Find maximas
		for(int az=0;az<stackValue.getDepth();az++)
			{
			int[] arrID=stackMask.getInt(az).getPixels().convertToInt(true).getArrayInt();
			
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
			HashMap<String, Object> p=info.getCreate(id);
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
