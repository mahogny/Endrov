package evplugin.imageWindow;

import java.awt.*;
import java.awt.image.*;

/**
 * Composite two images by putting them in different color channels
 * 
 * This code needs testing.
 * Color models are not properly handled
 * 
 * @author Johan Henriksson
 */
public class CompositeColor implements Composite, CompositeContext
	{
	int b;
	
	public CompositeColor(int b)
		{
		this.b=b;
		}
	
	public CompositeContext createContext(ColorModel cm1, ColorModel cm2, RenderingHints rh)
		{
		return this;
		}

	
	
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
		{
		int width = Math.min(src.getWidth(), dstIn.getWidth());
    int height = Math.min(src.getHeight(), dstIn.getHeight());
    
    //System.out.println("t "+src.getTransferType()+" "+DataBuffer.TYPE_BYTE+" "+DataBuffer.TYPE_INT+" "+DataBuffer.TYPE_USHORT);
    //Comes as int
    
    int[] srcPixels = new int[width];
    int[] dstPixels = new int[width];
    for (int y = 0; y < height; y++)
    	{
    	src.getDataElements(0, y, width, 1, srcPixels);
    	dstIn.getDataElements(0, y, width, 1, dstPixels);

    	if(b==0)
    		for (int x = 0; x < width; x++) 
	    		{
	  			int a = srcPixels[x];
	  			int b = dstPixels[x];
	  			int mask=0xFF<<16;
	  			dstPixels[x] = (b & ~mask) | (a & mask);
	    		}
    	else if(b==1)
    		for (int x = 0; x < width; x++) 
	    		{
	  			int a = srcPixels[x];
	  			int b = dstPixels[x];
	  			int mask=0xFF<<8;
	  			dstPixels[x] = (b & ~mask) | (a & mask);
	    		}
    	else if(b==2)
    		for (int x = 0; x < width; x++) 
	    		{
	  			int a = srcPixels[x];
	  			int b = dstPixels[x];
	  			int mask=0xFF;
	  			dstPixels[x] = (b & ~mask) | (a & mask);
	    		}
    	/*
    	else
    		for (int x = 0; x < width; x++) 
    			{
    			// pixels are stored as INT_ARGB
    			//or byte?
    			//strange order is due to ARGB, but b indexes RGB
    			int pixel = srcPixels[x];
    			srcPixel[0] = pixel & (0xFF<<16);
    			srcPixel[1] = pixel & (0xFF<<8);
    			srcPixel[2] = pixel & (0xFF);
    			srcPixel[3] = pixel & (0xFF<<24);



    			pixel = dstPixels[x];
    			dstPixel[0] = pixel & (0xFF<<16);
    			dstPixel[1] = pixel & (0xFF<<8);
    			dstPixel[2] = pixel & (0xFF);
    			dstPixel[3] = pixel & (0xFF<<24);

    			dstPixel[b]=srcPixel[b];

    			dstPixels[x] = dstPixel[3] | dstPixel[0] | dstPixel[1] |	dstPixel[2] ;
    			}
*/
    	dstOut.setDataElements(0, y, width, 1, dstPixels);
    	}
		}

	public void dispose()
		{
		}

	}
