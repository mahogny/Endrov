/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.lang.ref.WeakReference;
import java.util.PriorityQueue;

/**
 * Swap memory for images. I would normally say that the OS should deal with this, it is much better informed
 * collecting statistics through the MMU. The problem is on 32-bit systems which are going to live for another
 * while, the address space is so small that the swap+main memory might not fit. This is solved by this high-level
 * swapper which will totally unload images and hence free up addresses.
 * 
 * @author Johan Henriksson
 *
 */
public class SwapImages
	{

	//TODO implement
	//Least-recently used. all images have to register in this queue.
	//maybe the image cache should take care of hinting suitable candidates?
	
	
	private static LRUlist<WeakReference<EvImage>> lru=new LRUlist<WeakReference<EvImage>>();
	
	
	private static Thread swapthread=new Thread()
		{
		public void run()
			{
			
			//Get the next image
			EvImage evim;
			EvPixels p;
			synchronized (lru)
				{
				
				for(;;)
					{
					WeakReference<EvImage> ref=lru.getFirst();
					if(ref!=null)
						{
						evim=ref.get();
						if(evim!=null)
							{
							p=evim.getMemoryImage();
							if(evim.isDirty && p!=null)
								break;
							}
						}
					try
						{
						lru.wait();
						}
					catch (InterruptedException e)
						{
						e.printStackTrace();
						}
					}
				}
			
			//Write image to disk
			
			//Depends on the image type....
			
			
			
			
			
			//p.
			
			
			
			}
		
		}; 
	
		
	static
		{
		swapthread.start();
		}
		
		
	private static boolean canSwap(EvImage evim)
		{
		EvPixels p=evim.getMemoryImage();
		return evim.isDirty && p!=null;
		}
	
	
	/**
	 * Suggest that one memory plane is moved to the harddrive
	 */
	public static synchronized void hintSwapImage(EvImage evim)
		{
		synchronized (lru)
			{
			lru.addFirst(new WeakReference<EvImage>(evim));

			
			lru.notifyAll();
			
			}
		
		
		}
	
	
	
	}
