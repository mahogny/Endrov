/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

import endrov.bindingIJ.roi.ImageJroiImport;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.gui.window.BasicWindow;
import endrov.imageset.EvChannel;
import endrov.roi.window.*;
import endrov.util.EvDecimal;
import endrov.windowViewer2D.*;

/*
	cut
	copy
	copy to system

	clear
	clear outside
	fill
	*/

/**
 * Image Window: ROI menu
 */
public class ImageWindowExtensionROI implements Viewer2DExtension
	{
		
	public void newImageWindow(final Viewer2DWindow w)
		{
		JMenu miROI=new JMenu("ROI");
		w.addMenubar(miROI);
		
		w.addImageWindowTool(new ImageWindowToolROI(w));

		
		
		//ROI
		JMenu miNew=new JMenu("New");
		JMenu miModify=new JMenu("Modify");
		JMenu miComposite=new JMenu("Composite");
		JMenu miAnalyze=new JMenu("Analyze");
		JMenuItem miImportIJ=new JMenuItem("Import ImageJ");
		BasicWindow.addMenuItemSorted(miROI, miNew, "roi_1new");
		BasicWindow.addMenuItemSorted(miROI, miModify, "roi_2modify");
		BasicWindow.addMenuItemSorted(miROI, miComposite, "roi_3composite");
		BasicWindow.addMenuItemSorted(miROI, miAnalyze, "roi_4analyze");
		BasicWindow.addMenuItemSorted(miROI, miImportIJ, "roi_5IJ");

		//ROI Window
		JMenuItem miROIWindow=new JMenuItem("ROI Window...");
		miROI.add(miROIWindow);
		miROIWindow.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){WindowROI.getRoiWindow();}});
		
		//New
		for(final ROI.ROIType rt:ROI.getTypes())
			{
			if(rt.canPlace() && !rt.isCompound())
				{
				JMenuItem miNewROIthis;
				if(rt.getIcon()==null)
					miNewROIthis=new JMenuItem(rt.name());
				else
					miNewROIthis=new JMenuItem(rt.name(),rt.getIcon());
				miNewROIthis.addActionListener(new ActionListener()
					{public void actionPerformed(ActionEvent e)
						{
						ImageRendererROI renderer=w.getRendererClass(ImageRendererROI.class);
						w.setTool(new ToolDragCreateROI(w,rt.makeInstance(),renderer));
						}});
				BasicWindow.addMenuItemSorted(miNew, miNewROIthis);
				}
			}

		//Composite
		for(final ROI.ROIType rt:ROI.getTypes())
			{
			if(rt.isCompound())
				{
				JMenuItem miNewROIthis;
				if(rt.getIcon()==null)
					miNewROIthis=new JMenuItem(rt.name());
				else
					miNewROIthis=new JMenuItem(rt.name(),rt.getIcon());
				miNewROIthis.addActionListener(new ActionListener()
					{public void actionPerformed(ActionEvent e)
						{
						EvContainer data=w.getImageset();
						if(data!=null)
							{
							CompoundROI croi=(CompoundROI)rt.makeInstance();
							CompoundROI.makeCompoundROI(data,croi);
							croi.openEditWindow();
							}
						}});
				BasicWindow.addMenuItemSorted(miComposite, miNewROIthis);
				}
			}
		
		//Import ImageJ
		miImportIJ.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				JFileChooser fc=new JFileChooser();
				fc.setCurrentDirectory(EvData.getLastDataPath());
				int ret=fc.showOpenDialog(w);
				if(ret==JFileChooser.APPROVE_OPTION)
					{
					EvDecimal frame=w.getFrame();
					EvChannel ch=w.getCurrentChannel();
					frame=ch.closestFrame(frame);
					
					try
						{
						new ImageJroiImport(fc.getSelectedFile(), ch.getStack(frame), w.getImageset());
						}
					catch (IOException e1)
						{
						BasicWindow.showErrorDialog("Could not read file");
						e1.printStackTrace();
						}
					}
				
				}
			});
		
		
		///////////////// todo ////////////////
		
		//select all
		//select none
		//convert: make mask
		
		
/*
		
		//New
		final JMenuItem miNewPoint=new JMenuItem("Point (not impl)");
		final JMenuItem miNewLinesegment=new JMenuItem("Line segment (not impl)");
		final JMenuItem miNewWand=new JMenuItem("Wand (not impl)");
		JMenuItem miNewWatershed=new JMenuItem("Watershed");

		
		//Modify
		final JMenuItem miModConvexhull=new JMenuItem("Convex hull (not impl)");
		final JMenuItem miModGrow=new JMenuItem("Grow (not impl)");
		final JMenuItem miModShrink=new JMenuItem("Shrink (not impl)");
		final JMenuItem miModRotate=new JMenuItem("Rotate (not impl)");
		final JMenuItem miModInvert=new JMenuItem("Invert (not impl)");


		//Composite
		final JMenuItem miCompIntersection=new JMenuItem("Intersection (not impl)");
		final JMenuItem miCompDifference=new JMenuItem("Difference (not impl)");
		final JMenuItem miCompGroup=new JMenuItem("Group (not impl)");

		
		//Analyze
		JMenuItem miAnaVolume=new JMenuItem("Calculate volume (not impl)");
		JMenuItem miAnaArea=new JMenuItem("Calculate area (not impl)");
		BasicWindow.addMenuItemSorted(miAnalyze, miAnaVolume);
		BasicWindow.addMenuItemSorted(miAnalyze, miAnaArea);
*/		
		
		
		}

	
	
	
	
	}
