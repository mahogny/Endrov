package endrov.flowProjection;

import endrov.flowBasic.math.EvOpImageMaxImage;
import endrov.imageset.EvPixels;

/**
 * Projection: Max Z
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpProjectMaxZ extends EvOpProjectZ
	{
	@Override
	protected EvPixels combine(EvPixels a, EvPixels b)
		{
		return new EvOpImageMaxImage().exec1(a,b);
		}
	}
