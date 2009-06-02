package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;

/**
 * Operations to work with cumulative sums
 * 
 * @author Johan Henriksson
 *
 */
public class CumSumLine
	{
	/**
	 * Cumulative sum image, in effect, the 1D integral on each line
	 * 
	 * The integral can boost many algorithms since sum f(x) from a to b = F(b)-F(a).
	 * For convenience, the output is +1 larger in both directions. left and above are 0-filled.
	 * 
	 * Note the difference between an area and line cumsum. An area cumsum can have better complexity
	 * but a worse time constant.
	 * 
	 * 
	 * Complexity O(w*h)
	 * 
	 */
	public static EvPixels cumsum(EvPixels in)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixels.TYPE_INT,w+1,h+1); //Must be able to fit. Need not be original type.
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
		
		return out;
		}
	
	
	/**
	 * Equivalent to
	 *		for(int ax=x1;ax<x2;ax++)
	 *			sum+=...;
	 *
	 */
	public static int integralLineFromCumSum(EvPixels in, int x1, int x2, int y)
		{
		int[] inPixels=in.getArrayInt();
		int p11=in.getPixelIndex(x1, y);
		int p12=in.getPixelIndex(x2, y);
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
