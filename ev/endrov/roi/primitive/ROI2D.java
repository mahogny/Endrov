/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi.primitive;

import java.util.*;

import org.jdom.*;

import endrov.data.EvContainer;
import endrov.imageset.*;
import endrov.roi.*;
import endrov.util.EvDecimal;





/**
 * Subclass for ROIs which are inherently 2D in nature, expanded to higher dimensions with by product space
 * 
 * @author Johan Henriksson
 */
public abstract class ROI2D extends ROI
	{
	protected void saveMetadata2(Element e)
		{
		regionFrames.saveRange(e, "f");
		regionZ.saveRange(e, "z");
		regionChannels.saveRange(e, "channel");
		}

	protected void loadMetadata2(Element e)
		{
		regionFrames.loadRange(e,"f");
		regionZ.loadRange(e,"z");
		regionChannels.loadRange(e, "channel");
		}

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public ROI.SpanChannels regionChannels=new ROI.SpanChannels();
	public ROI.SpanNumeric regionFrames=new ROI.SpanNumeric();
	public ROI.SpanNumeric regionZ=new ROI.SpanNumeric();
	
	

	/**
	 * Get channels that at least are partially selected
	 */
	public Set<String> getChannels(EvContainer rec)
		{
		TreeSet<String> c=new TreeSet<String>();
		for(String s:getChannelMap(rec).keySet())
			if(regionChannels.channelInRange(s))
				c.add(s);
		return c;
		}
	
	/**
	 * Get frames that at least are partially selected
	 */
	public Set<EvDecimal> getFrames(EvContainer rec, String channel)
		{
		TreeSet<EvDecimal> c=new TreeSet<EvDecimal>();
		EvChannel ch=(EvChannel)rec.metaObject.get(channel);
		if(ch!=null)
			{
			for(EvDecimal f:ch.getFrames())
				if(regionFrames.inRange(f))
					c.add(f);
			}
		return c;
		}
	
	
	

	public boolean imageInRange2d(String channel, EvDecimal frame, double z)
		{
		
		return /*regionChannels.channelInRange(channel) &&*/ regionFrames.inRange(frame) && regionZ.inRange(z);
		}
	
	
	

	/**
	 * Get widget for editing this ROI
	 */
	/*
	public JComponent getROIWidget()
		{
		final SpanNumericWidget spans[]={
				new SpanNumericWidget("<= Frame <",regionFrames,true),
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
	*/
	
	

	public Vector<ROI> getSubRoi()
		{
		return new Vector<ROI>();
		}
	
	
	
	
	}
