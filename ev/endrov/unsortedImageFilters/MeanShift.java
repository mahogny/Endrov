package endrov.unsortedImageFilters;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.vecmath.Vector3d;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.flow.EvOpGeneral;
import endrov.flow.std.math.EvOpImageMulImage;
import endrov.flowAveraging.EvOpMovingAverage;
import endrov.flowAveraging.EvOpMovingSum;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.unsortedImageFilters.specialImage.GenerateSpecialImage;
import endrov.util.EvDecimal;
import endrov.util.Partitioning;
import endrov.util.Vector3i;

/**
 * Find pixels or classify areas using the Mean-shift algorithm 
 * @author Johan Henriksson
 *
 */
public class MeanShift
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
			EvOpGeneral op=new EvOpMovingSum(pw,ph);

			//Pre-convolve all positions
			momentX=op.exec1(new EvOpImageMulImage().exec1(incX, s));
			momentY=op.exec1(new EvOpImageMulImage().exec1(incY, s));
			moment0=op.exec1(s);
			
			//TODO ensure that the type is double already here
			
			
			
			
			Vector3i[][] pmid=new Vector3i[s.getDepth()][s.getWidth()*s.getHeight()];
			HashSet<Vector3i> mids=new HashSet<Vector3i>();
			HashMap<Vector3i, Integer> assignLevel=new HashMap<Vector3i, Integer>();
			
			//For each pixel, figure out where it belongs
			System.out.println("start");
			//for(int i=0;i<500;i++)
			int az=0;
			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					Vector3d pos=iterate(new Vector3d(ax,ay,0));
					Vector3i posi=new Vector3i((int)Math.round(pos.x),(int)Math.round(pos.y),(int)Math.round(pos.z));
					mids.add(posi);
					//System.out.println(posi);

					pmid[az][ax+ay*w]=posi;
					//Vector3i index=new Vector3i(ax,ay,0); //az

					}
			System.out.println(mids);
//			iterate(new Vector3d(0,0,0));
			
			//Assign a color for each group of pixels
			int curLevel=0;
			for(Vector3i v:mids)
				assignLevel.put(v,(Integer)(curLevel++));
			
			//Output colored image
			EvStack outs=new EvStack();
			outs.getMetaFrom(s);
			int cz=0;
			for(Map.Entry<EvDecimal, EvImage> entry:s.entrySet())
				{
				EvPixels p=new EvPixels(EvPixels.TYPE_INT,w,h);
				int[] pa=p.getArrayInt();
				Vector3i[] curpmid=pmid[cz];
				for(int i=0;i<curpmid.length;i++)
					pa[i]=assignLevel.get(curpmid[i]);
				outs.put(entry.getKey(),new EvImage(p));
				cz++;
				}
			
			
			
			
			System.out.println("end");
			System.out.println("mids "+mids.size());
			
			//return outs;
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

			for(Map.Entry<EvDecimal, EvImage> entry:momentX.entrySet())
				{
				double dz=entry.getKey().doubleValue()-pos.z;
				
				double thisw=Math.exp(-dz*dz/szsz2);
				//Later, can use a cut-off. should make it a lot faster. might run into convergence problems!!!
				
				EvPixels pX=entry.getValue().getPixels().convertTo(EvPixels.TYPE_DOUBLE, true);
				EvPixels pY=momentY.get(entry.getKey()).getPixels().convertTo(EvPixels.TYPE_DOUBLE, true);
				EvPixels p0=moment0.get(entry.getKey()).getPixels().convertTo(EvPixels.TYPE_DOUBLE, true);
				double[] apX=pX.getArrayDouble();
				double[] apY=pY.getArrayDouble();
				double[] ap0=p0.getArrayDouble();

				sumX+=apX[xyIndex]*thisw;
				sumY+=apY[xyIndex]*thisw;
				sumZ+=ap0[xyIndex]*entry.getKey().doubleValue()*thisw; //Z-integral calculated on the fly
				
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
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		EvData data=EvData.loadFile(new File("testimages/smoothvariation.png"));
	
		EvChannel chan=data.getIdObjectsRecursive(EvChannel.class).values().iterator().next();
		MeanShiftPreProcess p=new MeanShiftPreProcess(chan.getFirstStack(),1, 8, 8);
		
		System.exit(0);
		
		
		}
	
	
	}
