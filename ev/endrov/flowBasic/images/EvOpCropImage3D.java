/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.images;

import javax.vecmath.Vector3d;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.roi.primitive.BoxROI;
import endrov.util.MemoizeX;
import endrov.util.ProgressHandle;


/**
 * Crop image to fit within limits
 * 
 * TODO a resampling is equivalent to a crop, but more general and does not suffer from rotation problems!!!!
 * 
 * 
 * TODO BUG
 * in evopstack, output is pre-generated to be of same dimension in z. this causes huge problems!
 * 
 * 
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
	
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return apply(p[0]);
		}
	
	public EvStack apply(EvStack p)
		{
		return crop(p,roi);
		}
	
	
	
	/**
	 * Crop an entire stack to lie within ROI
	 */
	public static EvStack crop(EvStack stack, BoxROI roi)
		{
		int fromX=0;
		int fromY=0;
		int fromZ=0;
		int toX=stack.getWidth();
		int toY=stack.getHeight();
		int toZ=stack.getDepth();
	
		if(!roi.regionX.all)
			{
			int rXstart=(int)stack.transformWorldImageX(roi.regionX.start.doubleValue());
			int rXend=(int)stack.transformWorldImageX(roi.regionX.end.doubleValue());
			if(fromX<rXstart) fromX=rXstart;
			if(toX>rXend) toX=rXend;
			}
		if(!roi.regionY.all)
			{
			int rStart=(int)stack.transformWorldImageY(roi.regionY.start.doubleValue());
			int rEnd=(int)stack.transformWorldImageY(roi.regionY.end.doubleValue());
			if(fromY<rStart)	fromY=rStart;
			if(toY>rEnd) toY=rEnd;
			}
		if(!roi.regionZ.all)
			{
			int rStart=(int)stack.transformWorldImageZ(roi.regionZ.start.doubleValue());
			int rEnd=(int)stack.transformWorldImageZ(roi.regionZ.end.doubleValue());
			if(fromZ<rStart)	fromZ=rStart;
			if(toZ>rEnd) toZ=rEnd;
			}

		return crop(stack, fromX,toX,  fromY,toY, fromZ,toZ);
		}
	
	/**
	 * Crop an entire stack. Takes pixels fromX <= x < toX etc. Limits must be within bounds.
	 */
	public static EvStack crop(final EvStack stack,
			final int fromX, final int toX, final int fromY, final int toY,	final int fromZ, final int toZ)
		{
		EvStack stackOut=new EvStack();

		//Add offset etc
		stackOut.getMetaFrom(stack); 
		//Remove all loaders, since the number of slices can be different
		stackOut.clearStack();
		
		Vector3d disp=stack.getDisplacement();
		Vector3d res=stack.getRes();
		disp.add(new Vector3d(
				fromX*res.x,
				fromY*res.y,   //TODO this is wrong. will not work if stack is rotated!
				fromZ*res.z
				));
		stackOut.setDisplacement(disp);
		
		//Crop images
		for(int az=fromZ;az<toZ;az++)
			{
			EvImage newim=new EvImage();
			final int inZ=az;
			newim.io=new EvIOImage()
				{
				protected EvPixels eval(ProgressHandle ph)
					{
					return new EvOpCropImage2D(fromX, toX, fromY, toY).exec1(ph, stack.getInt(inZ).getPixels(ph));
					}
				};
			newim.registerLazyOp(newim.io); //TODO
			int outZindex=az-fromZ;
			stackOut.putInt(outZindex, newim);
			}
		
		System.out.println("Depth "+stackOut.getDepth());
		
		return stackOut;
		}
	
	
	
	
	
	}
