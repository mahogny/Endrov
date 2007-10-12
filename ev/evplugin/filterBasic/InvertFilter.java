package evplugin.filterBasic;

import java.awt.image.*;
import evplugin.filter.*;
import evplugin.imageset.*;
import evplugin.roi.*;

/**
 * Filter: invert image, c'=255-c
 * @author Johan Henriksson
 */
public class InvertFilter extends FilterROI
	{
	public static void initPlugin() {}
	static
		{
		FilterMeta.addFilter(new FilterInfo()
			{
			public String getCategory(){return "Transform";}
			public String getName(){return "Invert";}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new InvertFilter();}
			});
		}
	
//highest level. should we have a standard implementation here?	
	public void applyImage(EvImage evim, ROI roi)
		{
		//standard implementation
		BufferedImage i=evim.getJavaImage();
		BufferedImage i2=new BufferedImage(i.getWidth(),i.getHeight(),i.getType());
		applyImage(i2);
		//Later: transfer back using ROI
		evim.setImage(i2);
		}

//on entire image. could have a standard implementation of this one too.	
	public void applyImage(EvImage evim)
		{
		
		BufferedImage i=evim.getJavaImage();
		evim.setImage(i);

		applyImage(i);
		
		
		/////for standard implementation
		//copy original
		//call above with ROI covering everything
		//mix original and filtered
		//return
		}

	
	public void applyImage(BufferedImage i)
		{
		WritableRaster r=i.getRaster();

		int width=r.getWidth();
		int[] pix=new int[width];
		for(int ah=0;ah<r.getHeight();ah++)
			{
			r.getSamples(0, ah, width, 1, 0, pix);
			for(int aw=0;aw<width;aw++)
				pix[aw]=255-pix[aw];
			r.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	
	
	public void applyLine(EvImage im, LineIterator it)
		{
		
		}
	
	
	}
