package endrov.flow;

import java.util.Map;

import endrov.imageset.EvChannel;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;
import endrov.util.Memoize;

/**
 * Image operation defined by operation on stacks
 * @author Johan Henriksson
 *
 */
public abstract class OpStack1 implements OpGeneral
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
		stack.put(EvDecimal.ZERO, im);
		stack=exec1(stack);
		return stack.get(EvDecimal.ZERO).getPixels();
		}
	
	public EvPixels[] exec(EvPixels... p)
		{
		return new EvPixels[]{exec1(p)};
		}
	
	public EvChannel[] exec(EvChannel... ch)
		{
		return applyStackOp(ch, this);
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
	public static EvChannel[] applyStackOp(EvChannel[] ch, final OpStack1 op)
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
					
					//final EvImage evim=pe.getValue();
					EvImage newim=new EvImage();
					//newim.getMetaFrom(evim);
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
		}
	
	public int getNumberChannels()
		{
		return 1;
		}
	}