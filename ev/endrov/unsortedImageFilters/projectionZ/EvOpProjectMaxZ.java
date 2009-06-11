package endrov.unsortedImageFilters.projectionZ;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.imageMath.EvOpMaxImageImage;

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
