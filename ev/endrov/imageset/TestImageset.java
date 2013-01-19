/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.io.File;
import java.io.IOException;

import endrov.core.EndrovCore;
import endrov.data.EvData;
import endrov.util.EvDecimal;

public class TestImageset
	{
	public static void main(String[] arg)
		{
		EndrovCore.loadPlugins();
		
		try
			{
			EvData data=EvData.loadFile(new File("/home/mahogny/_imageset/red.ost"));
			
			Imageset imset=data.getObjects(Imageset.class).iterator().next();
			
			
			EvChannel chan=imset.getChannel("DIC");
			
			//This is a bisarre case, swapping two images
			//most difficult to handle
			EvImage im1=chan.getStack(new EvDecimal("00025010")).getInt(0);
			EvImage im2=chan.getStack(new EvDecimal("00026690")).getInt(0);
			chan.getStack(new EvDecimal("00025010")).putInt(1,im2);
			chan.getStack(new EvDecimal("00026690")).putInt(1,im1);
			im1.isDirty=true;
			im2.isDirty=true;
			
			
			data.saveData();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		System.exit(0);
		
		}
	
	}
