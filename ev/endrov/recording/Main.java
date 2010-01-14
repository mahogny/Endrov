/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import mmcorej.*;

public class Main
	{
	public static void main(String[] arg)
		{
		
		//mmcorej.MMCoreJ core=new MMCoreJ();
		
		MMCoreJ.getMaxStrLength();
		mmcorej.CMMCore mmc=new mmcorej.CMMCore();
		//C:\Micro-Manager1.2\MMConfig_demo.cfg
		try
			{
			mmc.loadSystemConfiguration("MMConfig.cfg");

			mmc.snapImage();
			/*			

			Object img=mmc.getImage();

			int width=(int)mmc.getImageWidth();
			int height=(int)mmc.getImageHeight();
			*/
//		>> img2=typecast(img,'uint8');
//		>> img2=reshape(img2,[height,width]);
//		>> imshow(img2); 
			
			
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		}
	}
