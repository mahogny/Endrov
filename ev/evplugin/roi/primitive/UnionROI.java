package evplugin.roi.primitive;

import java.util.*;

import javax.swing.*;
import org.jdom.*;

import evplugin.imageset.*;
import evplugin.roi.*;

public class UnionROI extends CompoundROI
	{
	public static void initPlugin() {}


	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private class UnionLineIterator extends LineIterator
		{
		final String channel;
		final int frame,z;
//		boolean hasNextA, hasNextB;
//		private LineIterator ita;
//		private LineIterator itb;

		private OneIt ita, itb;
		
		private class OneIt
			{
			public LinkedList<LineRange> ranges=new LinkedList<LineRange>();
			public int y;
			public boolean hasNext;
			public LineIterator it;
			public OneIt(LineIterator it)
				{
				this.it=it;
				it.next();
				}
			public void next()
				{
				hasNext=it.next();
				}
			public void step()
				{
				ranges.clear();
				ranges.addAll(it.ranges);
//				ranges=(LinkedList<LineRange>)(LinkedList/*<LineRange>*/)it.ranges.clone();
				System.out.println("% "+ranges.size());
				y=it.y;
//				z=it.z;
				next();   //bad! interfers with range
				}
			}
		
		public UnionLineIterator(EvImage im, LineIterator ita, LineIterator itb, String channel, int frame, int z)
			{
			this.channel=channel;
			this.frame=frame;
			this.z=z;
			this.ita=new OneIt(ita);
			this.itb=new OneIt(itb);

			//Get all started
			ita.next(); //eek. the ones lower down will be next:ed multiple times
			itb.next();
			}
		
		
		public boolean next()
			{
			if(!ita.hasNext)
				if(!itb.hasNext)
					return false;
				else
					{
					itb.step();
					return itb.hasNext;
					}
			else
				if(!itb.hasNext)
					{
					ita.step();
					return ita.hasNext;
					}
				else
					{
					if(ita.y<itb.y)
						ita.step();
					else if(ita.y>itb.y)
						itb.step();
					else //equal
						{
						
						//todo
						
						ita.step();
						itb.step();

						
						}
					return ita.hasNext || itb.hasNext;
					}
			}	
		}
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	
	public String getROIDesc()
		{
		return "Union";
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
				li=new DebugLineIterator(new UnionLineIterator(im, subRoi.get(i).getLineIterator(im, channel, frame, z), li, channel, frame, z));
			return li;
			}
		else
			return new EmptyLineIterator();
		}
	
	
	public void saveMetadata(Element e)
		{
		e.setName("ROI union");
		
		}
	
	
	/**
	 * Get widget for editing this ROI
	 */
	public JPanel getROIWidget()
		{
		return null;//There are no options
		}

	
	/**
	 * Get handles for corners
	 */
	public Handle[] getHandles() //should make a set instead, or linked list
		{
		LinkedList<Handle> h=new LinkedList<Handle>();
		for(ROI roi:subRoi)
			for(Handle th:roi.getHandles())
				h.add(th);
//	return (Handle[])h.toArray();
		Handle[] hh=new Handle[h.size()];
		int i=0;
		for(Handle th:h)
			{
			hh[i]=th;
			i++;
			}
		return hh;
		}

	
	}
