package endrov.recording.resolutionConfigWindow;

import net.imglib2.Cursor;
import net.imglib2.algorithm.fft.FourierTransform;
import net.imglib2.algorithm.fft.InverseFourierTransform;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.DevUtil;

import endrov.imageset.EvPixels;

/**
 * Finding displacement between two overlapping images using method by A.
 * Averbuch, Y. Keller, “A Unified Approach To FFT Based Image Registration”
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */
public class ImageDisplacementCorrelation
	{

	/*
	 * public static void main(String[] args) { EvLog.addListener(new
	 * EvLogStdout()); EV.loadPlugins(); try { EvPixels imA = new
	 * EvPixels(ImageIO.read(new File( "/Users/pswadmin/Desktop/b.png")));
	 * EvPixels imB = new EvPixels(ImageIO.read(new File(
	 * "/Users/pswadmin/Desktop/a.png"))); displacement(imA, imB); } catch
	 * (Exception e) { e.printStackTrace(); } System.exit(0); }
	 */
	/**
	 * Finding displacement between two overlapping images using method by A.
	 * Averbuch, Y. Keller, “A Unified Approach To FFT Based Image Registration”
	 */
	public static double[] displacement(EvPixels firstImg, EvPixels secondImg)
		{

		try
			{
			int w = firstImg.getWidth();
			int h = firstImg.getHeight();
			float[] firstFloatImg = firstImg.convertToFloat(true).getArrayFloat();
			float[] secondFloatImg = secondImg.convertToFloat(true).getArrayFloat();
			Img<FloatType> imageA = DevUtil.createImageFromArray(firstFloatImg,
					new long[]
						{ firstImg.getWidth(), firstImg.getHeight() });
			Img<FloatType> imageB = DevUtil.createImageFromArray(secondFloatImg,
					new long[]
						{ secondImg.getWidth(), secondImg.getHeight() });

			if (firstImg.getWidth()!=secondImg.getWidth()
					||firstImg.getHeight()!=secondImg.getHeight())
				throw new RuntimeException("Images of different size");

			// FFT on first image
			final FourierTransform<FloatType, ComplexFloatType> procFFTb = new FourierTransform<FloatType, ComplexFloatType>(
					imageB, new ComplexFloatType());
			if (!(procFFTb.checkInput()&&procFFTb.process()))
				{
				throw new RuntimeException("Cannot compute fourier transform: "
						+procFFTb.getErrorMessage());
				}
			final Img<ComplexFloatType> bFFT = procFFTb.getResult();

			// FFT on second image
			final FourierTransform<FloatType, ComplexFloatType> procFFTa = new FourierTransform<FloatType, ComplexFloatType>(
					imageA, new ComplexFloatType());
			if (!(procFFTa.checkInput()&&procFFTa.process()))
				{
				throw new RuntimeException("Cannot compute fourier transform: "
						+procFFTa.getErrorMessage());
				}
			final Img<ComplexFloatType> aFFT = procFFTa.getResult();

			// complex invert the kernel
			final ComplexFloatType c = new ComplexFloatType();
			final ComplexFloatType d = new ComplexFloatType();
			final Cursor<ComplexFloatType> cursorKernel = bFFT.cursor();
			final Cursor<ComplexFloatType> cursorImage = aFFT.cursor();
			final Cursor<ComplexFloatType> cursorCorr = bFFT.cursor();
			while (cursorKernel.hasNext())
				{
				final ComplexFloatType F1 = cursorKernel.next();
				final ComplexFloatType F2 = cursorImage.next();
				final ComplexFloatType C = cursorCorr.next();

				c.set(F1);
				d.set(F2);
				d.complexConjugate();
				c.mul(d);
				c.mul(1.0f/c.getPowerFloat());

				C.set(c);
				}

			// Compute corr in spatial space
			final InverseFourierTransform<FloatType, ComplexFloatType> cIFFT = new InverseFourierTransform<FloatType, ComplexFloatType>(
					bFFT, new FloatType());
			final Img<FloatType> cInverse;
			if (cIFFT.checkInput()&&cIFFT.process())
				cInverse = cIFFT.getResult();
			else
				{
				System.err.println("Cannot compute inverse fourier transform: "
						+cIFFT.getErrorMessage());
				return null;
				}

			// Find dx,dy
			final Cursor<FloatType> loc_cursor = cInverse.localizingCursor();
			float max = 0;
			FloatType newVal;
			double[] maxpos = new double[2];

			while (loc_cursor.hasNext())
				{
				newVal = loc_cursor.next();
				if (max<newVal.get())
					{
					max = newVal.get();
					loc_cursor.localize(maxpos);

					}
				}

			double dx = maxpos[0];
			double dy = maxpos[1];

			// Wrap around image for negative displacements
			if (dx>w/2)
				{
				dx -= w;
				System.out.println("dx problem");
				}
			if (dy>h/2)
				{
				dy -= h;
				System.out.println("dy problem");
				}

			maxpos[0] = dx;
			maxpos[1] = dy;

			// add pixels into a float array
			int k = 0;
			float[] cInverseFA = new float[(int) cInverse.size()];
			final Cursor<FloatType> f_cursor = cInverse.localizingCursor();
			while (f_cursor.hasNext())
				{
				cInverseFA[k] = f_cursor.next().get();
				k++;
				}

			return maxpos;
			}
		catch (IncompatibleTypeException e)
			{
			throw new RuntimeException(e.getMessage());
			}

		}

	}
