package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * 
 * @author tbudev3
 *
 */
public class TImage
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
		return cumim[y2][x2]+cumim[y1][x1]-(cumim[y1][x2]+cumim[y2][x1]);
		}
	
	
	

	/**
	 * Find variatiob by abs(a[y][x]-avg)
	 */
	public static BufferedImage findVariation(BufferedImage im)
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

		pix[0]=0;
		pix[1]=0;
		pix[2]=0;
		for(int y=0;y<im.getHeight();y++)
			for(int x=0;x<im.getWidth();x++)
				{
				pix[0]=Math.abs(a[y][x]-avg);
				sim.setPixel(x, y, pix);
				}
		return subim;
		}
	
	
	
	}
