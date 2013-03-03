/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeImageset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

import loci.common.DataTools;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.MetadataTools;
import loci.formats.in.TiffReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import endrov.core.log.EvLog;
import endrov.util.ProgressHandle;
import endrov.util.io.EvFileUtil;

/**
 * Fast image I/O using whatever libraries are available
 * @author Johan Henriksson
 *
 */
public class EvCommonImageIO
	{
	
	/**
	 * Load image file
	 */
	public static EvPixels loadImagePlane(File file, Integer z)
		{
		try
			{
			String fend=EvFileUtil.fileEnding(file);
			fend=fend.toLowerCase();
			//Use JAI if possible, it can be assumed to be very fast
			
			if(fend!=null)
				if(fend.equals("jpg") || fend.equals("jpeg") || fend.equals("png"))
					{
					BufferedImage bim;
					try
						{
						bim = ImageIO.read(file);
						if(bim==null)
							return null;
						else
							return new EvPixels(bim);
						}
					catch (Exception e)
						{
						System.out.println("Can't read "+file);
						e.printStackTrace();
						return null;
						}
					//System.out.println("bim   "+bim);
					}
			
			//Rely on Bioformats in the worst case. Use the most stupid reader available, or bio-formats might attempt
			//detecting a special format
			IFormatReader reader;
			if(fend.equals("tiff") || fend.equals("tif"))
				{
				TiffReader tr=new TiffReader();
				tr.setGroupFiles(false);
				reader=tr;
				}
			else
				{
				reader=new ImageReader();
				}
			//reader.setAllowOpenFiles(false);  //for scifio

			int id=z==null?0:z;
			reader.setId(file.getAbsolutePath());
			return new BioformatsSliceIO(reader,0,id,file, true).get(new ProgressHandle());
			
			
			}
		catch (FormatException e)
			{
			EvLog.printError("Bioformats failed to read image "+file, null);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		return null;
		}
	

	/**
	 * Get file with TIFF ending
	 */
	private static File getTiffFileName(File file)
		{
		String fend=EvFileUtil.fileEnding(file);
		String fname=file.getName();
		fname=fname.substring(0,fname.length()-fend.length())+"tiff";
		return new File(file.getParentFile(),fname);
		}
	
	
	/**
	 * Check if the image can be stored in an 8-bit file
	 */
	private static boolean fitsIn8bit(EvPixels p)
		{
		/*if(p.getType()==EvPixelsType.AWT)
			return true;
		if(!p.getType().isIntegral())
			return false;*/
		
		p=p.convertToInt(true);
		int[] arrayI=p.getArrayInt();

		//TODO this can be made more efficient

		//p=p.convertToShort(true);
		//short[] arrayI=p.getArrayShort();
		
		for(int d:arrayI)
			if(d>255 || d<0)
				return false;
		
		return true;
		}
	
	/**
	 * Save image to disk
	 * @return The actual filename used
	 */
	public static File saveImagePlane(EvPixels p, File file, int compression)
		{
		if(p.getType()==EvPixelsType.AWT)
			saveImagePlane(p.quickReadOnlyAWT(), file, compression);
		else if(p.getType().isIntegral())    
			{
			//Integers
			if(fitsIn8bit(p))
				saveImagePlane(p.quickReadOnlyAWT(), file, compression);
			else
				{
				file=getTiffFileName(file);
				EvCommonImageIO.saveImageAsTiffUsingBioformats(p, file);
				}
			}
		else 
			{
			//Floats
			p=p.convertToFloat(true);
			file=getTiffFileName(file);
			EvCommonImageIO.saveImageAsTiffUsingBioformats(p, file);
			}
		return file;
		}

	/**
	 * Save image to disk; it is an AWT image so gray-scale 8-bit
	 */
	public static void saveImagePlane(BufferedImage im, File file, int compression)
		{
		try
			{
			String fend=EvFileUtil.fileEnding(file);
			//Use JAI if possible, it can be assumed to be very fast
			if(fend!=null)
				{
				if(fend.equals("jpg") || fend.equals("jpeg"))
					{
					Iterator<javax.imageio.ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
					javax.imageio.ImageWriter writer = iter.next();
					ImageWriteParam iwp = writer.getDefaultWriteParam();
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					iwp.setCompressionQuality((float)(compression/100.0));

					FileImageOutputStream output = new FileImageOutputStream(file);
					writer.setOutput(output);
					IIOImage ioimage = new IIOImage(im, null, null);
					writer.write(null, ioimage, iwp);
					//ImageIO.write(im, "jpeg", file);
					return;
					}
				else if(fend.equals("png"))
					{
					ImageIO.write(im, "png", file);
					return;
					}
				}
			
			//Rely on Bioformats in the worst case. 
			//IT DOES NOT SUPPORT JPEG2000!
			ImageWriter writer=new ImageWriter();
			writer.setId(file.getAbsolutePath());
			/*			String[] compTypes=writer.getCompressionTypes();
			for(String s:compTypes)
				System.out.println(s);
			writer.setCompression(arg0)*/
//			writer.saveImage(im, true);
			System.out.println("Image could not be saved, no JAI!");
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		catch (FormatException e)
			{
			e.printStackTrace();
			}
		}


	/**
	 * Save single image as TIFF. 
	 */
	public static void saveImageAsTiffUsingBioformats(EvPixels p, File file)
		{
		try
			{
	
			// http://loci.wisc.edu/trac/java/browser/trunk/components/bio-formats/utils/MinimumWriter.java
	
			ServiceFactory factory = new ServiceFactory();
			OMEXMLService service = factory.getInstance(OMEXMLService.class);
			IMetadata store = service.createOMEXMLMetadata();
	
			store.createRoot();
			store.setImageID("Image:0", 0);
			store.setPixelsID("Pixels:0", 0);
	
			store.setPixelsSizeX(new PositiveInteger(p.getWidth()), 0);
			store.setPixelsSizeY(new PositiveInteger(p.getHeight()), 0);
			store.setPixelsSizeZ(new PositiveInteger(1), 0);
			store.setPixelsSizeC(new PositiveInteger(1), 0);
			store.setPixelsSizeT(new PositiveInteger(1), 0);
			store.setChannelID("Channel:0:0", 0, 0);
			store.setChannelSamplesPerPixel(new PositiveInteger(1), 0, 0);
	
			boolean isLittleEndian=true; //what is optimal?  
			store.setPixelsBinDataBigEndian(!isLittleEndian, 0, 0);
	
			//TODO treat values as signed
			
			//Convert to byte array
			int bfPixelFormat;
			byte[] barr;
			if(p.getType()==EvPixelsType.SHORT)
				{
				short[] array=p.getArrayShort();
				bfPixelFormat=FormatTools.UINT16;
				barr=DataTools.shortsToBytes(array, isLittleEndian);
				}
			else if(p.getType()==EvPixelsType.FLOAT)
				{
				float[] array=p.getArrayFloat();
				bfPixelFormat=FormatTools.FLOAT;
				barr=DataTools.floatsToBytes(array, isLittleEndian);
				}
			else if(p.getType()==EvPixelsType.DOUBLE)
				{
				double[] array=p.getArrayDouble();
				bfPixelFormat=FormatTools.DOUBLE;
				barr=DataTools.doublesToBytes(array, isLittleEndian);
				}			
			else  ////// Everything else, use INT
				{
				p=p.convertToInt(true);
				int[] array=p.getArrayInt();
				bfPixelFormat=FormatTools.UINT32;
				barr=DataTools.intsToBytes(array, isLittleEndian);
				}
	
			store.setPixelsBinDataBigEndian(!isLittleEndian, 0, 0);
			store.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
			store.setPixelsType(PixelType.fromString(FormatTools.getPixelTypeString(bfPixelFormat)), 0);
	
			MetadataTools.verifyMinimumPopulated(store);
			
			///// store binary data
			IFormatWriter writer = new ImageWriter();
			writer.setMetadataRetrieve(store);
			writer.setId(file.getAbsolutePath());
			writer.saveBytes(0, barr);
			writer.close();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	}
