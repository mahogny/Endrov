/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.util.*;

import javax.swing.JMenu;

import org.jdom.*;

import endrov.data.*;
import endrov.ev.EvLog;
import endrov.util.EvDecimal;

/**
 * Interface to one imageset + metadata
 * @author Johan Henriksson
 */
public class Imageset extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="imageset";
	
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,Imageset.class);
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	public Set<String> tags=new TreeSet<String>();
	
	
	public String getSampleID()
		{
		String val=metaOther.get("sampleID");
		if(val==null)
			return "";
		else
			return val;
		}

	public String getDescription()
		{
		String val=metaOther.get("description");
		if(val==null)
			return "";
		else
			return val;
		}

	public void setSampleID(String s)
		{
		metaOther.put("sampleID",s);
		}

	public void setDescription(String s)
		{
		metaOther.put("description",s);
		}

	/** Other */
	public HashMap<String,String> metaOther=new HashMap<String,String>();
	
	/** Frame data */
	public HashMap<EvDecimal,HashMap<String,String>> metaFrame=new HashMap<EvDecimal,HashMap<String,String>>();

	public String getMetaTypeDesc()
		{
		return metaType;
		}

	
	/**
	 * Get channel or null if it doesn't exist
	 */
	public EvChannel getChannel(String ch)
		{
		if(ch==null)
			return null;
		EvObject ob=metaObject.get(ch);
		if(ob==null)
			return null;
		else if(ob instanceof EvChannel)
			return (EvChannel)ob;
		else return null;
		}
	
	/**
	 * Channels and their names directly below. Changes to the map will not be reflected down
	 */
	public Map<String,EvChannel> getChannels()
		{
		return getIdObjects(EvChannel.class);
		}
	
	
	/**
	 * Create a channel if it doesn't exist
	 */
	public EvChannel getCreateChannel(String ch)
		{
		EvChannel im=getChannel(ch);
		if(im==null)
			metaObject.put(ch, im=new EvChannel());
		return im;
		}


	
	
	/**
	 * Remove channel images and metadata
	 */
	public void removeChannel(String ch)
		{
		metaObject.remove(ch);
		}
	
	
	
	
	
	
	/**
	 * Get access to an image
	 */
	public EvImage getImageLoader(String channel, EvDecimal frame, EvDecimal z)
		{
		EvChannel chim=getChannel(channel);
		if(chim!=null)
			return chim.getImageLoader(frame, z);
		else
			return null;
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
	public HashMap<String,String> getMetaFrame(EvDecimal fid)
		{
		HashMap<String,String> frame=metaFrame.get(fid);
		if(frame==null)
			metaFrame.put(fid, frame=new HashMap<String,String>());
		return frame;
		}
	
	
	/**
	 * Save down data
	 */
	public String saveMetadata(Element e)
		{
		for(String key:metaOther.keySet())
			{
			String val=metaOther.get(key);
			if(val!=null)
				e.addContent(new Element(key).addContent(val));
			}
		saveFrameMetadata(metaFrame, e);

		//Add all tags
		for(String tag:tags)
			{
			Element ne=new Element("tag");
			ne.setAttribute("name", tag);
			e.addContent(ne);
			}
		
		return metaType;
		}
	

	/**
	 * Save down frame data
	 * 
	 * TODO should this exist? first need to move to channel meta.
	 */
	private static void saveFrameMetadata(HashMap<EvDecimal,HashMap<String,String>> fd, Element e)
		{
		for(EvDecimal fid:fd.keySet())
			{
			Element frameEl=new Element("frame");
			frameEl.setAttribute("frame", fid.toString());
			
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
	
	
	

	

	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/


	public void loadMetadata(Element e)
		{
		for(Object oi:e.getChildren())
			{
			Element i=(Element)oi;

			try
				{
				if(i.getName().equals("frame"))
					extractFrame(metaFrame, i);
				else if(i.getName().equals("tag"))
					tags.add(i.getAttributeValue("name"));
				else
					metaOther.put(i.getName(), i.getValue());
				}
			catch (NumberFormatException e1)
				{
				EvLog.printError("Parse error, gracefully ignoring and resuming", e1);
				}
			}

		

		
		
		}


	/**
	 * Get frame metadata
	 */
	public void extractFrame(HashMap<EvDecimal,HashMap<String,String>> metaFrame, Element e)
		{
		EvDecimal fid=new EvDecimal(e.getAttributeValue("frame"));
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
