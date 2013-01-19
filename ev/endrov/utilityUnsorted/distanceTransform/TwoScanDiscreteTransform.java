package endrov.utilityUnsorted.distanceTransform;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * A class the defines the general representation for a discrete distance
 * transformation implementing the two scan distance transformation developed
 * by Frank Y.Shih and Yi-Ta Wu and presented in 'Fast Euclidean distance 
 * transformation in two scans using a 3x3 neighborhood'. 
 * 
 * @author Javier Fernandez
 */

public abstract class TwoScanDiscreteTransform extends DistanceTransform
	{
	int[] forwardArray;
	int[] backwardArray;
	private EvPixels backwardScanImage;  
	abstract int forwardDistance(int x, int y,int w, int h);
	abstract int backwardDistance(int x, int y, int w, int h);
	
	public TwoScanDiscreteTransform(EvPixels input){
		super(input);

		int w = binaryImage.getWidth();
		int h = binaryImage.getHeight();

		forwardArray = new int[w*h];
		backwardScanImage = new EvPixels(EvPixelsType.INT,w,h);
		backwardArray = backwardScanImage.getArrayInt();
	}
	
	/**
	 * Perform a two scan distance transform method. First a forward scan is performed, left
	 * to right and top to bottom, updating the values in forwardScanImage. 
	 * 
	 */
	@Override
	public EvPixels transform()
		{
			int w = binaryImage.getWidth();
			int h = binaryImage.getHeight();
			
			//Forward Scan (left to right, top to bottom)			
			double init = System.currentTimeMillis();
		
			int count = w+1;
			for (int py = 1; py< h-1; py++){//The borders are supposed as background
				for (int px = 1; px< w-1; px++){				
					forwardArray[count] = forwardDistance(px,py,w,h);					
					count++;
				}
				count+=2;
			}
			
			double end = System.currentTimeMillis();
			System.out.println("Forward Runtime: "+ (end - init));
			
			init = System.currentTimeMillis();
			count = (h-2)*w+(w-2);
			//backward Scan (right to left, bottom to top)
			for (int py = h-2; py>0; py--){//The borders are supposed as background
				for	(int px = w-2; px>0; px--){
					backwardArray[count] = backwardDistance(px,py,w,h);
					count--;
				}
				count-=2;
			}
			end = System.currentTimeMillis();
			System.out.println("Backward Runtime: "+ (end - init));
			
		return backwardScanImage;
		}
	}
