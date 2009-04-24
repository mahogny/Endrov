package endrov.filter;

import java.awt.image.*;

import endrov.imageset.*;
import endrov.roi.*;
import endrov.util.EvDecimal;

/**
 * Filter that works on a slice-by-slice basis. Wrapped so it also can work on ROI-basis.
 * 
 * @author Johan Henriksson
 */
public abstract class FilterSlice extends FilterROI
	{
	/**
	 * Apply filter to image given ROI and where this image is located
	 */
	public void applyImage(EvStack stack, EvImage evim, ROI roi, String channel, EvDecimal frame, EvDecimal z)
		{
		LineIterator it=roi.getLineIterator(stack, evim, channel, frame, z);
		applyImage(stack, evim, it);
		}

	
	/**
	 * Apply filter to image given iterator
	 */
	public void applyImage(EvStack stack, EvImage evim, LineIterator it)
		{
		BufferedImage i=evim.getJavaImage();
		BufferedImage i2=new BufferedImage(i.getWidth(),i.getHeight(),i.getType());
		applyImage(i,i2);
		WritableRaster rin=i2.getRaster();
		WritableRaster rout=i.getRaster();
		while(it.next())
			{
			for(LineIterator.LineRange lr:it.ranges)
				{
				int w=lr.end-lr.start;
				int[] pix=new int[w];
				rin.getSamples(lr.start, it.y, w, 1, 0, pix);
				rout.setSamples(lr.start, it.y, w, 1, 0, pix);
//				System.out.println("z "+it.startX+" "+it.endX+" "+it.y+" "+w);
				}
			}
		evim.setImage(i); //was i
		}
	
	
	
	/**
	 * Apply filter on entire image
	 */
	public void applyImage(EvStack stack, EvImage evim)
		{
		BufferedImage i=evim.getJavaImage();
		evim.setImage(i);
		applyImage(i,i);
		}


	/**
	 * Applies filter to an entire image
	 */
	protected abstract void applyImage(BufferedImage in, BufferedImage out);
	}
