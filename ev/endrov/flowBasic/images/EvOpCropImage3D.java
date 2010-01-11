package endrov.flowBasic.images;

import java.util.*;

import endrov.flow.EvOpStack1;
import endrov.flowBasic.math.EvOpImageMulScalar;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.roi.primitive.BoxROI;
import endrov.util.Memoize;
import endrov.util.Vector3i;


/**
 * Crop image to fit within limits
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpCropImage3D extends EvOpStack1
	{
	private BoxROI roi;
	
	public EvOpCropImage3D(BoxROI roi)
		{
		this.roi=roi;
		}
	
	public EvStack exec1(EvStack... p)
		{
		return apply(p[0]);
		}
	
	public EvStack apply(EvStack p)
		{
		return null;//crop(p,roi);
		}
	
	
	
	/**
	 * Crop an entire stack to lie within ROI
	 */
	/*
	public static EvStack crop(EvStack stack, BoxROI roi)
		{
		EvStack pout=new EvStack();
		
		
		
		
		}
	*/
	
	/**
	 * Crop an entire stack. Takes pixels fromX <= x < toX etc. Limits must be within bounds.
	 */
	public static EvStack crop(final EvStack stack,
			final int fromX, final int toX, final int fromY, final int toY,	final int fromZ, final int toZ)
		{
		EvStack stackOut=new EvStack();

		//Add offset
		stackOut.getMetaFrom(stack);
		stackOut.dispX+=fromX;
		stackOut.dispY+=fromY;
		stackOut.dispZ=stackOut.dispZ.add(stackOut.resZ.multiply(fromZ));
		
		//Crop images
		for(int az=fromZ;az<toZ;az++)
			{
			EvImage newim=new EvImage();
			final int inZ=az; 
			final Memoize<EvPixels> m=new Memoize<EvPixels>(){
				protected EvPixels eval()
					{
					return new EvOpCropImage2D(fromX, toX, fromY, toY).exec1(stack.getInt(inZ).getPixels());
					}
				};
			newim.io=new EvIOImage(){public EvPixels loadJavaImage(){return m.get();}};
			newim.registerLazyOp(m);
			stackOut.putInt(az-fromZ, newim);
			}
		
		return stackOut;
		}
	
	
	
	/**
	 * Crop one single 2D plane. Takes pixels fromX <= x < toX.
	 * Area must be within bounds
	 */
	public static EvPixels crop(EvPixels p, int fromX, int toX, int fromY, int toY)
		{
		p=p.convertToDouble(true);
		int width=p.getWidth();
		
		int newWidth=toX-fromX;
		int newHeight=toY-fromY;
		EvPixels newPixels=new EvPixels(EvPixelsType.DOUBLE, newWidth, newHeight);
		
		double[] inarr=p.getArrayDouble();
		double[] outarr=newPixels.getArrayDouble();
		for(int ay=0;ay<newWidth;ay++)
			for(int ax=0;ax<newHeight;ax++)
				outarr[ay*newWidth+ax]=inarr[(ay+fromY)*width+(ax+fromY)];
		
		return newPixels;
		}
	

	
	
	}
