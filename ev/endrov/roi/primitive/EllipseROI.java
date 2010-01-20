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

import endrov.data.EvData;
import endrov.imageset.*;
import endrov.roi.*;
import endrov.util.EvDecimal;

/**
 * Ellipse, flat
 * 
 * @author Johan Henriksson
 */
public class EllipseROI extends ROI
	{
	private static final String metaType="ROI_Ellipse";
	private static final String metaDesc="Ellipse";
	private static ImageIcon icon=new ImageIcon(DiffROI.class.getResource("iconEllipse.png"));
	
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
	private class ThisLineIterator extends LineIterator
		{
		int maxX;
		int maxY;
		double midx, midy, rx, ry; //bitmap coord
		public boolean next()
			{
			double dy=y-midy;
			double is=1.0-dy*dy/(ry*ry);
			double s=is<0 ? 0 : rx*Math.sqrt(is);
			int startX=(int)(midx-s);
			int endX=(int)(midx+s);
			if(startX<0)  startX=0;
			if(endX>maxX) endX=maxX;
			
			ranges.clear();
			ranges.add(new LineRange(startX,endX));
			y++;
			return y<maxY;
			}	
		}
	
	/******************************************************************************************************
	 *                               Handle                                                               *
	 *****************************************************************************************************/
	public class ThisHandle implements Handle
		{
		private final boolean isStartX, isStartY;
		private final String id;
		public ThisHandle(String id, boolean isStartX, boolean isStartY)
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
	public SpanNumeric regionFrames=new SpanNumeric();
	public SpanNumeric regionX=new SpanNumeric(false);
	public SpanNumeric regionY=new SpanNumeric(false);
	public SpanNumeric regionZ=new SpanNumeric();
	

	/**
	 * Create a box ROI with default: select everything
	 */
	public EllipseROI()
		{
		}
	
	
	public String getROIDesc()
		{
		return "Ellipse";
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
			ThisLineIterator it=new ThisLineIterator();
			EvPixels p=im.getPixels();
			it.maxX=p.getWidth();
			it.maxY=p.getHeight();
//			it.maxX=im.getJavaImage().getWidth();
	//		it.maxY=im.getJavaImage().getHeight();
			it.y=0;

			//Correct for span
			int rXend=(int)stack.transformWorldImageX(regionX.end.doubleValue());
			if(it.maxX>rXend) it.maxX=rXend;
			
			int rYstart=(int)stack.transformWorldImageY(regionY.start.doubleValue());
			int rYend=(int)stack.transformWorldImageY(regionY.end.doubleValue())+1;
			if(it.y<rYstart)	it.y=rYstart;
			if(it.maxY>rYend) it.maxY=rYend;
			
			it.midx=(stack.transformWorldImageX(regionX.start.doubleValue())+stack.transformWorldImageX(regionX.end.doubleValue()))/2.0;
			it.midy=(stack.transformWorldImageY(regionY.start.doubleValue())+stack.transformWorldImageY(regionY.end.doubleValue()))/2.0;
			it.rx=(stack.transformWorldImageX(regionX.end.doubleValue())-stack.transformWorldImageX(regionX.start.doubleValue()))/2.0;
			it.ry=(stack.transformWorldImageY(regionY.end.doubleValue())-stack.transformWorldImageY(regionY.start.doubleValue()))/2.0;
			
			
			//Sanity check
			if(it.y>it.maxY)
				return new EmptyLineIterator();
			else
				{
				//Go one line before when starting to work
				it.y--;
				return it;
				}
			}
				
		return new EmptyLineIterator();
		}
	

	
	//ImageIterator?
	
	//get iterator: image + line iterator or pixel iterator or entire image?
	//which channels, frames, slices are affected?
	
	
	
	
	
	/**
	 * Get widget for editing this ROI
	 */
	public JComponent getROIWidget()
		{
		final SpanNumericWidget spans[]={
				new SpanNumericWidget("<= Frame <",regionFrames, true),
				new SpanNumericWidget("<= X <",regionX, false),
				new SpanNumericWidget("<= Y <",regionY, false),
				new SpanNumericWidget("<= Z <",regionZ, true)};	
		final SpanChannelsWidget spanChannel=new SpanChannelsWidget(regionChannels);
		final JPanel pane=new JPanel(new GridLayout(spans.length+1,3));
		
		//Put widgets together
		pane.add(new JLabel("Channels"));
		pane.add(spanChannel);
		pane.add(new JLabel(""));
		for(ROI.SpanNumericWidget s:spans)
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
		return new Handle[]{getPlacementHandle1(), getPlacementHandle2()};
		}
	public Handle getPlacementHandle1(){return new ThisHandle("4",true,true);}	
	public Handle getPlacementHandle2(){return new ThisHandle("1",false,false);}
	public void initPlacement(String chan, EvDecimal frame, EvDecimal z)
		{
		regionChannels.add(chan);
		regionFrames.set(frame,frame.add(EvDecimal.ONE));
		regionZ.set(z, z.add(EvDecimal.ONE));
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
		EvData.supportedMetadataFormats.put(metaType,EllipseROI.class);
		
		ROI.addType(new ROIType(icon, EllipseROI.class, true,false,metaDesc));
		}
	
	}
