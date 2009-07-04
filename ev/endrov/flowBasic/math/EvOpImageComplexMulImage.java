package endrov.flowBasic.math;

import endrov.flow.EvOpSlice;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.Tuple;

/**
 * complex A * B
 * @author Johan Henriksson
 *
 */
public class EvOpImageComplexMulImage extends EvOpSlice
	{
	public EvPixels[] exec(EvPixels... p)
		{
		Tuple<EvPixels,EvPixels> ret=EvOpImageComplexMulImage.times(p[0], p[1], p[2], p[3]);
		return new EvPixels[]{ret.fst(),ret.snd()};
		}

	/**
	 * Multiply images. Assumes same size and position
	 */
	public static Tuple<EvPixels,EvPixels> times(EvPixels aReal, EvPixels aImag, EvPixels bReal, EvPixels bImag)
		{
		//Should use the common higher type here
		aReal=aReal.getReadOnly(EvPixelsType.DOUBLE);
		aImag=aImag.getReadOnly(EvPixelsType.DOUBLE);
		bReal=bReal.getReadOnly(EvPixelsType.DOUBLE);
		bImag=bImag.getReadOnly(EvPixelsType.DOUBLE);
	
		int w=aReal.getWidth();
		int h=aReal.getHeight();
	
		double[] aRealPixels=aReal.getArrayDouble();
		double[] aImagPixels=aImag.getArrayDouble();
		double[] bRealPixels=bReal.getArrayDouble();
		double[] bImagPixels=bImag.getArrayDouble();
	
		EvPixels outReal=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outRealPixels=outReal.getArrayDouble();
		EvPixels outImag=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outImagPixels=outImag.getArrayDouble();
	
	
		//   (a+bi)*(c+di)=ac-bd  + i( bc+ad  )
		for(int i=0;i<aRealPixels.length;i++)
			{
			outRealPixels[i]=aRealPixels[i]*bRealPixels[i] - aImagPixels[i]*bImagPixels[i]; 
			outImagPixels[i]=aImagPixels[i]*bRealPixels[i] + aRealPixels[i]*bImagPixels[i]; 
			}
	
		return Tuple.make(outReal,outImag);
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
	
	
	/**
	 * Multiply images. Assumes same size and position. Stores output back in A to conserve memory, the callee
	 * has to make sure no important data is overwritten.
	 * 
	 * a and b must be of type double
	 */
	public static void timesInPlaceDouble(EvPixels aReal, EvPixels aImag, EvPixels bReal, EvPixels bImag)
		{
		double[] aRealPixels=aReal.getArrayDouble();
		double[] aImagPixels=aImag.getArrayDouble();
		double[] bRealPixels=bReal.getArrayDouble();
		double[] bImagPixels=bImag.getArrayDouble();
	
		//   (a+bi)*(c+di)=ac-bd  + i( bc+ad  )
		for(int i=0;i<aRealPixels.length;i++)
			{
			double real=aRealPixels[i]*bRealPixels[i] - aImagPixels[i]*bImagPixels[i];
			double imag=aImagPixels[i]*bRealPixels[i] + aRealPixels[i]*bImagPixels[i];
			aRealPixels[i]=real; 
			aImagPixels[i]=imag; 
			}
		}
	
	/**
	 * Multiply images. Assumes same size and position. Stores output back in A to conserve memory, the callee
	 * has to make sure no important data is overwritten.
	 * 
	 * a and b must be of type double
	 */
	public static void timesInPlaceDouble(EvStack aReal, EvStack aImag, EvStack bReal, EvStack bImag)
		{
		int d=aReal.getDepth();
		
		EvImage[] aRealIm=aReal.getImages();
		EvImage[] aImagIm=aImag.getImages();
		EvImage[] bRealIm=bReal.getImages();
		EvImage[] bImagIm=bImag.getImages();
		
		for(int az=0;az<d;az++)
			timesInPlaceDouble(aRealIm[az].getPixels(), aImagIm[az].getPixels(), bRealIm[az].getPixels(), bImagIm[az].getPixels());
		
		}

	}