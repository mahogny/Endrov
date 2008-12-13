package endrov.imageset;

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
//import javax.vecmath.Vector2d;

/*import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.media.jai.codec.*;*/
import java.io.*;
import java.awt.image.*;

import endrov.ev.*;
import endrov.util.EvImageUtils;

//static int getNumDirectories(SeekableStream stream)

/**
 * Loader of images from single slice images using JAI
 * @author Johan Henriksson
 */
public abstract class EvImageJAI_OLD extends EvImage
	{
	private String filename;
	private int slice;
	
	/**
	 * Load a single-slice image
	 */
	public EvImageJAI_OLD(String filename)
		{
		this.filename=filename;
		this.slice=-1;
		}

	/**
	 * Load a slice in a stack
	 */
	public EvImageJAI_OLD(String filename, int slice)
		{
		this.filename=filename;
		this.slice=slice;
		}

	/**
	 * Get name of file
	 */
	public String jaiFileName()
		{
		return filename;
		/*
		if(slice==-1)
			return filename;
		else
			return filename+":"+slice;
			*/
		}

	/**
	 * Get slice number of -1 if it is the entire file
	 */
	public int jaiSlice()
		{
		return slice;
		}
	
	/**
	 * Load the image
	 */
	public BufferedImage loadJavaImage()
		{
		try
			{
			File file=new File(filename);
			if(!file.exists())
				return null;
			else
				{
				String fname=file.getName();
				fname=fname.substring(fname.lastIndexOf(".")+1);
				if(slice==-1 && (fname.equals("tif") || fname.equals("tiff"))) //ImageIO failed on a .tif
					{
					Raster ir=EvImageUtils.readTIFF(file).getRaster();
					
	/*				
					SeekableStream s = new FileSeekableStream(file);
					TIFFDecodeParam param = null;
	        ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
	        Raster ir=dec.decodeAsRaster();
*/
//	        Log.printDebug("Number of images in this TIFF: " + dec.getNumPages());
//        System.out.println("w"+ir.getWidth()+" h"+ir.getHeight()+" b"+ir.getSampleModel().getNumBands());

	        int type=ir.getSampleModel().getDataType();
	        if(type==0) type=BufferedImage.TYPE_BYTE_GRAY;//?
	        BufferedImage bim=new BufferedImage(ir.getWidth(),ir.getHeight(),type);
	        WritableRaster wr=bim.getRaster();
	        wr.setRect(ir);
	        return bim;
					}
				else if(slice==-1)
					{
					//Single-slice image
//					System.out.println(".. "+ImageIO.read(file)+" "+file.getAbsolutePath());
					return ImageIO.read(file);
					}
				else
					{
					//Multi-slice image
					//Only one type supported right now: tiff stacks. so assume this is the type

					
					
					ImageReader reader=ImageIO.getImageReadersByFormatName("tiff").next();
					reader.setInput(new FileImageInputStream(file));
					Raster ir=reader.read(0).getRaster();
/*
					Raster ir=reader.readRaster(0, null);
					
					SeekableStream s = new FileSeekableStream(file);
					TIFFDecodeParam param = null;
	        ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
	        Raster ir=dec.decodeAsRaster();
*/
	        
	        Log.printDebug("Number of images in this TIFF: " + reader.getNumImages(true));
//	        Log.printDebug("Number of images in this TIFF: " + dec.getNumPages());

	        BufferedImage bim=new BufferedImage(ir.getWidth(),ir.getHeight(),ir.getSampleModel().getDataType());
	        WritableRaster wr=bim.getRaster();
	        wr.setRect(ir);
	        return bim;
					}
				
				
				}
			}
		catch(Exception e)
			{
			Log.printError("Failed to read image "+filename,e);
			return null;
			}
		}
	
	
	//A lot of work is needed here....
	public void saveImage() throws Exception
		{
		saveImage(im, new File(filename), 99);
		}
	
	
	/**
	 * Save an image to disk
	 */
	private static void saveImage(BufferedImage im, File toFile, float quality) throws Exception
		{
		String fileEnding=getFileEnding(toFile.getName());
		if(fileEnding.equals("jpg") || fileEnding.equals("jpeg"))
			{
			EvImageUtils.saveJPEG(im, toFile, quality);
			
			
/*	    FileOutputStream toStream = new FileOutputStream(toFile); 
	    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(toStream); 
	    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(im); 
	    param.setQuality(quality, false); 
	    encoder.setJPEGEncodeParam(param); 
	    encoder.encode(im); */
			}
		else
			{
			ImageIO.write(im, fileEnding, toFile);
			}
		}
	
	
	
	/**
	 * Get file extension
	 */
	private static String getFileEnding(String s)
		{
		String fileEnding=s;
		fileEnding=fileEnding.substring(fileEnding.lastIndexOf('.')+1);
		return fileEnding;
		}
	
	
	}

