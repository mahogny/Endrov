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
public abstract class OpSlice implements OpGeneral //extends StackOp
	{
	//Could have multiple output
	//EvPixels or EvImage?
//	public abstract EvPixels[] exec(EvPixels... p);
	
	public abstract EvPixels[] exec(EvPixels... p);

	
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
	 * Turn a slice op into a stack op
	 */
	public static OpStack makeStackOp(final OpGeneral op)
		{
		return new OpStack()
			{
			@Override
			public EvStack[] exec(EvStack... p)
				{
				HashMap<EvDecimal,Memoize<EvPixels[]>> mems=new HashMap<EvDecimal, Memoize<EvPixels[]>>(); 
				EvStack[] retStack=new EvStack[op.getNumberChannels()];
				for(int ac=0;ac<op.getNumberChannels();ac++)
					{
					EvStack newstack=new EvStack();
					EvStack stack=p[0];
					newstack.getMetaFrom(stack);
					
					
					for(Map.Entry<EvDecimal, EvImage> pe:stack.entrySet())
						{
						//final EvImage evim=pe.getValue();
						EvImage newim=new EvImage();
						newstack.put(pe.getKey(), newim);
						
						final EvImage[] imlist=new EvImage[p.length];
						int ci=0;
						for(EvStack cit:p)
							{
							imlist[ci]=cit.get(pe.getKey());
							ci++;
							}
						
						//Memoize multiple returns
						Memoize<EvPixels[]> maybe=mems.get(pe.getKey());
						if(maybe==null)
							mems.put(pe.getKey(),maybe=new Memoize<EvPixels[]>(){
							protected EvPixels[] eval()
								{
								EvPixels[] plist=new EvPixels[imlist.length];
								for(int i=0;i<plist.length;i++)
									plist[i]=imlist[i].getPixels();
								return op.exec(plist);
								//return op.exec(evim.getPixels());
								}});
						
						final Memoize<EvPixels[]> m=maybe;
						final int thisAc=ac;
						newim.io=new EvIOImage(){public EvPixels loadJavaImage(){return m.get()[thisAc];}};
						
						newim.registerLazyOp(m);		
								
						retStack[ac]=newstack;
						System.out.println("created stack "+newstack.getResbinX()+" "+newstack.getResbinY());
						}
					}
				return retStack;
				}

			public int getNumberChannels()
				{
				return op.getNumberChannels();
				}
			};
		}
	}