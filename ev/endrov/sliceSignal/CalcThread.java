package endrov.sliceSignal;

import javax.swing.*;
import java.awt.image.*;
import java.io.*;

import endrov.ev.*;
import endrov.imageset.*;
import endrov.shell.*;
import endrov.util.EvDecimal;
import endrov.util.Vector2D;


/**
 * The thread for doing calculations
 */
public final class CalcThread extends BatchThread
	{
	private final Imageset rec;
	private final double stripevar;
	private final int numstripes;
	private final String channelName;
	private final int startFrame;
	private final int endFrame;
	private final String signalfilename;	
	private final Shell shell;
	
	public CalcThread(Imageset rec,	Shell shell,
										double stripevar, int numstripes,	String channelName,	
										int startFrame,	int endFrame,	
										String signalfilename)
		{
		this.rec=rec;
		this.shell=shell;
		this.stripevar=stripevar;
		this.numstripes=numstripes;
		this.channelName=channelName;
		this.startFrame=startFrame;
		this.endFrame=endFrame;
		this.signalfilename=signalfilename;
		}
	
	public String getBatchName()
		{
		return "Slice/Signal";
		}
	
	public void run()
		{
		double exptime=1;
				
    try
    	{
  		BufferedWriter signalfile = new BufferedWriter(new FileWriter(signalfilename));
    	double[][] lengthmap=null;
    	//can make this static if we first localize one frame
    	
    	if(signalfilename!=null)
    		{
    		Imageset.ChannelImages ch=rec.getChannel(channelName);
    				
    		//Figure out how to discretize
    		final double[] stripepos=new double[numstripes];
    		for(int i=0;i<numstripes;i++)
    			stripepos[i]=(double)i/numstripes + 0.5/numstripes;

    		//For all frames
    		EvDecimal curframe=new EvDecimal(startFrame);
    		curframe=ch.closestFrame(curframe);
     		while(curframe.lessEqual(new EvDecimal(endFrame)))
    			{
    			final double[] stripeint=new double[numstripes];
    			final double[] stripew=new double[numstripes];

    			//For all z
    			EvDecimal z=ch.closestZ(curframe, EvDecimal.ZERO);
    			for(;;)
    				{
    				//Check for premature stop
    				if(die)
    					{
    					batchDone();
    					return;
    					}

    				//Tell about progress
    				batchLog(""+curframe+"/"+z);

    				//Load image
    				try
    					{
  						EvImage imload=ch.getImageLoader(curframe, z);
      				if(imload!=null)
    						{
    						BufferedImage bufi=imload.getJavaImage();
    						if(bufi!=null)
    							{
    							Raster r=bufi.getData();
    							final int w=bufi.getWidth();
    							final int h=bufi.getHeight();									


    							//Create length map if there is none
    							if(lengthmap==null)
    								{
    								lengthmap=new double[h][w];
      			    		final int dispX=(int)ch.getMeta().dispX;
      			    		final int dispY=(int)ch.getMeta().dispY;
      			    		/*
      			    		System.out.println("Displaced "+dispX+" "+dispY+" / size "+w+" "+h);
      			    		System.out.println("shell "+shell.midx+" "+shell.midy+" "+shell.major+" --- "+(ch.getMeta().chBinning/(double)rec.meta.resX)+
      			    				" "+(ch.getMeta().chBinning/(double)rec.meta.resY));
      			    		*/
    								Vector2D dirvec=Vector2D.polar(shell.major, shell.angle);
    								Vector2D startpos=dirvec.add(new Vector2D(shell.midx,shell.midy));
    								dirvec=dirvec.normalize().mul(-1);
    								
    								//Calculate distances
    								for(int ay=0;ay<h;ay++)
    									for(int ax=0;ax<w;ax++)
    										{
    										//Convert to world coordinates
    										/*
    										Vector2D pos=new Vector2D(
    												(ax-dispX)*ch.getMeta().chBinning/(double)rec.meta.resX, 
    												(ay-dispY)*ch.getMeta().chBinning/(double)rec.meta.resY);
    										*/
    										Vector2D pos=new Vector2D(
    												((ax)*ch.getMeta().chBinning+dispX)/(double)rec.resX,  //TODO WTF??? 
    												((ay)*ch.getMeta().chBinning+dispY)/(double)rec.resY);
/*
    										Vector2D pos=new Vector2D(
    												(ax)*ch.getMeta().chBinning/(double)rec.meta.resX, 
    												(ay)*ch.getMeta().chBinning/(double)rec.meta.resY);
*/
    										//Check if this is within ellipse boundary
    										Vector2D elip=pos.sub(new Vector2D(shell.midx, shell.midy)).rotate(shell.angle); //todo: angle?
    										if(1 >= elip.y*elip.y/(shell.minor*shell.minor) + elip.x*elip.x/(shell.major*shell.major) )
    											{
    											//xy . dirvecx = cos(alpha) ||xy|| ||dirvecx||
    											lengthmap[ay][ax]=pos.sub(startpos).dot(dirvec)/(2*shell.major);
    											}
    										else
    											lengthmap[ay][ax]=-1;
    										//drgg
  									//		lengthmap[ay][ax]=pos.sub(startpos).dot(dirvec)/(2*shell.major);
    										}
    								
    								//Store distance map
    								BufferedWriter lengthfile = new BufferedWriter(new FileWriter("length.txt"));
    								for(int ay=0;ay<h;ay++)
    									{
      								for(int ax=0;ax<w;ax++)
      									lengthfile.write(" "+lengthmap[ay][ax]);
      								lengthfile.write("\n");
    									}
    								lengthfile.close();
    								}
    								
    							//Integrate
    							final int pixel[]=new int[r.getNumBands()];
    							for(int ay=0;ay<h;ay++)
    								for(int ax=0;ax<w;ax++)
    									if(lengthmap[ay][ax]!=-1)
    										{
    										r.getPixel(ax,ay,pixel);
    										final double p=pixel[0];
    										for(int i=0;i<numstripes;i++)
    											{
    											final double dx=lengthmap[ay][ax]-stripepos[i];
    											final double weight=Math.exp(-dx*dx/(2*stripevar));
    											stripeint[i]+=weight*p;
    											stripew[i]+=weight;
    											}
    										}
    							}
    						}
    					}
    				catch(Exception e)
    					{
    					System.out.println("Exception");
    					}

    				//Go to next z
    				final EvDecimal nz=ch.closestZAbove(curframe, z);
    				if(nz.equals(z))
    					break;
    				z=nz;
    				}

    			//Correction by exposure time
    			String exptimes=ch.getFrameMeta(curframe, "exposuretime");
    			if(exptimes!=null)
    				exptime=Double.parseDouble(exptimes);
    			else
    				System.out.println("No exposure time for frame "+curframe);
    			
    			
    			//Store away integration results
    			String newline=""+curframe+" "+exptime+" ";
    			for(int i=0;i<numstripes;i++)
    				{
    				final double div=stripew[i];
    				if(div==0)
    					{
    					JOptionPane.showMessageDialog(null, "Error occured: stripeWeight = 0. Check shell area and exposure time");
    					batchDone();
    					return;
    					}
    				newline=newline+(stripeint[i]/div)+" ";
    				}
    			newline=newline+"\n";
    			signalfile.write(newline);
  				System.out.print(newline);

    			//Go to next frame. End if there are no more frames.
  				EvDecimal newcurframe=ch.closestFrameAfter(curframe);
    			if(newcurframe.equals(curframe))
    				break;
    			curframe=newcurframe;
    			}
    		}

    	signalfile.close();
    	
    	//Normal exit
  		batchLog("Done");
    	}
    catch (IOException e)
    	{
  		batchLog("I/O error");
    	}
    batchDone();
		}
    
    
    
	}