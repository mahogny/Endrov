/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import endrov.imageset.AnyEvImage;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;

/**
 * General image processing operation. Programmers are normally not meant to implement this
 * class directly. Operations have a natural domain, single images or up to entire channels.
 * There are more convenient subclasses that work with each of these.
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvOpGeneral
	{
	/**
	 * Apply given several image planes, returns one or more image planes depending on operation
	 */
	public abstract EvPixels[] exec(EvPixels... p);
		
	/**
	 * Apply given several image planes, returns only the first image plane
	 */
	public abstract EvPixels exec1(EvPixels... p);
	
	/**
	 * Apply given several stacks, returns one or more stacks depending on operation
	 */
	public abstract EvStack[] exec(EvStack... p);
	
	/**
	 * Apply given several stacks, returns only the first stack
	 */
	public abstract EvStack exec1(EvStack... p);

	/**
	 * Apply given several channels, returns one or more channels depending on operation
	 */
	public abstract EvChannel[] exec(EvChannel... ch);
	
	/**
	 * Apply given several channels, returns only the first channel
	 */
	public abstract EvChannel exec1(EvChannel... ch);
	
	/**
	 * Execute operation on any level. Figure out level by the type
	 */
	public AnyEvImage exec1Untyped(AnyEvImage... ch)
		{
		return execUntyped(ch)[0];
		}
	
	/**
	 * Execute operation on any level. Figure out level by the type
	 */
	public AnyEvImage[] execUntyped(AnyEvImage... ch)
		{
		AnyEvImage fst=ch[0];
		if(fst instanceof EvPixels)
			{
			EvPixels[] arr=new EvPixels[ch.length];
			for(int i=0;i<ch.length;i++)
				arr[i]=(EvPixels)ch[i];
			return exec(arr);
			}
		else if(fst instanceof EvStack)
			{
			EvStack[] arr=new EvStack[ch.length];
			for(int i=0;i<ch.length;i++)
				arr[i]=(EvStack)ch[i];
			return exec(arr);
			}
		else if(fst instanceof EvChannel)
			{
			System.out.println("===passing as evchannel");
			EvChannel[] arr=new EvChannel[ch.length];
			for(int i=0;i<ch.length;i++)
				arr[i]=(EvChannel)ch[i];
			return exec(arr);
			}
		else
			{
			StringBuffer sb=new StringBuffer();
			for(Object o:ch)
				sb.append(ch.getClass()+": "+o+"\n");
			throw new RuntimeException("Wrong type for unchecked exec\n"+sb);
			}
		}

	/**
	 * Get the number of channels/stacks/image planes that this operation will return
	 */
	public abstract int getNumberChannels();
	}

