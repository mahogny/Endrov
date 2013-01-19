/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeImageset;

import java.util.*;
import java.lang.ref.*;

/**
 * Very simple image cache. Linear complexity in size, optimized for small sizes.
 * TODO cache locking, better algorithm
 * 
 * @author Johan Henriksson
 */
public class EvImageCache
	{
	private static int qsize=100; //this should be user configurable
	private static LinkedList<WeakReference<EvImagePlane>> queue=new LinkedList<WeakReference<EvImagePlane>>();
	
	/**
	 * Update image on cache queue. Also note that the image has a cache that can be removed
	 */
	public static synchronized void addToCache(EvImagePlane im)
		{
		WeakReference<EvImagePlane> ref=null;
		
		//Find and remove this image from list
		for(WeakReference<EvImagePlane> r:queue)
			{
			if(r.get()==im)
				{
				ref=r;
				break;
				}
			}
		queue.remove(ref);
		
//		queue.remove(im);
		queue.addLast(new WeakReference<EvImagePlane>(im));
		
		if(queue.size()>qsize)
			{
			EvImagePlane last=queue.poll().get();
			if(last!=null)
				last.clearCachedImage();
			}
		}
	
	}
