package endrov.flowMeasure;

import java.util.HashMap;

import endrov.imageset.EvStack;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class MeasureParticle
	{

	/**
	 * One property to measure
	 */
	public interface MeasurePropertyType
		{
		public String getDesc();
		
		
		
		public void analyze(EvStack stack);
		}
	
	/**
	 * Property types
	 */
	protected static HashMap<String, MeasurePropertyType> measures=new HashMap<String, MeasurePropertyType>();

	
	/**
	 * Register one property that can be measured
	 */
	public static void registerMeasure(String name, MeasurePropertyType t)
		{
		synchronized (measures)
			{
			measures.put(name, t);
			}
		}
	
	
	}
