package endrov.data;

import java.util.Set;
import java.util.TreeSet;

import endrov.imageset.ImagesetMeta;
import endrov.util.EvDecimal;

public class ImagesetNew
	{

	
	Set<String> deleteChannel=new TreeSet<String>();
	
	//channel renames would be expensive! undo?
	
	
	//this goes into each channel directly
	//private ImagesetMeta.Channel meta;
	
	public class ImageNew
		{
		boolean dirty; //save image if set, very simple
		}
	
	public class ChannelNew
		{
		
		Set<EvDecimal> deleteFrame=new TreeSet<EvDecimal>();


		
		
		
		}
	
	
	}
