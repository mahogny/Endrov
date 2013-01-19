package endrov.bindingIJ.roi;

import ij.gui.IJROIConverter;

import java.io.File;
import java.io.IOException;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvContainer;
import endrov.data.EvData;
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

	public ImageJroiImport(File f, EvStack stack, final EvContainer con) throws IOException
		{
		
		new IJROIConverter(f, stack){

			@Override
			public void gotROI(ROI roi)
				{
				
				
				//Find a name
				String name;
				int num=1;
				do
					{
					name="roi"+num;
					num++;
					} while(con.metaObject.containsKey(name));
				
				con.metaObject.put(name, roi);
				
				System.out.println("got roi "+roi);
				
				//roi.stroke;
				//roi.strokeColor
				//roi.name
				//roi.fillColor
				//roi.handleColor
				//roi.lineWidth
				
				}};
		}
	
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		
		
		try
			{
			new ImageJroiImport(new File("/Volumes/TBU_main06/customer/RoiSet.zip"), new EvStack(), new EvData());
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		}
	
	}
