/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ioImageCollections;

import java.io.File;

import endrov.typeImageset.EvCommonImageIO;
import endrov.typeImageset.EvImageReader;
import endrov.typeImageset.EvPixels;
import endrov.util.ProgressHandle;

/**
 * Read single-slice image into memory
 * @author Johan Henriksson
 *
 */
public class EvSingleImageFileReader extends EvImageReader
	{
	private File file;
	private Integer z=null;
	
	public EvSingleImageFileReader(File f)
		{
		file=f;
		}
	public EvSingleImageFileReader(File f, int z)
		{
		file=f;
		this.z=z;
		}
	
	public EvPixels eval(ProgressHandle progh)
		{
		return EvCommonImageIO.loadImagePlane(file, z);
		}
	
	public File getRawJPEGData()
		{
		return defaultGetRawJPEG(file);
		}

	
	
	}
