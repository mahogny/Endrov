/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeImageset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.core.log.EvLog;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.gui.window.EvBasicWindow;
import endrov.util.ProgressHandle;
import endrov.util.collection.EvListUtil;
import endrov.util.collection.MemoizeX;
import endrov.util.collection.MemoizeXImmediate;
import endrov.util.math.EvDecimal;

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
	private TreeMap<EvDecimal, MemoizeX<EvStack>> imageLoader = new TreeMap<EvDecimal, MemoizeX<EvStack>>();
	
	
	public EvDecimal getFirstFrame()
		{
		return imageLoader.firstKey(); 
		}
	
	public EvDecimal getLastFrame()
		{
		return imageLoader.lastKey();
		}
	
	public Set<EvDecimal> getFrames()
		{
		return imageLoader.keySet();
		}
	
	
	
	public EvStack getStack(ProgressHandle progh, EvDecimal frame)
		{
		MemoizeX<EvStack> stack=imageLoader.get(frame);
		if(stack==null)
			return null;
		else
			return stack.get(progh);
		}
	
	public MemoizeX<EvStack> getStackLazy(EvDecimal frame)
		{
		return imageLoader.get(frame);
		}
	
	public EvStack getStack(EvDecimal frame)
		{
		MemoizeX<EvStack> stack=imageLoader.get(frame);
		if(stack==null)
			return null;
		else
			return stack.get(new ProgressHandle());
		}
	
	public void putStack(EvDecimal frame, EvStack stack)
		{
		if(frame==null)
			throw new RuntimeException("frame is null");
		imageLoader.put(frame, new MemoizeXImmediate<EvStack>(stack));
		}

	public void putStack(EvDecimal frame, MemoizeXImmediate<EvStack> stack)
		{
		imageLoader.put(frame, stack);
		}

	public void removeStack(EvDecimal frame)
		{
		imageLoader.remove(frame);
		}
	

	/**
	 * Use with care
	 */
	public void __clearputStacksFrom(EvChannel ch)
		{
		imageLoader.clear();
		imageLoader.putAll(ch.imageLoader);
		}
	
	

	/**
	 * Get the first stack. Convenience method; meant mainly to be used when the
	 * channel contains a single stack
	 */
	public EvStack getFirstStack(ProgressHandle progh)
		{
		MemoizeX<EvStack> stack=imageLoader.values().iterator().next();
		if(stack==null)
			return null;
		else
			return stack.get(progh);
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
		SortedMap<EvDecimal, MemoizeX<EvStack>> before = imageLoader.headMap(frame);
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
		SortedMap<EvDecimal, MemoizeX<EvStack>> after = new TreeMap<EvDecimal, MemoizeX<EvStack>>(
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
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		JMenuItem miSwapTZ = new JMenuItem("Swap TZ-dimension");
		miSwapTZ.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					if (EvBasicWindow
							.showConfirmYesNoDialog("Do you really want to replace TZ?"))
						{
						StackHacks.swapTZ(EvChannel.this);
						EvBasicWindow.updateWindows();
						}
					}
			});
		menu.add(miSwapTZ);

		JMenuItem miSetResXYZ = new JMenuItem("Set XYZ-resolution");
		miSetResXYZ.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					String resX = EvBasicWindow.showInputDialog("Resolution X [px/um]", "");
					if (resX==null)
						return;
					String resY = EvBasicWindow.showInputDialog("Resolution Y [px/um]", "");
					if (resY==null)
						return;
					String resZ = EvBasicWindow.showInputDialog(
							"Resolution Z [um/px] (opposite!)", "");
					if (resZ==null)
						return;

					StackHacks.setResXYZ(EvChannel.this, Double.parseDouble(resX), Double.parseDouble(resY), Double.parseDouble(resZ));
					EvBasicWindow.updateWindows();
					}
			});
		menu.add(miSetResXYZ);

		JMenuItem miSetResT = new JMenuItem("Set T-resolution");
		miSetResT.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					String resT = EvBasicWindow.showInputDialog(
							"Resolution dt, time between stacks [s]", "");
					if (resT==null)
						return;

					StackHacks.setResT(EvChannel.this, new EvDecimal(resT));
					EvBasicWindow.updateWindows();
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
				
				else if (i.getName().equals("dispZ") || i.getName().equals("dispZum"))
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
		Vector3d vecDefaultRes=new Vector3d();
		
		//For conversion!
		metaOther.remove("dispX");
		metaOther.remove("dispY");
		
		//Retrieve default stack settings
		if(!imageLoader.isEmpty())
			{
			EvStack fstack=getFirstStack(new ProgressHandle());
			
			vecDefaultRes=fstack.getRes();
			defaultResX=vecDefaultRes.x;
			defaultResY=vecDefaultRes.y;
			defaultResZ=vecDefaultRes.z;

			defaultDisp=fstack.getDisplacement();

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
			EvStack stack=getStack(frame);
			
			if(!vecDefaultRes.equals(stack.getRes()))
				{
				otherMeta.put("resX", ""+stack.getRes().x);
				otherMeta.put("resY", ""+stack.getRes().y);
				otherMeta.put("resZ", ""+stack.getRes().z);
				}
			else
				{
				otherMeta.remove("resX");
				otherMeta.remove("resY");
				otherMeta.remove("resZ");
				}
			
			Vector3d sDisp=stack.getDisplacement();
			if(!sDisp.equals(defaultDisp))
				{
				otherMeta.put("dispXum", ""+-sDisp.x);
				otherMeta.put("dispYum", ""+-sDisp.y);
				otherMeta.put("dispZ", ""+-sDisp.z);
				}
			else
				{
				otherMeta.remove("dispXum");
				otherMeta.remove("dispYum");
				otherMeta.remove("dispZ");
				}
			
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

	public boolean isDirty()
		{
		for(EvDecimal frame:getFrames())
				if(getStack(frame).isDirty())
					return true;
		return false;
		}
	

	@Override
	public EvObject cloneEvObject()
		{
		throw new RuntimeException("Cannot clone channels - not implemented");
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
