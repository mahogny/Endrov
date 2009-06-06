package endrov.unsortedImageFilters.imageMath;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * A + b
 * @author Johan Henriksson
 *
 */
public class ImageAddScalarOp extends SliceOp
	{
	private Number b;
	public ImageAddScalarOp(Number b)
		{
		this.b = b;
		}
	public EvPixels exec(EvPixels... p)
		{
		return ImageAddScalarOp.plus(p[0], b);
		}
	/**
	 * TODO other types
	 */
	static EvPixels plus(EvPixels a, Number b)
		{
		return plus(a,b.intValue());
		}
	static EvPixels plus(EvPixels a, int b)
	{
	//Should use the common higher type here
	a=a.convertTo(EvPixels.TYPE_INT, true);
	
	int w=a.getWidth();
	int h=a.getHeight();
	EvPixels out=new EvPixels(a.getType(),w,h);
	int[] aPixels=a.getArrayInt();
	int[] outPixels=out.getArrayInt();
	
	for(int i=0;i<aPixels.length;i++)
		outPixels[i]=aPixels[i]+b;
	
	return out;
	}
	}