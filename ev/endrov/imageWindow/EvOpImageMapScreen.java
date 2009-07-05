package endrov.imageWindow;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.Raster;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Map values to show on screen
 * 
 * max(min(A*contrast+brightness,255),0)
 * @author Johan Henriksson
 *
 */
public class EvOpImageMapScreen extends EvOpSlice1
	{
	private Number contrast;
	private Number brightness;
	public EvOpImageMapScreen(Number contrast, Number brightness)
		{
		this.contrast = contrast;
		this.brightness = brightness;
		}
	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0], contrast.doubleValue(), brightness.doubleValue());
		}
	

	static EvPixels apply(EvPixels a, double contrast, double brightness)
		{
		if(a.getType()==EvPixelsType.AWT)
			{
			BufferedImage src=a.getAWT();
			
			int numBits=src.getSampleModel().getSampleSize(0);
			LookupOp f;
			
			if(numBits==8)
				{
				byte[] b=new byte[256];
				for(int i=0;i<256;i++)
					b[i]=clampByte((int)(i*contrast+brightness));     //Centralize contrast* maybe?
				ByteLookupTable table=new ByteLookupTable(0,b);
				f=new LookupOp(table,null);
				}
			else if(numBits==16)
				{
				short[] b=new short[65536];
				for(int i=0;i<65536;i++)
					b[i]=clampShort((int)(i*contrast+brightness));     //Centralize contrast* maybe?
				ShortLookupTable table=new ShortLookupTable(0,b);
				f=new LookupOp(table,null);
				
				}
			else
				f=null;
			
			WritableRaster wr = Raster.createWritableRaster(src.getSampleModel(),new Point(0,0));
			BufferedImage bufo = new BufferedImage(src.getColorModel(),wr,true,new Hashtable<Object,Object>());
			f.filter(src,bufo);
			
			return new EvPixels(bufo);
			}
		else
			{
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			double[] aPixels=a.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<aPixels.length;i++)
				{
				double c=aPixels[i]*contrast+brightness;
				if(c>255) c=255;
				else if(c<0) c=0;
				outPixels[i]=c;
				}
			
			return out;
			}
		}
	
	
	private static final byte clampByte(int i)
		{
		if(i > 255)
			return -1; //really correct?
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	private static final byte clampShort(int i)
		{
		if(i > 65535)
			return -1; //really correct?
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	}