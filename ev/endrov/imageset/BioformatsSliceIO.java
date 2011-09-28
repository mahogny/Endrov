/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;


import endrov.util.ProgressHandle;
import loci.common.DataTools;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;

/**
 * Image I/O
 */
public class BioformatsSliceIO extends EvIOImage
	{
	private int series;
	private int id;
	private IFormatReader imageReader;
	private Object sourceName;
	private boolean closeReaderOnFinalize;

	public BioformatsSliceIO(IFormatReader imageReader, int series, int id, Object sourceName, boolean closeReaderOnFinalize)
		{
		this.series=series;
		this.sourceName=sourceName;
		this.id=id;
		this.imageReader=imageReader;
		this.closeReaderOnFinalize=closeReaderOnFinalize;
		}

	
	/**
	 * Load the image
	 */
	public EvPixels eval(ProgressHandle progh)
		{
		try
			{
			byte[] bytes;
			synchronized(this)
				{
				imageReader.setSeries(series);
				bytes=imageReader.openBytes(id);
				}
			//FormatTools, DOUBLE, FLOAT, INT16, INT32, INT8, UINT16, UINT32, UINT8
			
			
			int w=imageReader.getSizeX();
			int h=imageReader.getSizeY();
			//DataInputStream di=new DataInputStream(new ByteArrayInputStream(bytes));
			
			int type=imageReader.getPixelType();
			int bpp=FormatTools.getBytesPerPixel(type);
			boolean isFloat = type == FormatTools.FLOAT || type == FormatTools.DOUBLE;
			boolean isLittle = imageReader.isLittleEndian();
			boolean isSigned = type == FormatTools.INT8 || type == FormatTools.INT16 || type == FormatTools.INT32;
			Object bfpixels = DataTools.makeDataArray(bytes, bpp, isFloat, isLittle);
			
			
			
			System.out.println("bpp:"+bpp+" fp:"+isFloat+" islittle:"+isLittle+" signed:"+isSigned+" class:"+bfpixels.getClass());
			
			//Much of this code modified from bioformats IJ-plugin. I deem it functional and hence not copyrightable
			//https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/plugins/Util.java?rev=4289
			//unsigned values have to be upconverted to fit into signed
			if (bfpixels instanceof byte[]) 
				{
				byte[] q = (byte[]) bfpixels;
				int len=w*h;
				if(isSigned)
					{
					if (q.length > w * h) 
						{
						byte[] tmp = q;
						q = new byte[w * h];
						System.arraycopy(tmp, 0, q, 0, q.length);
						}
					q = DataTools.makeSigned(q);
					return EvPixels.createFromUByte(w, h, q);
					}
				else
					return EvPixels.createFromShort(w, h, EvPixels.convertUbyteToShort(q, len));
				}
			else if (bfpixels instanceof short[])
				{
				short[] q = (short[]) bfpixels;
				
				int len=w*h;
				if(isSigned)
					{
					if (q.length > w * h) 
						{
						short[] tmp = q;
						q = new short[w * h];
						System.arraycopy(tmp, 0, q, 0, q.length);
						}
					q = DataTools.makeSigned(q);
					return EvPixels.createFromShort(w, h, q);
					}
				else
					{
					return EvPixels.createFromInt(w, h, EvPixels.convertUshortToInt(q, len));
					}
				/*
				if (q.length > w * h) 
					{
					short[] tmp = q;
					q = new short[w * h];
					System.arraycopy(tmp, 0, q, 0, q.length);
					}

				if (isSigned) 
					q = DataTools.makeSigned(q);

				
				//TODO unsigned - upconvert
				return EvPixels.createFromShort(w, h, q);*/
				}
			else if (bfpixels instanceof int[])
				{
				int[] q = (int[]) bfpixels;
				if (q.length > w * h) 
					{
					int[] tmp = q;
					q = new int[w * h];
					System.arraycopy(tmp, 0, q, 0, q.length);
					}

/*
				System.out.println("byte: ");
				for(int i:bytes)
					System.out.print(" "+i);
				System.out.println();

				System.out.println("int: ");
				for(int i:q)
					System.out.print(" "+i);
				System.out.println();
				*/
				if (isSigned) 
					q = DataTools.makeSigned(q);

				/*
				System.out.println("signed int: ");
				for(int i:q)
					System.out.print(" "+i);
				System.out.println();*/

				//unsigned? - screw it. would have to convert to float/double, evil.
				return EvPixels.createFromInt(w, h, q);
				}
			else if (bfpixels instanceof float[])
				{
				float[] q = (float[]) bfpixels;
				if (q.length > w * h) 
					{
					float[] tmp = q;
					q = new float[w * h];
					System.arraycopy(tmp, 0, q, 0, q.length);
					}
				return EvPixels.createFromFloat(w, h, q);
				}
			else if (bfpixels instanceof double[]) 
				{
				double[] q = (double[]) bfpixels;
				if (q.length > w * h) 
					{
					double[] tmp = q;
					q = new double[w * h];
					System.arraycopy(tmp, 0, q, 0, q.length);
					}
				return EvPixels.createFromDouble(w, h, q);
				}
			else
				{
				System.out.println("Bioformats returns unrecognized format");
				System.out.println(bfpixels.getClass()+"  "+isSigned);
				return null;
				}

			}
		catch(Exception e)
			{
			endrov.ev.EvLog.printError("Failed to read image, sourcename="+sourceName+"  id="+id,e);
			return null;
			}
		}

	

	@Override
	protected void finalize() throws Throwable
		{
		super.finalize();
		if(imageReader!=null && closeReaderOnFinalize)
			{
			//System.out.println("Closed bioformatsSliceIO for \""+sourceName+"\"");
			imageReader.close();
			imageReader=null;
			}
		}

	}
