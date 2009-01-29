package endrov.imageset;

import java.util.*;

import javax.swing.JMenu;

import org.jdom.*;

import endrov.data.*;
import endrov.ev.Log;
import endrov.imageWindow.ImageWindow;
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
		ImageWindow.addImageWindowExtension(new ImagesetImageExtension());
		EvData.extensions.put(metaType,Imageset.class);
		
		
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/


	/** List of all channels belonging to this imageset */
	public HashMap<String,EvChannel> channelImages=new HashMap<String,EvChannel>();

	/** Common resolution [px/um] */
	public double resX, resY, resZ; //TODO Deprecate Z once all OST converted. X and Y? or just keep these?
	
	/** Number of seconds each frame */
	public double metaTimestep=1; //TODO deprecate
	
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
		return channelImages.get(ch);
		}
	
	
	/**
	 * Create a channel if it doesn't exist
	 */
	public EvChannel getCreateChannel(String ch)
		{
		EvChannel im=channelImages.get(ch);
		if(im==null)
			channelImages.put(ch, im=new EvChannel());
		return im;
		}


	
	
	/**
	 * Remove channel images and metadata
	 */
	public void removeChannel(String ch)
		{
		channelImages.remove(ch);
		}
	
	
	
	
	
	
	/**
	 * Get access to an image
	 */
	public EvImage getImageLoader(String channel, EvDecimal frame, EvDecimal z)
		{
		EvChannel chim=channelImages.get(channel);
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
		for(Map.Entry<String, EvChannel> entry:channelImages.entrySet())
			{
			EvChannel ch=entry.getValue();
			
			Element elOstChannel=new Element("channel");
			elOstChannel.setAttribute("name", entry.getKey());
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
				if(i.getName().equals("timestep"))
					metaTimestep=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resX"))
					resX=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resY"))
					resY=Double.parseDouble(i.getValue());
				else if(i.getName().equals("resZ"))
					resZ=Double.parseDouble(i.getValue());
				else if(i.getName().equals("NA"))
					metaNA=Double.parseDouble(i.getValue());
				else if(i.getName().equals("objective"))
					metaObjective=Double.parseDouble(i.getValue());
				else if(i.getName().equals("optivar"))
					metaOptivar=Double.parseDouble(i.getValue());
				else if(i.getName().equals("campix"))
					metaCampix=Double.parseDouble(i.getValue());
				else if(i.getName().equals("slicespacing"))
					metaSlicespacing=Double.parseDouble(i.getValue());
				else if(i.getName().equals("sample"))
					metaSample=i.getValue();
				else if(i.getName().equals("description"))
					metaDescript=i.getValue();
				else if(i.getName().equals("channel"))
					extractChannel(i);
				else if(i.getName().equals("frame"))
					extractFrame(metaFrame, i);
				else
					metaOther.put(i.getName(), i.getValue());
				}
			catch (NumberFormatException e1)
				{
				Log.printError("Parse error, gracefully ignoring and resuming", e1);
				}
			}
		
		//Handle fucked up imagesets. Should not be used!
		if(resZ==0)
			resZ=1;
		if(resX==0)
			resX=1;
		if(resY==0)
			resY=1;
		if(metaTimestep==0)
			metaTimestep=1;
		}

	/**
	 * Extract channel XML data
	 */
	public void extractChannel(Element e)
		{
		String chname=e.getAttributeValue("name");

		EvChannel ch=getCreateChannel(chname);

		for(Object oi:e.getChildren())
			{
			Element i=(Element)oi;

			try
				{
				if(i.getName().equals("dispX"))
					{
					ch.dispX=Double.parseDouble(i.getValue());
					//System.out.println("dispX =" +ch.dispX);
					}
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
			catch (NumberFormatException e1)
				{
				Log.printError("Parse error, gracefully ignoring and resuming", e1);
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
