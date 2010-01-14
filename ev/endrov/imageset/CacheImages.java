/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.util.*;
import java.lang.ref.*;

/**
 * Very simple image cache. Linear complexity in size, optimized for small sizes.
 * TODO cache locking, better algorithm
 * 
 * @author Johan Henriksson
 */
public class CacheImages
	{
	private static int qsize=5;
	private static LinkedList<WeakReference<EvImage>> queue=new LinkedList<WeakReference<EvImage>>();
	
	/**
	 * Update image on cache queue. Also note that the image has a cache that can be removed
	 */
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
