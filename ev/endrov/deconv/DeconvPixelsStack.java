package endrov.deconv;

import java.util.ArrayList;

import endrov.imageset.EvPixels;

/**
 * Just a stack of pixels. No metadata.
 * 
 * @author Johan Henriksson
 *
 */
public class DeconvPixelsStack
	{
	public ArrayList<EvPixels> p=new ArrayList<EvPixels>();
	
	
	public void addSlice(EvPixels slice, int pos)
		{
		p.ensureCapacity(pos+1);
		p.set(pos, slice);
		}
	}
