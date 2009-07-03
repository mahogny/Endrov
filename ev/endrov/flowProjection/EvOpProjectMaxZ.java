package endrov.flowProjection;

import endrov.flowBasic.math.EvOpMaxImageImage;
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
		return new EvOpMaxImageImage().exec1(a,b);
		}
	}
