/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import java.util.List;

import endrov.flow.FlowType;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;

//These operations can be made faster using RLE images
/**
 * Binary morphology
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public abstract class MorphKernel
	{
	public static final FlowType FLOWTYPE=new FlowType(MorphKernel.class);
	

	/**
	 * Prune skeleton: Remove endpoints numTimes or until converged.
	 */
	/*
	public static EvPixels pruneSkeleton(Integer numTimes)
		{
		
		}*/
	
	//also, see analyze skeleton plugin. somehow get stats out
	
	//matlab handles borders differenly. matlab keeps more pixels with open . close
	
	/**
	 * Fillhole: Fill all minimas not connected to the image border.
	 * Soille and gratin, 1994 - fast algorithm  
	 */
	
	/*
	 * http://www.fmwconcepts.com/imagemagick/morphology/index.php
	 * imagemagick operations left to code: majority, edgein,edgeout, feather, average, spread,bottomhat,  
	*/

	
	
	/**
	 * Get a list of all the points (relative to center)
	 */
	public abstract List<Vector2i> getKernelPos();


	public abstract MorphKernel reflect();
	
	/**
	 * in (+) kernel. 
	 * <br/>
	 * Kernel has a specified center kcx,kcy. Outside image assumed empty. 
	 */
	public abstract EvPixels dilate(EvPixels in);



	/**
	 * in (-) kernel.
	 * <br/>
	 * Kernel has a specified center kcx,kcy. Outside image assumed empty. 
	 */
	public abstract EvPixels erode(EvPixels in);

	/**
	 * Open: dilate, then erode
	 */
	public abstract EvPixels open(EvPixels in);


	/**
	 * Close: Erode, then dilate
	 */
	public abstract EvPixels close(EvPixels in);

	/**
	 * White Tophat: WTH(image)=image - open(image)
	 * <br/>
	 * Also called Tophat
	 */
	public abstract EvPixels whitetophat(EvPixels in);


	/**
	 * Black Tophat: BTH(image)=close(image) - image
	 * <br/>
	 * Also called Bottomhat
	 */
	public abstract EvPixels blacktophat(EvPixels in);

	
	/**
	 * Internal gradient: image-erode(image)
	 */
	public abstract EvPixels internalGradient(EvPixels in);

	/**
	 * External gradient: dilate(image)-image
	 */
	public abstract EvPixels externalGradient(EvPixels in);

	/**
	 * Whole gradient: dilate(image)-erode(image)
	 */
	public abstract EvPixels wholeGradient(EvPixels in);


	
	/**
	 * Hit and miss transform. Automatically detects if it should be constrained or unconstrained.
	 * Foreground and background are by definition disjoint
	 */
	public abstract EvPixels hitmiss(EvPixels in);
	
	/**
	 * Binary hit and miss transform
	 */
	protected EvPixels hitmissBinary(MorphKernel kBG, EvPixels in)
		{
		MorphKernel kFG=this;
		
		EvPixels bgE=kBG.erode(EvOpMorphComplementBinary.apply(in));
		EvPixels fgE=kFG.erode(in);

		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,in.getWidth(),in.getHeight());
		double[] arrBGe=bgE.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		double[] arrFGe=fgE.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		double[] arrOut=out.getArrayDouble();
		
		//Eq 5.2
		for(int i=0;i<arrBGe.length;i++)
			arrOut[i]=Math.min(arrFGe[i],arrBGe[i]); //Multiplication would also work
		
		return out;
		}
	
	/**
	 * General unconstrained hit or miss.
	 * Unconstrained := origin is not included in fg or bg
	 */
	protected EvPixels hitmissUHMT(MorphKernel kBG, EvPixels in)
		{
		MorphKernel kFG=this;
		
		EvPixels bgD=kBG.dilate(in);
		EvPixels fgE=kFG.erode(in);

		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,in.getWidth(),in.getHeight());
		double[] arrBGd=bgD.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		double[] arrFGe=fgE.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		double[] arrOut=out.getArrayDouble();
		
		for(int i=0;i<arrBGd.length;i++)
			{
			//Eq 5.4
			double v=arrFGe[i]-arrBGd[i];
			if(v>0)
				arrOut[i]=v;
			else
				arrOut[i]=0;
			}
		
		return out;
		}
	
	/**
	 * General unconstrained hit or miss.
	 * Constrained := origin is included in fg or bg
	 */
	protected EvPixels hitmissCHMT(MorphKernel kBG, EvPixels in)
		{
		in=in.convertToDouble(true);
		MorphKernel kFG=this;
		
		EvPixels bgE=kBG.erode(in);
		EvPixels bgD=kBG.dilate(in);
		EvPixels fgE=kFG.erode(in);
		EvPixels fgD=kFG.dilate(in);

		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,in.getWidth(),in.getHeight());
		double[] arrBGe=bgE.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		double[] arrBGd=bgD.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		double[] arrFGe=fgE.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		double[] arrFGd=fgD.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		double[] arrOut=out.getArrayDouble();
		double[] arrIn=in.getArrayDouble();
		
		for(int i=0;i<arrBGd.length;i++)
			{
			//Eq 5.5
			double f=arrIn[i];
			double v1=f-arrBGd[i];
			double v2=arrBGe[i]-f;
			double ret;
			if(f==arrFGe[i] && v1>0)
				ret=v1;
			else if(f==arrFGd[i] && v2>0)
				ret=v2;
			else
				ret=0;
			arrOut[i]=ret;
			}
		
		return out;
		}
	
	
	}

