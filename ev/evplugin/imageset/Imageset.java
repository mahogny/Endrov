package evplugin.imageset;

import java.io.*;
import java.util.*;
import org.jdom.*;

//import evplugin.basicWindow.*;
//import evplugin.imageWindow.*;
import evplugin.metadata.*;

/**
 * Interface to one VWB Recording ie the set of images
 * @author Johan Henriksson
 */
public abstract class Imageset extends Metadata
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	/** Remember last path used to load an imageset */
	public static String lastImagesetPath="/Volumes/TBU_xeon01_data/johan_x1/daemonoutput/";

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	/*
	protected void finalize() throws Throwable 
	{
	super.finalize();
	System.out.println("Finalizing imageset "+getMetadataName());
	}*/

	/** Name of imageset */
	protected String imageset;
	
	/** List of all channels belonging to this imageset */
	public HashMap<String,ChannelImages> channelImages=new HashMap<String,ChannelImages>();
	
	/** Meta object belonging to the imageset. Never null */
	public ImagesetMeta meta=new ImagesetMeta();

	
	/** Scan recording for channels */
	public abstract void buildDatabase();
	
	/** Save meta for all channels */
	public abstract void saveMeta();

	/** 
	 * Directory for auxiliary data. null if one does not exist
	 */
	public abstract File datadir();

	
	
	/**
	 * Quick access to channels
	 * @param ch Name of channel
	 * @return Channel if it exists or null
	 */
	public ChannelImages getChannel(String ch)
		{
		return channelImages.get(ch);
		}
	
	
	public String getMetadataName()
		{
		return imageset;
		}
	public String toString()
		{
		return getMetadataName();
		}
	
	/**
	 * Save metadata to some specific files; mostly for imageset internal use. Implementations of imagesets
	 * should implement a function which stores the metadata in a standard location.
	 */
	public void saveMeta(File outfile)
		{
		//Add all objects
		Document document=saveXmlMetadata();
		
		//Add imageset XML
		Element imagesetEl=new Element("imageset");
		meta.saveMetadata(imagesetEl);
		document.getRootElement().addContent(imagesetEl);
		
		//Write out to disk
		writeXmlData(document, outfile);
		}
	

	/**
	 * Images for one channel
	 */
	public class ChannelImages
		{
		/** Private copy to channel specific meta data in meta */
		private ImagesetMeta.Channel meta;
		
		/**
		 * Get channel specific meta data
		 */
		public ImagesetMeta.Channel getMeta()
			{
			return meta;
			}
		
		/** Image loaders */
		public TreeMap<Integer, TreeMap<Integer, ImageLoader>> imageLoader=new TreeMap<Integer, TreeMap<Integer, ImageLoader>>();
		
		
		public ImageLoader getImageLoader(int frame, int z)
			{
			try
				{
				return imageLoader.get(frame).get(z);
				}
			catch(Exception e)
				{
				return null;
				}
			//new ImageLoaderJAI(imageLoader(frame,z));
			}
		
		
		/**
		 * Get property assigned to a frame
		 * @param frame Frame
		 * @param prop Property
		 * @return Value of property or null if it does not exist
		 */
		public String getFrameMeta(int frame, String prop)
			{
			HashMap<String,String> framedata=meta.metaFrame.get(frame);
			if(framedata==null)
				return null;
			return framedata.get(prop);
			}
		
		/**
		 * Create a new channel
		 * @param cn Name of channel
		 */
		public ChannelImages(ImagesetMeta.Channel cn)
			{
			meta=cn;
			}


		
		
		
		
		/**
		 * Find out the closest frame
		 * @param frame Which frame to match against
		 * @return If there are no frames or there is an exact match, then frame. Otherwise the closest frame.
		 */
		public int closestFrame(int frame)
			{
			if(imageLoader.get(frame)!=null || imageLoader.size()==0)
				return frame;
			else
				{
				SortedMap<Integer, TreeMap<Integer,ImageLoader>> before=imageLoader.headMap(frame);
				SortedMap<Integer, TreeMap<Integer,ImageLoader>> after=imageLoader.tailMap(frame);
				if(before.size()==0)
					return imageLoader.firstKey();
				else if(after.size()==0)
					return imageLoader.lastKey();
				else
					{
					int afterkey=after.firstKey();
					int beforekey=before.lastKey();
					
					if(afterkey-frame < frame-beforekey)
						return afterkey;
					else
						return beforekey;
					}
				}
			}
		
		
		/**
		 * Get the frame before
		 * @param frame Current frame
		 * @return The frame before or the same frame if no frame before found
		 */
		public int closestFrameBefore(int frame)
			{
			SortedMap<Integer, TreeMap<Integer,ImageLoader>> before=imageLoader.headMap(frame); 
			if(before.size()==0)
				return frame;
			else
				return before.lastKey();
			}
		/**
		 * Get the frame after
		 * @param frame Current frame
		 * @return The frame after or the same frame if no frame after found
		 */
		public int closestFrameAfter(int frame)
			{
			SortedMap<Integer, TreeMap<Integer,ImageLoader>> after=imageLoader.tailMap(frame+1);
			if(after.size()==0)
				return frame;
			else
				return after.firstKey();
			}
		
		
		/**
		 * Find the closest slice given a frame and slice
		 * @param frame Which frame to search
		 * @param z Z we wish to match
		 * @return Same z if frame does not exist or no slices exist, otherwise the closest z
		 */
		public int closestZ(int frame, int z)
			{
			TreeMap<Integer,ImageLoader> slices=imageLoader.get(frame);
			if(slices==null || slices.size()==0)
				return z;
			else
				{
				SortedMap<Integer,ImageLoader> before=slices.headMap(z);
				SortedMap<Integer,ImageLoader> after=slices.tailMap(z);
				if(before.size()==0)
					return after.firstKey();
				else if(after.size()==0)
					return before.lastKey();
				else
					{
					int afterkey=after.firstKey();
					int beforekey=before.lastKey();
					
					if(afterkey-z < z-beforekey)
						return afterkey;
					else
						return beforekey;
					}
				}
			}


		/**
		 * Find the closest slice above given a slice in a frame
		 * @param frame Which frame to search
		 * @param z Z we wish to match
		 * @return Same z if frame does not exist or no slices exist, otherwise the closest z above
		 */
		public int closestZAbove(int frame, int z)
			{
			TreeMap<Integer,ImageLoader> slices=imageLoader.get(frame);
			if(slices==null)
				return z;
			else
				{
				SortedMap<Integer,ImageLoader> after=slices.tailMap(z+1);
				if(after.size()==0)
					return z;
				else
					return after.firstKey();
				}
			}
		
		/**
		 * Find the closest slice below given a slice in a frame
		 * @param frame Which frame to search
		 * @param z Z we wish to match
		 * @return Same z if frame does not exist or no slices exist, otherwise the closest z below
		 */
		public int closestZBelow(int frame, int z)
			{
			TreeMap<Integer, ImageLoader> slices=imageLoader.get(frame);
			if(slices==null)
				return z;
			else
				{
				SortedMap<Integer, ImageLoader> before=slices.headMap(z);
				if(before.size()==0)
					return z;
				else
					return before.lastKey();
				}
			}		
		
		
		}

		



	}
