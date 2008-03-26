package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

import evplugin.ev.EV;
import evplugin.ev.Log;
import evplugin.ev.StdoutLog;


/**
 * 
 * @author tbudev3
 */
public class Train
	{


	/**
	 * @author tbudev3
	 */
	public static class TImage
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
		
		}

	
	
	
	
	
	
	private static List<TImage> images=new LinkedList<TImage>();
	
	

	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		File trueDir=new File("/Volumes/TBU_xeon01_500GB02/userdata/henriksson/current/traintrack/nucdic/true/");
		File falseDir=new File("/Volumes/TBU_xeon01_500GB02/userdata/henriksson/current/traintrack/nucdic/false/");
	
		int numread=3000;
		
		System.out.println("reading images");
		readIm(trueDir, numread, 1);
		readIm(falseDir, numread, -1);
		System.out.println("reading images done");
		

		//TImage tim=images.get(0);
		
	/*
		for(int i=0;i<1000;i++)
			{
			int x1=(int)(Math.random()*tim.w);
			int x2=(int)(Math.random()*tim.w);
			int y1=(int)(Math.random()*tim.h);
			int y2=(int)(Math.random()*tim.h);
			
			tim.getSum(x1, y1, x2, y2);
			
			}
		System.out.println("sum done");
		*/
		
		//Initialize weights
		for(TImage im:images)
			im.weightD=1.0/images.size();
		
		
		for(int t=0;t<1;t++)
			{
			System.out.println("iteration "+t);
			
			for(int s=5;s<15;s++)
				{
				System.out.println("S "+s);
			int numCorrect=0;
			int falseneg=0;
			for(TImage tim:images)
				{
				if(evalImage(tim,s))
					{
					numCorrect++;
					}
				else
					if(tim.valueY>0)
						falseneg++;
				}
			
				double pCorrect=(numCorrect/(double)images.size());
				double pFalseNeg=(falseneg/(double)images.size());
				System.out.println("correct "+pCorrect+" falseneg "+pFalseNeg+" "+pCorrect/pFalseNeg);
			
				}
			
			
			
			
			//Update weights, and normalize
			double Dsum=0;
			for(TImage im:images)
				{
				double htxi=0;
				im.weightD*=Math.exp(-im.valueY*htxi);
				Dsum+=im.weightD;
				}
			for(TImage im:images)
				im.weightD/=Dsum;
			}
		
		
		
		
		
		}		
		
	
	public static boolean evalImage(TImage tim, int s)
		{
		int a=tim.getSum(10-1, 10-1, 30-1, 30-1);
		int b=tim.getSum(0, 0, 40-1, 40-1);
//		int c=(b*20*20-a*40*40);
		double c=(double)(b-a)/(40*40-2*s*2*s)-(double)a/(2*s*2*s);
		
//		System.out.println(""+tim.valueY+" "+a+" "+b+"    "+c);
		
		boolean d=c>0;
		
		double th=0;
		
		if(d && tim.valueY>th)
			return true;
		else if(!d && tim.valueY<th)
			return true;
		else
			return false;
		}
	

	//should intensity be rescaled as well?
	
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
	
	/**
	 * Make all images the same size
	 */
	public static BufferedImage rescale(BufferedImage im)
		{
		int nw=40, nh=40;
		BufferedImage subim=new BufferedImage(nw,nh, im.getType());
		subim.getGraphics().drawImage(im, 0, 0, nw, nh, 0, 0, im.getWidth(), im.getHeight(), null);
		return subim;
		}
	
	
	/**
	 * Read images from directory
	 */
	public static void readIm(File dir, int cnt, double valueY)
		{
		try
			{
			for(File f:dir.listFiles())
				if(f.getName().endsWith("png"))
					{
					TImage tim=new TImage();
					tim.valueY=valueY;
					images.add(tim);
					BufferedImage im=rescale(findVariation(ImageIO.read(f)));
					tim.createCumIm(im);
					/*
					ImageIO.write(im,"png",
							new File("/Volumes/TBU_xeon01_500GB02/userdata/henriksson/current/nucdic/foo"+f.getName()));
					*/
					cnt--;
					if(cnt==0)
						return;
					}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		}
	
	
	
	
	
	}
