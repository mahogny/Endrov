package endrov.flowProjection;

import java.util.Map;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
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

	
	/**
	 * Function to combine 2 image planes
	 */
	protected abstract EvPixels combine(EvPixels a, EvPixels b);
	
	/**
	 * Project into a single-image stack
	 */
	public EvStack project(EvStack in)
		{
		EvStack out=new EvStack();
		out.getMetaFrom(in);

		EvImage proto=in.firstEntry().snd();
		EvPixels ptot=new EvPixels(EvPixelsType.INT,proto.getPixels().getWidth(),proto.getPixels().getHeight());
		for(EvImage plane:in.getImages())
			{
			//System.out.println("plane type "+plane.getValue().getPixels().getTypeString());
			ptot=combine(ptot,plane.getPixels());
			//System.out.println(">>>  "+plane.getValue().getPixels().asciiPart(120,50,80));
			//System.out.println("ptot "+ptot.asciiPart(100,20,40));
			}
		//Should not include 0-image
		
		
		EvImage imout=new EvImage();
		imout.setPixelsReference(ptot);
		//out.put(EvDecimal.ZERO,imout);
		
		int numZ=in.getDepth();
		for(int cz=0;cz<numZ;cz++)
			out.putInt(cz,imout.makeShadowCopy());

//		for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
//			out.put(plane.getKey(), imout); //incorrect TODO
//			out.put(plane.getKey(), imout.makeShadowCopy());
			
		return out;
		}
	}
