package evplugin.roi.primitive;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jdom.*;

import evplugin.roi.*;
import evplugin.basicWindow.BasicWindow;
import evplugin.imageset.*;

/**
 * Ellipse, flat
 * 
 * @author Johan Henriksson
 */
public class EllipseROI extends ROI
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
		public void set(double start, double end)
			{
			all=false;
			this.start=start;
			this.end=end;
			}
		public void set(double start)
			{
			set(start,start+1);
			}
		}
	

	/******************************************************************************************************
	 *                               Iterator                                                             *
	 *****************************************************************************************************/
	private class ThisLineIterator extends LineIterator
		{
		int maxX;
		int maxY;
		double midx, midy, rx, ry; //bitmap coord
		public boolean next()
			{
			double dy=y-midy;
			double is=1.0-dy*dy/(ry*ry);
			double s=is<0 ? 0 : rx*Math.sqrt(is);
			int startX=(int)(midx-s);
			int endX=(int)(midx+s);
			if(startX<0)  startX=0;
			if(endX>maxX) endX=maxX;
			
			ranges.clear();
			ranges.add(new LineRange(startX,endX));
			y++;
			return y<maxY;
			}	
		}
	
	/******************************************************************************************************
	 *                               Handle                                                               *
	 *****************************************************************************************************/
	public class ThisHandle implements Handle
		{
		private final boolean isStartX, isStartY;
		private final String id;
		public ThisHandle(String id, boolean isStartX, boolean isStartY)
			{
			this.id=id;
			this.isStartX=isStartX;
			this.isStartY=isStartY;
			}
		//TODO: what about "all"?
		public String getID()
			{
			return id;
			}
		public double getX()
			{
			if(isStartX) return regionX.start;
			else return regionX.end;
			}
		public double getY()
			{
			if(isStartY) return regionY.start;
			else return regionY.end;
			}
		public void setPos(double x, double y)
			{
			if(isStartX) regionX.start=x;
			else regionX.end=x;
			if(isStartY) regionY.start=y;
			else regionY.end=y;
			}
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public TreeSet<String> regionChannels=new TreeSet<String>(); //Empty=all
	public Span regionFrames=new Span();
	public Span regionX=new Span();
	public Span regionY=new Span();
	public Span regionZ=new Span();
	

	/**
	 * Create a box ROI with default: select everything
	 */
	public EllipseROI()
		{
		}
	
	
	public String getROIDesc()
		{
		return "Ellipse";
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
	private boolean channelInRange(String channel)
		{
		return regionChannels.isEmpty() || 
		       regionChannels.contains(channel);
		}
	

	public boolean imageInRange(String channel, double frame, int z)
		{
		return channelInRange(channel) && regionFrames.inRange(frame) && regionZ.inRange(z);
		}
	
	/**
	 * Get iterator over one image
	 */
	public LineIterator getLineIterator(EvImage im, final String channel, final int frame, final int z)
		{
		if(imageInRange(channel, frame, z))
			{
			//Initial boundary: cover entire image
			ThisLineIterator it=new ThisLineIterator();
			it.maxX=im.getJavaImage().getWidth();
			it.maxY=im.getJavaImage().getHeight();
			it.y=0;

			//Correct for span
			int rXend=(int)im.transformWorldImageX(regionX.end);
			if(it.maxX>rXend) it.maxX=rXend;
			
			int rYstart=(int)im.transformWorldImageY(regionY.start);
			int rYend=(int)im.transformWorldImageY(regionY.end)+1;
			if(it.y<rYstart)	it.y=rYstart;
			if(it.maxY>rYend) it.maxY=rYend;
			
			it.midx=(im.transformWorldImageX(regionX.start)+im.transformWorldImageX(regionX.end))/2.0;
			it.midy=(im.transformWorldImageY(regionY.start)+im.transformWorldImageY(regionY.end))/2.0;
			it.rx=(im.transformWorldImageX(regionX.end)-im.transformWorldImageX(regionX.start))/2.0;
			it.ry=(im.transformWorldImageY(regionY.end)-im.transformWorldImageY(regionY.start))/2.0;
			
			
			//Sanity check
			if(it.y>it.maxY)
				return new EmptyLineIterator();
			else
				{
				//Go one line before when starting to work
				it.y--;
				return it;
				}
			}
				
		return new EmptyLineIterator();
		}
	

	
	
	public void saveMetadata(Element e)
		{
		e.setName("ROI ellipse");
		
		}
	
	//ImageIterator?
	
	//get iterator: image + line iterator or pixel iterator or entire image?
	//which channels, frames, slices are affected?
	
	
	
	
	
	
	private class SpanWidget implements ActionListener, DocumentListener
		{
		public JTextField spinnerS=new JTextField();
		public JTextField spinnerE=new JTextField();
		public final JComponent cSpan;
		public final Span span;
		
		public SpanWidget(String name, Span span, boolean canSetAll)
			{
			if(canSetAll)
				{
				cSpan=new JCheckBox(name,!span.all);
				((JCheckBox)cSpan).addActionListener(this);
				}
			else
				cSpan=new JLabel(name);
			this.span=span;
			spinnerS.setText(""+span.start);
			spinnerE.setText(""+span.end);
			spinnerS.getDocument().addDocumentListener(this);
			spinnerE.getDocument().addDocumentListener(this);
			}

		public void actionPerformed(ActionEvent e){apply();}
		public void changedUpdate(DocumentEvent e){apply();}
		public void insertUpdate(DocumentEvent e){apply();}
		public void removeUpdate(DocumentEvent e){apply();}
		public void apply()
			{
			try
				{
				if(cSpan instanceof JCheckBox)
					span.all=!((JCheckBox)cSpan).isSelected();
				span.start=Double.parseDouble(spinnerS.getText());
				span.end  =Double.parseDouble(spinnerE.getText());
				BasicWindow.updateWindows();
				}
			catch (NumberFormatException e){}
			}
		}
	
	
	/**
	 * Get widget for editing this ROI
	 */
	public JPanel getROIWidget()
		{
		final SpanWidget spans[]={
				new SpanWidget("<= Frame <",regionFrames, true),
				new SpanWidget("<= X <",regionX, false),
				new SpanWidget("<= Y <",regionY, false),
				new SpanWidget("<= Z <",regionZ, true)};	
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
		fChannels.getDocument().addDocumentListener(new DocumentListener()
			{
			public void changedUpdate(DocumentEvent e){apply();}
			public void insertUpdate(DocumentEvent e){apply();}
			public void removeUpdate(DocumentEvent e){apply();}
			public void apply()
				{
				for(SpanWidget w:spans)
					w.apply();
				StringTokenizer tok=new StringTokenizer(fChannels.getText(),",");
				regionChannels.clear();
				while(tok.hasMoreTokens())
					regionChannels.add(tok.nextToken().trim());
				for(String s:regionChannels)
					System.out.print(" "+s);
				System.out.println("");
				BasicWindow.updateWindows();
				}
			});
		
		//Put widgets together
		pane.add(new JLabel("Channels"));
		pane.add(fChannels);
		pane.add(new JLabel(""));
		for(SpanWidget s:spans)
			{
			pane.add(s.spinnerS);
			pane.add(s.cSpan);
			pane.add(s.spinnerE);
			}
		return pane;
		}
	
	
	

	
	/**
	 * Get handles for corners
	 */
	public Handle[] getHandles()
		{
		return new Handle[]{new ThisHandle("1",false,false), new ThisHandle("4",true,true)};
		}
		
	
	public Vector<ROI> getSubRoi()
		{
		return new Vector<ROI>();
		}
	}
