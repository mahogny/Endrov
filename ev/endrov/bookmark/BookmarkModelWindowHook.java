/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.bookmark;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.modelWindow.ModelWindow;
import endrov.modelWindow.ModelWindowHook;
import endrov.modelWindow.TransparentRender;
import endrov.modelWindow.gl.GLCamera;
import endrov.undo.UndoOpPutObject;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * Bookmark integration with model window
 * @author Johan Henriksson
 *
 */
public class BookmarkModelWindowHook implements ModelWindowHook, ActionListener
	{
	private final ModelWindow w;
	private JMenuItem miAddBookmark=new JMenuItem("Add bookmark");
	private JMenu miBookmark=new JMenu("Bookmarks");
	
	public BookmarkModelWindowHook(ModelWindow w)
		{
		this.w=w;
		
		
		w.menuModel.add(miBookmark);
		datachangedEvent();
		}
	
	
	public Collection<Double> adjustScale()
		{
		return Collections.emptySet();
		}
	public Collection<Vector3d> autoCenterMid()
		{
		return Collections.emptySet();
		}
	public double autoCenterRadius(Vector3d mid)
		{
		return 0;
		}
	public boolean canRender(EvObject ob)
		{
		return false;
		}
	public void datachangedEvent()
		{
		EvSwingUtil.tearDownMenu(miBookmark);
		miBookmark.add(miAddBookmark);
		miAddBookmark.addActionListener(this);
		miBookmark.addSeparator();
		
		for(EvData data:EvData.openedData)
			{
			JMenu miData=new JMenu(data.getMetadataName());
			for(Map.Entry<EvPath, Bookmark> e:data.getIdObjectsRecursive(Bookmark.class).entrySet())
				{
				JMenuItem miGoto=new JMenuItem("=> "+e.getKey());
				miData.add(miGoto);
				final Bookmark m=e.getValue();
				
				miGoto.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						w.setFrame(m.frame);
						if(m.modelCamera!=null)
							w.view.camera=new GLCamera(m.modelCamera);
						w.repaint();
						}
					});
				}
			if(miData.getItemCount()>0)
				miBookmark.add(miData);
			}
		
		
		
		}
	public void displayFinal(GL gl, List<TransparentRender> transparentRenderers)
		{
		}
	public void displayInit(GL gl)
		{
		}
	public void displaySelect(GL gl)
		{
		}
	public void fillModelWindowMenus()
		{
		}
	public EvDecimal getFirstFrame()
		{
		return null;
		}
	public EvDecimal getLastFrame()
		{
		return null;
		}
	public void readPersonalConfig(Element e)
		{
		}
	public void savePersonalConfig(Element e)
		{
		}
	
	public void actionPerformed(ActionEvent e)
		{
		EvContainer container=w.getSelectedData();
		String name=Bookmark.addBookmarkDialog(w, container);
		if(name!=null)
			{
			Bookmark b=new Bookmark();
			b.frame=w.getFrame();
			b.modelCamera=new GLCamera(w.view.camera);
			new UndoOpPutObject("Add bookmark "+name, b, container, name);
			}
		}
	
	
	
	
	}