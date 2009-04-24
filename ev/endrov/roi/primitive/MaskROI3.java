package endrov.roi.primitive;

import java.util.*;
import javax.swing.*;
import org.jdom.*;

import endrov.data.*;
import endrov.imageset.*;
import endrov.roi.*;
import endrov.util.EvDecimal;

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
		EvData.supportedMetadataFormats.put(metaType,MaskROI3.class);
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
/*	private class ThisLineIterator extends LineIterator
		{
		public boolean next()
			{
			// TODO Auto-generated method stub
			return false;
			}
		}*/
	
	
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
	public Set<EvDecimal> getFrames(Imageset rec, String channel)
		{
		TreeSet<EvDecimal> c=new TreeSet<EvDecimal>();
		EvChannel ch=rec.getChannel(channel);
		if(ch!=null)
			{
			if(isSingleRegion)
				{
				for(EvDecimal f:ch.imageLoader.keySet())
					if(regionFrames.inRange(f))
						c.add(f);
				}
			else
				{
				for(EvDecimal f:ch.imageLoader.keySet())
					if(frameMask.containsKey(f)) //or interpolate? setting?
						c.add(f);
				
				}
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
				{
//			if(isSingleRegion)
				if(isSingleZ)
					for(EvDecimal f:stack.keySet())
						if(regionZ.inRange(f))
							c.add(f);
				}
			else
				{
//				if(isSingleRegion)
	
				//TODO
				
				for(EvDecimal f:stack.keySet())
					if(regionZ.inRange(f))
						c.add(f);
				}
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
			return new EmptyLineIterator();
			}
		else
			return new EmptyLineIterator();
		}
	
	
	public void saveMetadata(Element e)
		{
		}
	
	public void loadMetadata(Element e)
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
	public void initPlacement(String chan, EvDecimal frame, EvDecimal z){}
	
	
	public Vector<ROI> getSubRoi(){return new Vector<ROI>();}
	
	
	}
