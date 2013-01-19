package endrov.imglib;

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


import endrov.typeImageset.EvPixelsType;
import endrov.typeImageset.EvStack;
import net.imglib2.exception.ImgLibException;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.NativeType;

/**
 * {@link ImagePlusImg} for byte-stored data.
 *
 * @author Funke
 * @author Preibisch
 * @author Saalfeld
 * @author Schindelin
 * @author Jan Funke
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Johannes Schindelin
 * @author Johan Henriksson
 */
public class ShortEvStack< T extends NativeType< T > > extends EvStackImg< T, ShortArray >
{
	final EvStack imp;

	public ShortEvStack( final long[] dim, final int entitiesPerPixel )
	{
		super( dim, entitiesPerPixel );

		if ( entitiesPerPixel == 1 )
		{
		
			imp=new EvStack();
			imp.allocate(width, height, depth, EvPixelsType.SHORT);
		
			mirror.clear();
//			for ( int t = 0; t < frames; ++t )
				for ( int z = 0; z < depth; ++z )
//					for ( int c = 0; c < channels; ++c )
					mirror.add( new ShortArray( ( short[] )imp.getPlane(z).getPixels().getArrayShort() ) );
//						mirror.add( new ByteArray( ( byte[] )imp.getStack().getProcessor( imp.getStackIndex( c + 1, z + 1 , t + 1 ) ).getPixels() ) );
		}
		else
		{
			imp = null;

			mirror.clear();
			for ( int i = 0; i < numSlices; ++i )
				mirror.add( new ShortArray( width * height * entitiesPerPixel ) );
		}
	}

	public ShortEvStack( final EvStack imp )
	{
		super(
				imp.getWidth(),
				imp.getHeight(),
				1,//imp.getNSlices(),
				1,//imp.getNFrames(),
				1,//imp.getNChannels(),
				1 );

		this.imp = imp;

		mirror.clear();
//		for ( int t = 0; t < frames; ++t )
			for ( int z = 0; z < depth; ++z )
	//			for ( int c = 0; c < channels; ++c )
				mirror.add( new ShortArray( ( short[] )imp.getPlane(z).getPixels().getArrayShort() ) );
//					mirror.add( new ByteArray( ( byte[] )imp.getStack().getProcessor( imp.getStackIndex( c + 1, z + 1 , t + 1 ) ).getPixels() ) );
	}

	/**
	 * This has to be overwritten, otherwise two different instances exist (one in the imageplus, one in the mirror)
	 */
	@Override
	public void setPlane( final int no, final ShortArray plane )
	{
		System.arraycopy( plane.getCurrentStorageArray(), 0, mirror.get( no ).getCurrentStorageArray(), 0, plane.getCurrentStorageArray().length );
	}

	@Override
	public void close()
	{
//		if ( imp != null )
//			imp.close();
	}

	@Override
	public EvStack getEvStack() throws ImgLibException
	{
		if ( imp == null )
			throw new ImgLibException( this, "has no ImagePlus instance, it is not a standard type of ImagePlus (" + entitiesPerPixel + " entities per pixel)" );
		return imp;
	}
}