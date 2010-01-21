package endrov.flowMeasure;

import java.util.Set;

import endrov.imageset.EvStack;

/**
 * Measure: integral intensity
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureSumIntensity3d implements ParticleMeasure.MeasurePropertyType 
	{

	public void analyze(EvStack stackValue, EvStack stackMask, ParticleMeasure.FrameInfo info)
		{
		// TODO Auto-generated method stub
		
		}

	public String getDesc()
		{
		return "Maximum intensity of any pixel";
		}

	public Set<String> getColumns()
		{
		// TODO Auto-generated method stub
		return null;
		}

	
	
	
	}
