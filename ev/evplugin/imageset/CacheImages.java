package evplugin.imageset;

import java.util.*;

/**
 * Very simple image cache. Linear complexity in size, optimized for small sizes.
 * 
 * @author Johan Henriksson
 */
public class CacheImages
	{
	private static int qsize=5;
	private static LinkedList<EvImage> queue=new LinkedList<EvImage>();
	
	public static void addToCache(EvImage im)
		{
		queue.remove(im);
		queue.addLast(im);
		
		if(queue.size()>qsize)
			{
			EvImage last=queue.poll();
			last.clearCachedImage();
			}
		}
	
	}
