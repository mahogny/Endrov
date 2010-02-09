/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import endrov.util.Memoize;




/**
 * Endrov image plane. Can be swapped to disk, lazily read and lazily generated. Images can share data using
 * copy-on-write semantics; copies following this are called shadows.
 * 
 * The user has to ensure exclusive access to memory and that it is in fact in memory before writing to it.
 * Thread-safety should be implemented by locking on the EvImage object. Memory is kept in place if lock()
 * is called prior to writing it. Remember to unlock() it after use or it can never be swapped out.
 * 
 * 
 * 
 * The damn data representation problem! can have another class, EvImageData, with all possible representations.
 * These are: 
 * * Signed/unsigned 8/16/32bit integer array
 * * float/double array 
 * * AWT image
 * 
 * Bonus feature: Java cannot handle unsigned data! AWT does some low-level interpretation. ways around:
 * * let these go from -127 to 127. Non-standard!
 * * ignore sign. +-* are the same on binary level. / is not and need special code; / is not good for integer images anyway
 * * cut one bit to make it fit
 * 
 * Support for extremely large pictures, how does this affect interface?
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvImage  
	{

	//TODO
	//Memoize is not forgetful. Would be nice with a semi-lazy version that remembers evaluation time
	//and can throw away the result after a while. Forgetful memoization. But it should be possible
	//to permanently force evaluation such as when the source will become unavailable.
	
	
	/** memory lock counter */
	private int locks=0;
	
	/** Image this image shadows */
	private EvImage shadowedImage=null;
	/** Images shadowing this image */
	private WeakHashMap<EvImage, Object> shadowedBy=new WeakHashMap<EvImage, Object>(1,1.0f);
	/** Pending lazy operations */
	private WeakHashMap<Memoize<?>, Object> pendingLazy=new WeakHashMap<Memoize<?>, Object>(1,1.0f);
		
	/** 
	 * Connection to I/O. Allows lazy reading by postponing load operation. Also allows lazy generation by putting a generator as a loader.
	 * 
	 * */
	public EvIOImage io=null;
	
  /** Force rewrite, such as change of compression */ 
	public boolean isDirty=false;   
	
	/** In-memory image. Set to null if there is none. */
	private EvPixels memoryPixels=null;

	/** Swap file */
	private File swapIm=null;
	
	/**
	 * Cache: pointer to loaded image
	 */
	private SoftReference<EvPixels> cachedPixels=new SoftReference<EvPixels>(null);

	//Changes in resolution might screw up pending operations. Need to encapsulate!
	//TODO

	/**
	 * For I/O manager only: The image has been written to disk and there is an io-object set up to read it.
	 * Hence the image need not stay in image anymore. This unlocks the memory by placing the image in the cache.
	 */
	public void ioIsNowOnDisk()
		{
		cachedPixels=new SoftReference<EvPixels>(getPixels());
		memoryPixels=null;
		isDirty=false;
		CacheImages.addToCache(this);
		}
	
	public EvImage()
		{
		}

	/**
	 * Create an image and setPixelsReference
	 */
	public EvImage(EvPixels p)
		{
		setPixelsReference(p);
		}

	/**
	 * Register a lazy operation. Before this image is changed, it will be executed
	 */
	public void registerLazyOp(Memoize<?> op)
		{
		pendingLazy.put(op,null);
		}

	
	/**
	 * Make sure this is a hard copy. Always safe to call. Seldom useful, use only if you know what you are doing
	 */
	public void makeSureHardCopy()
		{
		if(shadowedImage!=null)
			getShadowDataInternal();
		}
	
	/**
	 * Copy data from shadowed image here. Make sure this truly is a shadowed image before calling
	 */
	private void getShadowDataInternal()
		{
		EvPixels otherPixels=shadowedImage.getPixels();
		if(otherPixels!=null)
			memoryPixels=new EvPixels(otherPixels);
		else
			memoryPixels=null;
		
		shadowedImage.shadowedBy.remove(this);
		shadowedImage=null;
		}
	
	/**
	 * Eliminate dependencies:
	 * * Give data to all images that shadow this image
	 * * Execute pending lazy operations
	 */
	private void eliminateDependencies()
		{
		for(EvImage evim:new LinkedList<EvImage>(shadowedBy.keySet()))
			evim.getShadowDataInternal();
		for(Memoize<?> op:new LinkedList<Memoize<?>>(pendingLazy.keySet()))
			op.get();
		}
	
	
	
	/**
	 * Make an image that points to this image for data.
	 * Data is copy-on-write
	 * 
	 *  
	 */
	public EvImage makeShadowCopy()
		{
		EvImage copy=new EvImage();
		copy.shadowedImage=this;
		shadowedBy.put(copy, null);
		return copy;
		}
	
	/**
	 * Precise copy of the image that contains its own data
	 */
	public EvImage makeHardCopy()
		{
		//This could be made potentially faster, keeping it abstract for now
		EvImage copy=makeShadowCopy();
		copy.getShadowDataInternal();
		return copy;
		}
	
	/**
	 * Must be called prior to making changes to mutable objects
	 */
	public void prepareForWrite()
		{
		eliminateDependencies();
		if(shadowedImage!=null)
			getShadowDataInternal();
		}
	
	
	
	
	/**
	 * Ensure that data is in memory. This does NOT guarantee thread safety. Lock on the evimage object for this.
	 */
	public void lock()
		{
		locks++;
		//TODO ensure memory is in memory
		}
	
	/**
	 * Undo lock
	 */
	public void unlock()
		{
		locks--;
		}
	
	public boolean isLocked()
		{
		return locks!=0;
		}
	
	
	
	/**
	 * ONLY for use by I/O system
	 */
	public EvPixels getMemoryImage()
		{
		return memoryPixels;
		}

	
	/**
	 * ONLY for use by I/O system
	 */
	public void setMemoryImage(BufferedImage im)
		{
		this.memoryPixels.setPixels(im);
		}
	public void setMemoryImage(EvPixels im)
		{
		this.memoryPixels.setPixels(im);
		}
	
	
	/**
	 * Remove cached image. Can be called whenever.
	 */
	public void clearCachedImage()
		{
		cachedPixels.clear();
		}
	
	/**
	 * Get AWT representation of image. This should be as fast as it can be, but since AWT has limitations, data might be lost.
	 * It is the choice for rendering or if AWT is guaranteed to be able to handle the image.
	 * 
	 * This image is read-only unless it is again set to be the AWT image of this EVImage. (best-practice?)
	 * This has to be done *before* writing to the image.
	 * TODO is this the best way? separate method?
	 * 
	 * 
	 * This does not give a copy. to be damn sure there won't be any problems, you need to lock the data!
	 * 
	 * @deprecated use expixels?
	 * 
	 */
	public BufferedImage getJavaImage()
		{
		return getPixels().getReadOnly(EvPixelsType.AWT).getAWT();
		}
	
	/**
	 * Get pixel data for image
	 * 
	 * TODO changes to pixels should stay
	 */
	public EvPixels getPixels()
		{
		//Use in-memory image
		if(memoryPixels!=null)
			return memoryPixels;
		else
			{
			//Use cache-memory
			EvPixels loaded=cachedPixels.get();
			if(loaded!=null)
				{
				CacheImages.addToCache(this);
				return loaded;
				}
			else
				{
				//Load from swap memory if previously unloaded
				if(swapIm!=null)
					{
					try
						{
						memoryPixels=new EvPixels(ImageIO.read(swapIm));
						swapIm=null;
						return memoryPixels;
						}
					catch (IOException e)
						{
						e.printStackTrace();
						return null;
						}
					}
				else
					{
					//If this image shadows another one, use it
					if(shadowedImage!=null)
						return shadowedImage.getPixels();
					else
						{
						//Use IO to load image (might also execute operation)
						loaded=new EvPixels(io.loadJavaImage());
						cachedPixels=new SoftReference<EvPixels>(loaded);
						CacheImages.addToCache(this);
						return loaded;
						}
					}
				}
			}
		}
	
	
	
	
	/**
	 * Get array representation of image.
	 * In the next-gen EV imaging API this will be the central function but at the moment
	 * the AWT interface is faster.
	 * 
	 * 2D arrray? java has trouble with these. the primary interface should maybe be a 1d-array+width
	 * 
	 * @deprecated use getPixels
	 */
	public double[][] getArrayImage()
		{
		return getPixels().getArrayDouble2D();
		}
	
	/**
	 * Check if this memory has been modified since it was loaded into memory
	 */
	public boolean modified()
		{
		return memoryPixels!=null || swapIm!=null || isDirty || (shadowedImage!=null && shadowedImage.modified());
		}
	
	/**
	 * Modify image by setting a new image in this container. AWT format: Only use this format if no data will be lost.
	 * Will call prepareForWrite automatically
	 * 
	 * @deprecated
	 */
	public void setImage(BufferedImage im)
		{
		prepareForWrite();
		this.memoryPixels=new EvPixels(im);
		cachedPixels.clear();
		cachedPixels=new SoftReference<EvPixels>(null); //Really needed?
		}

	/**
	 * Set pixel data. Will NOT make a copy, makes a reference. Caller has to supply a copy if the pixels are to be used elsewhere as well. 
	 */
	public void setPixelsReference(EvPixels im)
		{
		prepareForWrite();
		this.memoryPixels=im;
		cachedPixels.clear();
		cachedPixels=new SoftReference<EvPixels>(null); //Really needed?
		}

	
	
	//what to do about this? now it points to io
	
	
	public String toString()
		{
		return "EvImage mempxl: "+memoryPixels+" shdw:"+shadowedImage+" shdwBy#:"+shadowedBy.size();
		}

	}
