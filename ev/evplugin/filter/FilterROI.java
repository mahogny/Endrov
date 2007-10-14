package evplugin.filter;

//import java.awt.image.BufferedImage;

import evplugin.imageset.*;
import evplugin.roi.*;

/**
 * Filter that works on ROI level. This includes an "image level" since it is so easy to just cut out a ROI
 * after an entire image has been processed.
 * 
 * @author Johan Henriksson
 */
public abstract class FilterROI extends FilterMeta //make FilterRoiDefault
	{

	
	/*
	 *must be in default
	public abstract void applyImage(EvImage evim, ROI roi)
		{
		//standard implementation
		BufferedImage i=evim.getJavaImage();
		BufferedImage i2=new BufferedImage(i.getWidth(),i.getHeight(),i.getType());
		applyImage(i2);
		//Later: transfer back using ROI
		evim.setImage(i2);
		}
	*/

	public abstract void applyImage(Imageset rec, String channel, int frame, int z, ROI roi);
	public abstract void applyImage(EvImage im);
//	public abstract void applyImage(EvImage im, ROI roi);

//	public abstract void applyImage(BufferedImage im);
	
	}
