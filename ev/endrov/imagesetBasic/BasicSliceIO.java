package endrov.imagesetBasic;

import java.awt.image.BufferedImage;
import java.io.File;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvCommonImageIO;

/**
 * Read single-slice image into memory
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
		}
	
	public BufferedImage loadJavaImage()
		{
		return EvCommonImageIO.loadJavaImage(file, z);
		}

	
	
	}
