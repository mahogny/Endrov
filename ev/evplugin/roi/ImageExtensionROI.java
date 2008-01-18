package evplugin.roi;

import javax.swing.*;
import java.awt.event.*;
import evplugin.basicWindow.*;
import evplugin.imageWindow.*;
import evplugin.imageset.*;
import evplugin.roi.primitive.*;
import evplugin.roi.window.WindowROI;

/*
	cut
	copy
	copy to system

	clear
	clear outside
	fill
	*/


public class ImageExtensionROI implements ImageWindowExtension
	{
		
	public void newImageWindow(final ImageWindow w)
		{
		JMenu miROI=new JMenu("ROI");
		w.addMenubar(miROI);

		//select all
		//select none
		//convert: make mask
		
		
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
		miROIWindow.addActionListener(new ActionListener()
			{public void actionPerformed(ActionEvent e){
			WindowROI.getRoiWindow();
			//new WindowROI();
			}});
		
		
		//New
		final JMenuItem miNewRect=new JMenuItem("Box");
		final JMenuItem miNewEllipse=new JMenuItem("Ellipse");
		final JMenuItem miNewPoint=new JMenuItem("Point (not impl)");
		final JMenuItem miNewLinesegment=new JMenuItem("Line segment (not impl)");
		final JMenuItem miNewWand=new JMenuItem("Wand (not impl)");
//		JMenuItem miNewWatershed=new JMenuItem("Watershed");

		BasicWindow.addMenuItemSorted(miNew, miNewRect);
		BasicWindow.addMenuItemSorted(miNew, miNewEllipse);
		BasicWindow.addMenuItemSorted(miNew, miNewPoint);
		BasicWindow.addMenuItemSorted(miNew, miNewLinesegment);
		BasicWindow.addMenuItemSorted(miNew, miNewWand);
		
		//Modify
		final JMenuItem miModConvexhull=new JMenuItem("Convex hull (not impl)");
		final JMenuItem miModGrow=new JMenuItem("Grow (not impl)");
		final JMenuItem miModShrink=new JMenuItem("Shrink (not impl)");
		final JMenuItem miModRotate=new JMenuItem("Rotate (not impl)");
		final JMenuItem miModInvert=new JMenuItem("Invert (not impl)");

		BasicWindow.addMenuItemSorted(miModify, miModConvexhull);
		BasicWindow.addMenuItemSorted(miModify, miModGrow);
		BasicWindow.addMenuItemSorted(miModify, miModShrink);
		BasicWindow.addMenuItemSorted(miModify, miModRotate);
		BasicWindow.addMenuItemSorted(miModify, miModInvert);

		//Composite
		final JMenuItem miCompUnion=new JMenuItem("Union");
		final JMenuItem miCompIntersection=new JMenuItem("Intersection");
		final JMenuItem miCompDifference=new JMenuItem("Difference (not impl)");
		final JMenuItem miCompGroup=new JMenuItem("Group (not impl)");

		BasicWindow.addMenuItemSorted(miComposite, miCompUnion);
		BasicWindow.addMenuItemSorted(miComposite, miCompIntersection);
		BasicWindow.addMenuItemSorted(miComposite, miCompDifference);
		BasicWindow.addMenuItemSorted(miComposite, miCompGroup);
		
		//Analyze
		JMenuItem miAnaVolume=new JMenuItem("Calculate volume (not impl)");
		JMenuItem miAnaArea=new JMenuItem("Calculate area (not impl)");
		BasicWindow.addMenuItemSorted(miAnalyze, miAnaVolume);
		BasicWindow.addMenuItemSorted(miAnalyze, miAnaArea);
		
		
		
		//The listener
		ActionListener listener=new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==miNewRect)
					{
					//Create a Box-ROI
					BoxROI roi=new BoxROI();
					roi.regionChannels.add(w.getCurrentChannelName());
					double curFrame=w.frameControl.getFrame();
					double curZ=w.frameControl.getZ();
					roi.regionFrames.set(curFrame,curFrame+1);
					roi.regionZ.set(curZ, curZ+1);
					addROI(roi);
					
					
					//TODO
					roi.openEditWindow();
					}
				if(e.getSource()==miNewEllipse)
					{
					//Create a Ellipse-ROI
					EllipseROI roi=new EllipseROI();
					roi.regionChannels.add(w.getCurrentChannelName());
					double curFrame=w.frameControl.getFrame();
					double curZ=w.frameControl.getZ();
					roi.regionFrames.set(curFrame,curFrame+1);
					roi.regionZ.set(curZ, curZ+1);
					addROI(roi);
					
					//TODO
					roi.openEditWindow();
					}
				else if(e.getSource()==miCompUnion)
					{
					UnionROI roi=new UnionROI();
					addROI(roi);
					
					//TODO
					roi.openEditWindow();
					}
				else if(e.getSource()==miCompIntersection)
					{
					ROI roi=new IntersectROI();
					addROI(roi);
					
					//TODO
					roi.openEditWindow();
					}
				//add selected ROIs
				
				
				}
			
			public void addROI(ROI roi)
				{
				//Add ROI to data
				Imageset rec=w.getImageset();
				rec.addMetaObject(roi);
				BasicWindow.updateWindows();
				roi.openEditWindow();
				}
			};
		miNewRect.addActionListener(listener);
		miNewEllipse.addActionListener(listener);

		miCompUnion.addActionListener(listener);
		miCompIntersection.addActionListener(listener);
		miCompDifference.addActionListener(listener);
		
		
		

		
		ImageRendererROI r=new ImageRendererROI(w);
		w.imageWindowRenderers.add(r);
		w.imageWindowTools.add(new ImageToolROI(w,r));
		}

	
	
	
	
	}
