package endrov.data;

import java.util.Set;
import java.util.TreeSet;

import endrov.util.EvDecimal;

public class ImagesetNew
	{

	
	Set<String> deleteChannel=new TreeSet<String>();
	
	//channel renames would be expensive! undo?
	
	
	public class ImageNew
		{
		boolean dirty; //save image if set, very simple
		}
	
	public class ChannelNew
		{
		
		Set<EvDecimal> deleteFrame=new TreeSet<EvDecimal>();


		
		
		
		}
	
	
	}
