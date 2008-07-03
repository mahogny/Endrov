package endrov.imageset;

import java.util.*;
import java.lang.ref.*;

/**
 * Very simple image cache. Linear complexity in size, optimized for small sizes.
 * 
 * @author Johan Henriksson
 */
public class CacheImages
	{
	private static int qsize=5;
	private static LinkedList<WeakReference<EvImage>> queue=new LinkedList<WeakReference<EvImage>>();
	
	public static synchronized void addToCache(EvImage im)
		{
		WeakReference<EvImage> ref=null;
		
		//Find and remove this image from list
		for(WeakReference<EvImage> r:queue)
			{
			if(r.get()==im)
				{
				ref=r;
				break;
				}
			}
		queue.remove(ref);
		
//		queue.remove(im);
		queue.addLast(new WeakReference<EvImage>(im));
		
		if(queue.size()>qsize)
			{
			EvImage last=queue.poll().get();
			if(last!=null)
				last.clearCachedImage();
			}
		}
	
	}
