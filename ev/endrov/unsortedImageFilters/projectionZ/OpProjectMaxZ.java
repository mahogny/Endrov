package endrov.unsortedImageFilters.projectionZ;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.imageMath.OpMaxImageImage;

/**
 * Projection: Max Z
 * 
 * @author Johan Henriksson
 *
 */
public class OpProjectMaxZ extends OpProjectZ
	{
	@Override
	protected EvPixels combine(EvPixels a, EvPixels b)
		{
		return new OpMaxImageImage().exec1(a,b);
		}
	}
