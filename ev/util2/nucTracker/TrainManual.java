package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;


/**
 * 
 * @author tbudev3
 */
public class TrainManual
	{


	
	
	
	
	private static List<TImage> images=new LinkedList<TImage>();
	
	private static int standardSize=40;
	public static int meanWindowSize=60;
	//note training set not prescaled

	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		File trueDir =new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/dic/images/true/");
		File falseDir=new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/dic/images/false/");
	
		int numread=300;
		
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
		
		
		try
			{
			BufferedWriter outfile = new BufferedWriter(new FileWriter(
					"/Volumes/TBU_main03/userdata/henriksson/traintrack/dic/4.txt"));
			
			
			for(int t=0;t<1;t++)
				{
				System.out.println("iteration "+t);
			
				
				//Automatic ///////////////////////////
				
				
				
				
				
				
				
				
				
				//Manually programed //////////////////
				
//			for(int s=5;s<15;s++)
					{
					int numCorrect=0;
					int falseneg=0;
					for(TImage tim:images)
						{
						int a=tim.getSum(standardSize/4-1, standardSize/4-1, 
								(standardSize-standardSize/4)-1, (standardSize-standardSize/4)-1);
						int b=tim.getSum(0, 0, standardSize-1, standardSize-1)-a;


						outfile.write(""+a/(20.0*20.0)+" "+b/(40.0*40.0-20.0*20.0)+" "+tim.valueY+"\n");


						if(evalImageCorrect(tim,standardSize,0,0))
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
			
			
			outfile.close();
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		
		
		
		}		
		
	public static boolean evalImage(TImage tim, int wsize, int shiftX, int shiftY, double bias)
		{
		int q=wsize/4;
		int h=wsize/2;

//		System.out.println("" + q+ " "+h);
		
		int a=tim.getSum(shiftX+q-1, shiftY+q-1, 
				shiftX+(wsize-q)-1, shiftY+(wsize-q)-1);
		
		int b=tim.getSum(shiftX, shiftY, 
				shiftX+wsize-1, shiftY+wsize-1) - a;
		
		
		
		double adiv=(double)a/(double)(h*h);
		double bdiv=(double)b/(double)(wsize*wsize-h*h);
		double slant=1.25; //1.25 shown optimal
		
//		System.out.println(""+tim.valueY+" "+a+" "+b+"    "+c);
		

		boolean d=bdiv>slant*adiv+bias;
		if(d && bdiv>5)
			System.out.println(" "+slant*adiv+" vs "+bdiv);
		return d;
		
		
		
		
		
		}
	
	
	public static boolean evalImageCorrect(TImage tim, int wsize, int shiftX, int shiftY)
		{
		boolean d=evalImage(tim, wsize, shiftX, shiftY,0);
		
		if(d && tim.valueY>0)
			return true;
		else if(!d && tim.valueY<0)
			return true;
		else
			return false;
		}
	

	//should intensity be rescaled as well?
	
	
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
					
					BufferedImage im=ImageIO.read(f);
					
					im=rescale(TImage.findVariation(im,meanWindowSize));
					tim.createCumIm(im);
					
					
					
					ImageIO.write(im,"png",
							new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/dic/images/transformed/"+valueY+"_"+f.getName()));
					
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
	
	

	/**
	 * Make all images the same size
	 */
	public static BufferedImage rescale(BufferedImage im)
		{
		int nw=standardSize, nh=standardSize;
		BufferedImage subim=new BufferedImage(nw,nh, im.getType());
		subim.getGraphics().drawImage(im, 0, 0, nw, nh, 0, 0, im.getWidth(), im.getHeight(), null);
		return subim;
		}
	
	
	}
