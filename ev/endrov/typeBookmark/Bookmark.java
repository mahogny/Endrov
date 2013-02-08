/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeBookmark;


import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import endrov.data.*;
import endrov.gl.EvGLCamera;
import endrov.gui.window.EvBasicWindow;
import endrov.util.math.EvDecimal;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer3D.Viewer3DWindow;
import org.jdom.*;

/**
 * Bookmark of frame, z or whatever comes to mind. For recalling window state.
 * 
 * @author Johan Henriksson
 *
 */
public class Bookmark extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="bookmark";
	
	//private static final String metaElement="bookmark";
	
	
	/**
	 * Ask user to give the bookmark a name. Will give an error if data=null.
	 * @return A bookmark to fill with data if dialog worked, otherwise null
	 */
	public static String addBookmarkDialog(JComponent w, EvContainer data)
		{
		if(data==null)
			EvBasicWindow.showErrorDialog("No container selected");
		else
			{
			String name=JOptionPane.showInputDialog(w, "Name of bookmark");
			if(name!=null)
				{
				if(data.metaObject.containsKey(name))
					EvBasicWindow.showErrorDialog("Object with this name exists already");
				else
					{
					return name;
					/*
					Bookmark b=new Bookmark();
					data.metaObject.put(name, b);
					return b;*/
					}
				}
			}
		return null;
		}

	
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvDecimal frame, z;
	public EvGLCamera modelCamera;
	

	public String getMetaTypeDesc()
		{
		return metaType;
		}
	
	/**
	 * Save down data
	 */
	public String saveMetadata(Element e)
		{
		if(frame!=null)
			e.setAttribute("frame",frame.toString());
		if(z!=null)
			e.setAttribute("z",z.toString());
		if(modelCamera!=null)
			{
			Element sub=new Element("modelcamera");
			modelCamera.toElement(sub);
			e.addContent(sub);
			}
		return metaType;
		}


	
	
	public void loadMetadata(Element e)
		{
		String sFrame=e.getAttributeValue("frame");
		String sZ=e.getAttributeValue("z");
		if(sFrame!=null)
			frame=new EvDecimal(sFrame);
		if(sZ!=null)
			z=new EvDecimal(sZ);
		Element eModelCamera=e.getChild("modelcamera");
		if(eModelCamera!=null)
			{
			modelCamera=new EvGLCamera();
			modelCamera.fromElement(eModelCamera);
			}
		
		
		}


	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		/*
		JMenuItem miGoto=new JMenuItem("Go to");
		menu.add(miGoto);

		miGoto.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				if()
				
				}
		});*/
		//GOTO
		
		}

	
	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,Bookmark.class);
		Viewer3DWindow.addExtension(BookmarkModelWindowHook.class);
		Viewer2DWindow.addImageWindowExtension(new BookmarkImageWindowHook());
		}
	}
