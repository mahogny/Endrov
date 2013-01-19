/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic;

import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;

/**
 * Operations to work with cumulative sums
 * 
 * @author Johan Henriksson
 *
 */
public class CumSumLine
	{
	
	public EvPixels result; 
	
	
	/**
	 * Cumulative sum image, in effect, the 1D integral on each line
	 * <br/>
	 * The integral can boost many algorithms since sum f(x) from a to b = F(b)-F(a).
	 * For convenience, the output is +1 larger in both directions. left and above are 0-filled.
	 * <br/>
	 * Note the difference between an area and line cumsum. An area cumsum can have better complexity
	 * but a worse time constant.
	 * <br/>
	 * Complexity O(w*h)
	 * 
	 */
	public CumSumLine(EvPixels in)
		{
		if(in.getType()==EvPixelsType.INT)
			{
			in=in.getReadOnly(EvPixelsType.INT);
			int w=in.getWidth();
			int h=in.getHeight();
			EvPixels out=new EvPixels(EvPixelsType.INT,w+1,h+1); //Must be able to fit. Need not be original type.
			int[] inPixels=in.getArrayInt();
			int[] outPixels=out.getArrayInt();
			
			int curin=0;
			for(int ay=0;ay<h;ay++)
				{
				int sum=0;
				for(int ax=0;ax<w;ax++)
					{
					//int curin=in.getPixelIndex(ax, ay);
					
					sum+=inPixels[curin];
					
					int outnext=out.getPixelIndex(ax+1, ay+1);
					outPixels[outnext]=sum;
					curin++;
					}
				}
			
			result=out;
			}
		else
			{
			in=in.getReadOnly(EvPixelsType.DOUBLE);
			int w=in.getWidth();
			int h=in.getHeight();
			EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w+1,h+1); //Must be able to fit. Need not be original type.
			double[] inPixels=in.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			int curin=0;
			for(int ay=0;ay<h;ay++)
				{
				double sum=0;
				for(int ax=0;ax<w;ax++)
					{
					//int curin=in.getPixelIndex(ax, ay);
					
					sum+=inPixels[curin];
					
					int outnext=out.getPixelIndex(ax+1, ay+1);
					outPixels[outnext]=sum;
					curin++;
					}
				}
			
			result=out;
			}
		}
	
	
	/**
	 * Equivalent to
	 *		for(int ax=x1;ax<x2;ax++)
	 *			sum+=...;
	 *
	 */
	public int integralLineFromCumSumInt(int x1, int x2, int y)
		{
		int[] inPixels=result.getArrayInt();
		int p11=result.getPixelIndex(x1, y);
		int p12=result.getPixelIndex(x2, y);
		return inPixels[p12]-inPixels[p11];
		}
	
	
	/**
	 * Equivalent to
	 *		for(int ax=x1;ax<x2;ax++)
	 *			sum+=...;
	 *
	 */
	public double integralLineFromCumSumDouble(int x1, int x2, int y)
		{
		double[] inPixels=result.getArrayDouble();
		int p11=result.getPixelIndex(x1, y);
		int p12=result.getPixelIndex(x2, y);
		return inPixels[p12]-inPixels[p11];
		}

	
	/**
	 * Create cumulative image
	 */
	/*
	public void createCumIm(BufferedImage bim)
		{
		w=bim.getWidth();
		h=bim.getHeight();
		cumim=new int[h+1][w+1];
		
		WritableRaster r=bim.getRaster();
		
		int[] pix=new int[3];
		for(int x=0;x<w+1;x++)
			cumim[0][x]=0;
		
		for(int y=0;y<h;y++)
			{
			int sum=0;
			for(int x=0;x<w;x++)
				{
				r.getPixel(x, y, pix);
				sum+=pix[0];
				cumim[y+1][x+1]=sum;
				cumim[y+1][x+1]+=cumim[y][x+1];
				}
			}

		}

	//x2>x1, y2>y1
	public int getSum(int x1, int y1, int x2, int y2)
		{
		int p11=cumim[y1][x1];
		int p12=cumim[y1][x2];
		int p21=cumim[y2][x1];
		int p22=cumim[y2][x2];
		return p22+p11-(p12+p21);
		}
	*/
	}
