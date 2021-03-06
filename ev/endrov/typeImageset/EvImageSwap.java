/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeImageset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import endrov.core.EndrovCore;
import endrov.util.ProgressHandle;
import endrov.util.collection.LRUlist;


/**
 * Swap memory for images. I would normally say that the OS should deal with this, it is much better informed
 * collecting statistics through the MMU. The problem is on 32-bit systems which are going to live for another
 * while, the address space is so small that the swap+main memory might not fit. This is solved by this high-level
 * swapper which will totally unload images and hence free up addresses.
 * 
 * @author Johan Henriksson
 *
 */
public class EvImageSwap
	{

	//TODO implement
	//Least-recently used. all images have to register in this queue.
	//maybe the image cache should take care of hinting suitable candidates?
	
	
	private static LRUlist<WeakReference<EvImagePlane>> lru=new LRUlist<WeakReference<EvImagePlane>>();
	
	
	private static Integer swapImageCount=0;
	
	
	private static void countSwappedImages(int c)
		{
		synchronized (swapImageCount)
			{
			swapImageCount+=c;
			}
		}
	
	public static int getNumSwappedImage()
		{
		synchronized (swapImageCount)
			{
			return swapImageCount;
			}
		}
	
	
	
	private static Thread swapthread=new Thread()
		{
		public void run()
			{
			for(;;)
				{
				//Allow swapping to be paused
				waitForLocks();
				
				//Get the next image
				EvImagePlane evim;
				EvPixels p;
				synchronized (lru)
					{
					
					for(;;)
						{
						WeakReference<EvImagePlane> ref=lru.getFirst();
						if(ref!=null)
							{
							evim=ref.get();
							if(evim!=null)
								{
								p=evim.getMemoryImage();
								if(p!=null)
									break;
			//					else
		//							System.out.println("image is not a memory image");
								}
	//						else
//								System.out.println("null image");
							}
						else
							System.out.println("Swap: nothing to do");
						
						//If there is nothing to do then sleep
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
				EvImageReader io=serializeToDisk(p);
				if(io!=null)
					{
					evim.io=io;
					evim.setMemoryImage((EvPixels)null);
					System.gc(); //needed?
					System.out.println("Swapped image to disk");
					}
				else
					System.out.println("Failed to swap to disk");
				}
			}
		}; 
		
		
		
		
	static
		{
		swapthread.start();
		}
	
	/**
	 * Read image swapped to disk
	 * 
	 * @author Johan Henriksson
	 *
	 */
	private static class SerializedImageIO extends EvImageReader
		{
		/** Type of data, any of TYPE_* */
		private EvPixelsType type;
		/** Width */
		private int w;
		/** Height */ 
		private int h;

		private File swapFile;
		
		public SerializedImageIO(EvPixels p, File tempFile)
			{
			w=p.getWidth();
			h=p.getHeight();
			type=p.getType();
			this.swapFile=tempFile;
			
			countSwappedImages(1);

			//This gives additional guarantee that the file will be gone once the software is quit
			swapFile.deleteOnExit();
			
			}

		@Override
		protected EvPixels eval(ProgressHandle c)
			{
			try
				{
				ObjectInputStream is=new ObjectInputStream(new FileInputStream(swapFile));
				Object o=is.readObject();
				is.close();
				EvPixels p=EvPixels.createFromObject(type, w, h, o);

				return p;
				}
			catch (Exception e)
				{
				throw new RuntimeException("Unable to read swap file: "+e.getMessage());
				}
			}
		
		@Override
		protected void finalize() throws Throwable
			{
			countSwappedImages(-1);
			//This file cause the file to be deleted once the object is gone
			swapFile.delete();
			super.finalize();
			}
		
		public File getRawJPEGData()
			{
			return null;
			}

		}
		
		
	private static EvImageReader serializeToDisk(EvPixels p)
		{
		try
			{
			File tempFile=EndrovCore.createTempFile("endrovswap", ".im");
			//File.createTempFile("endrovswap", ".im");
			
			ObjectOutputStream os=new ObjectOutputStream(new FileOutputStream(tempFile));

			if(p.getType()==EvPixelsType.DOUBLE)
				os.writeObject(p.getArrayDouble());
			else if(p.getType()==EvPixelsType.FLOAT)
				os.writeObject(p.getArrayFloat());
			else if(p.getType()==EvPixelsType.INT)
				os.writeObject(p.getArrayInt());
			else if(p.getType()==EvPixelsType.SHORT)
				os.writeObject(p.getArrayShort());
			else if(p.getType()==EvPixelsType.UBYTE)
				os.writeObject(p.getArrayUnsignedByte());
			else if(p.getType()==EvPixelsType.AWT)
				os.writeObject(p.getAWT());
			else
				{
				System.out.println("Unable to serialize image, it has no data: "+p);
				os.close();
				return null;
				}
			
			os.close();
			
			return new SerializedImageIO(p, tempFile);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			return null;
			}

		
		
		}
	
	
	

		
		
	
	
	/**
	 * Suggest that one memory plane is moved to the harddrive
	 */
	public static synchronized void hintSwapImage(EvImagePlane evim)
		{
		synchronized (lru)
			{
			System.out.println("hint to swap image");
			lru.addFirst(new WeakReference<EvImagePlane>(evim));
			lru.notifyAll();
			}
		}

	
	private static WeakHashMap<SwapLock, Object> lockList=new WeakHashMap<SwapLock, Object>();
	
	
	private static void waitForLocks()
		{
		for(;;)
			synchronized (lockList)
				{
				try
					{
					if(!lockList.isEmpty())
						lockList.wait();
					return;
					}
				catch (InterruptedException e){}
				}
		}
	
	
	/**
	 * A lock on the swap. As long as it is held, the swap is paused 
	 */
	public static class SwapLock
		{
		/**
		 * Remove the lock on the swap. This function is automatically invoked by GC in case the lock gets lost (crash or similar),
		 * but it should anyway be manually invoked to ensure that the swap is allowed to proceed as soon as possible
		 */
		public void unlock()
			{
			synchronized (lockList)
				{
				lockList.remove(this);
				}
			}
		
		@Override
		protected void finalize() throws Throwable
			{
			unlock();
			}
		}
		
	
	public static SwapLock lock()
		{
		SwapLock lock=new SwapLock();
		synchronized (lockList)
			{
			lockList.put(lock, null);
			return lock;
			}
		}
	
	
	
	}
