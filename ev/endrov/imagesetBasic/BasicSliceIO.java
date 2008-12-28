package endrov.imagesetBasic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import loci.formats.FormatException;
import loci.formats.ImageReader;

import endrov.ev.Log;
import endrov.imageset.EvIOImage;
import endrov.util.EvFileUtil;

/**
 * Read single-slice image into memory using JAI 
 * @author Johan Henriksson
 *
 */
public class BasicSliceIO implements EvIOImage
	{
	private File file;
	private Integer z=null;
	
	public BasicSliceIO(File f)
		{
		file=f;
		}
	public BasicSliceIO(File f, int z)
		{
		file=f;
		this.z=z;
		//TODO use z
		}
	
	
	public BufferedImage loadJavaImage()
		{
		try
			{
			String fend=EvFileUtil.fileEnding(file);
			//Use JAI if possible, it can be assumed to be very fast
			if(fend!=null)
				if(fend.equals("jpg") || fend.equals("jpeg") || fend.equals("png"))
					return ImageIO.read(file);

			//Rely on Bioformats in the worst case
			ImageReader reader=new ImageReader();
			reader.setId(file.getAbsolutePath());

			int id=z==null?0:z;
			BufferedImage bim=reader.openImage(id);
			return bim;
			}
		catch (FormatException e)
			{
			Log.printError("Bioformats failed to read image "+file, null);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		return null;
		}

	
	
	}
