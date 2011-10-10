package endrov.IJ.roi;

import ij.gui.IJROIConverter;

import java.io.File;
import java.io.IOException;

import endrov.imageset.EvStack;



/**
 * Importing ImageJs ROI zip files
 * 
 * ij.plugin.RoiReader
 * ij.io.roidecoder
 * 
 * 
 * @author mahogny
 *
 */
public class ImageJroiImport
	{

	public ImageJroiImport(File f, EvStack stack) throws IOException
		{
		
		new IJROIConverter(f, stack);
		}
	
	
	
	}
