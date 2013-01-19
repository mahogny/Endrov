/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeImageset;


import java.io.File;

import endrov.util.ProgressHandle;
import loci.common.DataTools;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;

/**
 * Image I/O
 */
public class BioformatsSliceIO extends EvImageReader
	{
	private int series;
	private int id;
	private IFormatReader imageReader;
	private Object sourceName;
	private boolean closeReaderOnFinalize;
	public boolean isDicom=false;

	
	private static final boolean debug=false;
	
	/**
	 * Optional: Might return null
	 */
	public File getFile()
		{
		if(sourceName instanceof File)
			return (File)sourceName;
		else
			return null;
		}
	
	public BioformatsSliceIO(IFormatReader imageReader, int series, int id, Object sourceName, boolean closeReaderOnFinalize)
		{
		this.series=series;
		this.sourceName=sourceName;
		this.id=id;
		this.imageReader=imageReader;
		this.closeReaderOnFinalize=closeReaderOnFinalize;
		}

	/*public static boolean isSigned(int formatType)
		{
		return formatType == FormatTools.INT8 || formatType == FormatTools.INT16 || formatType == FormatTools.INT32;
		}*/
	
	/**
	 * Load the image
	 */
	public EvPixels eval(ProgressHandle progh)
		{
		System.out.println("getting series:"+ series + " id: "+id);
		
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
			boolean isFloat = FormatTools.isFloatingPoint(type); //type == FormatTools.FLOAT || type == FormatTools.DOUBLE;
			boolean isLittle = imageReader.isLittleEndian();
			boolean isSigned = FormatTools.isSigned(type);
			Object bfpixels = DataTools.makeDataArray(bytes, bpp, isFloat, isLittle);
			
			
			
			
			//if(debug)
				System.out.println("bpp:"+bpp+" fp:"+isFloat+" islittle:"+isLittle+" signed:"+isSigned+" class:"+bfpixels.getClass()+"   pixeltype:"+FormatTools.getPixelTypeString(type));
			
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
				
				//int len=w*h;
				//if(isSigned)
					//{
					if (q.length > w * h) 
						{
						short[] tmp = q;
						q = new short[w * h];
						System.arraycopy(tmp, 0, q, 0, q.length);
						}
					
					if(isSigned)
						q = DataTools.makeSigned(q);
					
					if(isDicom)
						{
						System.out.println("applying dicom fixes...");
						for(int i=0;i<w*h;i++)
							if(q[i]==30768)
								q[i]=0;
							else
								q[i]+=32768;
	//						if(q[i]==30768)
//								q[i]=0;
						}
					

					if(debug)
						{
						System.out.println("read short[]:");
						for(int i:q)
							System.out.print(" "+i);
						System.out.println();
						}
					
					if(!isSigned)
						{
						//System.out.println("------ not signed!!!");
						
						
						//Must store in an int!
						int[] arri=new int[w*h];
						for(int i=0;i<w*h;i++)
							{
							int value=q[i];
							if(value<0)
								value+=32768*2;
							arri[i]=value;
							}
						return EvPixels.createFromInt(w, h, arri);
						
						
						}
					else
						return EvPixels.createFromShort(w, h, q);
					/*
					}
				else
					{
					return EvPixels.createFromInt(w, h, EvPixels.convertUshortToInt(q, len));
					}*/
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
				
//				System.out.println("first int32 raw: "+q[0]);
				
				if (isSigned) 
					q = DataTools.makeSigned(q);

//				System.out.println("first int32 sign fixed: "+q[0]);

				if(debug)
					{
					System.out.println("read int[]:");
					for(int i:q)
						System.out.print(" "+i);
					System.out.println();
					}

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
			endrov.core.log.EvLog.printError("Failed to read image, sourcename="+sourceName+"  id="+id,e);
			return null;
			}
		}


	public File getRawJPEGData()
		{
		if(sourceName instanceof File)
			return defaultGetRawJPEG((File)sourceName);
		else
			return null;
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
