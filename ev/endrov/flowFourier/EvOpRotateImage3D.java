package endrov.flowFourier;

import java.util.Map;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;

/**
 * Rotate image
 * 
 * Complexity O(w*h)
 */
public class EvOpRotateImage3D extends EvOpStack1
	{
	Number px, py, pz;
	
	public EvOpRotateImage3D(Number px, Number py, Number pz)
		{
		this.px = px;
		this.py = py;
		this.pz = pz;
		}

	public EvStack exec1(EvStack... p)
		{
		return apply(p[0],intValue(px), intValue(py), intValue(pz));
		}
	
	private static Integer intValue(Number n)
		{
		if(n!=null)
			return n.intValue();
		else
			return null;
		}
	
	/**
	 * Rotate image. If rotation is null, then rotate half-way
	 */
	public static EvStack apply(EvStack in, Integer px, Integer py, Integer pz)
		{		
		int d=in.getDepth();

		EvStack out=new EvStack();
		out.getMetaFrom(in);

		int thepz;
		if(pz==null)
			thepz=d/2;
		else
			thepz=pz;

		int az=0;
		for(Map.Entry<EvDecimal, EvImage> e:in.entrySet())
			{
			int to=(az+thepz)%d;
			EvImage rot2d=new EvImage(EvOpRotateImage2D.apply(e.getValue().getPixels(), px, py));
			out.putInt(to, rot2d);
			az++;
			}
		return out;
		}
	}