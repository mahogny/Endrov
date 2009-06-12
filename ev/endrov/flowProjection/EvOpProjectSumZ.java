package endrov.flowProjection;

import endrov.flow.std.math.EvOpImageAddImage;
import endrov.imageset.EvPixels;

/**
 * Projection: Sum Z
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpProjectSumZ extends EvOpProjectZ
	{
	@Override
	protected EvPixels combine(EvPixels a, EvPixels b)
		{
		return new EvOpImageAddImage().exec1(a,b);
		}


	}
