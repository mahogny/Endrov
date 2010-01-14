/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * 
 * @author tbudev3
 *
 */
public class CopyOfTImage
	{


	int w, h;
	public double valueY;
	public double weightD;
	
	public int[][] cumim;

	
	
	
	/**
	 * Create cumulative image
	 */
	public void createCumIm(BufferedImage bim)
		{
		w=bim.getWidth();
		h=bim.getHeight();
		cumim=new int[h][w];
		
		WritableRaster r=bim.getRaster();
		
		int[] pix=new int[3];
		for(int y=0;y<h;y++)
			{
			int sum=0;
			for(int x=0;x<w;x++)
				{
				r.getPixel(x, y, pix);
				sum+=pix[0];
				cumim[y][x]=sum;
				if(y>0)
					cumim[y][x]+=cumim[y-1][x];
				}
			}
		
/*		
		for(int y=0;y<h;y++)
			{
			for(int x=0;x<w;x++)
				System.out.print(" "+cumim[y][x]);
			System.out.println();
			}
		System.out.println();
	*/	
		}

	//x2>x1, y2>y1
	public int getSum(int x1, int y1, int x2, int y2)
		{
		int p11=y1>=0 && x1>=0 ? cumim[y1][x1] : 0;
		int p12=y1>=0 ? cumim[y1][x2] : 0;
		int p21=x1>=0 ? cumim[y2][x1] : 0;
		int p22=cumim[y2][x2];
		return p22+p11-(p12+p21);
		}
	
	
	

	/**
	 * Find variatiob by abs(a[y][x]-avg)
	 */
	public static BufferedImage findVariation(BufferedImage im, Integer windowSize)
		{
		BufferedImage subim=new BufferedImage(im.getWidth(), im.getHeight(), im.getType());
		
		int[] pix=new int[3];
		WritableRaster rim=im.getRaster();
		WritableRaster sim=subim.getRaster();
		
		int avg=0;
		int[][] a=new int[im.getHeight()][im.getWidth()];
		for(int y=0;y<im.getHeight();y++)
			for(int x=0;x<im.getWidth();x++)
				{
				rim.getPixel(x, y, pix);
				a[y][x]=pix[0];
				avg+=pix[0];
				}
		avg/=(im.getWidth()*im.getHeight());

		CopyOfTImage avgImage=new CopyOfTImage();
		if(windowSize!=null)
			avgImage.createCumIm(im);

		pix[0]=0;
		pix[1]=0;
		pix[2]=0;
		int w=im.getWidth();
		int h=im.getHeight();
		for(int y=0;y<h;y++)
			for(int x=0;x<w;x++)
				{
				int thisAvg=avg;
				
				if(windowSize!=null)
					{
					int minx=x-windowSize;
					int maxx=x+windowSize;
					int miny=y-windowSize;
					int maxy=y+windowSize;
		//			if(minx<-1) minx=-1;
		//			if(miny<-1) miny=-1;
					if(minx<0) minx=0;
					if(miny<0) miny=0;
					if(maxx>=w) maxx=w-1;
					if(maxy>=h) maxy=h-1;
					thisAvg=avgImage.getSum(minx-1, miny-1, maxx, maxy);
					thisAvg/=(maxx-minx+1)*(maxy-miny+1);
					}
				pix[0]=Math.abs(a[y][x]-thisAvg);
				sim.setPixel(x, y, pix);
				}
		return subim;
		}
	
	
	
	}
