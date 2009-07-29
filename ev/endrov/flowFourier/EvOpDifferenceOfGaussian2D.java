package endrov.flowFourier;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.imageset.EvPixels;

/**
 * Difference of gaussian 2D
 * 
 * Complexity, same as FourierTransform
 */
public class EvOpDifferenceOfGaussian2D extends EvOpSlice1
	{
	private final Number sigma;
	
	public EvOpDifferenceOfGaussian2D(Number sigma)
		{
		this.sigma = sigma;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0], sigma.doubleValue());
		}
	
	public static EvPixels apply(EvPixels p, double sigma)
		{
		//stackHis=new EvOpImageConvertPixel(EvPixelsType.DOUBLE).exec1(stackHis);
		int whis=p.getWidth();
		int hhis=p.getHeight();
		EvPixels kernel1=GenerateSpecialImage.genGaussian2D(sigma, sigma, whis, hhis);
		EvPixels kernel2=GenerateSpecialImage.genGaussian2D(sigma*2, sigma*2, whis, hhis);
		EvPixels kernelDOG=EvOpImageSubImage.minus(kernel1, kernel2);
		return EvOpCircConv2D.apply(kernelDOG,p);
		}
	
	
	
	}