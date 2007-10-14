package evplugin.filter;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.imageWindow.*;
import evplugin.metadata.*;
import evplugin.imageset.*;
import evplugin.roi.*;

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
						
						for(MetaObject ob:rec.metaObject.values())
							if(ob instanceof ROI)
								{
								ROI roi=(ROI)ob;
								
								
								for(String chan:roi.getChannels(rec))
									for(int frame:roi.getFrames(rec, chan))
										for(int z:roi.getSlice(rec, chan, frame))
											{
											System.out.println("- "+chan+"/"+frame+"/"+z);
											
											
											fi.filterROI().applyImage(rec, chan, frame, z, roi);
											}
								
								/*
										Imageset.ChannelImages ch=w.getSelectedChannel();
										EvImage im=ch.getImageLoader((int)w.frameControl.getFrame(), w.frameControl.getZ());
								fi.filterROI().applyImage(im);
								*/
								
								/*
								String chan=w.getSelectedChannel().getMeta().name;
								int frame=(int)w.frameControl.getFrame();
								int z=w.frameControl.getZ();
								System.out.println("+ "+chan+" "+frame+" "+z);
								fi.filterROI().applyImage(rec,chan, frame, z, roi);
								*/
								BasicWindow.updateWindows();
								}
						
								
						}
					});
				}
			});
		
		//ROI filter menu action listener
		fillFilters(miOnSlice, new BindListener()
			{
			public void bind(final FilterInfo fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						Imageset.ChannelImages ch=w.getSelectedChannel();
						EvImage im=ch.getImageLoader((int)w.frameControl.getFrame(), w.frameControl.getZ());
						fi.filterROI().applyImage(im);
						BasicWindow.updateWindows();
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
						Imageset.ChannelImages ch=w.getSelectedChannel();
						TreeMap<Integer,EvImage> zs=ch.imageLoader.get((int)w.frameControl.getFrame());
						if(zs!=null)
							{
							for(EvImage im:zs.values())
								fi.filterROI().applyImage(im);
							BasicWindow.updateWindows();
							}
						}
					});
				}
			});
		


		}

	
	/**
	 * Fill a filter menu with all entries
	 */
	public void fillFilters(JMenu menu, BindListener bl)
		{
		HashMap<String, JMenu> categories=new HashMap<String, JMenu>();
		for(FilterInfo fi:FilterMeta.filterInfo.values())
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
	}
