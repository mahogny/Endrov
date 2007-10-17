package evplugin.roi;

import javax.swing.*;
import java.awt.event.*;
import evplugin.basicWindow.*;
import evplugin.imageWindow.*;
import evplugin.imageset.*;
import evplugin.roi.primitive.*;

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
			{public void actionPerformed(ActionEvent e){new WindowROI();}});
		
		
		//New
		JMenuItem miNewRect=new JMenuItem("Box");
		JMenuItem miNewEllipse=new JMenuItem("Ellipse");
//		JMenuItem miNewRect=new JMenuItem("Ellipsoid");
		JMenuItem miNewPoint=new JMenuItem("Point");
		JMenuItem miNewLinesegment=new JMenuItem("Line segment");
		JMenuItem miNewWand=new JMenuItem("Wand");
//		JMenuItem miNewWatershed=new JMenuItem("Watershed");
		JMenuItem miNewAll=new JMenuItem("Everything");

		BasicWindow.addMenuItemSorted(miNew, miNewRect);
		BasicWindow.addMenuItemSorted(miNew, miNewEllipse);
		BasicWindow.addMenuItemSorted(miNew, miNewPoint);
		BasicWindow.addMenuItemSorted(miNew, miNewLinesegment);
		BasicWindow.addMenuItemSorted(miNew, miNewWand);
		BasicWindow.addMenuItemSorted(miNew, miNewAll);
		
		//Modify
		JMenuItem miModConvexhull=new JMenuItem("Convex hull");
		JMenuItem miModGrow=new JMenuItem("Grow");
		JMenuItem miModShrink=new JMenuItem("Shrink");
		JMenuItem miModRotate=new JMenuItem("Rotate");
		JMenuItem miModInvert=new JMenuItem("Invert");

		BasicWindow.addMenuItemSorted(miModify, miModConvexhull);
		BasicWindow.addMenuItemSorted(miModify, miModGrow);
		BasicWindow.addMenuItemSorted(miModify, miModShrink);
		BasicWindow.addMenuItemSorted(miModify, miModRotate);
		BasicWindow.addMenuItemSorted(miModify, miModInvert);

		//Composite
		JMenuItem miCompUnion=new JMenuItem("Union");
		JMenuItem miCompIntersection=new JMenuItem("Intersection");
		JMenuItem miCompDifference=new JMenuItem("Difference");
		JMenuItem miCompGroup=new JMenuItem("Group");

		BasicWindow.addMenuItemSorted(miComposite, miCompUnion);
		BasicWindow.addMenuItemSorted(miComposite, miCompIntersection);
		BasicWindow.addMenuItemSorted(miComposite, miCompDifference);
		BasicWindow.addMenuItemSorted(miComposite, miCompGroup);
		
		//Analyze
		JMenuItem miAnaVolume=new JMenuItem("Calculate volume");
		JMenuItem miAnaArea=new JMenuItem("Calculate area");
		BasicWindow.addMenuItemSorted(miAnalyze, miAnaVolume);
		BasicWindow.addMenuItemSorted(miAnalyze, miAnaArea);
		
		
		
		//The listener
		ActionListener listenerRect=new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				ROI roi=new BoxROI();
				Imageset rec=w.getImageset();
				rec.addMetaObject(roi);
				BasicWindow.updateWindows();
				roi.openEditWindow();
				}
			};
		miNewRect.addActionListener(listenerRect);
		
		
//		BasicWindow.addMenuItemSorted(miRemove, miRemoveSlice);
		
		/*
		ActionListener listener=new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==miRemoveChannel)
					{
					String ch=w.getCurrentChannelName();
					if(JOptionPane.showConfirmDialog(null, "Do you really want to remove channel "+ch+"?","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						{
						w.getImageset().removeChannel(ch);
						BasicWindow.updateWindows();
						}
					}
				else if(e.getSource()==miRemoveFrame)
					{
					String ch=w.getCurrentChannelName();
					int frame=(int)w.frameControl.getFrame();
					
					if(JOptionPane.showConfirmDialog(null, "Do you really want to remove channel "+ch+", frame "+frame+"?","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						{
						w.getImageset().getChannel(ch).imageLoader.remove(frame);
						BasicWindow.updateWindows();
						}
					}
				else if(e.getSource()==miRemoveSlice)
					{
					String ch=w.getCurrentChannelName();
					int frame=(int)w.frameControl.getFrame();
					int z=w.frameControl.getZ();
					
					if(JOptionPane.showConfirmDialog(null, "Do you really want to remove channel "+ch+", frame "+frame+", slice "+z+"?","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						{
						w.getImageset().getChannel(ch).imageLoader.get(frame).remove(z);
						BasicWindow.updateWindows();
						}
					}
				
				
				}	
			};
		

		//Add listeners
		miRemoveChannel.addActionListener(listener);
		miRemoveFrame.addActionListener(listener);
		miRemoveSlice.addActionListener(listener);
				*/
		
		

		
		ImageRendererROI r=new ImageRendererROI(w);
		w.imageWindowRenderers.add(r);
		w.imageWindowTools.add(new ImageToolROI(w,r));
		}

	
	
	
	
	}
