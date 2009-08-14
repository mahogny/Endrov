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


	/** List of all channels belonging to this imageset */
	//public Map<String,EvChannel> channelImages=new TreeMap<String,EvChannel>();
//	public HashMap<String,EvChannel> channelImages=new HashMap<String,EvChannel>();
	//Sorting only for viewing convenience. Can be done in controls otherwise
	
	
	
	
	/** Common resolution [px/um] */
	public double resX, resY, resZ; //TODO Deprecate Z once all OST converted. X and Y? or just keep these?
	
	/** Number of seconds each frame */
	public double metaTimestep=1; //TODO deprecate
	
	public double metaNA=0;
	public double metaObjective=1;
	public double metaOptivar=1;
	public double metaCampix=1;
	public double metaSlicespacing=1;
	//public String metaSampleID="";
	//public String metaDescription="";
	
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
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		
		for(String key:metaOther.keySet())
			{
			String val=metaOther.get(key);
			if(val!=null)
				e.addContent(new Element(key).addContent(val));
			}
		saveFrameMetadata(metaFrame, e);
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
					metaOther.put("sampleID", i.getValue());
				/*
				else if(i.getName().equals("sample"))
					metaSampleID=i.getValue();
				else if(i.getName().equals("description"))
					metaDescription=i.getValue();*/
				else if(i.getName().equals("channel"))
					extractChannel(i);
				else if(i.getName().equals("frame"))
					extractFrame(metaFrame, i);
				else
					metaOther.put(i.getName(), i.getValue());
				}
			catch (NumberFormatException e1)
				{
				EvLog.printError("Parse error, gracefully ignoring and resuming", e1);
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

		/**
		 * For 3.2 -> 3.3: move hardware meta
		 */
		for(EvChannel chan:getChannels().values())
			{
			if(!chan.metaOther.containsKey("tbu_NA"))
				chan.metaOther.put("tbu_NA", ""+metaNA);
			if(!chan.metaOther.containsKey("tbu_Objective"))
				chan.metaOther.put("tbu_Objective", ""+metaObjective);
			if(!chan.metaOther.containsKey("tbu_Optivar"))
				chan.metaOther.put("tbu_Optivar", ""+metaOptivar);
			if(!chan.metaOther.containsKey("tbu_Campix"))
				chan.metaOther.put("tbu_Campix", ""+metaCampix);
			
			if(chan.defaultResX==null)
				chan.defaultResX=resX;
			if(chan.defaultResY==null)
				chan.defaultResY=resY;
			}
		
		}

	/**
	 * Extract channel XML data
	 */
	public void extractChannel(Element e)
		{
		String chname=e.getAttributeValue("name");

		EvChannel ch=getCreateChannel(chname);

		//only needed for 3.2->3.3
		for(Object oi:e.getChildren())
			{
			Element i=(Element)oi;

			try
				{
				if(i.getName().equals("dispX"))
					{
					ch.defaultDispX=Double.parseDouble(i.getValue());
					//System.out.println("dispX =" +ch.dispX);
					}
				else if(i.getName().equals("dispY"))
					ch.defaultDispY=Double.parseDouble(i.getValue());
				else if(i.getName().equals("binning"))
					ch.chBinning=Integer.parseInt(i.getValue());
				else if(i.getName().equals("tbu_Binning"))
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
				EvLog.printError("Parse error, gracefully ignoring and resuming", e1);
				}
			}
//		System.out.println("chanframemeta "+ch.metaFrame);
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
			
			if(i.getName().equals("date")) //Throw away
				continue;
				
			frame.put(i.getName(), i.getValue());
			}

		}

	
	
	
	
	
	
	}
