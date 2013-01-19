/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import java.io.File;
import java.util.HashMap;

import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvImageReader;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.collection.MemoizeX;


/**
 * TODO Could be an abstract class implementing StackOp. this saves typing.
 * problem is, stackop should have some other convenience functions. SliceOp inherits StackOp??
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvOpSlice extends EvOpGeneral //extends StackOp
	{

	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return exec(ph, p)[0];
		}
	
	public EvStack[] exec(ProgressHandle ph, EvStack... p)
		{
		return makeStackOpFromSliceOp(this).exec(ph, p);
		}
	
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return exec(ph,p)[0];
		}
	
	public EvChannel[] exec(ProgressHandle ph, EvChannel... ch)
		{
		return makeStackOpFromSliceOp(this).exec(ph, ch);
		}
	public EvChannel exec1(ProgressHandle ph, EvChannel... ch)
		{
		return exec(ph,ch)[0];
		}
	

	
	
	
	
	
	
	
	/**
	 * Turn a slice op into a stack op.
	 * Should ONLY be used on SliceOp* 
	 */
	static EvOpStack makeStackOpFromSliceOp(final EvOpGeneral op)
		{
		return new EvOpStack()
			{
			@Override
			public EvStack[] exec(ProgressHandle ph, EvStack... p)
				{
				HashMap<Integer,MemoizeX<EvPixels[]>> lazySliceOps=new HashMap<Integer, MemoizeX<EvPixels[]>>(); 
				EvStack[] retStack=new EvStack[op.getNumberChannels()];
				EvStack referenceStack=p[0];
				
				EvImagePlane[][] inputStackImages=new EvImagePlane[p.length][];
				for(int ac=0;ac<p.length;ac++)
					{
					inputStackImages[ac]=p[ac].getImagePlanes();
					//Consistency checks
					if(inputStackImages[ac]==null)
						throw new RuntimeException("Input plane "+ac+" is null");
					if(inputStackImages[ac].length!=inputStackImages[0].length)
						throw new RuntimeException("Input plane "+ac+" has different z-size");
					}
				
				//Memoize calculations for each Z, since output is demultiplexed
				for(int az=0;az<referenceStack.getDepth();az++)
					{
					//Collect slice from each channel
					EvImagePlane[] imlist=new EvImagePlane[p.length];
					for(int currentInputChannel=0;currentInputChannel<p.length;currentInputChannel++)
						{
						imlist[currentInputChannel]=inputStackImages[currentInputChannel][az];
						if(imlist[currentInputChannel]==null)
							{
							System.out.println("BAD! null values in imlist!");
							System.out.println("ci "+currentInputChannel+" "+az);
							}
						}
					
					//Memoize multiple returns
					lazySliceOps.put(az,new MemoizeExecSlice(imlist, op));
					}
				
				//Return lazy IO handlers for each output channel, but each really just picking one item from the result
				for(int currentReturnChannel=0;currentReturnChannel<op.getNumberChannels();currentReturnChannel++)
					{
					//Create one output channel. First argument decides shape of output stack
					EvStack newstack=new EvStack();
					newstack.copyMetaFrom(referenceStack);
					
					//Set up each slice
					for(int az=0;az<referenceStack.getDepth();az++)
						{
						//Get the calculation for this Z
						final MemoizeX<EvPixels[]> m=lazySliceOps.get(az);
						
						EvImagePlane newim=new EvImagePlane();
						newstack.putPlane(az, newim);

						final int thisAc=currentReturnChannel;
						newim.io=new EvImageReader(){public EvPixels eval(ProgressHandle progh)
							{
							System.out.println("------- eval of multiplexer ---------");
							
							EvPixels[] parr=m.get(progh);
							if(parr==null)
								throw new RuntimeException("EvOp programming error: Slice operation returns null array of channels");
							if(thisAc>=parr.length)
								throw new RuntimeException("EvOp programming error: Trying to get channel "+thisAc+" but only "+parr.length+" channels were returned");
							return parr[thisAc];
							}
						public File getRawJPEGData()
							{
							return null;
							}
						};
							
						newim.io.dependsOn(m);
						newim.registerLazyOp(m);		
						}
					retStack[currentReturnChannel]=newstack;
					}
				return retStack;
				}

			public int getNumberChannels()
				{
				return op.getNumberChannels();
				}
			};
		}
	
	
	private static class MemoizeExecSlice extends MemoizeX<EvPixels[]>
		{
		private EvImagePlane[] imlist;
		private EvOpGeneral op;
		
		public MemoizeExecSlice(EvImagePlane[] imlist, EvOpGeneral op)
			{
			this.imlist = imlist;
			this.op = op;
			
			for(EvImagePlane p:imlist)
				p.registerMemoizeXdepends(this);
			}

		@Override
		protected EvPixels[] eval(ProgressHandle ph)
			{
			
			System.out.println("------ evaluating slice --------");
			
			EvPixels[] plist=new EvPixels[imlist.length];
			for(int i=0;i<plist.length;i++)
				plist[i]=imlist[i].getPixels(ph);
			EvPixels[] ret=op.exec(ph, plist);
			if(ret==null)
				throw new RuntimeException("EvOp programming error (2): Slice operation returns null array of channels");
			return ret;
			}
	
		}
	}