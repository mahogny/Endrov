package endrov.flowThreshold;


import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Reduce the number of colors by quantizing them to a given number of levels. Some algorithms are O(numberOfColors)
 * and needs this prior to processing
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpQuantize2D extends EvOpSlice1
	{
	private final int numLevels;
	
	public EvOpQuantize2D(int numLevels)
		{
		this.numLevels = numLevels;
		}

	public EvPixels exec1(EvPixels... p)
		{
		EvPixels in=p[0];
		
		double[] arrIn=in.convertToDouble(true).getArrayDouble();
		EvPixels ret=new EvPixels(EvPixelsType.DOUBLE, in.getWidth(),in.getHeight());
		double[] arrOut=ret.getArrayDouble();

		double minVal=arrIn[0];
		double maxVal=arrIn[0];

		//Find current range
		for(double d:arrIn)
			{
			if(d<minVal)
				minVal=d;
			if(d>maxVal)
				maxVal=d;
			}
		double range=maxVal-minVal;
		double mul1=numLevels/range;
		double mul2=range/numLevels;
		
		for(int i=0;i<arrIn.length;i++)
			{
			double d=arrIn[i];
			arrOut[i]=Math.round((d-minVal)*mul1) * mul2 + minVal; 
			}
	
		return ret;
		}
	
	}