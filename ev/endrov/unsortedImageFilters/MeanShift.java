package endrov.unsortedImageFilters;

import java.util.Map;

import javax.vecmath.Vector3d;

import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;

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

		double sigmaZ;
		double szsz2;
		
		public MeanShiftPreProcess(EvStack s, double sigmaZ)
			{
			int w=s.getWidth();
			int h=s.getHeight();
			this.sigmaZ=sigmaZ;
			szsz2=sigmaZ*sigmaZ*2;
			
			//Put repeatImageZ somewhere else? a special exec? exec does not work unless
			//doing object instanceof analysis
			
			EvStack incX=GenerateSpecialImage.repeatImageZ(GenerateSpecialImage.genIncX(w, h),s);
			EvStack incY=GenerateSpecialImage.repeatImageZ(GenerateSpecialImage.genIncY(w, h),s);
			momentX=new ImageMath.MulImageOp().exec(incX, s);
			momentY=new ImageMath.MulImageOp().exec(incY, s);
			}
		

		/**
		 * Iterate toward convergence for a position
		 */
		public Vector3d iterate(Vector3d pos)
			{
			
			
			
			//Vector3d lastPos=null;
			for(;;)
				{
				//Calculate mean at this position
				double sumX=0;
				double weight=0;
				
				//momentX.entrySet()
				
				
				for(Map.Entry<EvDecimal, EvImage> entry:momentX.entrySet())
					{
					double dz=entry.getKey().doubleValue()-pos.z;
					
					double thisw=Math.exp(-dz*dz/szsz2);

					EvPixels pX=entry.getValue().getPixels();
					EvPixels pY=momentY.get(entry.getKey()).getPixels();
					
					int xw=pX.getWidth();
					int xh=pX.getHeight();
					
					
					
					
					//Later, can use a cut-off. should make it a lot faster. might run into convergence problems!!!
					
					weight+=thisw;
					
					
					
					}
				
				
				
				
				
				
				}
			
			
			
			}

		}
	
	
	
	/**
	 * 
	 * preprocess image
	 * 
	 * 
	 * 
	 */
	
	}
