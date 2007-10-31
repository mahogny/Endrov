package evplugin.imageWindow;

import java.awt.*;
import java.awt.image.*;

public class CompositeColor implements Composite, CompositeContext
	{
	int b;
	
	public CompositeColor(int b)
		{
		this.b=b;
		}
	
	public CompositeContext createContext(ColorModel cm1, ColorModel cm2, RenderingHints rh)
		{
		//TODO: do something about color models
	//	System.out.println(" cm"+cm1.getNumColorComponents()+ " "+cm2.getNumColorComponents());
		return this;
		}

	
	
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
		{
		int width = Math.min(src.getWidth(), dstIn.getWidth());
    int height = Math.min(src.getHeight(), dstIn.getHeight());
		
    
    //System.out.println("t "+src.getTransferType()+" "+DataBuffer.TYPE_BYTE+" "+DataBuffer.TYPE_INT+" "+DataBuffer.TYPE_USHORT);
    //Comes as int
    
    int[] srcPixel = new int[4];
    int[] dstPixel = new int[4];
    int[] srcPixels = new int[width];
    int[] dstPixels = new int[width];

    for (int y = 0; y < height; y++)
    	{
    	src.getDataElements(0, y, width, 1, srcPixels);
    	dstIn.getDataElements(0, y, width, 1, dstPixels);
    	for (int x = 0; x < width; x++) 
    		{
    		// pixels are stored as INT_ARGB

    		//or byte?
    		
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
    	
    	dstOut.setDataElements(0, y, width, 1, dstPixels);
    	}
		}
	
	
	/*
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
		{
		int width = Math.min(src.getWidth(), dstIn.getWidth());
    int height = Math.min(src.getHeight(), dstIn.getHeight());
    
  	int wh=width*height;
    int[] srcPixels = new int[wh];
    int[] dstPixels = new int[wh];

  	src.getDataElements(0, 0, width, height, srcPixels);
  	dstIn.getDataElements(0, 0, width, height, dstPixels);

  	
    for(int i=0;i<wh;i++)
    	{
    	int spixel=srcPixels[i];
    	int dpixel=dstPixels[i];
    	int opixel=(spixel&0xFFFFFFFF)|
    	           (dpixel&0x000000FF);
    	dstPixels[i]=opixel;
    	}

    //might be possible to skip
  	dstOut.setDataElements(0, 0, width, height, dstPixels);
		}
	*/

	/*
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
		{
		int width = Math.min(src.getWidth(), dstIn.getWidth());
    int height = Math.min(src.getHeight(), dstIn.getHeight());
    
  	for(int y=0;y<height;y++)
  		{
      final int[] srcPixels = new int[width];
      final int[] dstPixels = new int[width];
    	src.getDataElements(0, y, width, 1, srcPixels);
    	dstIn.getDataElements(0, y, width, 1, dstPixels);
	    for(int x=0;x<width;x++)
	    	{
	    	int spixel=srcPixels[x];
	    	int dpixel=dstPixels[x];
	    	int opixel=(spixel & 0x000000FF) |
	    	           (dpixel & 0xFFFFFF00);
	    	dstPixels[x]=opixel;
	    	}
	
	    //might be possible to skip
	  	dstOut.setDataElements(0, y, width, 1, dstPixels);
  		}
		}
	
	*/
	
	
	/*
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
		{
		int width = Math.min(src.getWidth(), dstIn.getWidth());
    int height = Math.min(src.getHeight(), dstIn.getHeight());
		
    
    int[] srcPixel = new int[4];
    int[] dstPixel = new int[4];
    int[] srcPixels = new int[width];
    int[] dstPixels = new int[width];

    for (int y = 0; y < height; y++)
    	{
    	src.getDataElements(0, y, width, 1, srcPixels);
    	dstIn.getDataElements(0, y, width, 1, dstPixels);
    	for (int x = 0; x < width; x++) 
    		{
    		// pixels are stored as INT_ARGB

    		//or byte?
    		
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

//    		dstPixel[0]=srcPixel[0];

    		dstPixels[x] = dstPixel[3] | srcPixel[0] | dstPixel[1] |	dstPixel[2] ;
    		}
    	
    	dstOut.setDataElements(0, y, width, 1, dstPixels);
    	}
		}
	*/
	public void dispose()
		{
		}

	
	
	}
