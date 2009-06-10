package endrov.unsortedImageFilters.projectionZ;

import endrov.flow.std.math.OpImageAddImage;
import endrov.imageset.EvPixels;

/**
 * Projection: Sum Z
 * 
 * @author Johan Henriksson
 *
 */
public class OpProjectSumZ extends OpProjectZ
	{
	@Override
	protected EvPixels combine(EvPixels a, EvPixels b)
		{
		return new OpImageAddImage().exec1(a,b);
		}


	}
