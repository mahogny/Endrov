package endrov.imageset;

import java.io.*;
import java.util.*;

import javax.swing.JMenu;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import endrov.data.*;
import endrov.imageWindow.ImageWindow;
import endrov.imageset.ImagesetMeta.Channel;
import endrov.imageset.ImagesetMeta.ImagesetMetaObjectExtension;
import endrov.util.EvDecimal;

/**
 * Interface to one imageset + metadata
 * @author Johan Henriksson
 */
public class ImagesetNew extends EvObject
	{
	/** List of all channels belonging to this imageset */
	public HashMap<String,ChannelImages> channelImages=new HashMap<String,ChannelImages>();
	
	

	


	
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
			ImagesetNew.Channel m=getCreateChannelMeta(ch);
			im=new ChannelImages(m);
			channelImages.put(ch, im);
			}
		return im;
		}

	/**
	 * Remove channel images and metadata
	 */
	public void removeChannel(String ch)
		{
		channelImages.remove(ch);
		channelMeta.remove(ch);
		}
	
	
	
	/**
	 * Save metadata to some specific files; mostly for imageset internal use. Implementations of imagesets
	 * should implement a function which stores the metadata in a standard location.
	 */
	/*
	public void saveMeta(OutputStream os) throws IOException
		{
		//Add all objects
		Document document=saveXmlMetadata();
		
		//Add imageset XML
		Element imagesetEl=new Element("imageset");
		saveMetadata(imagesetEl);
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
*/
	
	/**
	 * Get access to an image
	 */
	public EvImage getImageLoader(String channel, EvDecimal frame, EvDecimal z)
		{
		ChannelImages chim=channelImages.get(channel);
		if(chim!=null)
			return chim.getImageLoader(frame, z);
		else
			return null;
		}

	/**
	 * Images for one channel
	 */
	public class ChannelImages
		{
		/** Private copy to channel specific meta data in meta */
		private ImagesetMeta.Channel meta;
				
		/** Image loaders */
		public TreeMap<EvDecimal, TreeMap<EvDecimal, EvImage>> imageLoader=new TreeMap<EvDecimal, TreeMap<EvDecimal, EvImage>>();

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
		public EvImage getImageLoader(EvDecimal frame, EvDecimal z)
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
		public EvImage createImageLoader(EvDecimal frame, EvDecimal z)
			{
			EvImage im=getImageLoader(frame, z);
			if(im!=null)
				return im;
			else
				{
				TreeMap<EvDecimal, EvImage> frames=imageLoader.get(frame);
				if(frames==null)
					{
					frames=new TreeMap<EvDecimal, EvImage>();
					imageLoader.put(frame, frames);
					}
				im=new EvImage();
				frames.put(z, im);
				return im;
				}
			}

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
		public String getFrameMeta(EvDecimal frame, String prop)
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
		public EvDecimal closestFrame(EvDecimal frame)
			{
			if(imageLoader.get(frame)!=null || imageLoader.size()==0)
				return frame;
			else
				{
				SortedMap<EvDecimal, TreeMap<EvDecimal,EvImage>> before=imageLoader.headMap(frame);
				SortedMap<EvDecimal, TreeMap<EvDecimal,EvImage>> after=imageLoader.tailMap(frame);
				if(before.size()==0)
					return imageLoader.firstKey();
				else if(after.size()==0)
					return imageLoader.lastKey();
				else
					{
					EvDecimal afterkey=after.firstKey();
					EvDecimal beforekey=before.lastKey();
					
					if(afterkey.subtract(frame).less(frame.subtract(beforekey)))
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
		public EvDecimal closestFrameBefore(EvDecimal frame)
			{
			SortedMap<EvDecimal, TreeMap<EvDecimal,EvImage>> before=imageLoader.headMap(frame); 
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
		public EvDecimal closestFrameAfter(EvDecimal frame)
			{
			//Can be made faster by iterator
			SortedMap<EvDecimal, TreeMap<EvDecimal,EvImage>> after=new TreeMap<EvDecimal, TreeMap<EvDecimal,EvImage>>(imageLoader.tailMap(frame));
			after.remove(frame);
			
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
		public EvDecimal closestZ(EvDecimal frame, EvDecimal z)
			{
			TreeMap<EvDecimal,EvImage> slices=imageLoader.get(frame);
			if(slices==null || slices.size()==0)
				return z;
			else
				{
				SortedMap<EvDecimal,EvImage> before=slices.headMap(z);
				SortedMap<EvDecimal,EvImage> after=slices.tailMap(z);
				if(before.size()==0)
					return after.firstKey();
				else if(after.size()==0)
					return before.lastKey();
				else
					{
					EvDecimal afterkey=after.firstKey();
					EvDecimal beforekey=before.lastKey();
					
					if(afterkey.subtract(z).less(z.subtract(beforekey)))
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
		public EvDecimal closestZAbove(EvDecimal frame, EvDecimal z)
			{
			TreeMap<EvDecimal,EvImage> slices=imageLoader.get(frame);
			if(slices==null)
				return z;
			else
				{
				//Can be made faster
				SortedMap<EvDecimal,EvImage> after=new TreeMap<EvDecimal, EvImage>(slices.tailMap(z));
				after.remove(z);
				
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
		public EvDecimal closestZBelow(EvDecimal frame, EvDecimal z)
			{
			TreeMap<EvDecimal, EvImage> slices=imageLoader.get(frame);
			if(slices==null)
				return z;
			else
				{
				SortedMap<EvDecimal, EvImage> before=slices.headMap(z);
				if(before.size()==0)
					return z;
				else
					return before.lastKey();
				}
			}		
		
		
		}

		

	public static ImagesetNew castEmpty(EvObject data)
		{
		if(data instanceof ImagesetNew)
			return (ImagesetNew)data;
		else
			return new EmptyImageset();
		}

	public static ImagesetNew castNull(EvObject data)
		{
		if(data instanceof ImagesetNew)
			return (ImagesetNew)data;
		else
			return null;
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="imageset";
	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ImagesetImageExtension());
		EvData.extensions.put(metaType,new ImagesetMetaObjectExtension());
		
		
		}

	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	public static class ImagesetMetaObjectExtension implements EvObjectType
		{
		public EvObject extractObjects(Element e)
			{
			ImagesetNew meta=new ImagesetNew();
			
			for(Object oi:e.getChildren())
				{
				Element i=(Element)oi;
				
				if(i.getName().equals("timestep"))
					meta.metaTimestep=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resX"))
					meta.resX=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resY"))
					meta.resY=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resZ"))
					meta.resZ=Double.parseDouble(i.getValue());
				else if(i.getName().equals("NA"))
					meta.metaNA=Double.parseDouble(i.getValue());
				else if(i.getName().equals("objective"))
					meta.metaObjective=Double.parseDouble(i.getValue());
				else if(i.getName().equals("optivar"))
					meta.metaOptivar=Double.parseDouble(i.getValue());
				else if(i.getName().equals("campix"))
					meta.metaCampix=Double.parseDouble(i.getValue());
				else if(i.getName().equals("slicespacing"))
					meta.metaSlicespacing=Double.parseDouble(i.getValue());
				else if(i.getName().equals("sample"))
					meta.metaSample=i.getValue();
				else if(i.getName().equals("description"))
					meta.metaDescript=i.getValue();
				else if(i.getName().equals("channel"))
					{
					ImagesetNew.Channel ch=extractChannel(meta, i);
					meta.channelMeta.put(ch.name, ch);
					}
				else if(i.getName().equals("frame"))
					extractFrame(meta.metaFrame, i);
				else
					meta.metaOther.put(i.getName(), i.getValue());
				}
			
			return meta;
			}
		
		/**
		 * Extract channel XML data
		 */
		public ImagesetNew.Channel extractChannel(ImagesetNew data, Element e)
			{
			ImagesetNew.Channel ch=new ImagesetNew.Channel();
			ch.name=e.getAttributeValue("name");
			
			for(Object oi:e.getChildren())
				{
				Element i=(Element)oi;
				
				if(i.getName().equals("dispX"))
					ch.dispX=Double.parseDouble(i.getValue());
				else if(i.getName().equals("dispY"))
					ch.dispY=Double.parseDouble(i.getValue());
				else if(i.getName().equals("binning"))
					ch.chBinning=Integer.parseInt(i.getValue());
				else if(i.getName().equals("compression"))
					ch.compression=Integer.parseInt(i.getValue());
				else if(i.getName().equals("frame"))
					extractFrame(ch.metaFrame, i);
				else
					ch.metaOther.put(i.getName(), i.getValue());
				}
			
			return ch;
			}
		
		/**
		 * Get frame metadata
		 */
		public void extractFrame(HashMap<Integer,HashMap<String,String>> metaFrame, Element e)
			{
			int fid=Integer.parseInt(e.getAttributeValue("frame"));
			for(Object oi:e.getChildren())
				{
				Element i=(Element)oi;
				HashMap<String,String> frame=metaFrame.get(fid);
				if(frame==null)
					{
					frame=new HashMap<String,String>();
					metaFrame.put(fid, frame);
					}
				frame.put(i.getName(), i.getValue());
				}
			
			}
		
		}

	
	/******************************************************************************************************
	 *                               Channel                                                              *
	 *****************************************************************************************************/

	/**
	 * Channel specific meta data
	 */
	public static class Channel
		{
		public String name;
		
		/** Binning, a scale factor from the microscope */
		public int chBinning=1;
		
		/** Displacement */
		public double dispX=0, dispY=0;
		
		/** Comppression 0-100, 100=lossless, what compression to apply to new images */
		public int compression=100;
		
		/** Other */
		public HashMap<String,String> metaOther=new HashMap<String,String>();
		
		/** frame data */
		public HashMap<Integer,HashMap<String,String>> metaFrame=new HashMap<Integer,HashMap<String,String>>();
		
		
		
		
		/** Get (other) meta data in form of a string (default="") */
		public String getMetaValueString(String s)
			{
			String t=metaOther.get(s);
			if(t==null)	return "";
			else return t;
			}

		/** Get (other) meta data in form of a double (default=0) */
		public double getMetaValueDouble(String s)
			{
			String t=getMetaValueString(s);
			if(t.equals("")) return 0;
			else return Double.parseDouble(t);
			}
		
		/**
		 * Get a common frame. Creates structure if it does not exist.
		 */
		public HashMap<String,String> getMetaFrame(int fid)
			{
			HashMap<String,String> frame=metaFrame.get(fid);
			if(frame==null)
				{
				frame=new HashMap<String,String>();
				metaFrame.put(fid, frame);
				}
			return frame;
			}
		}

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	/** Common resolution [px/um] */
	public double resX, resY, resZ; //TODO Deprecate Z once all OST converted
	
	/** Number of seconds each frame */
	public double metaTimestep=1;
	
	public double metaNA=0;
	public double metaObjective=1;
	public double metaOptivar=1;
	public double metaCampix=1;
	public double metaSlicespacing=1;
	public String metaSample="";
	public String metaDescript="";
	
	/** Other */
	public HashMap<String,String> metaOther=new HashMap<String,String>();
	
	/** Frame data */
	public HashMap<Integer,HashMap<String,String>> metaFrame=new HashMap<Integer,HashMap<String,String>>();
	
	/** Channel specific data */
	public HashMap<String,Channel> channelMeta=new HashMap<String,Channel>();

	public String getMetaTypeDesc()
		{
		return metaType;
		}

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	/** Get (other) meta data in form of a string (default="") */
	public String getMetaValueString(String s)
		{
		String t=metaOther.get(s);
		if(t==null)	return "";
		else return t;
		}

	/** Get (other) meta data in form of a double (default=0) */
	public double getMetaValueDouble(String s)
		{
		String t=getMetaValueString(s);
		if(t.equals("")) return 0;
		else return Double.parseDouble(t);
		}
	
	/**
	 * Get a common frame. Creates structure if it does not exist.
	 */
	public HashMap<String,String> getMetaFrame(int fid)
		{
		HashMap<String,String> frame=metaFrame.get(fid);
		if(frame==null)
			{
			frame=new HashMap<String,String>();
			metaFrame.put(fid, frame);
			}
		return frame;
		}
	
	
	/**
	 * Get a channel. Creates structure if it does not exist.
	 */
	public Channel getCreateChannelMeta(String ch)
		{
		Channel c=channelMeta.get(ch);
		if(c==null)
			{
			c=new Channel();
			c.name=ch;
			channelMeta.put(ch,c);
			}
		return c;
		}
	
	/**
	 * Save down data
	 */
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		
		//Common
		e.addContent(new Element("resX").addContent(""+resX));
		e.addContent(new Element("resY").addContent(""+resY));
		e.addContent(new Element("resZ").addContent(""+resZ));
		e.addContent(new Element("timestep").addContent(""+metaTimestep));
		e.addContent(new Element("NA").addContent(""+metaNA));
		e.addContent(new Element("objective").addContent(""+metaObjective));
		e.addContent(new Element("optivar").addContent(""+metaOptivar));
		e.addContent(new Element("campix").addContent(""+metaCampix));
		e.addContent(new Element("slicespacing").addContent(""+metaSlicespacing));
		e.addContent(new Element("sample").addContent(""+metaSample));
		e.addContent(new Element("description").addContent(""+metaDescript));
		for(String key:metaOther.keySet())
			e.addContent(new Element(key).addContent(""+metaOther.get(key)));
		saveFrameMetadata(metaFrame, e);
		
		//Channels
		for(Channel ch:channelMeta.values())
			{
			Element elOstChannel=new Element("channel");
			elOstChannel.setAttribute("name", ch.name);
			e.addContent(elOstChannel);
			
			elOstChannel.addContent(new Element("binning").addContent(""+ch.chBinning));
			elOstChannel.addContent(new Element("dispX").addContent(""+ch.dispX));
			elOstChannel.addContent(new Element("dispY").addContent(""+ch.dispY));
			elOstChannel.addContent(new Element("comression").addContent(""+ch.compression));
			for(String key:ch.metaOther.keySet())
				elOstChannel.addContent(new Element(key).addContent(""+ch.metaOther.get(key)));
			saveFrameMetadata(ch.metaFrame, elOstChannel);
			}
		
		}
	

	/**
	 * Save down frame data
	 */
	private static void saveFrameMetadata(HashMap<Integer,HashMap<String,String>> fd, Element e)
		{
		for(int fid:fd.keySet())
			{
			Element frameEl=new Element("frame");
			frameEl.setAttribute("frame", ""+fid);
			
			HashMap<String,String> frame=fd.get(fid);
			for(String field:frame.keySet())
				{
				String value=frame.get(field);
				Element fieldEl=new Element(field);
				fieldEl.addContent(value);
				frameEl.addContent(fieldEl);
				}
			
			e.addContent(frameEl);
			}
		}
	
	
	
	
	
	
	
	
	
	
	}
