package util;

import evplugin.ev.*;
import evplugin.imagesetOST.*;
//import evplugin.nuc.*;
import evplugin.data.*;
import evplugin.imageset.*;

import java.util.*;
import java.io.*;
import java.awt.image.*;
//import javax.vecmath.*;

/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BatchExtractIntens
{

public static void calcAP(File file)
	{
	try
		{
		//System.out.println("current Imageset "+file.getPath());
		String currentpath = file.getPath();
		System.out.println("current imageset: "+currentpath);
		//if (0==0) // all imagesets
		if (currentpath.contains("NRed")) // only deal with these image sets
			{	
			System.out.println("imageset found, executing...");

			long currentTime=System.currentTimeMillis();
			OstImageset ost=new OstImageset(file.getPath());

			//System.out.println("ost.basedir "+ost.basedir);
			String currentostname = ost.toString();
			//System.out.println("current imageset: " + currentostname);
			//String currentostDescription = ost.description.toString();
			double timeStep = 1;
			timeStep = ost.meta.metaTimestep;
			//System.out.println("time step: " + timeStep + " seconds");

			Imageset.ChannelImages ch=ost.channelImages.get("RFP");
			TreeMap<Integer, TreeMap<Integer, EvImage>> images=ch.imageLoader;

			//create an output file in the data directory
			BufferedWriter outFile;
			File outFilePath=new File(ost.datadir(),"RFPintensTable.txt");
			outFile = new BufferedWriter( new FileWriter(outFilePath) );

			//outFile.write("# imageset: " + currentostname + "\n");
			//outFile.write("# frame\texposureTime\tvalue\n");
			outFile.write(currentostname + "" +currentostname+"\t");
			int allframes = images.size(); //determine the number of frames
			double exptimeArray[] = new double[allframes];
			double intensities[] = new double [allframes];
			double signalpixels[] = new double [allframes];
			int frameCounter = 0;
			int goodFrameCounter = 0;
			int overexposed = 0;

			for(int frame:images.keySet())
				{
				TreeMap<Integer, EvImage> zs=images.get(frame);
				//get exposure
				double exptime=0;
				String exptimes=ch.getFrameMeta(frame, "exposuretime");
				if(exptimes!=null)
					exptime=Double.parseDouble(exptimes); // convert to double
				else
					System.out.println("No exposure time for frame "+frame);

				double totalintens = 0;
				double pixCounter = 0;
				overexposed = 0;
				for(int z:zs.keySet())
					{
					EvImage evim=zs.get(z);	
					//get image
					BufferedImage bufi=evim.getJavaImage();
					Raster r=bufi.getData();
					bufi=evim.getJavaImage();
					r=bufi.getData();
					int w=bufi.getWidth();
					int h=bufi.getHeight();
					int s=zs.lastKey()+1; //stack Z
					//loop through every pixel of this image

					for (int co=0;co<w;co++)
						for (int ro=0;ro<h;ro++)
							{
							double[] pix=new double[3];	
							r.getPixel(co, ro, pix);
							double p = pix[0];											
							if (p>=30) // empirically determined threshold (background)
								{
								totalintens+=p;
								pixCounter++;
								if (p > 254) // overexposed
									{
									overexposed++;
									}
								}
							}
					}
				System.out.println("intensity found in frame " + frame + ": "+ (totalintens / pixCounter) + " exp: "+exptime);
				intensities[frameCounter] = totalintens;
				signalpixels[frameCounter] = pixCounter;
				exptimeArray[frameCounter] = exptime;
				System.out.println("intensities[]=" + intensities[frameCounter] + ", signalpixels[frameCounter]=" + signalpixels[frameCounter] + " exptimeArray[frameCounter]="+exptimeArray[frameCounter]);
				//System.out.println(totalpixels+ " pixels found in frame "+ frame);
				//System.out.println("frame: " +frame + " average: " + (totalintens / totalpixels) * exptime);
				//outFile.write("frame\n");
				//outFile.write(""+frame+"\n");
				//outFile.write("exptime\n");
				//outFile.write(""+exptime+"\n");

				//outFile.write(""+localintensity+"\t");
				//outFile.write("\n");
				//outFile.flush(); //force write
				if (overexposed < 10)
					{	
					goodFrameCounter++;
					}
				else
					{
					System.out.println("overexposed! ("+overexposed+")");
					intensities[frameCounter] = -1;
					signalpixels[frameCounter] = -1;
					exptimeArray[frameCounter] = -1;
					}
				frameCounter++;
				}


			double eA[] = new double[goodFrameCounter];
			double iA[] = new double[goodFrameCounter];

			int gf = 0;
			System.out.println("frameCounter=" + frameCounter + ", goodFrameCounter="+goodFrameCounter);
			for (int ff=0;ff<frameCounter;ff++)
				{
				if (signalpixels[ff] > 0)
					{
					eA[gf] = exptimeArray[ff];
					iA[gf] = intensities[ff]/signalpixels[ff];
					System.out.println("fn:" +ff+ " eA: " + eA[gf] + " iA: " + iA[gf]);
					gf++;
					}
				}

			RegConst linR = linRegR(eA,iA);
			System.out.println("R: " + linR.R + " a= " + linR.a + " b= " + linR.b);
			System.out.println("corrected expt: " + (linR.a + linR.b) + "");

			outFile.write(""+(linR.a+linR.b)+"\t"+linR.R+"\n");
			outFile.flush();
			outFile.close();
			System.out.println(" timeX "+(System.currentTimeMillis()-currentTime));
			System.out.println("extraction  done");
			}
		else
			System.out.println("skipping");
		}
	catch (Exception e)
		{
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}


public static class RegConst
{
double a,b,R;
}

public static RegConst linRegR(double[] linRegQ, double[] linRegS)
	{
	double R = 0;
	final int n = linRegQ.length;
	double Sx = 0;
	double Sy = 0;
	double Sx2 = 0;
	double Sy2 = 0;
	double Sx_y = 0;

	for (int a=0;a<n;a++)
		{
		Sx += linRegQ[a];
		Sy += linRegS[a];

		Sx2 += linRegQ[a]*linRegQ[a];
		Sy2 += linRegS[a]*linRegS[a];

		Sx_y += linRegQ[a]*linRegS[a];
		}

	double Sxx = Sx2 - Sx*Sx/n;
	double Syy = Sy2 - Sy*Sy/n;
	double Sxy = Sx_y - Sx*Sy/n;

	R = Sxy/Math.sqrt(Sxx*Syy);

	RegConst rc=new RegConst();

	rc.b = Sxy/Sxx;
	rc.a = Sy/n - rc.b * Sx/n;
	rc.R = R;
	return rc;
	}





/**
 * Entry point
 * @param arg Command line arguments
 */
public static void main(String[] arg)
	{
	Log.listeners.add(new StdoutLog());
	EV.loadPlugins();

	if(arg.length==0)
		arg=new String[]{
				//"/Volumes/TBU_xeon01_500GB01/ost3dfailed/",
				//"/Volumes/TBU_xeon01_500GB01/ost3dgood/",
				//"/Volumes/TBU_xeon01_500GB01/ost4dgood/",
				"/Volumes/TBU_xeon01_500GB02/daemon/output/"

	};
	for(String s:arg)
		for(File file:(new File(s)).listFiles())
			if(file.isDirectory())
				{
				long currentTime=System.currentTimeMillis();
				calcAP(file);
				System.out.println(" timeY "+(System.currentTimeMillis()-currentTime));
				}
	}
}


//read a pixel
//int x=0;
//int y=0;
//double[] pix=new double[3];
//r.getPixel(x, y, pix);
//double p=pix[0];
//System.out.println("pixel 0,0,"+frame+" = "+p+"; with expt: "+(p*exptime));


//sample: get image coordinates from real world coordinates
//double imx=evim.transformWorldImageX(antpos.x);
//double imy=evim.transformWorldImageY(antpos.y);


//get real world coordinates from image
//double realx=evim.transformImageWorldX(x);
//double realy=evim.transformImageWorldY(y);
//double worldz=z/ost.meta.resZ;
