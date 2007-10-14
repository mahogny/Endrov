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
//	public void applyImage(EvImage evim, ROI roi)
	public void applyImage(Imageset rec, String channel, int frame, int z, ROI roi)
		{
		EvImage evim=rec.getChannel(channel).getImageLoader(frame,z);
		
		
		//standard implementation
		BufferedImage i=evim.getJavaImage();
		BufferedImage i2=new BufferedImage(i.getWidth(),i.getHeight(),i.getType());
		applyImage(i,i2);
		
		LineIterator it=roi.getLineIterator(rec, channel, frame, z);
		WritableRaster rin=i2.getRaster();
		WritableRaster rout=i.getRaster();
		while(it.next())
			{
			int w=it.endX-it.startX;
			int[] pix=new int[w];
			rin.getSamples(it.startX, it.y, w, 1, 0, pix);
			rout.setSamples(it.startX, it.y, w, 1, 0, pix);
//			System.out.println("z "+it.startX+" "+it.endX+" "+it.y+" "+w);
			}
		
		//batching
		
		//Later: transfer back using ROI
		evim.setImage(i);
		}

//on entire image. could have a standard implementation of this one too.	
	public void applyImage(EvImage evim)
		{
		
		BufferedImage i=evim.getJavaImage();
		evim.setImage(i);

		applyImage(i,i);
		
		
		/////for standard implementation
		//copy original
		//call above with ROI covering everything
		//mix original and filtered
		//return
		}

	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		WritableRaster rin=in.getRaster();
		WritableRaster rout=out.getRaster();

		int width=rin.getWidth();
		int[] pix=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			rin.getSamples(0, ah, width, 1, 0, pix);
			for(int aw=0;aw<width;aw++)
				pix[aw]=255-pix[aw];
			rout.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	
	
	public void applyLine(EvImage im, LineIterator it)
		{
		
		}
	
	
	}
