
package endrov.distanceTransform;

import endrov.imageset.EvPixels;

public class ChessboardTransform extends TwoScanDiscreteTransform
	{
		private int fourNeighbors[];
	
	public ChessboardTransform(EvPixels input){
		super(input);
		fourNeighbors = new int[4];
	}
	
	@Override
	int forwardDistance(int x, int y,int w, int h)
		{
		int pixelIndex = y*w+x;
		if (binaryArray[pixelIndex] ==0) {return 0;}
		
		fourNeighbors[0] = forwardArray[pixelIndex -1] +1; //left y*w+ (x-1)
		fourNeighbors[1] = forwardArray[pixelIndex - w]+1; //up (y-1)*w +x 
		fourNeighbors[2] = forwardArray[pixelIndex -w-1]+1; //left-up (y-1)*w+(x-1)
		fourNeighbors[3] = forwardArray[pixelIndex -w +1]+1; //right-up (y-1)*w+(x+1)
		
		//find minimum value and actualize forwardScanImage
		int min = fourNeighbors[0];
		for (int it=1;it<4;it++){
			if(fourNeighbors[it]<min) min=fourNeighbors[it];
		}		
		return min;	
	}
	
	@Override
	int backwardDistance(int x, int y,int w, int h)
		{
		int pixelIndex = y*w+x;
		fourNeighbors[0] = backwardArray[pixelIndex +1]+1; //left y*w+ (x+1)
		fourNeighbors[1] = backwardArray[pixelIndex +w]+1;  //up (y+1)*w +x
		fourNeighbors[2] = forwardArray[pixelIndex +w +1]+1; //left-up  (y+1)*w+(x+1)
		fourNeighbors[3] = forwardArray[pixelIndex +w -1]+1; //right-up (y+1)*w+(x-1)
		
		//find minimum value and actualize forwardScanImage
		int min = fourNeighbors[0];
		for (int it=1;it<4;it++){
			if(fourNeighbors[it]<min) min=fourNeighbors[it];
		}
		
		int fvalue = forwardArray[pixelIndex];
		if (fvalue < min) {return fvalue;}
		return min; //else consequence
		}
	}
