/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;

/**
 * Image operation defined by operation on stacks
 * @author Johan Henriksson
 *
 */
public abstract class EvOpStack1 extends EvOpGeneral
	{
	//By necessity, stack operators have to deal with laziness manually.
	//Example: avgZ only computes one slice and then duplicates it. other operands compute entire
	//stack. cannot fit together. possible to make functions beneath this.
	//public abstract EvStack[] exec(EvStack... p);
	
	public EvPixels exec1(EvPixels... p)
		{
		//TODO only one pixel supported
		//TODO where is lazyness? where is events?
		EvImage im=new EvImage();
		im.setPixelsReference(p[0]);
		EvStack stack=new EvStack();
		stack.putInt(0, im);
		stack=exec1(stack);
		return stack.getInt(0).getPixels();
		}
	
	public EvPixels[] exec(EvPixels... p)
		{
		return new EvPixels[]{exec1(p)};
		}
	
	public EvChannel[] exec(EvChannel... ch)
		{
//		return applyStackOp(ch, this);
		return EvOpStack.applyStackOp(ch, this);
		}
	


	public EvStack[] exec(EvStack... p)
		{
		return new EvStack[]{exec1(p)};
		}
	
	public abstract EvStack exec1(EvStack... p);
	
	public EvChannel exec1(EvChannel... ch)
		{
		return exec(ch)[0];
		}

	
	/**
	 * Lazily create a channel using an operator that combines input channels
	 */
	/*
	public static EvChannel[] applyStackOp(EvChannel[] ch, final EvOpStack1 op)
		{
		//Not quite final: what if changes should go back into the channel? how?
		EvChannel[] retch=new EvChannel[op.getNumberChannels()];
		
		for(int ac=0;ac<retch.length;ac++)
			{
			EvChannel newch=new EvChannel();
			
			//How to combine channels? if A & B, B not exist, make B black?
			
			//Currently operates on common subset of channels
			
			for(Map.Entry<EvDecimal, EvStack> se:ch[0].imageLoader.entrySet())
				{
				EvStack newstack=new EvStack();
				EvStack stack=se.getValue();
				
				
		
				//TODO register lazy operation
				
				final EvStack[] imlist=new EvStack[ch.length];
				int ci=0;
				for(EvChannel cit:ch)
					{
					imlist[ci]=cit.imageLoader.get(se.getKey());
					ci++;
					}
				
				final Memoize<EvStack[]> ms=new Memoize<EvStack[]>(){
				protected EvStack[] eval()
					{
					return op.exec(imlist);
					}};
				
				//TODO without lazy stacks, prior stacks are forced to be evaluated.
				//only fix is if the laziness is added directly at the source.
				
				final int thisAc=ac;
				for(Map.Entry<EvDecimal, EvImage> pe:stack.entrySet())
					{
					EvImage newim=new EvImage();
					newstack.put(pe.getKey(), newim);
					
					newstack.getMetaFrom(stack); //This design makes it impossible to generate resolution lazily
					
					final EvDecimal z=pe.getKey();
						
					newim.io=new EvIOImage(){public EvPixels loadJavaImage(){return ms.get()[thisAc].get(z).getPixels();}};
					
					newim.registerLazyOp(ms);
							
					}
				newch.imageLoader.put(se.getKey(), newstack);
				retch[ac]=newch;
				}
			}
		return retch;
		}*/
	
	public int getNumberChannels()
		{
		return 1;
		}
	}