/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.ev.EvLog;
import endrov.util.EvDecimal;
import endrov.util.EvListUtil;

/**
 * Images for one channel
 * 
 * @author Johan Henriksson
 */
public class EvChannel extends EvObject implements AnyEvImage
	{
	private final static String metaType = "channel";



	/****************************************************************************************/
	/******************************* Image data *********************************************/
	/****************************************************************************************/

	/** Image loaders */
	public TreeMap<EvDecimal, EvStack> imageLoader = new TreeMap<EvDecimal, EvStack>();

	/**
	 * Get access to an image
	 */
	/*public EvImage getImageLoader(EvDecimal frame, EvDecimal z)
		{
		try
			{
			return imageLoader.get(frame).get(z);
			}
		catch (Exception e)
			{
			return null;
			}
		}*/

	/*
	public EvImage getImageLoaderInt(EvDecimal frame, int z)
		{
		try
			{
			return imageLoader.get(frame).getInt(z);
			}
		catch (Exception e)
			{
			return null;
			}
		}*/

	/**
	 * Get the first stack. Convenience method; meant mainly to be used when the
	 * channel contains a single stack
	 */
	public EvStack getFirstStack()
		{
		return imageLoader.values().iterator().next();
		}

	/**
	 * Get a frame, create if needed. Should only be used if the content of the frame will be deleted, or otherwise ensure that data is correct
	 */
	public EvStack getCreateFrame(EvDecimal frame)
		{
		EvStack f = imageLoader.get(frame);
		if (f==null)
			imageLoader.put(frame, f = new EvStack());
		return f;
		}

	/**
	 * Get a frame
	 */
	public EvStack getFrame(EvDecimal frame)
		{
		return imageLoader.get(frame);
		}

	/****************************************************************************************/
	/******************************* Find frames/z ******************************************/
	/****************************************************************************************/

	/**
	 * Find out the closest frame
	 * 
	 * @param frame
	 *          Which frame to match against
	 * @return If there are no frames or there is an exact match, then frame.
	 *         Otherwise the closest frame.
	 */
	public EvDecimal closestFrame(EvDecimal frame)
		{
		return EvListUtil.closestFrame(imageLoader, frame);
		}

	/**
	 * Get the frame before
	 * 
	 * @param frame
	 *          Current frame
	 * @return The frame before or the same frame if no frame before found
	 */
	public EvDecimal closestFrameBefore(EvDecimal frame)
		{
		SortedMap<EvDecimal, EvStack> before = imageLoader.headMap(frame);
		if (before.size()==0)
			return frame;
		else
			return before.lastKey();
		}

	/**
	 * Get the frame strictly after. If there is no frame after, then frame is
	 * returned
	 * 
	 * @param frame
	 *          Current frame
	 * @return The frame after or the same frame if no frame after found
	 */
	public EvDecimal closestFrameAfter(EvDecimal frame)
		{
		// Can be made faster by iterator
		SortedMap<EvDecimal, EvStack> after = new TreeMap<EvDecimal, EvStack>(
				imageLoader.tailMap(frame));
		after.remove(frame);

		if (after.size()==0)
			return frame;
		else
			return after.firstKey();
		}


	/****************************************************************************************/
	/************************** Channel Meta data *******************************************/
	/****************************************************************************************/

	/** Binning, a scale factor from the microscope */
	public int chBinning = 1;

	/** Displacement um */
	public Vector3d defaultDisp=new Vector3d();
	//public double defaultDispX = 0, defaultDispY = 0, defaultDispZ = 0;

	/** Resolution um/px */
	public Double defaultResX = null, defaultResY = null, defaultResZ = null;

	/** Comppression 0-100, 100=lossless, what compression to apply to new images */
	public int compression = 100;

	/** Other */
	public HashMap<String, String> metaOther = new HashMap<String, String>();

	/** frame data */
	public HashMap<EvDecimal, HashMap<String, String>> metaFrame = new HashMap<EvDecimal, HashMap<String, String>>();

	/**
	 * Get property assigned to a frame
	 * 
	 * @param frame
	 *          Frame
	 * @param prop
	 *          Property
	 * @return Value of property or null if it does not exist
	 */
	public String getFrameMeta(EvDecimal frame, String prop)
		{
		HashMap<String, String> framedata = metaFrame.get(frame);
		if (framedata==null)
			return null;
		return framedata.get(prop);
		}

	public void setFrameMeta(EvDecimal frame, String prop, String value)
		{
		HashMap<String, String> framedata = metaFrame.get(frame);
		if (framedata==null)
			metaFrame.put(frame, framedata=new HashMap<String, String>());
		framedata.put(prop, value);
		}

	
	/** Get (other) meta data in form of a string (default="") */
	public String getMetaValueString(String s)
		{
		String t = metaOther.get(s);
		if (t==null)
			return "";
		else
			return t;
		}

	/** Get (other) meta data in form of a double (default=0) */
	public double getMetaValueDouble(String s)
		{
		String t = getMetaValueString(s);
		if (t.equals(""))
			return 0;
		else
			return Double.parseDouble(t);
		}

	/**
	 * Get a common frame. Creates structure if it does not exist.
	 */
	public HashMap<String, String> getMetaFrame(EvDecimal fid)
		{
		HashMap<String, String> frame = metaFrame.get(fid);
		if (frame==null)
			{
			frame = new HashMap<String, String>();
			metaFrame.put(fid, frame);
			}
		return frame;
		}

	/**
	 * Additional menu items for channel object
	 */
	public void buildMetamenu(JMenu menu)
		{
		JMenuItem miSwapTZ = new JMenuItem("Swap TZ-dimension");
		miSwapTZ.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					if (BasicWindow
							.showConfirmDialog("Do you really want to replace TZ?"))
						{
						StackHacks.swapTZ(EvChannel.this);
						BasicWindow.updateWindows();
						}
					}
			});
		menu.add(miSwapTZ);

		JMenuItem miSetResXYZ = new JMenuItem("Set XYZ-resolution");
		miSetResXYZ.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					String resX = BasicWindow.showInputDialog("Resolution X [px/um]", "");
					if (resX==null)
						return;
					String resY = BasicWindow.showInputDialog("Resolution Y [px/um]", "");
					if (resY==null)
						return;
					String resZ = BasicWindow.showInputDialog(
							"Resolution Z [um/px] (opposite!)", "");
					if (resZ==null)
						return;

					StackHacks.setResXYZ(EvChannel.this, Double.parseDouble(resX), Double.parseDouble(resY), Double.parseDouble(resZ));
					BasicWindow.updateWindows();
					}
			});
		menu.add(miSetResXYZ);

		JMenuItem miSetResT = new JMenuItem("Set T-resolution");
		miSetResT.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					String resT = BasicWindow.showInputDialog(
							"Resolution dt, time between stacks [s]", "");
					if (resT==null)
						return;

					StackHacks.setResT(EvChannel.this, new EvDecimal(resT));
					BasicWindow.updateWindows();
					}
			});
		menu.add(miSetResT);

		}

	public String getMetaTypeDesc()
		{
		return "Channel";
		}

	public void loadMetadata(Element e)
		{
		double defaultDispX=0;
		double defaultDispY=0;
		double defaultDispZ=0;
		Double defaultDispXpx=null;
		Double defaultDispYpx=null;
		
		for (Object oi : e.getChildren())
			{
			Element i = (Element) oi;

			
			try
				{
				if (i.getName().equals("dispX"))
					defaultDispXpx = Double.parseDouble(i.getValue());
				else if (i.getName().equals("dispY"))
					defaultDispYpx = Double.parseDouble(i.getValue());
				
				else if (i.getName().equals("dispXum"))
					defaultDispX = Double.parseDouble(i.getValue());
				else if (i.getName().equals("dispYum"))
					defaultDispY = Double.parseDouble(i.getValue());
				
				else if (i.getName().equals("dispZ"))
					defaultDispZ = Double.parseDouble(i.getValue());
				
				else if (i.getName().equals("resX"))
					defaultResX = Double.parseDouble(i.getValue());
				else if (i.getName().equals("resY"))
					defaultResY = Double.parseDouble(i.getValue());
				
				
				else if (i.getName().equals("resZ"))
					defaultResZ = Double.parseDouble(i.getValue());

				else if (i.getName().equals("binning"))
					chBinning = Integer.parseInt(i.getValue());
				else if (i.getName().equals("tbu_Binning"))
					chBinning = Integer.parseInt(i.getValue());
				else if (i.getName().equals("compression"))
					compression = Integer.parseInt(i.getValue());
				else if (i.getName().equals("frame"))
					extractFrame(metaFrame, i);
				else
					metaOther.put(i.getName(), i.getValue());
				}
			catch (NumberFormatException e1)
				{
				e1.printStackTrace();
				EvLog.printError("Parse error, gracefully ignoring and resuming", e1);
				}
			
			}
		

		//Convert old displacement into um
		if(defaultDispXpx!=null)
			defaultDispX=defaultDispXpx*defaultResX;
		if(defaultDispYpx!=null)
			defaultDispY=defaultDispYpx*defaultResY;
		
		//Set disp vector
		defaultDisp=new Vector3d(-defaultDispX, -defaultDispY, -defaultDispZ);
		}

	public String saveMetadata(Element e)
		{
		//For conversion!
		metaOther.remove("dispX");
		metaOther.remove("dispY");
		
		//Retrieve default stack settings
		if(!imageLoader.isEmpty())
			{
			EvStack fstack=getFirstStack();
			
			defaultResX=fstack.resX;
			defaultResY=fstack.resY;
			defaultResZ=fstack.resZ;

			defaultDisp=fstack.getDisplacement();
/*			Vector3d sDisp=fstack.getDisplacement();
			defaultDispX=sDisp.x;
			defaultDispY=sDisp.y;
			defaultDispZ=sDisp.z;
			*/

			metaOther.put("resX", ""+defaultResX);
			metaOther.put("resY", ""+defaultResY);
			metaOther.put("resZ", ""+defaultResZ);
			
			metaOther.put("dispXum", ""+-defaultDisp.x);
			metaOther.put("dispYum", ""+-defaultDisp.y);
			metaOther.put("dispZum", ""+-defaultDisp.z);
			
			}

		e.addContent(new Element("tbu_Binning").addContent(""+chBinning));
		e.addContent(new Element("comression").addContent(""+compression));
		for(String key:metaOther.keySet())
			e.addContent(new Element(key).addContent(""+metaOther.get(key)));
		
		for(EvDecimal frame:new TreeSet<EvDecimal>(imageLoader.keySet()))
			{
			HashMap<String,String> otherMeta=metaFrame.get(frame);
			if(otherMeta==null)
				{
//				System.out.println("No meta for frame "+frame);
				//TODO: look for data elsewhere and repair?
				otherMeta=new HashMap<String, String>();
				}
			


			//Override default stack settings?
			EvStack stack=imageLoader.get(frame);
			Vector3d sDisp=stack.getDisplacement();
			//stack.getResbinZinverted(); //TODO will not be needed later
			if(stack.resX!=defaultResX)
				otherMeta.put("resX", ""+stack.resX);
			else
				otherMeta.remove("resX");
			if(stack.resY!=defaultResY)
				otherMeta.put("resY", ""+stack.resY);
			else
				otherMeta.remove("resY");
			if(stack.resZ!=defaultResZ)
				otherMeta.put("resZ", ""+stack.resZ);
			else
				otherMeta.remove("resZ");
			
			if(sDisp.x!=defaultDisp.x)
				otherMeta.put("dispXum", ""+-sDisp.x);
			else
				otherMeta.remove("dispXum");
			
			if(sDisp.y!=defaultDisp.y)
				otherMeta.put("dispYum", ""+-sDisp.y);
			else
				otherMeta.remove("dispYum");
			
			if(sDisp.z!=defaultDisp.z)
				otherMeta.put("dispZ", ""+-sDisp.z);
			else
				otherMeta.remove("dispZ");
			
			
			if(!otherMeta.isEmpty())
				{
				Element frameEl = new Element("frame");
				frameEl.setAttribute("frame", frame.toString());
				for (String field : otherMeta.keySet())
					{
					String value = otherMeta.get(field);
					Element fieldEl = new Element(field);
					fieldEl.addContent(value);
					frameEl.addContent(fieldEl);
					}
				e.addContent(frameEl);
				}
			
			}
		
		return metaType;
		}


	/**
	 * Get frame metadata
	 */
	public void extractFrame(HashMap<EvDecimal, HashMap<String, String>> metaFrame, Element e)
		{
		EvDecimal fid = new EvDecimal(e.getAttributeValue("frame"));
		//System.out.println("got frame "+fid);
		for (Object oi : e.getChildren())
			{
			
			Element i = (Element) oi;
			HashMap<String, String> frame = metaFrame.get(fid);
			if (frame==null)
				{
				frame = new HashMap<String, String>();
				metaFrame.put(fid, frame);
				}
			frame.put(i.getName(), i.getValue());
			}

		}

	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType, EvChannel.class);
		}
	}
