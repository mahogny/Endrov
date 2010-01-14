/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi.primitive;

import java.util.*;
import javax.swing.*;

import org.jdom.*;

import endrov.data.EvData;
import endrov.imageset.*;
import endrov.roi.*;
import endrov.util.EvDecimal;



//TODO: restrict region better


/**
 * ROI: set theoretic symmetric difference of other ROIs
 * @author Johan Henriksson
 */
public class DiffROI extends CompoundROI
	{
	private static final String metaType="ROI_Difference";
	private static final String metaDesc="Difference";
	private static ImageIcon icon=new ImageIcon(DiffROI.class.getResource("iconDiff.png"));

	public static void initPlugin(){}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,DiffROI.class);
		
		ROI.addType(new ROIType(icon, DiffROI.class, false,true,metaDesc));
		}


	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private class ThisLineIterator extends LineIterator
		{
		private OneIt ita, itb;
		
		private class OneIt
			{
			public LinkedList<LineRange> oneItRanges=new LinkedList<LineRange>();
			public int oneItY;
			public boolean hasNext;
			public LineIterator it;
			public OneIt(LineIterator it)
				{
				this.it=it;
				next();
				}
			public void next()
				{
				hasNext=it.next();
				oneItRanges.clear();
				oneItRanges.addAll(it.ranges);
				oneItY=it.y;
				}
			public void copyRange()
				{
				ranges.clear();
				ranges.addAll(oneItRanges);
				y=oneItY;
				next();
				}
			}
		
		public ThisLineIterator(EvImage im, LineIterator ita, LineIterator itb, String channel, EvDecimal frame, EvDecimal z)
			{
			this.z=z;
			this.ita=new OneIt(ita);
			this.itb=new OneIt(itb);
			}
		
		
		public void addRest(LinkedList<LineRange> to, Iterator<LineRange> from)
			{
			while(from.hasNext())
				to.add(from.next());
			}
		
		public boolean next()
			{
			if(!ita.hasNext)
				if(!itb.hasNext)
					return false;
				else
					{
					//Only B left
					itb.copyRange();
					return true;
					}
			else
				if(!itb.hasNext)
					{
					//Only A left
					ita.copyRange();
					return true;
					}
				else
					{
					if(ita.oneItY<itb.oneItY)
						//A is lagging
						{
						ita.copyRange();
						return true;
						}
					else if(ita.oneItY>itb.oneItY)
						//B is lagging
						{
						itb.copyRange();
						return true;
						}
					else //A and B has next, and on the same row. need to merge
						{
						ranges.clear();

						Iterator<LineRange> itala=ita.oneItRanges.iterator();
						Iterator<LineRange> italb=itb.oneItRanges.iterator();
						LineRange ra=null;
						LineRange rb=null;
						try
							{
							ra=itala.next();
							rb=italb.next();
							for(;;)
								{
								if(ra.end<rb.start)
									{
									ranges.add(ra);
									ra=null;
									ra=itala.next();
									}
								else if(rb.end<ra.start)
									{
									ranges.add(rb);
									rb=null;
									rb=italb.next();
									}
								else
									{
									if(ra.start<rb.start)
										ranges.add(new LineRange(ra.start,rb.start));
									else
										ranges.add(new LineRange(rb.start,ra.start));
									if(ra.end<rb.end)
										ranges.add(new LineRange(ra.end,rb.end));
									else
										ranges.add(new LineRange(rb.end,ra.end));
									ra=null;
									rb=null;
									ra=itala.next();
									rb=italb.next();
									}
								}
							}
						catch (NoSuchElementException e)
							{
							if(ra!=null) ranges.add(ra);
							if(rb!=null) ranges.add(rb);
							addRest(ranges, itala);
							addRest(ranges, italb);
							}
						
						y=ita.oneItY;
						ita.next();
						itb.next();
						return true;
						}
					}
			}	
		}
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	
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
		for(ROI roi:getSubRoi())
			c.addAll(roi.getChannels(rec));
		return c;
		}
	
	/**
	 * Get frames that at least are partially selected
	 */
	public Set<EvDecimal> getFrames(Imageset rec, String channel)
		{
		TreeSet<EvDecimal> c=new TreeSet<EvDecimal>();
		for(ROI roi:getSubRoi())
			c.addAll(roi.getFrames(rec, channel));
		return c;
		}
	
	
	/**
	 * Get slices that at least are partially selected
	 */
	public Set<EvDecimal> getSlice(Imageset rec, String channel, EvDecimal frame)
		{
		TreeSet<EvDecimal> c=new TreeSet<EvDecimal>();
		for(ROI roi:getSubRoi())
			c.addAll(roi.getSlice(rec, channel, frame));
		return c;
		}
	
	

	public boolean imageInRange(String channel, EvDecimal frame, EvDecimal z)
		{
		for(ROI roi:getSubRoi())
			if(roi.imageInRange(channel, frame, z))
				return true;
		return false;
		}
	
	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(EvStack stack, EvImage im, final String channel, final EvDecimal frame, final EvDecimal z)
		{
		Collection<ROI> subRoi=getSubRoi();
		if(imageInRange(channel, frame, z) && !subRoi.isEmpty())
			{
			Iterator<ROI> it=subRoi.iterator();
			LineIterator li=it.next().getLineIterator(stack, im, channel, frame, z);
			while(it.hasNext())
			for(int i=1;i<subRoi.size();i++)
				li=new ThisLineIterator(im, it.next().getLineIterator(stack, im, channel, frame, z), li, channel, frame, z);
			return li;
			}
		else
			return new EmptyLineIterator();
		}
	
	
	
	public String saveMetadata(Element e)
		{
		return metaType;
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
	public Handle[] getHandles(){return getCompoundHandles();}
	public Handle getPlacementHandle1(){return null;}
	public Handle getPlacementHandle2(){return null;}
	public void initPlacement(String chan, EvDecimal frame, EvDecimal z){}
	
	
	
	
	}
