package endrov.roi.primitive;

import java.util.*;

import javax.swing.*;

import org.jdom.*;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvObjectType;
import endrov.imageset.*;
import endrov.roi.*;

//TODO: restrict region better

/**
 * ROI: set theoretic intersection of other ROIs
 * @author Johan Henriksson
 */
public class IntersectROI extends CompoundROI
	{
	private static final String metaType="ROI_Intersection";
	private static final String metaDesc="Intersection";
	private static ImageIcon icon=new ImageIcon(IntersectROI.class.getResource("iconIntersect.png"));
	public static void initPlugin()
		{
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				IntersectROI meta=new IntersectROI();
				meta.loadCompoundMetadata(e);
				return meta;
				}
			});
		
		ROI.addType(new ROIType()
			{
			public boolean canPlace(){return false;}
			public boolean isCompound(){return true;}
			public String name(){return metaDesc;};
			public ROI makeInstance(){return new IntersectROI();}
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
		
		public ThisLineIterator(EvImage im, LineIterator ita, LineIterator itb, String channel, int frame, int z)
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
					return false;
					}
				else
					{
					if(ita.oneItY<itb.oneItY)
						//A is lagging
						{
						ita.next();
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
									ra=null;
									ra=itala.next();
									}
								else if(rb.end<ra.start)
									{
									rb=null;
									rb=italb.next();
									}
								else
									{
									int left=ra.start;
									int right=ra.end;
									if(left<rb.start) left=rb.start;
									if(right>rb.end)  right=rb.end;
									ranges.add(new LineRange(left,right));
									ra=null;
									rb=null;
									ra=itala.next();
									rb=italb.next();
									}
								}
							}
						catch (NoSuchElementException e)
							{
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
	public Set<Integer> getFrames(Imageset rec, String channel)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		for(ROI roi:subRoi)
			c.addAll(roi.getFrames(rec, channel));
		return c;
		}
	
	
	/**
	 * Get slices that at least are partially selected
	 */
	public Set<Integer> getSlice(Imageset rec, String channel, int frame)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		for(ROI roi:subRoi)
			c.addAll(roi.getSlice(rec, channel, frame));
		return c;
		}
	
	

	public boolean imageInRange(String channel, double frame, int z)
		{
		for(ROI roi:subRoi)
			if(roi.imageInRange(channel, frame, z))
				return true;
		return false;
		}
	
	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(EvImage im, final String channel, final int frame, final int z)
		{
		if(imageInRange(channel, frame, z) && !subRoi.isEmpty())
			{
			LineIterator li=subRoi.get(0).getLineIterator(im, channel, frame, z);
			for(int i=1;i<subRoi.size();i++)
				li=new ThisLineIterator(im, subRoi.get(i).getLineIterator(im, channel, frame, z), li, channel, frame, z);
			return li;
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
	public void initPlacement(String chan, double frame, double z){}
	}
