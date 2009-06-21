package endrov.unsortedImageFilters;

import java.io.File;
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

//			EvOpGeneral op=new EvOpMovingAverage(pw,ph);
			EvOpGeneral op=new EvOpMovingSum(pw,ph);

			//Pre-convolve all positions
			momentX=op.exec1(new EvOpImageMulImage().exec1(incX, s));
			momentY=op.exec1(new EvOpImageMulImage().exec1(incY, s));
			moment0=op.exec1(s);
			
			//TODO ensure that the type is double already here
			
			
			HashSet<Vector3i> mids=new HashSet<Vector3i>();
			
			System.out.println("start");
			//for(int i=0;i<500;i++)
			for(int ay=0;ay<50;ay++)
				for(int ax=0;ax<50;ax++)
					{
					Vector3d pos=iterate(new Vector3d(ax,ay,0));
					Vector3i posi=new Vector3i((int)Math.round(pos.x),(int)Math.round(pos.y),(int)Math.round(pos.z));
					mids.add(posi);
//					System.out.println(pos);
					}
//			iterate(new Vector3d(0,0,0));
			
			
			System.out.println("end");
			System.out.println("mids "+mids.size());
			}
		

		/**
		 * Iterate toward convergence for a position
		 */
		public Vector3d iterate(Vector3d pos)
			{
			//For our purpose, not enough z-resolution to build up a table. have to track position with fraction plane precision.
			
			Vector3d lastPos=null;
			for(;;)
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
				
				
				lastPos=pos;
//				System.out.println(pos);
				pos=new Vector3d(sumX,sumY,sumZ);
				
				Vector3d diff=new Vector3d(pos);
				diff.sub(lastPos);
				if(pos.equals(lastPos))
					return pos;
//					System.exit(0);
//				if(diff.lengthSquared()<1*1)
	//				System.exit(0);
				
				//If it gets stuck: possible to search at subpixel resolution. calculate position in 4 (x,y), use bilinear interpolation
				
				
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
		MeanShiftPreProcess p=new MeanShiftPreProcess(chan.getFirstStack(),1, 15, 15);
		
		System.exit(0);
		
		
		}
	
	
	}
