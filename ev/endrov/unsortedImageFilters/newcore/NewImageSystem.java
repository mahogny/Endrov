package endrov.unsortedImageFilters.newcore;


/**
 * The lowest level image operations. These are maps from image data -> image data.
 * 
 * @author Johan Henriksson
 *
 */
public class NewImageSystem
	{

	

	

	/**
	 * Lazily create a channel using an operator that combines input channels
	 * TODO turn into a stackop, use that
	 */
	/*
	public static EvChannel applySliceOp(EvChannel[] ch, final SliceOp op)
		{
		return applyStackOp(ch, makeStackOp(op));
*/
		/*
		//Not quite final: what if changes should go back into the channel? how?
		EvChannel newch=new EvChannel();
		
		//How to combine channels? if A & B, B not exist, make B black?
		
		//Currently operates on common subset of channels
		
		for(Map.Entry<EvDecimal, EvStack> se:ch[0].imageLoader.entrySet())
			{
			EvStack newstack=new EvStack();
			EvStack stack=se.getValue();
			newstack.getMetaFrom(stack);
			for(Map.Entry<EvDecimal, EvImage> pe:stack.entrySet())
				{
				//final EvImage evim=pe.getValue();
				EvImage newim=new EvImage();
				newstack.put(pe.getKey(), newim);
				
				//TODO register lazy operation
				
				//TODO lazy stack operations would take us out of this mess.
				//it would however force lazy slices to be in lazy stacks because the latter requires
				//keys to be evaluated.
				//if resolution goes into stack then no keys need be evaluated, but other things still.
				
				final EvImage[] imlist=new EvImage[ch.length];
				int ci=0;
				for(EvChannel cit:ch)
					{
					imlist[ci]=cit.imageLoader.get(se.getKey()).get(pe.getKey());
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
			newch.imageLoader.put(se.getKey(), newstack);
			}
		return newch;
		*/
		//}

	
	
	
	
	}
