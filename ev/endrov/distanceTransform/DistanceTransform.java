package endrov.distanceTransform;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * General representation of a distance transformation class.
 * Each subclass must be constructed providing a binary image
 * and implement the scanImage method.
 * 
 * @author Javier Fernandez
 */
public abstract class DistanceTransform
	{
		EvPixels binaryImage;
		int[] binaryArray;
		
		public DistanceTransform(EvPixels input){
			binaryImage = invertBinary(input);
			binaryArray = binaryImage.getArrayInt();
		}
		
		/**
		 * Distance transformation method to be implemented by the subclasses
		 * taking a binary image 
		 * @return An EvPixels with the corresponding distance transformation
		 */
		abstract EvPixels transform();	
		private EvPixels invertBinary(EvPixels input){	
			int w = input.getWidth();
			int h = input.getHeight();
			EvPixels out = new EvPixels(EvPixelsType.INT, w, h);
			int[] inputArray = input.getArrayInt();
			int[] outputArray = out.getArrayInt();
			
			
			for (int x=0;x<w;x++){
					for (int y=0;y<h;y++){
						outputArray[y*w +x] = (inputArray[y*w+x] == 0)? 1:0;
					}
				}
			return out;
		}
	}
