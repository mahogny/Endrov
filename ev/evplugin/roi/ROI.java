package evplugin.roi;

import java.awt.EventQueue;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom.Element;

import evplugin.imageWindow.*;
import evplugin.imageset.*;
import evplugin.roi.window.WindowROI;
import evplugin.basicWindow.BasicWindow;
import evplugin.data.*;
import evplugin.ev.SimpleObserver;

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
	private static final HashSet<ROI> selected=new HashSet<ROI>();
	public static final SimpleObserver selectionChanged=new SimpleObserver();
	
	public static Collection<ROI> getSelected()
		{
		return Collections.unmodifiableCollection(selected);
		}
	public static boolean isSelected(ROI roi)
		{
		return selected.contains(roi);
		}
	public static void setSelected(Collection<ROI> newsel)
		{
		selected.clear();
		selected.addAll(newsel);
		selectionChanged.emit(null);
		}
	
	
	public static void deleteSelected()
		{
		for(EvData data:EvData.metadata)
			{
			for(EvObject ob:data.metaObject.values())
				if(ob instanceof CompoundROI)
					deleteSelected((CompoundROI)ob);
			for(ROI roi:selected)
				data.removeMetaObjectByValue(roi);
			}
		selected.clear();
//		selectionChanged.emit(null);
		BasicWindow.updateWindows(null); //to remove TODO
		}
	
	//maybe all objects should have a parent assigned
	
	private static void deleteSelected(CompoundROI from)
		{
		for(ROI child:from.subRoi)
			if(child instanceof CompoundROI)
				deleteSelected((CompoundROI)child);
		for(ROI roi:selected)
			from.subRoi.remove(roi);
		}
	
	
	
	
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
	 *            Class: Information about a ROI type                                                     *
	 *****************************************************************************************************/
	public static interface ROIType
		{
		/** Can be made by dragging two points in image window */
		public boolean canPlace();
		/** Instance is subclass of CompoundROI */
		public boolean isCompound();
		public String name();
		/** Create an instance of this ROI */
		public ROI makeInstance();
		/** Icon, can be null */
		public ImageIcon getIcon();
		}
	
	private static TreeMap<String, ROIType> types=new TreeMap<String, ROIType>();
	public static void addType(ROIType rt){types.put(rt.name(),rt);}
	public static Collection<ROIType> getTypes(){return types.values();}
	
	/******************************************************************************************************
	 *                               Instance                                                                 *
	 *****************************************************************************************************/

	
	public static final SimpleObserver roiParamChanged=new SimpleObserver();
	public static final SimpleObserver roiStructChanged=new SimpleObserver();

	
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

	//TODO: move below to roi window
	
	/**
	 * Open window allowing settings for ROI to be changed
	 */
	public void openEditWindow()
		{
		Vector<ROI> v=new Vector<ROI>();
		v.add(this);
		WindowROI.getRoiWindow();
		ROI.setSelected(v);
		System.out.println("editroi "+this);
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
	public abstract JComponent getROIWidget();
	
	
	
	
	
	public abstract Set<String> getChannels(Imageset rec);
	public abstract Set<Integer> getFrames(Imageset rec, String channel);
	public abstract Set<Integer> getSlice(Imageset rec, String channel, int frame);

	public abstract Handle[] getHandles();
	
	//only interesting for placeable ROIs
	public abstract Handle getPlacementHandle1();
	public abstract Handle getPlacementHandle2();
	public abstract void initPlacement(String chan, double frame, double z);
	
	public abstract boolean imageInRange(String channel, double frame, int z);
	public abstract LineIterator getLineIterator(EvImage im, String channel, int frame, int z);
	
	public abstract Vector<ROI> getSubRoi();
	
	
	
	
	
	/******************************************************************************************************
	 *                               Span class: numeric                                                  *
	 *****************************************************************************************************/
	public static class SpanNumeric
		{
		public SpanNumeric(){all=true;}
		public SpanNumeric(boolean initialAll){all=initialAll;}
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

	
	
	/**
	 * Widget to edit a numeric span
	 */
	public static class SpanNumericWidget implements ActionListener, DocumentListener, SimpleObserver.Listener
		{
		public final JTextField spinnerS=new JTextField();
		public final JTextField spinnerE=new JTextField();
		public final JComponent cSpan;
		public final SpanNumeric span;
		private final WeakReference<SpanNumericWidget> tthis=new WeakReference<SpanNumericWidget>(this);
		
		/** ROI updated */
		public void observerEvent(Object src)
			{
			if(src!=this)
				{
				EventQueue.invokeLater(new Runnable(){public void run() {
					spinnerS.getDocument().removeDocumentListener(tthis.get());
					spinnerE.getDocument().removeDocumentListener(tthis.get());
					String start=""+span.start;
					String end=""+span.end;
					if(!spinnerS.getText().equals(start)) spinnerS.setText(""+span.start);
					if(!spinnerE.getText().equals(end)) spinnerE.setText(""+span.end);
					spinnerS.getDocument().addDocumentListener(tthis.get());
					spinnerE.getDocument().addDocumentListener(tthis.get());
					}});
				}
			}
	
		public SpanNumericWidget(String name, SpanNumeric span, boolean canSetAll)
			{
			if(canSetAll)
				{
				cSpan=new JCheckBox(name,!span.all);
				((JCheckBox)cSpan).addActionListener(this);
				}
			else
				cSpan=new JLabel(name);
			this.span=span;
			observerEvent(null);
			roiParamChanged.addWeakListener(this);
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
				roiParamChanged.emit(tthis.get());
				}
			catch (NumberFormatException e){}
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


	/**
	 * Widget to edit channel span
	 */
	public static class SpanChannelsWidget extends JTextField implements ActionListener, DocumentListener, SimpleObserver.Listener
		{
		public static final long serialVersionUID=0;
		private final WeakReference<SpanChannelsWidget> tthis=new WeakReference<SpanChannelsWidget>(this);
		//private final JTextField fChannels=new JTextField();
		private ROI.SpanChannels span=new ROI.SpanChannels();
		
		/** ROI updated */
		public void observerEvent(Object src)
			{
			if(src!=this)
				{
				EventQueue.invokeLater(new Runnable(){public void run() {
				/*fChannels.*/getDocument().removeDocumentListener(tthis.get());
	
				String outc="";
				for(String s:span)
					{
					if(outc.equals(""))
						outc=outc+s;
					else
						outc=outc+s+",";
					}
	
				if(!getText().equals(outc)) setText(outc);
				getDocument().addDocumentListener(tthis.get());
				}});
				}
			}
	
		public SpanChannelsWidget(SpanChannels span)
			{
			this.span=span;
			observerEvent(null);
			roiParamChanged.addWeakListener(this);
			}
	
		public void actionPerformed(ActionEvent e){apply();}
		public void changedUpdate(DocumentEvent e){apply();}
		public void insertUpdate(DocumentEvent e){apply();}
		public void removeUpdate(DocumentEvent e){apply();}
		public void apply()
			{
			try
				{
				StringTokenizer tok=new StringTokenizer(/*fChannels.*/getText(),",");
				span.clear();
				while(tok.hasMoreTokens())
					span.add(tok.nextToken().trim());
				ROI.roiParamChanged.emit(this);
				}
			catch (NumberFormatException e){}
			}
		}
		
	

	}
