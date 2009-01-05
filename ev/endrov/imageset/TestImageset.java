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
		EvImage im1=chan.imageLoader.get(new EvDecimal("00025010")).get(new EvDecimal("0"));
		EvImage im2=chan.imageLoader.get(new EvDecimal("00026690")).get(new EvDecimal("0"));
		chan.imageLoader.get(new EvDecimal("00025010")).put(new EvDecimal("1"),im2);
		chan.imageLoader.get(new EvDecimal("00026690")).put(new EvDecimal("1"),im1);
		im1.isDirty=true;
		im2.isDirty=true;
		
		
		data.saveData();
		
		System.exit(0);
		
		}
	
	}
