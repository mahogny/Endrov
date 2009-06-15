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
	public abstract EvPixels[] exec(EvPixels... p);
	public abstract EvPixels exec1(EvPixels... p);
	
	public abstract EvStack[] exec(EvStack... p);
	public abstract EvStack exec1(EvStack... p);
	
	public abstract EvChannel[] exec(EvChannel... ch);
	public abstract EvChannel exec1(EvChannel... ch);
	
	/*
	public Object exec1Untyped(Object ch)
		{
		if(ch instanceof EvPixels)
			return exec((EvPixels)ch);
		else if(ch instanceof EvStack)
			return exec((EvStack)ch);
		else if(ch instanceof EvChannel)
			return exec((EvChannel)ch);
		else
			throw new RuntimeException("Wrong type for unchecked exec: "+ch.getClass());
		}*/
	
	/**
	 * Execute operation on any level. Figure out level by the type
	 */
	public AnyEvImage exec1Untyped(Object... ch)
		{
		return execUntyped(ch)[0];
		/*
		Object fst=ch[0];
		if(fst instanceof EvPixels)
			{
			EvPixels[] arr=new EvPixels[ch.length];
			for(int i=0;i<ch.length;i++)
				arr[i]=(EvPixels)ch[i];
			return exec1(arr);
			}
		else if(fst instanceof EvStack)
			{
			EvStack[] arr=new EvStack[ch.length];
			for(int i=0;i<ch.length;i++)
				arr[i]=(EvStack)ch[i];
			return exec1(arr);
			}
		else if(fst instanceof EvChannel)
			{
			EvChannel[] arr=new EvChannel[ch.length];
			for(int i=0;i<ch.length;i++)
				arr[i]=(EvChannel)ch[i];
			return exec1(arr);
			}
		else
			{
			StringBuffer sb=new StringBuffer();
			for(Object o:ch)
				sb.append(ch.getClass()+": "+o+"\n");
			throw new RuntimeException("Wrong type for unchecked exec\n"+sb);
			}*/
		}
	
	
	
	/**
	 * Execute operation on any level. Figure out level by the type
	 */
	public AnyEvImage[] execUntyped(Object... ch)
		{
		Object fst=ch[0];
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

	
	public abstract int getNumberChannels();
	
	/**
	 * Takes stacks and pixels. Matches these together, produces a stack
	 */
	/*
	public EvStack execStack(EvObject... p)
		{
		
		
		return makeStackOp(this).exec(p);
		}
		*/
	}

