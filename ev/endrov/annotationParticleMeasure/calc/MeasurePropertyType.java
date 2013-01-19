package endrov.annotationParticleMeasure.calc;

import java.util.Set;

import endrov.annotationParticleMeasure.ParticleMeasure;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * One property to measure. Columns must be fixed. 
 * 
 */
public interface MeasurePropertyType
	{
	public String getDesc();
	
	/**
	 * Which properties, must be fixed and the same for all particles 
	 */
	public Set<String> getColumns();
	
	/**
	 * Evaluate a stack, store in info. If a particle is not in the list it must be
	 * created. All particles in the map must receive data.
	 */
	public void/*Map<Integer,Map<String, Object>>*/  analyze(ProgressHandle progh, EvStack stackValue, EvStack stackMask, ParticleMeasure.Frame info);
	}