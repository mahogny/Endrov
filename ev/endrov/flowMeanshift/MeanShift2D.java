package endrov.flowMeanshift;

import java.util.HashMap;
import java.util.HashSet;

import javax.vecmath.Vector2d;
import endrov.flow.EvOpGeneral;
import endrov.flowAveraging.EvOpSumRect;
import endrov.flowBasic.math.EvOpImageMulImage;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;

/**
 * Find pixels or classify areas using the Mean-shift algorithm 
 * @author Johan Henriksson
 *
 */
public class MeanShift2D
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
		EvPixels momentX;
		EvPixels momentY;
		EvPixels moment0;

		public MeanShiftPreProcess(EvPixels s,int pw, int ph)
			{
			int w=s.getWidth();
			int h=s.getHeight();
			
			//Put repeatImageZ somewhere else? a special exec? exec does not work unless
			//doing object instanceof analysis
			
			EvPixels incX=GenerateSpecialImage.genIncX(w, h);
			EvPixels incY=GenerateSpecialImage.genIncY(w, h);

			//Different kernel? +?
			EvOpGeneral op=new EvOpSumRect(pw,ph);

			//Pre-convolve all positions
			momentX=op.exec1(new EvOpImageMulImage().exec1(incX, s));
			momentY=op.exec1(new EvOpImageMulImage().exec1(incY, s));
			moment0=op.exec1(s);
			
			//TODO ensure that the type is double already here
			
			
			
			//return outs;
			}
		
		
		public void doAll(EvPixels s, int roiSX, int roiEX, int roiSY, int roiEY)
			{
			int w=s.getWidth();
			int h=s.getHeight();
			
			Vector2i[] pmid=new Vector2i[w*h];
			HashSet<Vector2i> mids=new HashSet<Vector2i>();
			HashMap<Vector2i, Integer> assignLevel=new HashMap<Vector2i, Integer>();
			
			//For each pixel, figure out where it belongs
			System.out.println("start");
			for(int ay=roiSY;ay<h && ay<roiEY;ay++)
				for(int ax=roiSX;ax<w && ax<roiEX;ax++)
//			for(int ay=0;ay<h;ay++)
	//			for(int ax=0;ax<w;ax++)
					{
					Vector2d pos=iterate(new Vector2d(ax,ay));
					Vector2i posi=new Vector2i((int)Math.round(pos.x),(int)Math.round(pos.y));
					mids.add(posi);
//					System.out.println(posi);

					pmid[ax+ay*w]=posi;
					//Vector2i index=new Vector2i(ax,ay,0); //az

					}
			System.out.println(mids);
//			iterate(new Vector2d(0,0,0));
			
			//Assign a color for each group of pixels
			int curLevel=1;
			for(Vector2i v:mids)
				assignLevel.put(v,(Integer)(curLevel++));
			
			//Output colored image
			EvPixels p=new EvPixels(EvPixelsType.INT,w,h);
			int[] pa=p.getArrayInt();
			Vector2i[] curpmid=pmid;
			for(int i=0;i<curpmid.length;i++)
				{
				Integer lev=assignLevel.get(curpmid[i]);
				if(lev!=null)
					pa[i]=lev;
				}



			
			System.out.println("end");
			System.out.println("mids "+mids.size());
			
			}
		

		private Vector2d next(Vector2d pos)
			{
			//Calculate mean at this position
			double sumX=0;
			double sumY=0;
			double sum0=0;
			//double sumweight=0;
			
			//momentX.entrySet()
			
			int w=momentX.getWidth();
			//int h=momentX.getHeight();

			int xyIndex=(int)(Math.round(pos.x)+Math.round(pos.y)*w);
			
			EvPixels pX=momentX;
			EvPixels pY=momentY;
			EvPixels p0=moment0;
			double[] apX=pX.getArrayDouble();
			double[] apY=pY.getArrayDouble();
			double[] ap0=p0.getArrayDouble();

			sumX+=apX[xyIndex];
			sumY+=apY[xyIndex];
			sum0+=ap0[xyIndex];
				
			//Turn into local moment
			sumX/=sum0;
			sumY/=sum0;
			
			return new Vector2d(sumX,sumY);
			}
		
		/**
		 * Iterate toward convergence for a position
		 */
		public Vector2d iterate(Vector2d pos)
			{
			//For our purpose, not enough z-resolution to build up a table. have to track position with fraction plane precision.
			
			Vector2d lastPos;
			for(;;)
				{
				lastPos=pos;
//				System.out.println(pos);
				
				////fractional position
				Vector2d pos1=new Vector2d(Math.floor(pos.x),Math.floor(pos.y));
				Vector2d pos2=new Vector2d(Math.ceil(pos.x),Math.floor(pos.y));
				Vector2d pos3=new Vector2d(Math.floor(pos.x),Math.ceil(pos.y));
				Vector2d pos4=new Vector2d(Math.ceil(pos.x),Math.ceil(pos.y));
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
				
				Vector2d diff=new Vector2d(pos);
				diff.sub(lastPos);
				double okdiff=0.002; //Has huge influence on number of found candidates. This is a hand-tuned value.
				if(diff.lengthSquared()<okdiff*okdiff)
					return pos;
				}
			}

		}
	
	
	}
