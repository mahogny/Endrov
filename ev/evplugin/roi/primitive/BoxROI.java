package evplugin.roi.primitive;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jdom.Element;

import evplugin.roi.*;
import evplugin.imageset.*;

/**
 * Rectangle, Box, or higher dimension
 * 
 * @author Johan Henriksson
 */
public class BoxROI extends ROI
	{
	/******************************************************************************************************
	 *                               Range class                                                          *
	 *****************************************************************************************************/
	public static class Span
		{
		public Span(){all=true;}
		public Span(double start, double end){this.start=start;this.end=end;all=false;}
		public boolean all;
		public double start, end;
		public boolean inRange(double x)
			{
			return all || (x>=start && x<end);
			}
		}
	

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
			//Ignoring boundaries for now
			}	
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public TreeSet<String> regionChannels=new TreeSet<String>(); //Empty=all
	public Span regionFrames=new Span();
	public Span regionX=new Span(50,200);
	public Span regionY=new Span();
	public Span regionZ=new Span();
	
	
	
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
	
	/**
	 * Check if a channel is included in the ROI
	 */
	public boolean channelInRange(String channel)
		{
		return regionChannels.isEmpty() || 
		       regionChannels.contains(channel);
		}
	

	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(final Imageset rec, final String channel, final int frame, final int z)
		{
		if(channelInRange(channel) && regionFrames.inRange(frame) && regionZ.inRange(z))
			{
			//todo: check if there is something here
			EvImage im=rec.getChannel(channel).getImageLoader(frame, z);

			//Initial boundary: cover entire image
			RectLineIterator it=new RectLineIterator();
			it.startX=0;
			it.endX=im.getJavaImage().getWidth();
			it.endY=im.getJavaImage().getHeight();
			it.y=0;
			
			//Correct boundaries based on span
			if(!regionX.all)
				{
				if(it.startX<regionX.start)
					it.startX=(int)regionX.start; //todo: change coordinates
				if(it.endX>regionX.end)
					it.endX=(int)regionX.end;
				
				//todo: other coords
				}

			if(!regionY.all)
				{
				if(it.y<regionY.start)
					it.y=(int)regionY.start; //todo: change coordinates
				if(it.endY>regionY.end)
					it.endY=(int)regionY.end;
				
				//todo: other coords
				}

			//todo: sanity check
			
			//One line before when starting to work
			it.y--;
			return it;
			}
				
		return new EmptyLineIterator();
		}
	

	
	
	public void saveMetadata(Element e)
		{
		e.setName("ROI rect");
		
		}
	
	//ImageIterator?
	
	//get iterator: image + line iterator or pixel iterator or entire image?
	//which channels, frames, slices are affected?
	
	
	
	
	
	
	private class SpanWidget 
		{
		public SpinnerModel ms  =new SpinnerNumberModel(0.0, -1000000.0, 100000.0, 1.0);
		public SpinnerModel me  =new SpinnerNumberModel(0.0, -1000000.0, 100000.0, 1.0);
		public JSpinner spinnerS=new JSpinner(ms);
		public JSpinner spinnerE=new JSpinner(me);
		public final JCheckBox cSpan;
		public final Span span;
		
		public SpanWidget(String name, Span span)
			{
			cSpan=new JCheckBox(name);
			this.span=span;
			}

		public void apply()
			{
			span.all=!cSpan.isSelected();
			span.start=(Double)spinnerS.getValue();
			span.end=  (Double)spinnerE.getValue();
			System.out.println("mod! "+span.all+" "+span.start+" "+span.end);
			}
		}
	
	
	/**
	 * Get widget for editing this ROI
	 */
	public JPanel getWidget()
		{
		final SpanWidget spans[]={
				new SpanWidget("<= Frame <",regionFrames),
				new SpanWidget("<= X <",regionX),
				new SpanWidget("<= Y <",regionY),
				new SpanWidget("<= Z <",regionZ)};	
		JPanel pane=new JPanel(new GridLayout(spans.length+1,3));

		//Channel list
		String outc="";
		for(String s:regionChannels)
			{
			if(outc.equals(""))
				outc=outc+s;
			else
				outc=outc+s+",";
			}
		final JTextField fChannels=new JTextField(outc);
		JButton bApply=new JButton("Apply");
		bApply.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				for(SpanWidget w:spans)
					w.apply();
				StringTokenizer tok=new StringTokenizer(fChannels.getText(),",");
				regionChannels.clear();
				while(tok.hasMoreTokens())
					regionChannels.add(tok.nextToken());
				for(String s:regionChannels)
					System.out.print(" "+s);
				System.out.println("");
				
				}
			});
		
		
		pane.add(new JLabel("Channels"));
		pane.add(fChannels);
		pane.add(bApply);
		
		for(SpanWidget s:spans)
			{
			pane.add(s.spinnerS);
			pane.add(s.cSpan);
			pane.add(s.spinnerE);
			}
		
		return pane;
		}
	
	
	
	}
