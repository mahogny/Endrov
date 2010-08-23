/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
public abstract class EvOpStack extends EvOpGeneral
	{
	//By necessity, stack operators have to deal with laziness manually.
	//Example: avgZ only computes one slice and then duplicates it. other operands compute entire
	//stack. cannot fit together. possible to make functions beneath this.
	//public abstract EvStack[] exec(EvStack... p);
	
	public EvPixels[] exec(EvPixels... p)
		{
		//TODO only one pixel supported
		//TODO where is lazyness? where is events?
		EvImage im=new EvImage();
		im.setPixelsReference(p[0]);
		EvStack stack[]=new EvStack[]{new EvStack()};
		stack[0].put(EvDecimal.ZERO, im);
		stack=exec(stack);
		EvPixels[] ret=new EvPixels[stack.length];
		for(int ac=0;ac<ret.length;ac++)
			ret[ac]=stack[ac].getInt(0).getPixels();
		return ret;
		}
	
	
	

	public EvChannel[] exec(EvChannel... ch)
		{
		System.out.println("here1");
		return applyStackOp(ch, this);
		}
	
	public EvPixels exec1(EvPixels... p)
		{
		return exec(p)[0];
		}

	public EvStack exec1(EvStack... p)
		{
		return exec(p)[0];
		}
	
	public EvChannel exec1(EvChannel... ch)
		{
		System.out.println("#### "+exec(ch)[0]);
		return exec(ch)[0];
		}

	
	/**
	 * Lazily create a channel using an operator that combines input channels.
	 * 
	 * Should ONLY be used for EvOpStack and EvOpStack1 
	 * 
	 */
	static EvChannel[] applyStackOp(EvChannel[] ch, EvOpGeneral op)
		{
		//System.out.println("here3 ");

		int numInputChannels=ch.length;
		int numOutputChannels=op.getNumberChannels();
		
		//Not quite final: what if changes should go back into the channel? how?
		EvChannel[] retch=new EvChannel[numOutputChannels];
		
		//First argument decides which frames to apply for
		EvChannel refChannel=ch[0];
		
		//System.out.println("#chan "+retch.length+"  zzz "+refChannel.imageLoader);
		for(int curOutputChanIndex=0;curOutputChanIndex<retch.length;curOutputChanIndex++)
			{
			EvChannel curReturnChan=new EvChannel();
			retch[curOutputChanIndex]=curReturnChan;
			
			//How to combine channels? if A & B, B not exist, make B black?
			
			//Currently operates on common subset of channels
			for(Map.Entry<EvDecimal, EvStack> channelEntry:refChannel.imageLoader.entrySet())
				{
				final EvStack curInputStack=channelEntry.getValue();
				final EvStack curReturnStack=new EvStack();
				curReturnStack.getMetaFrom(curInputStack);
				curReturnChan.imageLoader.put(channelEntry.getKey(), curReturnStack);
				
				//TODO register lazy operation
				
				final EvStack[] imlist=new EvStack[numInputChannels];
				int ci=0;
				for(EvChannel cit:ch)
					{
					imlist[ci]=cit.imageLoader.get(channelEntry.getKey());
					ci++;
					}
				
				final Memoize<EvStack[]> ms=new MemoizeExecStack(imlist, op);
				
				
				//TODO without lazy stacks, prior stacks are forced to be evaluated.
				//only fix is if the laziness is added directly at the source.
				
				final int finalCurReturnChanIndex=curOutputChanIndex;
				
				for(int az=0;az<curInputStack.getDepth();az++)
//				for(final Map.Entry<EvDecimal, EvImage> stackEntry:curInputStack.entrySet())
					{
					EvImage newim=new EvImage();
					//curReturnStack.put(stackEntry.getKey(), newim);
					curReturnStack.putInt(az, newim);
					
					curReturnStack.getMetaFrom(curInputStack); 
					//TODO This design makes it impossible to generate resolution lazily
					//TODO in particular, crop will not work nicely
					
					//final EvDecimal z=stackEntry.getKey();
					final int z=az;	
					
					newim.io=new EvIOImage(){public EvPixels loadJavaImage(){
					try
						{
						EvStack[] chans=ms.get();
						if(finalCurReturnChanIndex>=chans.length)
							throw new RuntimeException("Trying to use index "+finalCurReturnChanIndex+" but there are only "+chans.length+" entries in chans");
						EvStack stack=chans[finalCurReturnChanIndex];
						if(stack==null)
							throw new RuntimeException("EvOp programming error: got null stack");
						EvImage evim=stack.getInt(z);
						if(evim==null)
							throw new RuntimeException("There is no image for this z: "+z);
						return evim.getPixels();
						}
					catch (Exception e)
						{
						e.printStackTrace();
						System.out.println("want to get z: "+z);
						System.out.println("index "+finalCurReturnChanIndex);
						System.out.println("Incoming z set "+curInputStack.keySet());
						System.out.println(ms.get()[finalCurReturnChanIndex].keySet());
						throw new RuntimeException("failed in lazy execution");
						}
					}};
					
					newim.registerLazyOp(ms);		
					}
				}
//			System.out.println("here2 "+curReturnChan.imageLoader);
			}
		return retch;
		}
	
	
	private static class MemoizeExecStack extends Memoize<EvStack[]>
		{
		private EvStack[] imlist;
		private EvOpGeneral op;
		
		public MemoizeExecStack(EvStack[] imlist, EvOpGeneral op)
			{
			this.imlist = imlist;
			this.op = op;
			}

		@Override
		protected EvStack[] eval()
			{
			EvStack[] ret=op.exec(imlist);
			//System.out.println("----- "+op.getClass());
			/*for(EvStack s:ret)
				{
				System.out.println("one stack: "+s);
				System.out.println(s.entrySet());
				}*/
			if(ret==null)
				throw new RuntimeException("EvOp programming error (2): Stack operation returns null array of channels");

			//Help GC. There might be space leaks without this.
			op=null;
			imlist=null;
			return ret;
			}
	
		}
	
	}