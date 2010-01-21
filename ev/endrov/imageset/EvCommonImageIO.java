/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import endrov.ev.EvLog;
import endrov.util.EvFileUtil;

/**
 * Fast image I/O using whatever libraries are available
 * @author Johan Henriksson
 *
 */
public class EvCommonImageIO
	{
	//TODO: 16-bit support
	public static EvPixels loadPixels(File file, Integer z)
		{
		return loadJavaImage(file, z);
		//return im==null ? null : new EvPixels(im);
		}

	
	/**
	 * Load image file
	 */
	public static EvPixels loadJavaImage(File file, Integer z)
		{
		try
			{
			String fend=EvFileUtil.fileEnding(file);
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
			
			//Rely on Bioformats in the worst case
			ImageReader reader=new ImageReader();
			reader.setId(file.getAbsolutePath());

			int id=z==null?0:z;
			//BufferedImage bim=new BioformatsSliceIO(reader,id,0,"").loadJavaImage().quickReadOnlyAWT();
			return new BioformatsSliceIO(reader,id,0,"").loadJavaImage();
			//BufferedImage bim=reader.openImage(id);
			//return bim;
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
	
	
	private static File getTiffFile(File file)
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
	public static File saveImage(EvPixels p, File file, int compression)
		{
		if(p.getType()==EvPixelsType.AWT)
			saveImage(p.quickReadOnlyAWT(), file, compression);
		else if(p.getType().isIntegral())    
			{
			//Integers
			if(fitsIn8bit(p))
				saveImage(p.quickReadOnlyAWT(), file, compression);
			else
				{
				file=getTiffFile(file);
				BioformatsSliceIO.saveImageAsTiff(p, file);
				}
			}
		else 
			{
			//Floats
			p=p.convertToFloat(true);
			file=getTiffFile(file);
			BioformatsSliceIO.saveImageAsTiff(p, file);
			}
		return file;
		}

	/**
	 * Save image to disk; it is an AWT image so gray-scale 8-bit
	 */
	public static void saveImage(BufferedImage im, File file, int compression)
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
	
	
	/*
	public static void main(String[] arg)
		{
		BufferedImage bim=new BufferedImage(10,10,BufferedImage.TYPE_3BYTE_BGR);
		saveImage(bim, new File("foo.jp2"),100);
		}
		*/
	
	}
