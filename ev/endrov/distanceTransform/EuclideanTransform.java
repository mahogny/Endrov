package endrov.distanceTransform;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

public class EuclideanTransform extends DistanceTransform
	{
	//relative coordinates vector: records the horizontal and vertical pixel
	//distances between p and the closest background pixel
	private IntPair r[];
	private int width;
	private int height;
	private EvPixels backwardScanImage;
	private int[] backwardArray;
	

	
	public EuclideanTransform(EvPixels input){
		super(input);
		this.width = binaryImage.getWidth();
		this.height = binaryImage.getHeight();
		
		backwardScanImage = new EvPixels(EvPixelsType.INT,width,height);
		backwardArray = backwardScanImage.getArrayInt();
		
		r=new IntPair[width*height];
		for (int x=0; x<width;x++){
			for (int y=0; y<height; y++){
					r[y*width+x] = new IntPair(0,0);
			}
		} 
	}
	
	/**
	 * Get the position pair in the image array of a given
	 * neighbor q, from a given pixel p
	 * 
	 */
	public IntPair qFromP(IntPair p, int qNum){
  	switch (qNum) {
      case 1: return new IntPair(p.x-1,p.y);
      case 2: return new IntPair(p.x-1,p.y-1);
      case 3: return new IntPair(p.x,p.y-1);
      case 4: return new IntPair(p.x+1,p.y-1);
      case 5: return new IntPair(p.x+1,p.y);
      case 6: return new IntPair(p.x+1,p.y+1);
      case 7: return new IntPair(p.x,p.y+1);
      case 8: return new IntPair(p.x-1,p.y+1);
      default: return null;
  	}
	}
  
	public int binaryValue(IntPair p){
  	return binaryArray[p.y*width+p.x];
  }
	/**
	 * Return the pair of values of r for the given pixel q
	 * 
	 */
	public IntPair rValue(IntPair q){
		return r[q.y*width + q.x];
	}
	
	
  /**
   * The difference of the squared Euclidean distances of a given neighbor q	
   */
	public int euclideanDistanceDifference(IntPair q, int qNum){
	switch(qNum){
		case 1: return rValue(q).x*2+1;
		case 5: return rValue(q).x*2+1;
		case 3: return rValue(q).y*2+1;
		case 7: return rValue(q).y*2+1;
		case 2: return (rValue(q).x + rValue(q).y +1)*2;
		case 4: return (rValue(q).x + rValue(q).y +1)*2;
		case 6: return (rValue(q).x + rValue(q).y +1)*2;
		case 8: return (rValue(q).x + rValue(q).y +1)*2;
		default: return -1;
		}
	}
	
	public IntPair relativeCoordinatesDifference(int qNum){
	switch(qNum){
	case 1: return new IntPair(1,0);
	case 5: return new IntPair(1,0);
	case 3: return new IntPair(0,1);
	case 7: return new IntPair(0,1);
	case 2: return new IntPair(1,1);
	case 4: return new IntPair(1,1);
	case 6: return new IntPair(1,1);
	case 8: return new IntPair(1,1);
	default: return new IntPair(-1,-1);
	}
	
	}
	
	@Override
	public EvPixels transform()
		{
		// Forward scan
		//Forward Scan (left to right, top to bottom)			
		for (int py = 1; py< height-1; py++){//The borders are supposed as background
			for (int px = 1; px< width-1; px++){
				int min = java.lang.Integer.MAX_VALUE;
				int pixelIndex = py*width+px;
				if (binaryArray[pixelIndex] ==0) {continue;}
				
				int bestq = 0;
				for (int qNum = 1; qNum<=4; qNum++){
					IntPair q = qFromP(new IntPair(px,py),qNum);
					int diff = binaryValue(q) + euclideanDistanceDifference(q, qNum);
					
					if (diff < min ){
						min = diff;
						bestq = qNum;
					} 
				}
				binaryArray[py*width+px] = min;
				
				if (bestq != 0){
					IntPair rq = rValue(qFromP(new IntPair(px,py),bestq));
					IntPair gq = relativeCoordinatesDifference(bestq);
					r[pixelIndex].x = rq.x + gq.x;
					r[pixelIndex].y = rq.y + gq.y;
				}				
			}
		}
		
		//Backward scan
		for (int py = height-2; py>0; py--){//The borders are supposed as background
			for	(int px = width-2; px>0; px--){
			
			int pixelIndex = py*width+px;
			int min = binaryArray[pixelIndex];
			if (min ==0) {continue;}
			
			int bestq = 0;
			for (int qNum = 5; qNum<=8; qNum++){
				IntPair q = qFromP(new IntPair(px,py),qNum);
				int diff = binaryValue(q) + euclideanDistanceDifference(q, qNum);
				
				if (diff < min ){
					min = diff;
					bestq = qNum;
				}
			}
			binaryArray[pixelIndex] = min;
			
			if (bestq != 0){
				IntPair rq = rValue(qFromP(new IntPair(px,py),bestq));
				IntPair gq = relativeCoordinatesDifference(bestq);
				r[py*width+px].x = rq.x + gq.x;
				r[py*width+px].y = rq.y + gq.y;				
			}			
			backwardArray[pixelIndex] = (int)Math.sqrt((double)min);
		}
	}
		
		return backwardScanImage;
		}

		public class IntPair{
			int x;
			int y;
			public IntPair(int x,int y){
				this.x=x;
				this.y=y;
			}
			public IntPair(){
				this.x=0;
				this.y=0;
			}
		
		}
	
	}
