package endrov.utilityUnsorted.distanceTransform;

import endrov.imageset.EvPixels;

public class ManhattanTransform extends TwoScanDiscreteTransform
	{
		private int twoNeighbors[];
	
	public ManhattanTransform(EvPixels input){
		super(input);
		twoNeighbors = new int[2];
	}
	
	@Override
	int forwardDistance(int x, int y,int w, int h)
		{
		int pixelIndex = (y*w)+x;
		if (binaryArray[pixelIndex] ==0) {return 0;}
		
		twoNeighbors[0] = forwardArray[pixelIndex -1] +1 ; //left y*w+(x-1)
		twoNeighbors[1] = forwardArray[pixelIndex -w]+1;  //up (y-1)*w+x		
		
		//find minimum value and actualize forwardArray
		int min = twoNeighbors[0];
		if (min > twoNeighbors[1]) min = twoNeighbors[1];		
		return min;	
	}
	
	@Override
	int backwardDistance(int x, int y,int w, int h)
		{
		int pixelIndex = (y*w)+x;
		twoNeighbors[0] = backwardArray[pixelIndex+1]+1; //left y*w+ (x+1)
		twoNeighbors[1] = backwardArray[pixelIndex+w]+1;  //up (y+1)*w +x]
		
		//find minimum value and actualize forwardScanImage
		int min = twoNeighbors[0];
		if (min > twoNeighbors[1]) min = twoNeighbors[1];
		
		int fvalue = forwardArray[pixelIndex];
		if (fvalue < min) {return fvalue;}
		return min; //else consequence

		}
	}
