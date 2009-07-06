package endrov.flowBasic;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Operations to work with cumulative sums
 * 
 * @author Johan Henriksson
 *
 */
public class CumSumArea
	{
	
	public EvPixels result; 

	private CumSumArea(){};

	/**
	 * Cumulative sum image, in effect, the 2D integral.
	 * <br/>
	 * The integral can boost many algorithms since sum f(x) from a to b = F(b)-F(a).
	 * For convenience, the output is +1 larger in both directions. left and above are 0-filled.
	 * <br/>
	 * Complexity O(w*h)
	 * 
	 */
	public CumSumArea(EvPixels in)
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
					int outbefore=outnext-(w+1);//out.getPixelIndex(ax+1, ay);
					outPixels[outnext]=sum+outPixels[outbefore];
					curin++;
					}
				}
			
			//return out;
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
				int sum=0;
				for(int ax=0;ax<w;ax++)
					{
					//int curin=in.getPixelIndex(ax, ay);
					
					sum+=inPixels[curin];
					
					int outnext=out.getPixelIndex(ax+1, ay+1);
					int outbefore=outnext-(w+1);//out.getPixelIndex(ax+1, ay);
					outPixels[outnext]=sum+outPixels[outbefore];
					curin++;
					}
				}
			
			//return out;
			result=out;
			}
		}
	
	
	/**
	 * Special optimized version of cumsum. We have the property Var(x)=E(x^2)-(E(x))^2. Since it is used so much
	 * to find local variance, this function exist and returns cumsum(x^2)
	 * <br/>
	 * Complexity O(w*h)
	 * 
	 */
	public static CumSumArea cumsum2(EvPixels in)
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
				sum+=inPixels[curin]*inPixels[curin];
				
				int outnext=out.getPixelIndex(ax+1, ay+1);
				int outbefore=outnext-(w+1);//out.getPixelIndex(ax+1, ay);
				outPixels[outnext]=sum+outPixels[outbefore];
				curin++;
				}
			}
		
		CumSumArea c=new CumSumArea();
		c.result=out;
//		return out;
		return c;
		}
	
	
	
	
	
	/**
	 * Equivalent to
	 *	for(int ay=y1;ay<y2;ay++)
	 *		for(int ax=x1;ax<x2;ax++)
	 *			sum+=...;
	 *
	 * CHECK THIS
	 * y1<=y2
	 *
	 */
	private static int integralFromCumSumInteger(EvPixels in, int x1, int x2, int y1, int y2)
		{
		if(in.getType()==EvPixelsType.DOUBLE)
			return (int)integralFromCumSumDouble(in, x1, x2, y1, y2);
		int[] inPixels=in.getArrayInt();
		int p11=in.getPixelIndex(x1, y1);
		int p12=in.getPixelIndex(x2, y1);
		int p21=in.getPixelIndex(x1, y2);
		int p22=in.getPixelIndex(x2, y2);
		return inPixels[p22]+inPixels[p11]-(inPixels[p12]+inPixels[p21]);
		}
	
	public int integralFromCumSumInteger(int x1, int x2, int y1, int y2)
		{
		return integralFromCumSumInteger(result, x1, x2, y1, y2);
		}
	
	
	/**
	 * Equivalent to
	 *	for(int ay=y1;ay<y2;ay++)
	 *		for(int ax=x1;ax<x2;ax++)
	 *			sum+=...;
	 *
	 * CHECK THIS
	 * y1<=y2
	 *
	 */
	private static double integralFromCumSumDouble(EvPixels in, int x1, int x2, int y1, int y2)
		{
		if(in.getType()==EvPixelsType.INT)
			return integralFromCumSumInteger(in, x1, x2, y1, y2); //TODO this is a mess
		double[] inPixels=in.getArrayDouble();
		int p11=in.getPixelIndex(x1, y1);
		int p12=in.getPixelIndex(x2, y1);
		int p21=in.getPixelIndex(x1, y2);
		int p22=in.getPixelIndex(x2, y2);
		return inPixels[p22]+inPixels[p11]-(inPixels[p12]+inPixels[p21]);
		}

	public double integralFromCumSumDouble(int x1, int x2, int y1, int y2)
		{
		return integralFromCumSumDouble(result, x1, x2, y1, y2);
		}
	
	/**
	 * Equivalent to
	 *		for(int ax=x1;ax<x2;ax++)
	 *			sum+=...;
	 *
	 */
	/*
	public static int integralLineFromCumSum(EvPixels in, int x1, int x2, int y)
		{
		int[] inPixels=in.getArrayInt();
		int p11=in.getPixelIndex(x1, y);
		int p12=in.getPixelIndex(x2, y);
		int p21=in.getPixelIndex(x1, y+1);
		int p22=in.getPixelIndex(x2, y+1);
		return inPixels[p22]+inPixels[p11]-(inPixels[p12]+inPixels[p21]);
		}
	*/
	
	
	}
