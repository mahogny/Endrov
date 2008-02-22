package evplugin.roi.primitive;

import java.util.*;
import javax.swing.*;
import org.jdom.*;

import evplugin.data.*;
import evplugin.imageset.*;
import evplugin.roi.*;

/**
 * 
 * @author mahogny
 *
 */
public class MaskROI3 extends ROI
	{

	
	private static final String metaType="ROI_Mask";
	private static final String metaDesc="Mask";
	//private static ImageIcon icon=null;//new ImageIcon(UnionROI.class.getResource("iconUnion.png"));	
	public static void initPlugin()
		{
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				MaskROI3 meta=new MaskROI3();
				return meta;
				}
			});
		/*
		ROI.addType(new ROIType()
			{
			public boolean canPlace(){return false;}
			public boolean isCompound(){return true;}
			public String name(){return metaDesc;};
			public ROI makeInstance(){return new UnionROI();}
			public ImageIcon getIcon(){return icon;}
			});*/
		}


	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private class ThisLineIterator extends LineIterator
		{
		public boolean next()
			{
			// TODO Auto-generated method stub
			return false;
			}
		}
	
	
	/******************************************************************************************************
	 *                               Classes                                                              *
	 *****************************************************************************************************/
	private static class MaskFrame
		{
		TreeMap<Integer,MaskSlice> z=new TreeMap<Integer, MaskSlice>();
		}
	
	private static class MaskSlice
		{
		TreeMap<Integer,Vector<MaskRange>> rows=new TreeMap<Integer, Vector<MaskRange>>();
		}
	private static class MaskRange
		{
		int start, end;
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public ROI.SpanChannels regionChannels=new ROI.SpanChannels();
	public ROI.SpanNumeric regionFrames=new ROI.SpanNumeric();
	public ROI.SpanNumeric regionZ=new ROI.SpanNumeric();

	double resX, resY;
	
	/** If the single region should be used (and frames via regionFrames) or if it is a 4D mask */
	private boolean isSingleRegion=true;
	private boolean isSingleZ=true;
	
//	private MaskFrame oneMask=new MaskFrame();
	private TreeMap<Integer,MaskFrame> frameMask=new TreeMap<Integer, MaskFrame>();
	
	
	//2d, 3d, 4d
	//draw functions... will simplify if it is just one type
	
	
	public String getROIDesc()
		{
		return metaDesc;
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
			if(isSingleRegion)
				{
				for(int f:ch.imageLoader.keySet())
					if(regionFrames.inRange(f))
						c.add(f);
				}
			else
				{
				for(int f:ch.imageLoader.keySet())
					if(frameMask.containsKey(f)) //or interpolate? setting?
						c.add(f);
				
				}
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
				{
//			if(isSingleRegion)
				if(isSingleZ)
					for(int f:slices.keySet())
						if(regionZ.inRange(f))
							c.add(f);
				}
			else
				{
//				if(isSingleRegion)
	
				//TODO
				
				for(int f:slices.keySet())
					if(regionZ.inRange(f))
						c.add(f);
				}
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
			return new EmptyLineIterator();
			}
		else
			return new EmptyLineIterator();
		}
	
	
	public void saveMetadata(Element e)
		{
		}
	
	
	/**
	 * Get widget for editing this ROI
	 */
	public JComponent getROIWidget()
		{
		return null;//There are no options
		}

	
	
	
	/**
	 * Get handles for corners
	 */
	public Handle[] getHandles(){return null;}
	public Handle getPlacementHandle1(){return null;}
	public Handle getPlacementHandle2(){return null;}
	public void initPlacement(String chan, double frame, double z){}
	
	
	public Vector<ROI> getSubRoi(){return new Vector<ROI>();}
	
	
	}
