package endrov.flowBasic.images;

import java.util.*;

import endrov.flow.EvOpStack1;
import endrov.flowBasic.math.EvOpImageMulScalar;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.roi.primitive.BoxROI;
import endrov.util.Vector3i;


/**
 * Crop image to fit within 
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpCropImage extends EvOpStack1
	{
	private BoxROI roi;
	
	public EvOpCropImage(BoxROI roi)
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
	/*
	public static EvStack crop(EvStack stack, int fromX, int toX, int fromY, int toY, int fromZ, int toZ)
		{
		EvStack stackOut=new EvStack();
		
		stackOut.getMetaFrom(stack);
		
		
		//TODO now need to change offset in the cut stack
		

		
		
		
		
		
		stack.
		
		
		}
	
	*/
	
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
