package evplugin.roi;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import org.jdom.Element;

import evplugin.imageWindow.*;
import evplugin.imageset.*;
import evplugin.data.*;
import evplugin.ev.*;

/**
 * ROI (Region Of Interest), selects a region on channel X frames X x,y,z (5D)
 * 
 * @author Johan Henriksson
 */
public abstract class ROI extends EvObject
	{
	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	//private static final String metaType="ROI";
	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ImageExtensionROI());
		}

	

	/**
	 * Set of all selected ROI:s
	 */
	public static final HashSet<ROI> selected=new HashSet<ROI>();

	/******************************************************************************************************
	 *            Class: Handle in image window                                                           *
	 *****************************************************************************************************/
	
	/**
	 * One handle for a ROI: marks a part of a ROI (in image window).
	 * The user can then drag this in (x,y) to resize the selection.
	 * @author Johan Henriksson
	 */
	public static interface Handle
		{
		public String getID();
		public double getX();
		public double getY();
		public void setPos(double x, double y);
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                                 *
	 *****************************************************************************************************/

	/**
	 * Add options for ROI metaobject to metaobject menu
	 */
	public void buildMetamenu(JMenu menu)
		{
		JMenuItem miEdit=new JMenuItem("Edit");
		menu.add(miEdit);
		miEdit.addActionListener(new ActionListener()
			{public void actionPerformed(ActionEvent e){openEditWindow();}});
		}

	/**
	 * Open window allowing settings for ROI to be changed
	 */
	public void openEditWindow()
		{
		JFrame frame=new JFrame(EV.programName+" Edit "+getMetaTypeDesc());
		JComponent c=getROIWidget();
		if(c==null)
			c=new JLabel("There are no options");
		frame.add(c);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
	
	//name of ROI? = name of metaobject?
	
	/**
	 * Description of this type of metadata. ROI's should implement getROIDesc instead to assure some sort of
	 * consistency in the descriptions
	 */
	public String getMetaTypeDesc()
		{
		return "ROI ("+getROIDesc()+")";
		}

	/** Description/short name for this type of ROI */
	public abstract String getROIDesc();
	
	/** Get a widget to edit the parameters of this ROI */
	public abstract JPanel getROIWidget();
	
	
	
	
	
	public abstract Set<String> getChannels(Imageset rec);
	public abstract Set<Integer> getFrames(Imageset rec, String channel);
	public abstract Set<Integer> getSlice(Imageset rec, String channel, int frame);

	public abstract Handle[] getHandles();
	
	public abstract boolean imageInRange(String channel, double frame, int z);
	public abstract LineIterator getLineIterator(EvImage im, String channel, int frame, int z);
	
	public abstract Vector<ROI> getSubRoi();
	
	
	
	
	
	/******************************************************************************************************
	 *                               Span class: numeric                                                  *
	 *****************************************************************************************************/
	public static class SpanNumeric
		{
		public SpanNumeric(){all=true;}
		public SpanNumeric(double start, double end){this.start=start;this.end=end;all=false;}
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
		public void saveRange(Element e, String a)
			{
			if(!all)
				{
				e.setAttribute(a+"start", Double.toString(start));
				e.setAttribute(a+"end", Double.toString(end));
				}
			}
		public void loadRange(Element e, String a)
			{
			try
				{
				start=Double.parseDouble(e.getAttributeValue(a+"start"));
				end=Double.parseDouble(e.getAttributeValue(a+"end"));
				all=false;
				}
			catch(Exception e2)
				{
				all=true;
				}
			}
		}


	/******************************************************************************************************
	 *                               Span class: channels                                                 *
	 *****************************************************************************************************/
	public static class SpanChannels implements Iterable<String>
		{
		public TreeSet<String> c=new TreeSet<String>(); //Empty=all
		
		public void saveRange(Element e, String a)
			{
			for(String s:c)
				{
				Element f=new Element(a);
				f.addContent(s);
				e.addContent(f);
				}
			}
		public void loadRange(Element e, String a)
			{
			c.clear();
			for(Object o:e.getChildren())
				{
				Element e2=(Element)o;
				if(e2.getName().equals(a))
					{
					System.out.println(e2.getValue());
					c.add(e2.getValue());
					}
				}
			}
		
		/**
		 * Check if a channel is included in the ROI
		 */
		public boolean channelInRange(String channel)
			{
			return c.isEmpty() || c.contains(channel);
			}
		
		public Iterator<String> iterator()
			{
			return c.iterator();
			}
		
		public void clear()
			{
			c.clear();
			}
		
		public void add(String channel)
			{
			c.add(channel);
			}
		}

	}
