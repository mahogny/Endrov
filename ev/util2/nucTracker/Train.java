package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

	static String channelName="RFP";
	
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
		
		
		File trueDir =new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/"+channelName+"/images/true/");
		File falseDir=new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/"+channelName+"/images/false/");
	
		int numread=300;
		
		System.out.println("reading images");
		readIm(trueDir, numread, 1);
		readIm(falseDir, numread, -1);
		System.out.println("reading images done");

		AdaBoost adaboost=new AdaBoost();

		//Initialize weights
//		for(TImage im:images)
//			im.weightD=1.0/images.size();
		for(TImage tim:images)
			if(tim.valueY==-1)
				tim.weightD=4;
			else
				tim.weightD=1;
		
		try
			{
			BufferedWriter outfile = new BufferedWriter(new FileWriter(
					"/Volumes/TBU_main03/userdata/henriksson/traintrack/"+channelName+"/4.txt"));

			
			
			
			
			double lastPcorrect=0;
			for(int round=0;round<30;round++)
				{
				//Normalize
				double sumd=0;
				for(TImage tim:images)
					sumd+=tim.weightD;
				for(TImage tim:images)
					tim.weightD/=sumd;
				
				
				//Create feature
				SimpleClassifier feat=new SimpleClassifier();
				double inner=Math.random()*0.45;
				feat.r1.x1=inner;
				feat.r1.y1=inner;
				feat.r1.x2=1-inner;
				feat.r1.y2=1-inner;
				feat.r2.x1=0;
				feat.r2.y1=0;
				feat.r2.x2=1;
				feat.r2.y2=1;
				
/*				
				if(round==0)
					{
					feat.r1.x1=0.25;
					feat.r1.y1=0.25;
					feat.r1.x2=0.75;
					feat.r1.y2=0.75;
					feat.r2.x1=0;
					feat.r2.y1=0;
					feat.r2.x2=1;
					feat.r2.y2=1;
					}
				else if(round==1)
					{
					feat.r1.x1=0.4;
					feat.r1.y1=0.4;
					feat.r1.x2=0.6;
					feat.r1.y2=0.6;
					feat.r2.x1=0;
					feat.r2.y1=0;
					feat.r2.x2=1;
					feat.r2.y2=1;
					}
				else if(round==2)
					{
					feat.r1.x1=0.3;
					feat.r1.y1=0.3;
					feat.r1.x2=0.7;
					feat.r1.y2=0.7;
					feat.r2.x1=0;
					feat.r2.y1=0;
					feat.r2.x2=1;
					feat.r2.y2=1;
					}*/
				feat.optimize(images,standardSize);
				
				System.out.println("Optimal c "+feat.c+", s "+feat.s+", eps "+feat.optEps);
	
				//Add classifier
				double alphat=0.5*Math.log((1.0-feat.optEps)/feat.optEps);
				adaboost.addClassifier(alphat, feat);
				
				
				
				
				
				
				//Verify classifer
				int numCorrect=0;
				int falseneg=0;
				int falsepos=0;
				for(TImage tim:images)
					{
					if(feat.eval(tim,standardSize,0,0)==tim.valueY)
						numCorrect++;
					else
						{
						if(tim.valueY>0)
							falseneg++;
						else
							falsepos++;
						}
					}
				double pCorrect=(numCorrect/(double)images.size());
				double pFalseNeg=(falseneg/(double)images.size());
				double pFalsePos=(falsepos/(double)images.size());
				System.out.println("correct "+pCorrect+" f.neg "+pFalseNeg+" f.pos "+pFalsePos+" "+pCorrect/pFalseNeg);
				//System.out.println("toteps "+totalEps);
	
				if(pCorrect>lastPcorrect)
					{
					lastPcorrect=pCorrect;
					
					//Update weights
					double totalEps=0;
					for(TImage tim:images)
						{
						double e=feat.eval(tim,standardSize,0,0);
						if(e!=tim.valueY)
							totalEps+=tim.weightD;
						tim.weightD*=Math.exp(-alphat*tim.valueY*e);
//						if(e!=tim.valueY && e>0) //prioritize false positive
//							tim.weightD*=3;
						sumd+=tim.weightD;
						}
					
					}
				else
					{
					adaboost.dropLastClassifier();
					}
				System.out.println("total correct "+lastPcorrect);
				System.out.println("---");
				}
			
			for(int i=0;i<images.size();i++)
				{
				TImage tim=images.get(i);
				if(adaboost.eval(tim)!=tim.valueY)
					System.out.println("Failed: "+i+" for "+tim.valueY);
				}
			
					
			System.out.println(adaboost.toString());
			
			/*
			for(int t=0;t<1;t++)
				{
				System.out.println("iteration "+t);
			
				
				//Automatic ///////////////////////////
				
				
				
				
				
				
				
				
				
				//Manually programed //////////////////
				
					{
					int numCorrect=0;
					int falseneg=0;
					for(TImage tim:images)
						{
						int a=tim.getSum(standardSize/4-1, standardSize/4-1, 
								(standardSize-standardSize/4)-1, (standardSize-standardSize/4)-1);
						int b=tim.getSum(0, 0, standardSize, standardSize)-a;


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
			*/
			
			outfile.close();
			
			}
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		
		
		
		
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
					
					im=rescale(im);
//					im=rescale(TImage.findVariation(im,meanWindowSize));
					tim.createCumIm(im);
					
					
					/*
					ImageIO.write(im,"png",
							new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/"+channelName+"/images/transformed/"+valueY+"_"+f.getName()));
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
