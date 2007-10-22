package evplugin.roi;

import java.util.*;
import javax.swing.*;
import org.jdom.*;

import evplugin.imageset.*;

/**
 * Internal ROI for temporary selections of channel (and frame (and z))
 * Later maybe make public
 * 
 * @author Johan Henriksson
 */
public class InternalROI extends ROI
	{
	
	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private static class RectLineIterator extends LineIterator
		{
		int endY;
		public boolean next()
			{
			y++;
			return y<endY;
			}	
		}
	

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
		
	public String chosenChannel=null;
	public Double chosenFrame=null;
	public Integer chosenZ=null;
	
	
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
			if(channelInRange(s))
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
				if(frameInRange(f))
					c.add(f);
			}
		return c;
		}
	
	
	private boolean zInRange(int z)
		{
		return chosenZ==null || z==chosenZ;
		}
	
	private boolean frameInRange(double frame)
		{
		return chosenFrame==null || frame==chosenFrame;
		}
	
	private boolean channelInRange(String channel)
		{
		return chosenChannel==null || channel==chosenChannel;
		}

	public boolean imageInRange(String channel, double frame, int z)
		{
		return channelInRange(channel) && frameInRange(frame) && zInRange(z);
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
				for(int z:slices.keySet())
					if(zInRange(z))
						c.add(z);
			}
		return c;
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
			
			//Go one line before when starting to work
			it.y--;
			return it;
			}
				
		return new EmptyLineIterator();
		}
	

	
	
	public void saveMetadata(Element e)
		{
		//should not be saved
		}
	
	
	
	
	/**
	 * Get widget for editing this ROI
	 */
	public JPanel getROIWidget()
		{
		//should not be edited
		return null;
		}
	
	
	

	
	/**
	 * Get handles for corners
	 */
	public Handle[] getHandles()
		{
		return new Handle[]{};
		}
	}
