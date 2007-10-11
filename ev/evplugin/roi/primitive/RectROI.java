package evplugin.roi.primitive;

import evplugin.roi.*;
import java.util.*;

import org.jdom.Element;

/**
 * Rectangle, Box, or higher dimension
 * @author Johan Henriksson
 */
public class RectROI extends ROI
	{
	public Vector<String> regionChannels=new Vector<String>(); //Empty=all?
	public Span regionFrames=new Span();
	public Span regionX=new Span();
	public Span regionY=new Span();
	public Span regionZ=new Span();
	//never null?
	
	public static class Span
		{
		int start, end;
		//double?
		}
	
	public Set<String> getChannels()
		{
		TreeSet<String> c=new TreeSet<String>();
		
		return c;
		}

	public Set<Integer> getFrames(String channel)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		
		return c;
		}

	public Set<Integer> getSlice(String channel, int frame)
		{
		TreeSet<Integer> c=new TreeSet<Integer>();
		
		return c;
		}
	
	
	public int bestIteratorType()
		{
		return LINE_ITERATOR;
		}

	

	public LineIterator getLineIterator(final String channel, final int frame, final int z)
		{
		for(String s:regionChannels)
			if(s.equals(channel))
				{
				if(regionFrames==null || (frame<=regionFrames.start && frame<regionFrames.end))
					if(regionFrames==null || (frame<=regionFrames.start && frame<regionFrames.end))
						{/*
						LineIterator it=new LineIterator(){
							
							public boolean next()
								{
								startX=regionFrames
								return false;
								}
						};
						it.channel=channel;
						it.startX=
						return it;
						*/
						return null;
						}
				}
		return new EmptyLineIterator();
		}
	
	public PixelIterator getPixelIterator(String channel, int frame, int z)
		{
		return new LineToPixelIterator(getLineIterator(channel,frame,z));
		}

	
	
	public void saveMetadata(Element e)
		{
		e.setName("ROI rect");
		
		}
	
	//ImageIterator?
	
	//get iterator: image + line iterator or pixel iterator or entire image?
	//which channels, frames, slices are affected?
	
	
	
	
	
	}
