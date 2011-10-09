package endrov.roi.ij;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import endrov.imageset.EvStack;
import endrov.roi.ROI;



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
		ZipFile zf=new ZipFile(f);
		
		Enumeration<? extends ZipEntry> e=zf.entries();
		while(e.hasMoreElements())
			{
			ZipEntry ze=e.nextElement();
			
			InputStream is=zf.getInputStream(ze);
			byte[] b=IOUtils.toByteArray(is);
			
			ROI roi=new ROIDecoder(b, ze.getName()).getROI(stack);
			
			}
		
		
		// TODO Auto-generated constructor stub
		}
	
	}
