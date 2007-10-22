package evplugin.filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.imageWindow.*;
import evplugin.imageset.*;
import evplugin.roi.*;
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
		public abstract void bind(FilterInfo fi, JMenuItem mi);
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
	
	
	
	public static class FilterDialog extends JFrame implements ActionListener
		{
		static final long serialVersionUID=0;
		FilterSeq filterseq=new FilterSeq();
		final private Imageset rec;
		final private ROI roi;
		
		public FilterDialog(FilterROI firoi, Imageset rec, ROI roi)
			{
			this.rec=rec;
			this.roi=roi;
						
			setLayout(new BorderLayout());
			setTitle(EV.programName+" "+firoi.getMetaTypeDesc());
			
			JButton bApply=new JButton("Apply");
			bApply.addActionListener(this);			
			
			filterseq.sequence.add(firoi);
			WidgetFilterSeq wfilter=new WidgetFilterSeq();
			wfilter.setFilterSeq(filterseq);
			
			JMenuBar menubar=new JMenuBar();
			setJMenuBar(menubar);
			
			menubar.add(wfilter.buildMenu());

			
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
		}
	
	
	
	public void newImageWindow(final ImageWindow w)
		{
		//Create menus
		JMenu miOnImageset=new JMenu("On imageset");
		JMenu miOnChannel=new JMenu("On channel");
		JMenu miOnFrame=new JMenu("On frame");
		JMenu miOnSlice=new JMenu("On slice");
		JMenu miOnROI=new JMenu("On ROI");
		BasicWindow.addMenuItemSorted(w.menuImage, miOnImageset);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnChannel);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnFrame);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnSlice);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnROI);
		
		//ROI filter menu action listener
		fillFilters(miOnROI, new BindListener()
			{
			public void bind(final FilterInfo fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();
						FilterROI firoi=fi.filterROI();
						ROI roi=null;
						
						for(EvObject ob:rec.metaObject.values())
							if(ob instanceof ROI)
								roi=(ROI)ob;

						new FilterDialog(firoi, rec, roi);
						}
					});
				}
			});
		
		//Slice filter menu action listener
		fillFilters(miOnSlice, new BindListener()
			{
			public void bind(final FilterInfo fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();
						FilterROI firoi=fi.filterROI();

						InternalROI roi=new InternalROI();
						roi.chosenChannel=w.getCurrentChannelName();
						roi.chosenFrame=w.frameControl.getFrame();
						roi.chosenZ=w.frameControl.getZ();
						
						//this just killed the need for a single image to be applied? or?
						//maybe try to use optimized call after all
						
						new FilterDialog(firoi, rec, roi);
						}
					});
				}
			});
		
		//Frame filter menu action listener
		fillFilters(miOnFrame, new BindListener()
			{
			public void bind(final FilterInfo fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();
						FilterROI firoi=fi.filterROI();

						InternalROI roi=new InternalROI();
						roi.chosenChannel=w.getCurrentChannelName();
						roi.chosenFrame=w.frameControl.getFrame();
						
						new FilterDialog(firoi, rec, roi);
						}
					});
				}
			});
		
		//Channel filter menu action listener
		fillFilters(miOnChannel, new BindListener()
			{
			public void bind(final FilterInfo fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();
						FilterROI firoi=fi.filterROI();

						InternalROI roi=new InternalROI();
						roi.chosenChannel=w.getCurrentChannelName();
						
						new FilterDialog(firoi, rec, roi);
						}
					});
				}
			});

		//Imageset filter menu action listener
		fillFilters(miOnImageset, new BindListener()
			{
			public void bind(final FilterInfo fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset rec=w.getImageset();
						FilterROI firoi=fi.filterROI();

						InternalROI roi=new InternalROI();
						
						new FilterDialog(firoi, rec, roi);
						}
					});
				}
			});

		}

	}
