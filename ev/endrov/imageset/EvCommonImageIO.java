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
	public static EvPixels loadPixels(File file, Integer z)
		{
		BufferedImage im=loadJavaImage(file, z);
		return im==null ? null : new EvPixels(im);
		}

	
	/**
	 * Load image file
	 */
	public static BufferedImage loadJavaImage(File file, Integer z)
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
						return bim;
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
			BufferedImage bim=reader.openImage(id);
			return bim;
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
			writer.saveImage(im, true);
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
	
	
	public static void main(String[] arg)
		{
		BufferedImage bim=new BufferedImage(10,10,BufferedImage.TYPE_3BYTE_BGR);
		saveImage(bim, new File("foo.jp2"),100);
		}
	
	}
