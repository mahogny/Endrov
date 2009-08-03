package endrov.flowMorphology;

import java.util.List;

import endrov.flow.FlowType;
import endrov.imageset.EvPixels;
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



	}

