package endrov.imageset;

import java.util.Map;
import endrov.data.EvData;
import endrov.util.EvDecimal;

/**
 * Methods for fixing improperly saved data
 * 
 * @author Johan Henriksson
 *
 */
public class StackHacks
	{

	/**
	 * Swap Z and time
	 * @param ch
	 */
	public EvChannel swapTZ(EvChannel ch)
		{
		EvChannel newch=new EvChannel();
		
		int si=0;
		for(Map.Entry<EvDecimal, EvStack> se:ch.imageLoader.entrySet())
			{
			//EvDecimal frame=se.getKey();
			int zi=0;
			for(Map.Entry<EvDecimal, EvImage> ze:se.getValue().entrySet())
				{
				//EvDecimal z=ze.getKey();
				EvStack newStack=newch.imageLoader.get(new EvDecimal(zi));
				if(newStack==null)
					newch.imageLoader.put(new EvDecimal(zi),newStack=new EvStack());
				newStack.put(new EvDecimal(si), ze.getValue());
				zi++;
				}
			si++;
			}
		return newch;
		}

	
	/**
	 * Move image loaders from src to dest. Will not copy the images, take care!
	 */
	public static void replaceLoaders(EvChannel srcch, EvChannel destch)
		{
		destch.imageLoader.clear();
		destch.imageLoader.putAll(srcch.imageLoader);
		}
	
	
	
	
	}
