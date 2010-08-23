/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.io.File;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.util.EvDecimal;

public class TestImageset
	{
	public static void main(String[] arg)
		{
		EV.loadPlugins();
		
		EvData data=EvData.loadFile(new File("/home/mahogny/_imageset/red.ost"));
		
		Imageset imset=data.getObjects(Imageset.class).iterator().next();
		
		
		EvChannel chan=imset.getChannel("DIC");
		
		//This is a bisarre case, swapping two images
		//most difficult to handle
		EvImage im1=chan.imageLoader.get(new EvDecimal("00025010")).getInt(0);
		EvImage im2=chan.imageLoader.get(new EvDecimal("00026690")).getInt(0);
		chan.imageLoader.get(new EvDecimal("00025010")).putInt(1,im2);
		chan.imageLoader.get(new EvDecimal("00026690")).putInt(1,im1);
		im1.isDirty=true;
		im2.isDirty=true;
		
		
		data.saveData();
		
		System.exit(0);
		
		}
	
	}
