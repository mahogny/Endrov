package endrov.typeParticleMeasure.calc;

import java.util.HashMap;

public class MeasureProperty
	{

	/**
	 * Property types
	 */
	public static final HashMap<String, MeasurePropertyType> measures=new HashMap<String, MeasurePropertyType>();

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
