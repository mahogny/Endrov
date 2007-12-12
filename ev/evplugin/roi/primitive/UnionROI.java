package evplugin.roi.primitive;

import java.util.*;

import javax.swing.*;
import org.jdom.*;

import evplugin.imageset.*;
import evplugin.roi.*;

public class UnionROI extends CompoundROI
	{
	

	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private class UnionLineIterator extends LineIterator
		{
		final String channel;
		final int frame,z;
		Vector<LineIterator> its=new Vector<LineIterator>();
		public UnionLineIterator(EvImage im, String channel, int frame, int z)
			{
			this.channel=channel;
			this.frame=frame;
			this.z=z;
			
			for(ROI roi:subRoi)
				its.add(roi.getLineIterator(im, channel, frame, z));
			}
		
		
		//int endY;
		public boolean next()
			{
			return false;
//			y++;
	//		return y<endY;
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
		if(imageInRange(channel, frame, z))
			return new UnionLineIterator(im, channel, frame, z);
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
