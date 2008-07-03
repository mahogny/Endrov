package endrov.roi.primitive;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import org.jdom.*;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvObjectType;
import endrov.imageset.*;
import endrov.roi.*;

/**
 * Ellipse, flat
 * 
 * @author Johan Henriksson
 */
public class EllipseROI extends ROI
	{
	private static final String metaType="ROI_Ellipse";
	private static ImageIcon icon=new ImageIcon(DiffROI.class.getResource("iconEllipse.png"));
	public static void initPlugin() {}
	static
		{
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				EllipseROI meta=new EllipseROI();
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
			public String name(){return "Ellipse";};
			public ROI makeInstance(){return new EllipseROI();}
			public ImageIcon getIcon(){return icon;}
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
			ThisLineIterator it=new ThisLineIterator();
			it.maxX=im.getJavaImage().getWidth();
			it.maxY=im.getJavaImage().getHeight();
			it.y=0;

			//Correct for span
			int rXend=(int)im.transformWorldImageX(regionX.end);
			if(it.maxX>rXend) it.maxX=rXend;
			
			int rYstart=(int)im.transformWorldImageY(regionY.start);
			int rYend=(int)im.transformWorldImageY(regionY.end)+1;
			if(it.y<rYstart)	it.y=rYstart;
			if(it.maxY>rYend) it.maxY=rYend;
			
			it.midx=(im.transformWorldImageX(regionX.start)+im.transformWorldImageX(regionX.end))/2.0;
			it.midy=(im.transformWorldImageY(regionY.start)+im.transformWorldImageY(regionY.end))/2.0;
			it.rx=(im.transformWorldImageX(regionX.end)-im.transformWorldImageX(regionX.start))/2.0;
			it.ry=(im.transformWorldImageY(regionY.end)-im.transformWorldImageY(regionY.start))/2.0;
			
			
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
	public void initPlacement(String chan, double frame, double z)
		{
		regionChannels.add(chan);
		regionFrames.set(frame,frame+1);
		regionZ.set(z, z+1);
		}
		
	
	public Vector<ROI> getSubRoi()
		{
		return new Vector<ROI>();
		}
	}
