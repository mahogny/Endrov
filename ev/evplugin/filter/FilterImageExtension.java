package evplugin.filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jdom.Element;

import java.util.*;

import evplugin.basicWindow.*;
import evplugin.imageWindow.*;
import evplugin.imageset.*;
import evplugin.roi.*;
import evplugin.roi.primitive.*;
import evplugin.data.*;
import evplugin.ev.*;

/**
 * ImageWindow extension: Filter menus
 * 
 * @author Johan Henriksson
 */
public class FilterImageExtension implements ImageWindowExtension
	{
	public static abstract class BindListener
		{
		public abstract void bind(Object fi, JMenuItem mi); //either FilterInfo or FilterSequence
		}
	
	
	/**
	 * Fill a filter menu with all entries
	 */
	public static void fillFilters(JMenu menu, BindListener bl)
		{
		HashMap<String, JMenu> categories=new HashMap<String, JMenu>();
		for(FilterInfo fi:Filter.filterInfo.values())
			{
			if(!categories.containsKey(fi.getCategory()))
				{
				JMenu mi=new JMenu(fi.getCategory());
				categories.put(fi.getCategory(),mi);
				BasicWindow.addMenuItemSorted(menu, mi);
				}
			JMenu cmenu=categories.get(fi.getCategory());
			JMenuItem mi=new JMenuItem(fi.getName());
			cmenu.add(mi);
			bl.bind(fi, mi);
			}
		}	
	
	/**
	 * Fill a filter menu with all entries
	 */
	public static void fillFilterSeq(JMenu menu, BindListener bl)
		{
		JMenu mSeq=new JMenu("Filter sequence");
		menu.add(mSeq);
		for(EvData data:EvData.metadata)
			{
			for(int id:data.metaObject.keySet())
				{
				EvObject ob=data.metaObject.get(id);
				if(ob instanceof FilterSeq)
					{
					JMenuItem mi=new JMenuItem(data.getMetadataName()+" "+id);
					mSeq.add(mi);
					bl.bind((FilterSeq)ob, mi);
					}
				}
			}
		}	
	
	public static class FilterDialog extends BasicWindow implements ActionListener
		{
		static final long serialVersionUID=0;
		final FilterSeq filterseq;
		final private Imageset rec;
		final private ROI roi;
		
		JMenu mAdd=new JMenu("Add");
		WidgetFilterSeq wfilter=new WidgetFilterSeq();
		
		public FilterDialog(FilterROI firoi, Imageset rec, ROI roi)
			{
			this(new FilterSeq(new Filter[]{firoi}), rec, roi);
			}
		
		public FilterDialog(FilterSeq fseq, Imageset rec, ROI roi)
			{
			filterseq=fseq;

			this.rec=rec;
			this.roi=roi;
						
			setLayout(new BorderLayout());
			setTitle(EV.programName+" Apply filter");
			
			JButton bApply=new JButton("Apply");
			bApply.addActionListener(this);			
			
			wfilter.setFilterSeq(filterseq);
			
			addMenubar(mAdd);
			wfilter.buildMenu(mAdd);

			add(wfilter, BorderLayout.CENTER);
			add(bApply, BorderLayout.SOUTH);
			
			pack();
			setBounds(100,100,300,400);
			setVisible(true);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			filterseq.apply(rec, roi);
			BasicWindow.updateWindows();
			}

		public void dataChangedEvent()
			{
			mAdd.removeAll();
			wfilter.buildMenu(mAdd);
			}

		public void windowPersonalSettings(Element e)
			{
			}
		}
	
	
	
	public void newImageWindow(final ImageWindow w)
		{
		//Create menus
		final JMenu miOnImageset=new JMenu("On imageset");
		final JMenu miOnChannel=new JMenu("On channel");
		final JMenu miOnFrame=new JMenu("On frame");
		final JMenu miOnSlice=new JMenu("On slice");
		final JMenu miOnROI=new JMenu("On ROI");
		BasicWindow.addMenuItemSorted(w.menuImage, miOnImageset);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnChannel);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnFrame);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnSlice);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnROI);
		
		//ROI filter menu action listener
		final BindListener bROI=new BindListener()
			{
			public void bind(final Object fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();
//						ROI roi=null;
						
						for(ROI roi:ROI.getSelected())
							{
							if(fi instanceof FilterInfo)
								new FilterDialog(((FilterInfo)fi).filterROI(), rec, roi);
							else
								new FilterDialog((FilterSeq)fi, rec, roi);
							}
	/*					
						for(EvObject ob:rec.metaObject.values())
							if(ob instanceof ROI)
								roi=(ROI)ob;
*/
						}
					});
				}
			};
		
		//Slice filter menu action listener
		final BindListener bSlice=new BindListener()
			{
			public void bind(final Object fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();
						
						BoxROI roi=new BoxROI();
						roi.regionChannels.add(w.getCurrentChannelName());
						roi.regionFrames.set(w.frameControl.getFrame());
						roi.regionZ.set(w.frameControl.getZ());
						
						//this just killed the need for a single image to be applied? or?
						//maybe try to use optimized call after all
						
						if(fi instanceof FilterInfo)
							new FilterDialog(((FilterInfo)fi).filterROI(), rec, roi);
						else
							new FilterDialog((FilterSeq)fi, rec, roi);				
						}
					});
				}
			};
	
	
		//Frame filter menu action listener
		final BindListener bFrame=new BindListener()
			{
			public void bind(final Object fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();

						BoxROI roi=new BoxROI();
						roi.regionChannels.add(w.getCurrentChannelName());
						roi.regionFrames.set(w.frameControl.getFrame());
						
						if(fi instanceof FilterInfo)
							new FilterDialog(((FilterInfo)fi).filterROI(), rec, roi);
						else
							new FilterDialog((FilterSeq)fi, rec, roi);			
						}
					});
				}
			};
		
		//Channel filter menu action listener
		final BindListener bChannel=new BindListener()
			{
			public void bind(final Object fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();

						BoxROI roi=new BoxROI();
						roi.regionChannels.add(w.getCurrentChannelName());
						
						if(fi instanceof FilterInfo)
							new FilterDialog(((FilterInfo)fi).filterROI(), rec, roi);
						else
							new FilterDialog((FilterSeq)fi, rec, roi);			
						}
					});
				}
			};

		//Imageset filter menu action listener
		final BindListener bImageset=new BindListener()
			{
			public void bind(final Object fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();

						BoxROI roi=new BoxROI();
						
						if(fi instanceof FilterInfo)
							new FilterDialog(((FilterInfo)fi).filterROI(), rec, roi);
						else
							new FilterDialog((FilterSeq)fi, rec, roi);
						}
					});
				}
			};

		fillFilters(miOnROI, bROI);
		fillFilters(miOnSlice, bSlice);
		fillFilters(miOnFrame, bFrame);
		fillFilters(miOnChannel, bChannel);
		fillFilters(miOnImageset, bImageset);
		
		fillFilterSeq(miOnROI, bROI);
		fillFilterSeq(miOnSlice, bSlice);
		fillFilterSeq(miOnFrame, bFrame);
		fillFilterSeq(miOnChannel, bChannel);
		fillFilterSeq(miOnImageset, bImageset);
		
		
		w.imageWindowRenderers.add(new ImageWindowRenderer()
			{
			public void draw(Graphics g)
				{				
				}
			public void dataChangedEvent()
				{
				miOnROI.removeAll();
				miOnSlice.removeAll();
				miOnFrame.removeAll();
				miOnChannel.removeAll();
				miOnImageset.removeAll();
				
				fillFilters(miOnROI, bROI);
				fillFilters(miOnSlice, bSlice);
				fillFilters(miOnFrame, bFrame);
				fillFilters(miOnChannel, bChannel);
				fillFilters(miOnImageset, bImageset);
				
				fillFilterSeq(miOnROI, bROI);
				fillFilterSeq(miOnSlice, bSlice);
				fillFilterSeq(miOnFrame, bFrame);
				fillFilterSeq(miOnChannel, bChannel);
				fillFilterSeq(miOnImageset, bImageset);
				}
			});
		}

	
	
	
	}
