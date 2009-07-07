package endrov.flow;

import java.util.HashMap;
import java.util.Map;

import endrov.imageset.EvChannel;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;
import endrov.util.Memoize;


/**
 * TODO Could be an abstract class implementing StackOp. this saves typing.
 * problem is, stackop should have some other convenience functions. SliceOp inherits StackOp??
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvOpSlice extends EvOpGeneral //extends StackOp
	{

	public EvPixels exec1(EvPixels... p)
		{
		return exec(p)[0];
		}
	
	public EvStack[] exec(EvStack... p)
		{
		return makeStackOp(this).exec(p);
		}
	
	public EvStack exec1(EvStack... p)
		{
		return exec(p)[0];
		}
	
	public EvChannel[] exec(EvChannel... ch)
		{
		return makeStackOp(this).exec(ch);
		}
	public EvChannel exec1(EvChannel... ch)
		{
		return exec(ch)[0];
		}
	

	
	
	
	
	
	
	
	/**
	 * Turn a slice op into a stack op.
	 * Should ONLY be used on SliceOp* 
	 */
	static EvOpStack makeStackOp(final EvOpGeneral op)
		{
		return new EvOpStack()
			{
			@Override
			public EvStack[] exec(EvStack... p)
				{
				HashMap<EvDecimal,Memoize<EvPixels[]>> mems=new HashMap<EvDecimal, Memoize<EvPixels[]>>(); 
				EvStack[] retStack=new EvStack[op.getNumberChannels()];
				EvStack referenceStack=p[0];
				//System.out.println("makestackop #chan "+op.getNumberChannels());
				
				EvImage[][] inputStackImages=new EvImage[p.length][];
				for(int ac=0;ac<p.length;ac++)
//				for(int ac=0;ac<op.getNumberChannels();ac++)
					{
					inputStackImages[ac]=p[ac].getImages();
					//Consistency checks
					if(inputStackImages[ac]==null)
						throw new RuntimeException("Input plane "+ac+" is null");
					if(inputStackImages[ac].length!=inputStackImages[0].length)
						throw new RuntimeException("Input plane "+ac+" has different z-size");
					}
				
				for(int currentReturnChannel=0;currentReturnChannel<op.getNumberChannels();currentReturnChannel++)
					{
					//Create one output channel. First argument decides shape of output stack
					EvStack newstack=new EvStack();
					newstack.getMetaFrom(referenceStack);
					
					//Set up each slice
					System.out.println("stack got "+referenceStack.entrySet().size());
					int currentSliceIndex=0;
					for(Map.Entry<EvDecimal, EvImage> pe:referenceStack.entrySet())
						{
						EvImage newim=new EvImage();
						newstack.put(pe.getKey(), newim);
						
						//Collect slice from each input stack
						EvImage[] imlist=new EvImage[p.length];
						int currentInputChannel=0;
						for(EvStack cit:p)
							{
							imlist[currentInputChannel]=inputStackImages[currentInputChannel][currentSliceIndex];
							if(imlist[currentInputChannel]==null)
								{
								System.out.println("BAD! null values in imlist!");
								System.out.println("ci "+currentInputChannel+" "+pe.getKey()+" "+cit.keySet());
								}
							currentInputChannel++;
							}
						
						//Memoize multiple returns
						Memoize<EvPixels[]> maybe=mems.get(pe.getKey());
						if(maybe==null)
							mems.put(pe.getKey(),maybe=new MemoizeExecSlice(imlist, op));
						
						final Memoize<EvPixels[]> m=maybe;
						final int thisAc=currentReturnChannel;
						newim.io=new EvIOImage(){public EvPixels loadJavaImage(){return m.get()[thisAc];}};
						
						newim.registerLazyOp(m);		
						currentSliceIndex++;
						}
					retStack[currentReturnChannel]=newstack;
					System.out.println("created stack "+newstack.getResbinX()+" "+newstack.getResbinY());
					}
				return retStack;
				}

			public int getNumberChannels()
				{
				return op.getNumberChannels();
				}
			};
		}
	
	
	private static class MemoizeExecSlice extends Memoize<EvPixels[]>
		{
		private EvImage[] imlist;
		private EvOpGeneral op;
		
		public MemoizeExecSlice(EvImage[] imlist, EvOpGeneral op)
			{
			this.imlist = imlist;
			this.op = op;
			}

		@Override
		protected EvPixels[] eval()
			{
			EvPixels[] plist=new EvPixels[imlist.length];
			for(int i=0;i<plist.length;i++)
				plist[i]=imlist[i].getPixels();
			EvPixels[] ret=op.exec(plist);
			//GC cannot know that this function will only be called once. Hence manually remove
			//the references.
			op=null;
			imlist=null;
			return ret;
			}
	
		}
	}