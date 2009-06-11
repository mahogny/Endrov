package endrov.unsortedImageFilters.projectionZ;

import java.util.Map;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;

/**
 * Projections along Z that can combine 2 images at a time
 * 
 * TODO with little work, could also project other axis (but slowly)
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvOpProjectZ extends EvOpStack1
	{
	
	
	
	@Override
	public EvStack exec1(EvStack... p)
		{
		return project(p[0]);
		}

	
	protected abstract EvPixels combine(EvPixels a, EvPixels b);
	
	
	public EvStack project(EvStack in)
		{
		EvImage proto=in.firstEntry().snd();
		
		EvStack out=new EvStack();


		EvPixels ptot=new EvPixels(EvPixels.TYPE_INT,proto.getPixels().getWidth(),proto.getPixels().getHeight());
		for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
			ptot=combine(ptot,plane.getValue().getPixels());
			//ImageMath.plus(ptot, plane.getValue().getPixels());

		EvImage imout=new EvImage();
		out.getMetaFrom(in);
		imout.setPixelsReference(ptot);
		
		//Lazy stack op will use all planes!
		
		for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
			out.put(plane.getKey(), imout.makeShadowCopy());
			
		return out;
		}
	}
