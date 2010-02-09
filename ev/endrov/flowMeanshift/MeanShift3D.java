/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeanshift;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import javax.vecmath.Vector3d;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flow.EvOpGeneral;
import endrov.flowBasic.math.EvOpImageMulImage;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.flowImageStats.EvOpSumRect;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.Vector3i;

/**
 * Find pixels or classify areas using the Mean-shift algorithm 
 * @author Johan Henriksson
 *
 */
public class MeanShift3D
	{

	
	/**
	 * Preprocessed image
	 * @author Johan Henriksson
	 *
	 */
	public static class MeanShiftPreProcess
		{
		/**
		 * Cumulative sums
		 */
		EvStack momentX;
		EvStack momentY;
		EvStack moment0;
		
		//EvStack kernelX;
//		EvStack kernelY;

		double sigmaZ;
		double szsz2;
		
		public MeanShiftPreProcess(EvStack s, double sigmaZ, int pw, int ph)
			{
			int w=s.getWidth();
			int h=s.getHeight();
			this.sigmaZ=sigmaZ;
			szsz2=sigmaZ*sigmaZ*2;
			
			//Put repeatImageZ somewhere else? a special exec? exec does not work unless
			//doing object instanceof analysis
			
			EvStack incX=GenerateSpecialImage.repeatImageZ(GenerateSpecialImage.genIncX(w, h),s);
			EvStack incY=GenerateSpecialImage.repeatImageZ(GenerateSpecialImage.genIncY(w, h),s);

			//Different kernel? +?
			EvOpGeneral op=new EvOpSumRect(pw,ph);

			//Pre-convolve all positions
			momentX=op.exec1(new EvOpImageMulImage().exec1(incX, s));
			momentY=op.exec1(new EvOpImageMulImage().exec1(incY, s));
			moment0=op.exec1(s);
			
			//TODO ensure that the type is double already here
			
			
			
			//return outs;
			}
		
		
		public void doAll(EvStack s, int roiSX, int roiEX, int roiSY, int roiEY)
			{
			int w=s.getWidth();
			int h=s.getHeight();
			int d=s.getDepth();
			
			Vector3i[][] pmid=new Vector3i[d][w*h];
			HashSet<Vector3i> mids=new HashSet<Vector3i>();
			HashMap<Vector3i, Integer> assignLevel=new HashMap<Vector3i, Integer>();
			
			//For each pixel, figure out where it belongs
			System.out.println("start");
			//for(int i=0;i<500;i++)
			int az=0;
			for(int ay=roiSY;ay<h && ay<roiEY;ay++)
				for(int ax=roiSX;ax<w && ax<roiEX;ax++)
//			for(int ay=0;ay<h;ay++)
	//			for(int ax=0;ax<w;ax++)
					{
					Vector3d pos=iterate(new Vector3d(ax,ay,0));
					Vector3i posi=new Vector3i((int)Math.round(pos.x),(int)Math.round(pos.y),(int)Math.round(pos.z));
					mids.add(posi);
//					System.out.println(posi);

					pmid[az][ax+ay*w]=posi;
					//Vector3i index=new Vector3i(ax,ay,0); //az

					}
			System.out.println(mids);
//			iterate(new Vector3d(0,0,0));
			
			//Assign a color for each group of pixels
			int curLevel=1;
			for(Vector3i v:mids)
				assignLevel.put(v,(Integer)(curLevel++));
			
			//Output colored image
			EvStack outs=new EvStack();
			outs.getMetaFrom(s);
//			outs.allocate(w, h, EvPixelsType.TYPE_INT, s);
			//int[][] outarr=outs.getArraysInt();
			//int cz=0;
			EvImage[] inArr=s.getImages();
			for(int cz=0;cz<inArr.length;cz++)
			//for(Map.Entry<EvDecimal, EvImage> entry:s.entrySet())
				{
				EvPixels p=new EvPixels(EvPixelsType.INT,w,h);
				int[] pa=p.getArrayInt();
				Vector3i[] curpmid=pmid[cz];
				for(int i=0;i<curpmid.length;i++)
					{
					Integer lev=assignLevel.get(curpmid[i]);
					if(lev!=null)
						pa[i]=lev;
					}
				outs.putInt(cz,new EvImage(p));
				//cz++;
				}
			
			
			
			
			System.out.println("end");
			System.out.println("mids "+mids.size());
			
			}
		

		private Vector3d next(Vector3d pos)
			{
			//Calculate mean at this position
			double sumX=0;
			double sumY=0;
			double sumZ=0;
			double sum0=0;
			//double sumweight=0;
			
			//momentX.entrySet()
			
			int w=momentX.getWidth();
			//int h=momentX.getHeight();

			int xyIndex=(int)(Math.round(pos.x)+Math.round(pos.y)*w);
			
			EvImage[] imArr=momentX.getImages();
			for(int az=0;az<imArr.length;az++)
			//for(Map.Entry<EvDecimal, EvImage> entry:momentX.entrySet())
				{
				//double dz=entry.getKey().doubleValue()-pos.z;
				double thisZ=momentX.transformImageWorldZ(az);
				double dz=thisZ-pos.z;
				
				double thisw=Math.exp(-dz*dz/szsz2);
				//Later, can use a cut-off. should make it a lot faster. might run into convergence problems!!!
				
				EvPixels pX=imArr[az].getPixels().getReadOnly(EvPixelsType.DOUBLE);
				EvPixels pY=momentY.getInt(az).getPixels().getReadOnly(EvPixelsType.DOUBLE);
				EvPixels p0=moment0.getInt(az).getPixels().getReadOnly(EvPixelsType.DOUBLE);
				double[] apX=pX.getArrayDouble();
				double[] apY=pY.getArrayDouble();
				double[] ap0=p0.getArrayDouble();

				sumX+=apX[xyIndex]*thisw;
				sumY+=apY[xyIndex]*thisw;
				sumZ+=ap0[xyIndex]*thisZ*thisw; //Z-integral calculated on the fly
				
				sum0+=ap0[xyIndex]*thisw;
				//sumweight+=thisw;
				}
			//Turn into local moment
			sumX/=sum0;
			sumY/=sum0;
			sumZ/=sum0;
			
			return new Vector3d(sumX,sumY,sumZ);
			}
		
		/**
		 * Iterate toward convergence for a position
		 */
		public Vector3d iterate(Vector3d pos)
			{
			//For our purpose, not enough z-resolution to build up a table. have to track position with fraction plane precision.
			
			Vector3d lastPos;
			for(;;)
				{
				lastPos=pos;
//				System.out.println(pos);
				
				////fractional position
				Vector3d pos1=new Vector3d(Math.floor(pos.x),Math.floor(pos.y),pos.z);
				Vector3d pos2=new Vector3d(Math.ceil(pos.x),Math.floor(pos.y),pos.z);
				Vector3d pos3=new Vector3d(Math.floor(pos.x),Math.ceil(pos.y),pos.z);
				Vector3d pos4=new Vector3d(Math.ceil(pos.x),Math.ceil(pos.y),pos.z);
				double dx=pos.x-Math.floor(pos.x);
				double dy=pos.y-Math.floor(pos.y);
				pos1=next(pos1);
				pos2=next(pos2);
				pos3=next(pos3);
				pos4=next(pos4);
				pos1.scale((1-dx)*(1-dy));
				pos2.scale(dx*(1-dy));
				pos3.scale((1-dx)*dy);
				pos4.scale(dx*dy);
				pos1.add(pos2);
				pos1.add(pos3);
				pos1.add(pos4);
				pos=pos1;

				////Rounded position
				//pos=next(pos);
				
				Vector3d diff=new Vector3d(pos);
				diff.sub(lastPos);
				double okdiff=0.002; //Has huge influence on number of found candidates. This is a hand-tuned value.
				if(diff.lengthSquared()<okdiff*okdiff)
					return pos;
				}
			}

		}
	
	
	public static void main(String[] args)
		{
		//Init
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
//		EvData data=EvData.loadFile(new File("testimages/smoothvariation.png"));
		// /Volumes/TBU_main02/ost4dgood/TB2164_080118.ost/
		EvData data=EvData.loadFile(new File("/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost/imset-im/ch-RFP/00014750/00000010.jpg"));
		EvChannel chan=data.getIdObjectsRecursive(EvChannel.class).values().iterator().next();
//		MeanShiftPreProcess p=new MeanShiftPreProcess(chan.getFirstStack(),1, 8, 8);
		MeanShiftPreProcess p=new MeanShiftPreProcess(chan.getFirstStack(),1, 10, 10);
		p.doAll(chan.getFirstStack(), 50, 230, 50, 150);
		
		System.exit(0);
		
		///Volumes/TBU_main02/ost4dgood/TB2164_080118.ost
		
		/*
		"/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost/imset-im/ch-RFP/00014750/00000010.jpg"

		"/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost/imset-im/ch-RFP/00012790/00000008.5.jpg" 

		"/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost/imset-im/ch-RFP/00013210/00000010.jpg"
*/
		}
	
	
	}
