/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import endrov.util.EvDecimal;

/**
 * Methods for fixing improperly saved data
 * 
 * TODO
 * * splitting channels at some time
 * * joining channels (concatenate, plain map join)
 * * splitting interleaved channels
 * 
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class StackHacks
	{

	
	
	/**
	 * Swap Z and time. Does not make copy of objects, only returns new references!
	 */
	public static void swapTZ(EvChannel ch)
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
					{
					newch.imageLoader.put(new EvDecimal(zi),newStack=new EvStack());
					newStack.getMetaFrom(se.getValue());
					}
				newStack.put(new EvDecimal(si), ze.getValue());
				zi++;
				}
			si++;
			}
		
		ch.imageLoader.clear();
		ch.imageLoader.putAll(newch.imageLoader);
		}

	
	/**
	 * Move image loaders from src to dest. Will not copy the images, take care!
	 */
	public static void replaceLoaders(EvChannel srcch, EvChannel destch)
		{
		destch.imageLoader.clear();
		destch.imageLoader.putAll(srcch.imageLoader);
		}
	
	
	/**
	 * Set spatial resolution
	 */
	public static void setResXYZ(EvChannel ch, double resX, double resY, EvDecimal resZ)
		{
		for(Map.Entry<EvDecimal, EvStack> se:ch.imageLoader.entrySet())
			{
			EvStack stack=se.getValue();
			stack.resX=resX;
			stack.resY=resY;
			//TODO stack.resZ=resZ;
			
			List<EvImage> images=new ArrayList<EvImage>();
			for(EvImage im:stack.getImages())
				images.add(im);
			
			stack.entrySet().clear();
			for(int i=0;i<images.size();i++)
				stack.put(resZ.multiply(i), images.get(i));
			}
		}
	
	/**
	 * Set time resolution
	 */
	public static void setResT(EvChannel ch, EvDecimal dt)
		{
		EvChannel newch=new EvChannel();
		
		EvDecimal time=EvDecimal.ZERO;
		for(Map.Entry<EvDecimal, EvStack> se:ch.imageLoader.entrySet())
			{
			EvStack stack=se.getValue();
			newch.imageLoader.put(time, stack);
			time=time.add(dt);
			}
		
		ch.imageLoader.clear();
		ch.imageLoader.putAll(newch.imageLoader);
		}
	
	
	}
