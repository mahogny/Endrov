package endrov.unsortedImageFilters.imageMath;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * A - b
 * @author Johan Henriksson
 *
 */
public class ImageSubScalarOp extends SliceOp
	{
	private Number b;
	public ImageSubScalarOp(Number b)
		{
		this.b = b;
		}
	public EvPixels exec(EvPixels... p)
		{
		return ImageSubScalarOp.minus(p[0], b);
		}
	static EvPixels minus(EvPixels a, Number bval)
	{
	//Should use the common higher type here
	a=a.convertTo(EvPixels.TYPE_INT, true);
	
	int b=bval.intValue();
	
	int w=a.getWidth();
	int h=a.getHeight();
	EvPixels out=new EvPixels(a.getType(),w,h);
	int[] aPixels=a.getArrayInt();
	int[] outPixels=out.getArrayInt();
	
	for(int i=0;i<aPixels.length;i++)
		outPixels[i]=aPixels[i]-b;
	
	return out;
	}
	static EvPixels minus(Number aVal, EvPixels b)
	{
	//Should use the common higher type here
	b=b.convertTo(EvPixels.TYPE_INT, true);
	
	int a=aVal.intValue();
	
	int w=b.getWidth();
	int h=b.getHeight();
	EvPixels out=new EvPixels(b.getType(),w,h);
	int[] bPixels=b.getArrayInt();
	int[] outPixels=out.getArrayInt();
	
	for(int i=0;i<bPixels.length;i++)
		outPixels[i]=a-bPixels[i];
	
	return out;
	}
	}