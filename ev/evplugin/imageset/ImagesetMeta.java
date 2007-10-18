package evplugin.imageset;

import org.jdom.*;
import java.util.*;
import javax.swing.*;

import evplugin.data.*;
import evplugin.imageWindow.*;

public class ImagesetMeta extends EvObject
	{
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
			ImagesetMeta meta=new ImagesetMeta();
			
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
					ImagesetMeta.Channel ch=extractChannel(meta, i);
					meta.channel.put(ch.name, ch);
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
		public ImagesetMeta.Channel extractChannel(ImagesetMeta data, Element e)
			{
			ImagesetMeta.Channel ch=new ImagesetMeta.Channel();
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
	public double resX, resY, resZ;
	
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
	public HashMap<String,Channel> channel=new HashMap<String,Channel>();

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
	public Channel getChannel(String ch)
		{
		Channel c=channel.get(ch);
		if(c==null)
			{
			c=new Channel();
			c.name=ch;
			channel.put(ch,c);
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
		for(Channel ch:channel.values())
			{
			Element elOstChannel=new Element("channel");
			elOstChannel.setAttribute("name", ch.name);
			e.addContent(elOstChannel);
			
			elOstChannel.addContent(new Element("binning").addContent(""+ch.chBinning));
			elOstChannel.addContent(new Element("dispX").addContent(""+ch.dispX));
			elOstChannel.addContent(new Element("dispY").addContent(""+ch.dispY));
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
