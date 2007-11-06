package evplugin.filterBasic;

import java.awt.image.*;
import evplugin.ev.*;

public class ContrastBrightnessOp
	{
	private LookupOp f;

	/**
	 * Constructor - Set parameters right away
	 * @param contrast Contrast
	 * @param brightness Brightness
	 */
	public ContrastBrightnessOp(double contrast, double brightness)
		{
		byte[] b=new byte[256];
		for(int i=0;i<256;i++)
			b[i]=clampByte((int)(i*contrast+brightness));     //Centralize contrast* maybe?
		ByteLookupTable table=new ByteLookupTable(0,b);
	
		f=new LookupOp(table,null);
		}
	
	/**
	 * Apply filter
	 * @param src Source
	 * @param dst Destination
	 * @return Filtered image
	 */
	public BufferedImage filter(BufferedImage src, BufferedImage dst)
		{
		if(f==null || src==null || dst==null)
			Log.printError(""+(f==null)+" "+(src==null)+" "+(dst==null),null);
		return f.filter(src,dst);
		}
	
	
	private final byte clampByte(int i)
		{
		if(i > 255)
			return -1;
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	
	}
	
	

