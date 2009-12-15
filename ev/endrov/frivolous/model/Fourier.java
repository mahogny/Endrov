package endrov.frivolous.model;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

public class Fourier
	{
	int w, h;
	FloatFFT_2D fourierTransform;

	public Fourier(int w, int h)
		{
		this.w = w;
		this.h = h;
		fourierTransform = new FloatFFT_2D(w, h);
		}

	public ComplexArray forward(ComplexArray complex, boolean real)
		{
		float[] tmp;
		if (real)
			{
			tmp = ComplexLib.getRealArrayFirstHalf(complex);
			fourierTransform.realForwardFull(tmp);
			}
		else
			{
			tmp = ComplexLib.getCombinedArray(complex);
			fourierTransform.complexForward(tmp);
			}
		return ComplexLib.getComplexArray(tmp,w,h);
		}

	public ComplexArray backward(ComplexArray complex)
		{
		float[] tmp = ComplexLib.getCombinedArray(complex);
		fourierTransform.complexInverse(tmp, true);
		return ComplexLib.getComplexArray(tmp,w,h);
		}
	}
