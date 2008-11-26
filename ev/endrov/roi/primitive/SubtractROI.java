package endrov.roi.primitive;

import java.util.*;
import javax.swing.*;

import org.jdom.*;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvObjectType;
import endrov.imageset.*;
import endrov.roi.*;
import endrov.util.EvDecimal;


//TODO: restrict interval better

/**
 * ROI: set theoretic union of other ROIs
 * @author Johan Henriksson
 */
public class SubtractROI extends CompoundROI
	{
	private static final String metaType="ROI_Subtract";
	private static final String metaDesc="Subtract";
	private static ImageIcon icon=new ImageIcon(SubtractROI.class.getResource("iconSub.png"));
	public static void initPlugin(){}
	static
		{
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				SubtractROI meta=new SubtractROI();
				meta.loadCompoundMetadata(e);
				return meta;
				}
			});
		
		ROI.addType(new ROIType()
			{
			public boolean canPlace(){return false;}
			public boolean isCompound(){return true;}
			public String name(){return metaDesc;};
			public ROI makeInstance(){return new SubtractROI();}
			public ImageIcon getIcon(){return icon;}
			});
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
					return false;
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
						itb.next();
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
									if(ra.end>rb.end)
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
		for(ROI roi:subRoi)
			c.addAll(roi.getChannels(rec));
		return c;
		}
	
	/**
	 * Get frames that at least are partially selected
	 */
	public Set<EvDecimal> getFrames(Imageset rec, String channel)
		{
		TreeSet<EvDecimal> c=new TreeSet<EvDecimal>();
		for(ROI roi:subRoi)
			c.addAll(roi.getFrames(rec, channel));
		return c;
		}
	
	
	/**
	 * Get slices that at least are partially selected
	 */
	public Set<EvDecimal> getSlice(Imageset rec, String channel, EvDecimal frame)
		{
		TreeSet<EvDecimal> c=new TreeSet<EvDecimal>();
		for(ROI roi:subRoi)
			c.addAll(roi.getSlice(rec, channel, frame));
		return c;
		}
	
	

	public boolean imageInRange(String channel, EvDecimal frame, EvDecimal z)
		{
		for(ROI roi:subRoi)
			if(roi.imageInRange(channel, frame, z))
				return true;
		return false;
		}
	
	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(EvImage im, final String channel, final EvDecimal frame, final EvDecimal z)
		{
		if(imageInRange(channel, frame, z) && !subRoi.isEmpty())
			{
			if(subRoi.size()>=2)
				return new ThisLineIterator(im,
						subRoi.get(0).getLineIterator(im, channel, frame, z),
						subRoi.get(1).getLineIterator(im, channel, frame, z),channel,frame,z);
			else if(subRoi.size()==1)
				return subRoi.get(0).getLineIterator(im, channel, frame, z);
			else
				return new EmptyLineIterator();
			}
		else
			return new EmptyLineIterator();
		}
	
	
	public void saveMetadata(Element e)
		{
		saveCompoundMetadata(metaType, e);
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
