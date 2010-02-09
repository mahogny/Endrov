/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi.primitive;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import org.jdom.*;

import endrov.data.*;
import endrov.imageset.*;
import endrov.roi.*;
import endrov.util.EvDecimal;





//ImageIterator?

//get iterator: image + line iterator or pixel iterator or entire image?
//which channels, frames, slices are affected?





/**
 * Rectangle, Box, or higher dimension
 * 
 * @author Johan Henriksson
 */
public class BoxROI extends ROI
	{
	private static final String metaType="ROI_Box";
	private static ImageIcon icon=new ImageIcon(DiffROI.class.getResource("iconBox.png"));
	
	
	public String saveMetadata(Element e)
		{
		regionFrames.saveRange(e, "f");
		regionX.saveRange(e, "x");
		regionY.saveRange(e, "y");
		regionZ.saveRange(e, "z");
		regionChannels.saveRange(e, "channel");
		return metaType;
		}

	public void loadMetadata(Element e)
		{
		regionFrames.loadRange(e,"f");
		regionX.loadRange(e,"x");
		regionY.loadRange(e,"y");
		regionZ.loadRange(e,"z");
		regionChannels.loadRange(e, "channel");
		}

	
	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private static class RectLineIterator extends LineIterator
		{
		int startX, endX;
		int endY;
		public boolean next()
			{
			ranges.clear();
			ranges.add(new LineRange(startX,endX));
			y++;
//			System.out.println("boxit: "+startX+" "+endX+" "+y+" "+endY+"    y "+y+ "size "+ranges.size());
			return y<endY;
			}
		}
	
	/******************************************************************************************************
	 *                               Handle                                                               *
	 *****************************************************************************************************/
	public class BoxHandle implements Handle
		{
		private final boolean isStartX, isStartY;
		private final String id;
		public BoxHandle(String id, boolean isStartX, boolean isStartY)
			{
			this.id=id;
			this.isStartX=isStartX;
			this.isStartY=isStartY;
			}
		//TODO: what about "all"?
		public String getID()
			{
			return id;
			}
		public double getX()
			{
			if(isStartX) return regionX.start.doubleValue();
			else return regionX.end.doubleValue();
			}
		public double getY()
			{
			if(isStartY) return regionY.start.doubleValue();
			else return regionY.end.doubleValue();
			}
		public void setPos(double x, double y)
			{
			if(isStartX) regionX.start=new EvDecimal(x);
			else regionX.end=new EvDecimal(x);
			if(isStartY) regionY.start=new EvDecimal(y);
			else regionY.end=new EvDecimal(y);
			ROI.roiParamChanged.emit(null);
			}
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public ROI.SpanChannels regionChannels=new ROI.SpanChannels();
	public ROI.SpanNumeric regionFrames=new ROI.SpanNumeric();
	public ROI.SpanNumeric regionX=new ROI.SpanNumeric();
	public ROI.SpanNumeric regionY=new ROI.SpanNumeric();
	public ROI.SpanNumeric regionZ=new ROI.SpanNumeric();
	

	/**
	 * Create a box ROI with default: select everything
	 */
	public BoxROI()
		{
		}
	
	
	public String getROIDesc()
		{
		return "Box";
		}
	
	

	/**
	 * Get channels that at least are partially selected
	 */
	public Set<String> getChannels(Imageset rec)
		{
		TreeSet<String> c=new TreeSet<String>();
		for(String s:rec.getChannels().keySet())
			if(regionChannels.channelInRange(s))
				c.add(s);
		return c;
		}
	
	/**
	 * Get frames that at least are partially selected
	 */
	public Set<EvDecimal> getFrames(Imageset rec, String channel)
		{
		TreeSet<EvDecimal> c=new TreeSet<EvDecimal>();
		EvChannel ch=rec.getChannel(channel);
		if(ch!=null)
			{
			for(EvDecimal f:ch.imageLoader.keySet())
				if(regionFrames.inRange(f))
					c.add(f);
			}
		return c;
		}
	
	
	/**
	 * Get slices that at least are partially selected
	 */
	public Set<EvDecimal> getSlice(Imageset rec, String channel, EvDecimal frame)
		{
		TreeSet<EvDecimal> c=new TreeSet<EvDecimal>();
		EvChannel ch=rec.getChannel(channel);
		if(ch!=null)
			{
			EvStack stack=ch.imageLoader.get(frame);
			if(stack!=null)
				for(EvDecimal f:stack.keySet())
					if(regionZ.inRange(f))
						c.add(f);
			}
		return c;
		}
	
	
	

	public boolean imageInRange(String channel, EvDecimal frame, EvDecimal z)
		{
		return regionChannels.channelInRange(channel) && regionFrames.inRange(frame) && regionZ.inRange(z);
		}
	
	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(EvStack stack, EvImage im, final String channel, final EvDecimal frame, final EvDecimal z)
		{
		if(imageInRange(channel, frame, z))
			{
			//Initial boundary: cover entire image
			RectLineIterator it=new RectLineIterator();
			it.startX=0;
			it.y=0;
			EvPixels p=im.getPixels();
			it.endX=p.getWidth();
			it.endY=p.getHeight();
			/*
			it.endX=im.getJavaImage().getWidth();
			it.endY=im.getJavaImage().getHeight();
*/
			//Correct for span
			if(!regionX.all)
				{
				int rXstart=(int)stack.transformWorldImageX(regionX.start.doubleValue());
				int rXend=(int)stack.transformWorldImageX(regionX.end.doubleValue());
				if(it.startX<rXstart)	it.startX=rXstart;
				if(it.endX>rXend) it.endX=rXend;
				}
			if(!regionY.all)
				{
				int rYstart=(int)stack.transformWorldImageY(regionY.start.doubleValue());
				int rYend=(int)stack.transformWorldImageY(regionY.end.doubleValue());
				if(it.y<rYstart)	it.y=rYstart;
				if(it.endY>rYend) it.endY=rYend;
				}
			
//			System.out.println("newit: "+it.startX+" "+it.endX+" "+it.y+" "+it.endY+"");
			
			//Sanity check
			if(it.y>it.endY || it.startX>it.endX)
				return new EmptyLineIterator();
			else
				{
				//Go one line before when starting to work
				it.y--;
				return it;
				}
			}
		else
			return new EmptyLineIterator();
		}
	

	/**
	 * Get widget for editing this ROI
	 */
	public JComponent getROIWidget()
		{
		final SpanNumericWidget spans[]={
				new SpanNumericWidget("<= Frame <",regionFrames,true),
				new SpanNumericWidget("<= X <",regionX,true),
				new SpanNumericWidget("<= Y <",regionY,true),
				new SpanNumericWidget("<= Z <",regionZ,true)};	
		final SpanChannelsWidget spanChannel=new SpanChannelsWidget(regionChannels);
		final JPanel pane=new JPanel(new GridLayout(spans.length+1,3));
				
		//Put widgets together
		pane.add(new JLabel("Channels"));
		pane.add(spanChannel);
		pane.add(new JLabel(""));
		for(SpanNumericWidget s:spans)
			{
			pane.add(s.spinnerS);
			pane.add(s.cSpan);
			pane.add(s.spinnerE);
			}
		return pane;
		}
	
	
	

	
	/**
	 * Get handles for corners
	 */
	public Handle[] getHandles()
		{
		//button in x,y window: place in middle?
		//imagewindow temporary tools: like create box etc
		if(regionX.all && regionY.all)
			return new Handle[]{};
		else
			return new Handle[]{
					getPlacementHandle1(),new BoxHandle("2",true,false), 
					new BoxHandle("3",false,true), getPlacementHandle2()};
		}
	public Handle getPlacementHandle1(){return new BoxHandle("4",true,true);}		
	public Handle getPlacementHandle2(){return new BoxHandle("1",false,false);}
	public void initPlacement(String chan, EvDecimal frame, EvDecimal z)
		{
		regionX.all=false;
		regionY.all=false;
		regionChannels.add(chan);
		regionFrames.set(frame);
		regionZ.set(z);
		}
	
	
	public Vector<ROI> getSubRoi()
		{
		return new Vector<ROI>();
		}
	
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,BoxROI.class);
		
		ROI.addType(new ROIType(icon, BoxROI.class, true,false,"Box"));
		}
	
	
	}
