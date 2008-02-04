package evplugin.roi;

import javax.swing.*;
import java.awt.event.*;
import evplugin.basicWindow.*;
import evplugin.data.EvData;
import evplugin.imageWindow.*;
import evplugin.roi.window.*;

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
public class ImageExtensionROI implements ImageWindowExtension
	{
		
	public void newImageWindow(final ImageWindow w)
		{
		JMenu miROI=new JMenu("ROI");
		w.addMenubar(miROI);

		//ROI
		JMenu miNew=new JMenu("New");
		JMenu miModify=new JMenu("Modify");
		JMenu miComposite=new JMenu("Composite");
		JMenu miAnalyze=new JMenu("Analyze");
		BasicWindow.addMenuItemSorted(miROI, miNew);
		BasicWindow.addMenuItemSorted(miROI, miModify);
		BasicWindow.addMenuItemSorted(miROI, miAnalyze);
		BasicWindow.addMenuItemSorted(miROI, miComposite);

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
					{public void actionPerformed(ActionEvent e){w.setTool(new ToolDragCreateROI(w,rt.makeInstance()));}});
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
						EvData data=w.getImageset();
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
		
		
		ImageRendererROI r=new ImageRendererROI(w);
		w.imageWindowRenderers.add(r);
		w.imageWindowTools.add(new ImageToolROI(w,r));
		}

	
	
	
	
	}
