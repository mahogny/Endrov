package endrov.util;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.imageio.stream.*;


/**
 * Functions for Image I/O, JAI in particular
 * @author Johan Henriksson
 */
public class EvImageUtils
	{
	public static void saveJPEG(BufferedImage im, File out, float quality) throws IOException
		{
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
		ImageWriter writer=iter.next();
		ImageWriteParam iwp=writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(quality);
		FileImageOutputStream toStream=new FileImageOutputStream(out);
		writer.setOutput(toStream);
		writer.write(null, new IIOImage(im,null,null), iwp);
		}
	
	public static BufferedImage readTIFF(File file) throws IOException
		{
		//Does not work on linux. deprecate all use of TIFF! or use bioformats
		ImageReader reader=ImageIO.getImageReadersByFormatName("tiff").next();
		reader.setInput(new FileImageInputStream(file));
		return reader.read(0);
		}
	
	
	}
