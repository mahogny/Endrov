package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import endrov.data.EvObject;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.imageset.EvImage;
import endrov.imageset.Imageset;
import endrov.imagesetOST.OstImageset;
import endrov.nuc.NucLineage;

public class CopyOfTest
	{
	public static NucLineage getLin(Imageset ost)
		{
		for(EvObject evob:ost.metaObject.values())
			{
			if(evob instanceof NucLineage)
				{
				NucLineage lin=(NucLineage)evob;
				return lin;
				}
			}
		return null;
		}
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		
		
		//Load all worms
		String[] wnlist={
				"/Volumes/TBU_main02/ost4dgood/TB2164_080118",
//				"/Volumes/TBU_main02/ost4dgood/N2_071114",
				}; 
		Vector<Imageset> worms=new Vector<Imageset>();
		for(String s:wnlist)
			{
			Imageset ost=new OstImageset(new File(s));
			if(getLin(ost)!=null)
				worms.add(ost);
			}

//		String channelName="DIC";
		String channelName="RFP";


		//For all imagesets
		for(Imageset ost:worms)
			{
//			NucLineage lin=getLin(ost);

			for(int frame:ost.getChannel(channelName).imageLoader.keySet())
				{
				if(frame!=1202)
					continue;
//				Map<NucPair, NucLineage.NucInterp> inter=lin.getInterpNuc(frame);


				for(int z:ost.getChannel(channelName).imageLoader.get(frame).keySet())
					{
					if(z!=46)
						continue;
					System.out.println("frame "+frame+ "z "+z);
					
					
					EvImage im=ost.getChannel("DIC").getImageLoader(frame, z);
					BufferedImage jim=im.getJavaImage();
					jim=TImage.findVariation(jim,Train.meanWindowSize);
					TImage tim=new TImage();
					tim.createCumIm(jim);
					tim.valueY=1;

					try
						{
						ImageIO.write(jim,"png",
								new File("/Volumes/TBU_iomega_700GB/traintrack/test.png"));
						}
					catch (IOException e)
						{
						e.printStackTrace();
						}

					//Scan image
					for(int wsize=40;wsize<200;wsize+=3)
						{
						int jump=wsize/5;
						if(jump<3)
							jump=3;
						jump=2;
//						System.out.println("wsize "+wsize);

						for(int x=0;x<jim.getWidth()-wsize;x+=jump)
							for(int y=0;y<jim.getHeight()-wsize;y+=jump)
								{
								if(TrainManual.evalImage(tim, wsize, x, y,0))
									System.out.println("candidate z"+z+" wsize "+wsize+" "+x+" "+y);
	//							else
//									System.out.println("not "+x+" "+y);
								}


						}


					}

				break;

				}





			}





		}




	
	}
