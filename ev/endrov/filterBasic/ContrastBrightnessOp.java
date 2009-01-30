package endrov.filterBasic;

import java.awt.image.*;

import endrov.ev.*;

public class ContrastBrightnessOp
	{

	private double contrast, brightness;
	
	/**
	 * Constructor - Set parameters right away
	 * @param contrast Contrast
	 * @param brightness Brightness
	 */
	public ContrastBrightnessOp(double contrast, double brightness)
		{
		this.contrast=contrast;
		this.brightness=brightness;
		
		}
	
	/**
	 * Apply filter
	 * @param src Source
	 * @param dst Destination
	 * @return Filtered image
	 */
	public BufferedImage filter(BufferedImage src, BufferedImage dst)
		{
		LookupOp f;
		
		int numBits=src.getSampleModel().getSampleSize(0);

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
			
		
		
		if(f==null || src==null || dst==null)
			{
			Log.printError(""+(f==null)+" "+(src==null)+" "+(dst==null),null);
			return dst;
			}
		else
			return f.filter(src,dst);
		}
	
	
	private final byte clampByte(int i)
		{
		if(i > 255)
			return -1; //really correct?
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	private final byte clampShort(int i)
		{
		if(i > 65535)
			return -1; //really correct?
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	}
	
	

