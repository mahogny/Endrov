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
	public abstract EvPixels exec(EvPixels... p);
	
	public EvStack exec(EvStack... p)
		{
		return makeStackOp(this).exec(p);
		}
	
	public EvChannel exec(EvChannel... ch)
		{
		return makeStackOp(this).exec(ch);
		}
	
	/**
	 * Turn a slice op into a stack op
	 */
	public static OpStack makeStackOp(final OpSlice op)
		{
		return new OpStack()
			{
			public EvStack exec(EvStack... p)
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
					
					final Memoize<EvPixels> m=new Memoize<EvPixels>(){
					protected EvPixels eval()
						{
						EvPixels[] plist=new EvPixels[imlist.length];
						for(int i=0;i<plist.length;i++)
							plist[i]=imlist[i].getPixels();
						return op.exec(plist);
						//return op.exec(evim.getPixels());
						}};
						
					newim.io=new EvIOImage(){public EvPixels loadJavaImage(){return m.get();}};
					
					newim.registerLazyOp(m);		
							
					}
					

				System.out.println("created stack "+newstack.getResbinX()+" "+newstack.getResbinY());
				return newstack;
				}
			};
		}
	}