
/*
 * #%L
 * Modified from imglib2. Modifications (C) Johan Henriksson
 * %%
 * Copyright (C) 2009 - 2012 Stephan Preibisch, Stephan Saalfeld, Tobias
 * Pietzsch, Albert Cardona, Barry DeZonia, Curtis Rueden, Lee Kamentsky, Larry
 * Lindsey, Johannes Schindelin, Christian Dietz, Grant Harris, Jean-Yves
 * Tinevez, Steffen Jaensch, Mark Longair, Nick Perry, and Jan Funke.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package endrov.imglib;

import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import net.imglib2.Cursor;
import net.imglib2.converter.Converter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

/**
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Johan Henriksson
 */
public class EvStackAdapter
{
	@SuppressWarnings( "unchecked" )
	public static < T extends NumericType< T > & NativeType< T > > EvStackImg< T, ? > wrap( final EvStack imp )
	{
		return ( EvStackImg< T, ? > ) wrapLocal( imp );
	}

	public static EvStackImg<?,?> wrapReal( final EvStack imp )
	{
		return wrapLocalReal( imp );
	}

	public static EvStackImg<?,?> wrapNumeric( final EvStack imp )
	{
		return wrapLocal( imp );
	}

	public static < T extends NumericType< T > & NativeType< T > > ImgPlus< T > wrapEvStack( final EvStack imp )
	{
		Img< T > img = wrap( imp );
		ImgPlus< T > image = new ImgPlus< T >( img );

		// set calibration
		setCalibrationFromEvStack( image, imp );

		// set title
//		image.setName( imp.getTitle() );

		// set axes
		setAxesFromEvStack( image, imp );

		return image;
	}
	

	protected static EvStackImg< ?, ? > wrapLocal( final EvStack imp )
	{
		switch( imp.getPixelFormat() )
		{
		case UBYTE : 
		{
			return wrapByte( imp );
		}
		case SHORT: 
		{
			return wrapShort( imp );
		}
		case INT: 
		{
			return wrapInt( imp );
		}
		case FLOAT: 
		{
			return wrapFloat( imp );
		}
		default :
		{
			throw new RuntimeException("This type is not supported: "+imp.getPixelFormat());
		}
		}
	}

	protected static EvStackImg< ?, ? > wrapLocalReal( final EvStack imp )
	{
		switch( imp.getPixelFormat() )
		{		
		case UBYTE : 
		{
			return wrapByte( imp );
		}
		case SHORT : 
		{
			return wrapShort( imp );
		}
		case INT : 
		{
			return wrapInt( imp );
		}
		case FLOAT : 
		{
			return wrapFloat( imp );
		}
		default :
		{
			throw new RuntimeException("Only 8, 16 and 32-bit supported!");
		}
		}
	}

	protected static < T extends NumericType< T > & NativeType< T > > void setAxesFromEvStack( final ImgPlus<T> image, final EvStack imp ) 
	{
	/*

		int currentDim = 2;

		if (imp.getNChannels() > 1) {
			image.setAxis(Axes.CHANNEL, currentDim);
			currentDim++;
		}

		if (imp.getNSlices() > 1) {
			image.setAxis(Axes.Z, currentDim);
			currentDim++;
		}

		if (imp.getNFrames() > 1) {
			image.setAxis(Axes.TIME, currentDim);
		}
*/
	}


	/**
	 * TODO!!!!!!!!!!!!!
	 */
	protected static < T extends NumericType< T > & NativeType< T > > void setCalibrationFromEvStack( final ImgPlus<T> image, final EvStack imp ) 
	{
		final int d = image.numDimensions();
		final float [] spacing = new float[d];

		for( int i = 0; i < d; ++i )
			spacing[i] = 1f;

		spacing[0]=imp.getWidth();
		spacing[1]=imp.getHeight();

//		final Calibration c = new ;//imp.getCalibration();
		/*
		final Calibration c = imp.getCalibration();

		// Fill out calibration array. We must make sure that the element
		// matches the dimension; the resulting ImgPlus skips singleton dimensions. 
		if( c != null ) 
		{
			if( d >= 1 )
				spacing[0] = (float)c.pixelWidth;

			if( d >= 2 )
				spacing[1] = (float)c.pixelHeight;

			// Extra dimensions. We must take  care of the dimensions order and
			// of singleton dimensions. 
			int currentDim = 2;

			if (imp.getNChannels() > 1) {
				spacing[currentDim] = 1;
				currentDim++;
			}

			if (imp.getNSlices() > 1) {
				spacing[currentDim] = (float) c.pixelDepth;
				currentDim++;
			}

			if (imp.getNFrames() > 1) {
				spacing[currentDim] = (float) c.frameInterval;
			}

		}
		*/

//		image.setCalibration( spacing );
	}

	public static ByteEvStack<UnsignedByteType> wrapByte( final EvStack imp )
	{
		if ( imp.getPixelFormat() != EvPixelsType.UBYTE)
			return null;

		final ByteEvStack< UnsignedByteType > container = new ByteEvStack< UnsignedByteType >( imp );

		// create a Type that is linked to the container
		final UnsignedByteType linkedType = new UnsignedByteType( container );

		// pass it to the NativeContainer
		container.setLinkedType( linkedType );

		return container;
	}

	public static ShortEvStack<UnsignedShortType> wrapShort( final EvStack imp )
	{
		if ( imp.getPixelFormat() != EvPixelsType.SHORT)
			return null;

		final ShortEvStack< UnsignedShortType > container = new ShortEvStack< UnsignedShortType >( imp );

		// create a Type that is linked to the container
		final UnsignedShortType linkedType = new UnsignedShortType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;						
	}

	public static IntEvStack<IntType> wrapInt( final EvStack imp )
	{
		if ( imp.getPixelFormat() != EvPixelsType.INT)
			return null;

		final IntEvStack<IntType> container = new IntEvStack<IntType>( imp );

		// create a Type that is linked to the container
		final IntType linkedType = new IntType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;				
	}	


	public static FloatEvStack<FloatType> wrapFloat( final EvStack imp )
	{
		if ( imp.getPixelFormat() != EvPixelsType.FLOAT)
			return null;

		final FloatEvStack<FloatType> container = new FloatEvStack<FloatType>( imp );

		// create a Type that is linked to the container
		final FloatType linkedType = new FloatType( container );

		// pass it to the DirectAccessContainer
		container.setLinkedType( linkedType );

		return container;				
	}	

	
	public static Img< FloatType > convertFloat( final EvStack imp )
	{

		switch( imp.getPixelFormat() )
		{		
		case UBYTE : 
		{
			return convertToFloat( wrapByte( imp ), new NumberToFloatConverter<UnsignedByteType>() );
		}
		case SHORT : 
		{
			return convertToFloat( wrapShort( imp ), new NumberToFloatConverter<UnsignedShortType>() );
		}
		case FLOAT : 
		{
			return wrapFloat( imp );
		}
		case INT : 
		{
		return convertToFloat( wrapInt( imp ), new NumberToFloatConverter<IntType>() );
		}
		default :
		{
			throw new RuntimeException("Only 8, 16, 32-bit and RGB supported!");
		}
		}
	}


	static private class NumberToFloatConverter< T extends ComplexType< T > > implements Converter< T, FloatType >
	{
//		@Override
		public void convert(final T input, final FloatType output) {
			output.setReal( input.getRealFloat() );
		}		
	}

	protected static < T extends Type< T > > Img< FloatType > convertToFloat(
			final Img< T > input, final Converter< T, FloatType > c )
			{		
		final EvStackImg< FloatType, ? > output = new EvStackImgFactory< FloatType >().create( input, new FloatType() );

		final Cursor< T > in = input.cursor();
		final Cursor< FloatType > out = output.cursor();

		while ( in.hasNext() )
		{
			in.fwd();
			out.fwd();

			c.convert(in.get(), out.get());
		}

		return output;
			}
}