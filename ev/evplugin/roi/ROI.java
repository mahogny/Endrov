package evplugin.roi;


//import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.jdom.Element;

import evplugin.imageWindow.ImageWindow;
import evplugin.imageset.*;
import evplugin.metadata.*;
import evplugin.ev.*;

public abstract class ROI extends MetaObject
	{
	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="ROI";
	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ROIImageExtension());
//		Metadata.extensions.put(metaType,new ImagesetMetaObjectExtension());
		}



	/**
	 * Set of all selected ROI:s
	 */
	public static final TreeSet<SelectedROI> selected=new TreeSet<SelectedROI>();

	
	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	public static class ImagesetMetaObjectExtension implements MetaObjectExtension
		{
		public MetaObject extractObjects(Element e)
			{
			return null;
			}
	
	

		}
	


	
	/******************************************************************************************************
	 *                               Instance                                                                 *
	 *****************************************************************************************************/


	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		}


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
		frame.add(getROIWidget());
		frame.pack();
		frame.setVisible(true);
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

	
	public abstract LineIterator getLineIterator(Imageset rec, String channel, int frame, int z);
	}
