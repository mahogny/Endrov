package evplugin.roi.primitive;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import org.jdom.*;

import evplugin.roi.*;
import evplugin.data.*;
import evplugin.imageset.*;





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
	public static void initPlugin() {}
	static
		{
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				BoxROI meta=new BoxROI();
				meta.regionFrames.loadRange(e,"f");
				meta.regionX.loadRange(e,"x");
				meta.regionY.loadRange(e,"y");
				meta.regionZ.loadRange(e,"z");
				meta.regionChannels.loadRange(e, "channel");
				return meta;
				}
			});
		
		ROI.addType(new ROIType()
			{
			public boolean canPlace(){return true;}
			public boolean isCompound(){return false;}
			public String name(){return "Box";};
			public ROI makeInstance(){return new BoxROI();}
			});
		
		
		}
	
	
	
	
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		regionFrames.saveRange(e, "f");
		regionX.saveRange(e, "x");
		regionY.saveRange(e, "y");
		regionZ.saveRange(e, "z");
		regionChannels.saveRange(e, "channel");
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
			if(isStartX) return regionX.start;
			else return regionX.end;
			}
		public double getY()
			{
			if(isStartY) return regionY.start;
			else return regionY.end;
			}
		public void setPos(double x, double y)
			{
			if(isStartX) regionX.start=x;
			else regionX.end=x;
			if(isStartY) regionY.start=y;
			else regionY.end=y;
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
		for(String s:rec.channelImages.keySet())
			if(regionChannels.channelInRange(s))
				c.add(s);
		return c;
		}
	
	/**
	 * Get frames that at least are partially selected
	 */
	public Set<Integer> getFrames(Imageset rec, String channel)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		Imageset.ChannelImages ch=rec.getChannel(channel);
		if(ch!=null)
			{
			for(int f:ch.imageLoader.keySet())
				if(regionFrames.inRange(f))
					c.add(f);
			}
		return c;
		}
	
	
	/**
	 * Get slices that at least are partially selected
	 */
	public Set<Integer> getSlice(Imageset rec, String channel, int frame)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		Imageset.ChannelImages ch=rec.getChannel(channel);
		if(ch!=null)
			{
			TreeMap<Integer,EvImage> slices=ch.imageLoader.get(frame);
			if(slices!=null)
				for(int f:slices.keySet())
					if(regionZ.inRange(f))
						c.add(f);
			}
		return c;
		}
	
	
	

	public boolean imageInRange(String channel, double frame, int z)
		{
		return regionChannels.channelInRange(channel) && regionFrames.inRange(frame) && regionZ.inRange(z);
		}
	
	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(EvImage im, final String channel, final int frame, final int z)
		{
		if(imageInRange(channel, frame, z))
			{
			//Initial boundary: cover entire image
			RectLineIterator it=new RectLineIterator();
			it.startX=0;
			it.endX=im.getJavaImage().getWidth();
			it.endY=im.getJavaImage().getHeight();
			it.y=0;

			//Correct for span
			if(!regionX.all)
				{
				int rXstart=(int)im.transformWorldImageX(regionX.start);
				int rXend=(int)im.transformWorldImageX(regionX.end);
				if(it.startX<rXstart)	it.startX=rXstart;
				if(it.endX>rXend) it.endX=rXend;
				}
			if(!regionY.all)
				{
				int rYstart=(int)im.transformWorldImageY(regionY.start);
				int rYend=(int)im.transformWorldImageY(regionY.end);
				if(it.y<rYstart)	it.y=rYstart;
				if(it.endY>rYend) it.endY=rYend;
				}
			
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
	public Handle getPlacementHandle1(){return new BoxHandle("1",false,false);}
	public Handle getPlacementHandle2(){return new BoxHandle("4",true,true);}		
	public void initPlacement(String chan, double frame, double z)
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
	}
