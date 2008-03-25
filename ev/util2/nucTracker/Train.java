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

	public static BufferedImage findCrap(BufferedImage im)
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
//		for(int y=1;y<im.getHeight()-1;y++)
//			for(int x=1;x<im.getWidth()-1;x++)
		for(int y=0;y<im.getHeight();y++)
			for(int x=0;x<im.getWidth();x++)
				{/*
				int p=
				a[y][x+1]+a[y][x-1]+
				a[y+1][x]+a[y-1][x]+
				a[y+1][x+1]+a[y-1][x-1]+
				a[y+1][x-1]-a[y-1][x+1]-8*a[y][x];
				*/
				int p=Math.abs(a[y][x]-avg);
				
				pix[0]=p;
				sim.setPixel(x, y, pix);
				}
		
		
		return subim;
		}
	
	
	
	/**
	 * @author tbudev3
	 */
	public static class TImage
		{
		
		int w, h;
		public boolean isTrue;
		
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
			for(int x=1;x<w;x++)
				{
				r.getPixel(x, 0, pix);
				cumim[0][x]=pix[0]+cumim[0][x-1];
				}
			for(int y=1;y<h;y++)
				{
				r.getPixel(0, y, pix);
				cumim[y][0]=pix[0]+cumim[y-1][0];
				}
			for(int y=1;y<h;y++)
				{
				r.getPixel(0, 0, pix);
				int sum=pix[0];
				for(int x=1;x<w;x++)
					{
					r.getPixel(x, 0, pix);
					cumim[y][x]=pix[0]+cumim[y-1][x]+cumim[0][x-1];
					}
				}
*/
			
		
			for(int y=0;y<h;y++)
				{
				for(int x=0;x<w;x++)
					System.out.print(" "+cumim[y][x]);
				System.out.println();
				}
			System.out.println();
			
			}

		//x2>x1, y2>y1
		public int getSum(int x1, int y1, int x2, int y2)
			{
			return cumim[y2][x2]+cumim[y1][x1]-(cumim[y1][x2]+cumim[y2][x1]);
			}
		
		}

	
	
	
	
	private static List<TImage> images=new LinkedList<TImage>();
	
	
	public static void readIm(File dir, int cnt, boolean isTrue)
		{
		
		
		try
			{
			for(File f:dir.listFiles())
				if(f.getName().endsWith("png"))
					{
					TImage tim=new TImage();
					tim.isTrue=isTrue;
					images.add(tim);
	
					tim.createCumIm(findCrap(ImageIO.read(f)));
					
//					ImageIO.write(findCrap(ImageIO.read(f)),"png",
//							new File("/Volumes/TBU_xeon01_500GB02/userdata/henriksson/current/nucdic/foo"+f.getName()));
					
					cnt--;
					if(cnt==0)
						return;
					}
			}
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		}
	
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		File trueDir=new File("/Volumes/TBU_xeon01_500GB02/userdata/henriksson/current/nucdic/true/");
		File falseDir=new File("/Volumes/TBU_xeon01_500GB02/userdata/henriksson/current/nucdic/false/");
	
		System.out.println("reading images");
		readIm(trueDir, 10, true);
//		readIm(falseDir, 10, false);
		System.out.println("reading images done");
		

		TImage tim=images.get(0);
		
	
		for(int i=0;i<1000;i++)
			{
			int x1=(int)(Math.random()*tim.w);
			int x2=(int)(Math.random()*tim.w);
			int y1=(int)(Math.random()*tim.h);
			int y2=(int)(Math.random()*tim.h);
			
			tim.getSum(x1, y1, x2, y2);
			
			}
		System.out.println("sum done");
		
		

		}		
		
	
	}
