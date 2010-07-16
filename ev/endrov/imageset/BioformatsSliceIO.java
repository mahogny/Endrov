/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.io.File;

import loci.common.DataTools;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.out.TiffWriter;

/**
 * Image I/O
 */
public class BioformatsSliceIO implements EvIOImage
	{
	private int id;
	//private Integer subid;
	private IFormatReader imageReader;
	private String sourceName;
	private boolean closeReaderOnFinalize;
	
	public BioformatsSliceIO(IFormatReader imageReader, int id, Integer subid, String sourceName, boolean closeReaderOnFinalize)
		{
		this.sourceName=sourceName;
		this.id=id;
		this.imageReader=imageReader;
		this.closeReaderOnFinalize=closeReaderOnFinalize;
		//this.subid=subid;
		}

	
	public String sourceName()
		{
		return sourceName;
		}

	
	
	/**
	 * Load the image
	 */
	public EvPixels loadJavaImage()
		{
		try
			{
			byte[] bytes=imageReader.openBytes(id);
			
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
			
			//System.out.println("bpp "+bpp+" fp "+isFloat+" islittle "+isLittle);
			
			//System.out.println(bfpixels.getClass()+"  "+isSigned);
			
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
					return EvPixels.createFromInt(w, h, EvPixels.convertUshortToInt(q, len));
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

				/*for(int i:q)
					System.out.println(" "+i);
				System.out.println();*/
				
				if (isSigned) 
					q = DataTools.makeSigned(q);

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
			endrov.ev.EvLog.printError("Failed to read image "+sourceName(),e);
			return null;
			}
		}

	

	/**
	 * Save single image as TIFF. 
	 */
	public static void saveImageAsTiff(EvPixels p, File file)
		{
		//For Bio-formats usage, see
		//loci-plugins/src/loci/plugins/exporter/Exporter.java
		//System.out.println(file);
		
		MetadataStore store = MetadataTools.createOMEXMLMetadata();
		store.createRoot();
		
		store.setPixelsSizeX(p.getWidth(), 0, 0);
		store.setPixelsSizeY(p.getHeight(), 0, 0);
		store.setPixelsSizeZ(1, 0, 0);
		store.setPixelsSizeC(1, 0, 0);
		store.setPixelsSizeT(1, 0, 0);
		
		//Convert to byte array
		int ptype;
		byte[] barr;
		if(p.getType()==EvPixelsType.SHORT)
			{
			short[] array=p.getArrayShort();
			ptype=FormatTools.INT16;
			barr=DataTools.shortsToBytes(array, true);
			}
		else if(p.getType()==EvPixelsType.FLOAT)
			{
			float[] array=p.getArrayFloat();
			ptype=FormatTools.FLOAT;
			barr=DataTools.floatsToBytes(array, true);
			}
		else
			//TODO Double
			{
			p=p.convertToInt(true);
			int[] array=p.getArrayInt();
			ptype=FormatTools.INT32;
			barr=DataTools.intsToBytes(array, true);
			}
		
		store.setPixelsPixelType(FormatTools.getPixelTypeString(ptype),0,0);
		
		store.setPixelsBigEndian(Boolean.FALSE, 0, 0);
		store.setPixelsDimensionOrder("XYCZT", 0, 0);
		store.setLogicalChannelSamplesPerPixel(new Integer(1), 0, 0);
		
		MetadataRetrieve retrieve = MetadataTools.asRetrieve(store);
		//If file is not deleted first then the TIFF-writer will append on it
		file.delete();
		TiffWriter writer=new TiffWriter();
		writer.setMetadataRetrieve(retrieve);
		try
			{
			writer.setCompression(TiffWriter.COMPRESSION_LZW);
			//writer.setCompression(TiffWriter.COMPRESSION_UNCOMPRESSED);
			writer.setId(file.getPath());
			writer.saveBytes(barr, true);
			writer.close();
			
			//for(int i=0;i<barr.length;i+=4)
			//	System.out.println("  "+barr[i]+" "+barr[i+1]+" "+barr[i+2]+" "+barr[i+3]);
			}
		catch (Exception e)
			{
			e.printStackTrace();
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
