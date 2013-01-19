/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imglib.evop;

import net.imglib2.Cursor;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.exception.ImgLibException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import endrov.flow.EvOpStack1;
import endrov.flowBasic.images.EvOpImageConvertPixel;
import endrov.imglib.EvStackAdapter;
import endrov.imglib.EvStackImgFactory;
import endrov.imglib.FloatEvStack;
import endrov.typeImageset.EvPixelsType;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Scale image
 * 
 * TODO no reason it need to work directly on stack-level. possible to change? or make lazy
 * 
 * 
 */
public class EvOpScaleImage extends EvOpStack1
	{
	private final double scaleX, scaleY;
	
	public EvOpScaleImage(double scaleX, double scaleY)
		{
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		}

	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return apply(p[0], scaleX, scaleY);
		}
	
	
	public static EvStack apply(EvStack in, double scaleX, double scaleY)
		{
		in=new EvOpImageConvertPixel(EvPixelsType.FLOAT).exec1(null, in);
		
		ImgPlus<FloatType> imp=EvStackAdapter.wrapEvStack(in);
		
		Img<FloatType> image=imp.getImg();
	
//		new ScaleAreaAveraging2d<FloatType, FloatType>()
	
		
		
		
		
		// Define the transformation model
//		RigidModel2D model = new RigidModel2D();
	//	model.set( (float)Math.toRadians( 15 ),  0, 0 );

		// Define the transformation model
		//TranslationModel2D model = new TranslationModel2D();
		//model.set( 10.1f, -12.34f );

			// compute the gradient on the image
		/*
		Matrix3x3 m=new Matrix3x3();

		Matrix
		
		AffineTransform2D t2=new AffineTransform2D();

		
		AffineTransform t=new AffineTransform(m
				);
		t.scale(scaleX);
		*/
//		AffineModel2D model=new AffineModel();
//		model.set(t);
		
//		model.
		
//		ImageTransform<FloatType> t=new ImageTransform<FloatType>(image, model, new NLinearInterpolatorFactory<FloatType>());
		
//			Img<FloatType> transformed = ImageTransform;
//			.transform( image );
	
			// show the new Img that contains the gradient

		
		
		
		
		
		NLinearInterpolatorFactory< FloatType > factory2 = new NLinearInterpolatorFactory< FloatType >();

		RealRandomAccessible< FloatType > interpolant2 = Views.interpolate(
			Views.extendMirrorSingle( image ), factory2 );

		// define the area in the interpolated image
		double[] min = new double[]{ 0,0 };
		double[] max = new double[]{ in.getWidth(), in.getHeight() };

		FinalRealInterval interval = new FinalRealInterval( min, max );

		Img<FloatType> imgout= magnify( interpolant2, interval, new EvStackImgFactory< FloatType >(), scaleX ) ;
		
//		Img<FloatType> imgout=Gauss.toFloat(8, image);
		//Img<FloatType> imgout=Gauss.toFloat(8, image);
		
		FloatEvStack<FloatType> s2=(FloatEvStack<FloatType>)imgout;

		try
			{
			EvStack out=
					s2.getEvStack();
			
			out.copyMetaFrom(in);
			out.setRes(
					in.getRes().x*scaleX,
					in.getRes().y*scaleY,
					in.getRes().z
					);
			return out;
			}
		catch (ImgLibException e)
			{
			e.printStackTrace();
			throw new RuntimeException();
			}

		
		
		
		
		
		
		
		
		
		

				
			
			
	
			
			
			
		
		
		}
	
	
	
	///From https://raw.github.com/imagej/imglib/master/imglib2/examples/src/main/java/Example7.java
	
	/**
	 * Compute a magnified version of a given real interval
	 *
	 * @param source - the input data
	 * @param interval - the real interval on the source that should be magnified
	 * @param factory - the image factory for the output image
	 * @param magnification - the ratio of magnification
	 * @return - an Img that contains the magnified image content
	 */
	public static < T extends Type< T > > Img< T > magnify( RealRandomAccessible< T > source,
		RealInterval interval, ImgFactory< T > factory, double magnification)
	{
		int numDimensions = interval.numDimensions();

		// compute the number of pixels of the output and the size of the real interval
		long[] pixelSize = new long[ numDimensions ];
		double[] intervalSize = new double[ numDimensions ];

		for ( int d = 0; d < numDimensions; ++d )
		{
			intervalSize[ d ] = interval.realMax( d ) - interval.realMin( d );
			pixelSize[ d ] = Math.round( intervalSize[ d ] * magnification ) + 1;
		}

		// create the output image
		Img< T > output = factory.create( pixelSize, Util.getTypeFromRealRandomAccess( source ) );

		// cursor to iterate over all pixels
		Cursor< T > cursor = output.localizingCursor();

		// create a RealRandomAccess on the source (interpolator)
		RealRandomAccess< T > realRandomAccess = source.realRandomAccess();

		// the temporary array to compute the position
		double[] tmp = new double[ numDimensions ];

		// for all pixels of the output image
		while ( cursor.hasNext() )
		{
			cursor.fwd();

			// compute the appropriate location of the interpolator
			for ( int d = 0; d < numDimensions; ++d )
				tmp[ d ] = cursor.getDoublePosition( d ) / output.realMax( d ) * intervalSize[ d ]
						+ interval.realMin( d );

			// set the position
			realRandomAccess.setPosition( tmp );

			// set the new value
			cursor.get().set( realRandomAccess.get() );
		}

		return output;
	}
	
	}