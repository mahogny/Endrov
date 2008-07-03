package endrov.imageset;

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import endrov.data.*;

/**
 * Interface to one imageset + metadata
 * @author Johan Henriksson
 */
public abstract class Imageset extends EvData
	{
	/** Name of imageset */
	protected String imageset;
	
	/** List of all channels belonging to this imageset */
	public HashMap<String,ChannelImages> channelImages=new HashMap<String,ChannelImages>();
	
	
	/** Meta object belonging to the imageset. Never null. Referenced elsewhere, do not change pointer */
	public ImagesetMeta meta=new ImagesetMeta();

	
	/** Scan recording for channels */
	public abstract void buildDatabase();
	
	/** Save meta for all channels */
	public abstract void saveMeta();

	/** 
	 * Directory for auxiliary data. null if one does not exist
	 */
	public abstract File datadir();


	public String getMetadataName()
		{
		return imageset;
		}
	public String toString()
		{
		return getMetadataName();
		}

	
	/**
	 * Quick access to channels
	 * @param ch Name of channel
	 * @return Channel if it exists or null
	 */
	public ChannelImages getChannel(String ch)
		{
		return channelImages.get(ch);
		}
	
	
	/**
	 * Create a channel if it doesn't exist
	 */
	public ChannelImages createChannel(String ch)
		{
		ChannelImages im=channelImages.get(ch);
		if(im==null)
			{
			ImagesetMeta.Channel m=meta.getCreateChannelMeta(ch);
			im=internalMakeChannel(m);// new ChannelImages(m);
			channelImages.put(ch, im);
			}
		return im;
		}

	protected abstract ChannelImages internalMakeChannel(ImagesetMeta.Channel ch);
	
	/**
	 * Remove channel images and metadata
	 */
	public void removeChannel(String ch)
		{
		channelImages.remove(ch);
		meta.channelMeta.remove(ch);
		}
	
	
	/**
	 * Save metadata to some specific files; mostly for imageset internal use. Implementations of imagesets
	 * should implement a function which stores the metadata in a standard location.
	 */
	public void saveMeta(OutputStream os) throws IOException
		{
		//Add all objects
		Document document=saveXmlMetadata();
		
		//Add imageset XML
		Element imagesetEl=new Element("imageset");
		meta.saveMetadata(imagesetEl);
		document.getRootElement().addContent(imagesetEl);
		
		//Write out to disk
		
		Format format=Format.getPrettyFormat();
		XMLOutputter outputter = new XMLOutputter(format);

//		writeXmlData(document, os);

		outputter.output(document, os);
		setMetadataModified(false);
		}
	public void saveMeta(File outfile) throws IOException
		{
//		FileWriter writer = new FileWriter(outfile);
		FileOutputStream writer2=new FileOutputStream(outfile);
		saveMeta(writer2);
		writer2.close();
		}
	
	
	public void loadImagesetXmlMetadata(InputStream fileInputStream)
		{
		//Load metadata
		loadXmlMetadata(fileInputStream);
		for(String oi:metaObject.keySet())
			if(metaObject.get(oi) instanceof ImagesetMeta)
				{
				meta=(ImagesetMeta)metaObject.get(oi);
				metaObject.remove(oi);
				break;
			}
		}


	/**
	 * Images for one channel
	 */
	public abstract class ChannelImages
		{
		/** Private copy to channel specific meta data in meta */
		private ImagesetMeta.Channel meta;
				
		/** Image loaders */
		public TreeMap<Integer, TreeMap<Integer, EvImage>> imageLoader=new TreeMap<Integer, TreeMap<Integer, EvImage>>();

		/**
		 * Create a new channel
		 */
		public ChannelImages(ImagesetMeta.Channel channelName)
			{
			meta=channelName;
			}
		

		/****************************************************************************************/
		/******************************* Image data *********************************************/
		/****************************************************************************************/
		
		/**
		 * Get access to an image
		 */
		public EvImage getImageLoader(int frame, int z)
			{
			try
				{
				return imageLoader.get(frame).get(z);
				}
			catch(Exception e)
				{
				return null;
				}
			}
		
		/**
		 * Get or create an image
		 */
		public EvImage createImageLoader(int frame, int z)
			{
			EvImage im=getImageLoader(frame, z);
			if(im!=null)
				return im;
			else
				{
				TreeMap<Integer, EvImage> frames=imageLoader.get(frame);
				if(frames==null)
					{
					frames=new TreeMap<Integer, EvImage>();
					imageLoader.put(frame, frames);
					}
				im=internalMakeLoader(frame, z);
				frames.put(z, im);
				return im;
				}
			}

		protected abstract EvImage internalMakeLoader(int frame, int z);
		
	
		
		/****************************************************************************************/
		/******************************* Meta data **********************************************/
		/****************************************************************************************/

		
		/**
		 * Get channel specific meta data
		 */
		public ImagesetMeta.Channel getMeta()
			{
			return meta;
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
		
		

		/****************************************************************************************/
		/******************************* Find frames/z ******************************************/
		/****************************************************************************************/
		
		
		
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
				SortedMap<Integer, TreeMap<Integer,EvImage>> before=imageLoader.headMap(frame);
				SortedMap<Integer, TreeMap<Integer,EvImage>> after=imageLoader.tailMap(frame);
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
			SortedMap<Integer, TreeMap<Integer,EvImage>> before=imageLoader.headMap(frame); 
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
			SortedMap<Integer, TreeMap<Integer,EvImage>> after=imageLoader.tailMap(frame+1);
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
			TreeMap<Integer,EvImage> slices=imageLoader.get(frame);
			if(slices==null || slices.size()==0)
				return z;
			else
				{
				SortedMap<Integer,EvImage> before=slices.headMap(z);
				SortedMap<Integer,EvImage> after=slices.tailMap(z);
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
			TreeMap<Integer,EvImage> slices=imageLoader.get(frame);
			if(slices==null)
				return z;
			else
				{
				SortedMap<Integer,EvImage> after=slices.tailMap(z+1);
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
			TreeMap<Integer, EvImage> slices=imageLoader.get(frame);
			if(slices==null)
				return z;
			else
				{
				SortedMap<Integer, EvImage> before=slices.headMap(z);
				if(before.size()==0)
					return z;
				else
					return before.lastKey();
				}
			}		
		
		
		}

		



	}
