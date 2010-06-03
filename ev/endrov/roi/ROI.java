/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi;

import java.awt.EventQueue;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.*;
import endrov.ev.SimpleObserver;
import endrov.imageWindow.*;
import endrov.imageset.*;
import endrov.roi.window.WindowROI;
import endrov.util.EvDecimal;

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

	/*
	public static class ROISelect implements EvSelectable
		{
		ROI roi;
		public ROISelect(ROI roi)
			{
			this.roi=roi;
			}
	
		public void setColor(EvColor c)
			{
			}
		}*/
	
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
		System.out.println("selected "+selected);
		selectionChanged.emit(null);
		}
	
	
	public static void deleteSelected()
		{
		for(EvData data:EvData.openedData)
			{
			for(CompoundROI ob:data.getObjects(CompoundROI.class))
				deleteSelected(ob);
			for(ROI roi:selected)
				data.removeMetaObjectByValue(roi);
			}
		selected.clear();
//		selectionChanged.emit(null);
		BasicWindow.updateWindows(null); //to remove TODO updateWindows
		}
	
	//maybe all objects should have a parent assigned
	
	private static void deleteSelected(EvContainer from)
		{
		for(EvContainer child:from.metaObject.values())
			deleteSelected(child);
		for(ROI roi:selected)
			from.metaObject.values().remove(roi);
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
	public static class ROIType
		{
		private final ImageIcon icon;
		private final Class<? extends ROI> roiClass;
		private final boolean canPlace;
		private final String name;
		private final boolean isCompound;
		public ROIType(ImageIcon icon, Class<? extends ROI> roiClass, boolean canPlace, boolean isCompound, String name)
			{
			this.icon=icon;
			this.roiClass=roiClass;
			this.canPlace=canPlace;
			this.isCompound=isCompound;
			this.name=name;
			}
		
		/** Can be made by dragging two points in image window */
		public boolean canPlace(){return canPlace;}
		
		/** Compound ROI: puts together ROI from sub objects */
		public boolean isCompound(){return isCompound;}
		
		public String name(){return name;}
		
		/** Create an instance of this ROI */
		public ROI makeInstance()
			{
			try
				{
				return roiClass.newInstance();
				}
			catch (Exception e)
				{
				e.printStackTrace();
				return null;
				}
			}
		
		/** Icon, can be null */
		public ImageIcon getIcon(){return icon;}
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
	
	/**
	 * Description of this type of metadata. ROI's should implement getROIDesc instead to assure some sort of
	 * consistency in the descriptions
	 */
	public String getMetaTypeDesc()
		{
		return "ROI ("+getROIDesc()+")";
		}

	/** 
	 * Description/short name for this type of ROI 
	 */
	public abstract String getROIDesc();
	
	/**
	 * Get a widget to edit the parameters of this ROI 
	 */
	public abstract JComponent getROIWidget();
	
	/**
	 * Get which channels might be relevant
	 */
	public abstract Set<String> getChannels(Imageset rec);
	
	/**
	 * Get which frames might be relevant
	 */
	public abstract Set<EvDecimal> getFrames(Imageset rec, String channel);
	
	/**
	 * Get which slices might be relevant
	 */
	public abstract Set<EvDecimal> getSlice(Imageset rec, String channel, EvDecimal frame);

	/**
	 * Get handles for resizing ROI
	 */
	public abstract Handle[] getHandles();

	/**
	 * Get first handle to place, if ROI can be placed
	 */
	public abstract Handle getPlacementHandle1();
	
	/**
	 * Get second handle to place, if ROI can be placed
	 */
	public abstract Handle getPlacementHandle2();
	

	/**
	 * First called when placing ROI
	 */
	public abstract void initPlacement(String chan, EvDecimal frame, EvDecimal z);
	
	/**
	 * Check if an image might be relevant?
	 */
	public abstract boolean imageInRange(String channel, EvDecimal frame, EvDecimal z);
	
	/**
	 * Get iterator over ROI
	 */
	public abstract LineIterator getLineIterator(EvStack stack, EvImage im, String channel, EvDecimal frame, EvDecimal z);
	
	/**
	 * Check if a given point (world coordinates) is in a ROI
	 */
	public abstract boolean pointInRange(String channel, EvDecimal frame, double x, double y, EvDecimal z);
	
	
	/******************************************************************************************************
	 *                               Span class: numeric                                                  *
	 *****************************************************************************************************/
	public static class SpanNumeric
		{
		public SpanNumeric(){all=true;}
		public SpanNumeric(boolean initialAll){all=initialAll;}
		public SpanNumeric(EvDecimal start, EvDecimal end){this.start=start;this.end=end;all=false;}
		public boolean all;
		public EvDecimal start, end;
		public boolean inRange(EvDecimal x)
			{
			return all || (x.greaterEqual(start) && x.less(end));
			}
		public void set(EvDecimal start, EvDecimal end)
			{
			all=false;
			this.start=start;
			this.end=end;
			}
		public void set(EvDecimal start)
			{
			set(start,start.add(EvDecimal.ONE));
			}
		public void saveRange(Element e, String a)
			{
			if(!all)
				{
				e.setAttribute(a+"start", start.toString());
				e.setAttribute(a+"end", end.toString());
				}
			}
		public void loadRange(Element e, String a)
			{
			try
				{
				start=new EvDecimal(e.getAttributeValue(a+"start"));
				end=new EvDecimal(e.getAttributeValue(a+"end"));
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
				span.start=new EvDecimal(spinnerS.getText());
				span.end  =new EvDecimal(spinnerE.getText());
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
		
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ImageWindowExtensionROI());
		
		ImageWindow.addImageWindowRendererExtension(new ImageWindowRendererExtension()
			{
			public void newImageWindow(ImageWindowInterface w)
				{
				w.addImageWindowRenderer(new ImageRendererROI(w));
				}
			});
		}
	
	}
