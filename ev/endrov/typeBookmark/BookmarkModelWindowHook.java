/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeBookmark;

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
import endrov.data.gui.EvDataGUI;
import endrov.gl.EvGLCamera;
import endrov.gui.EvSwingUtil;
import endrov.gui.undo.UndoOpPutObject;
import endrov.util.math.EvDecimal;
import endrov.windowViewer3D.BoundingBox3D;
import endrov.windowViewer3D.Viewer3DWindow;
import endrov.windowViewer3D.Viewer3DWindowHook;
import endrov.windowViewer3D.TransparentRenderer3D;

/**
 * Bookmark integration with model window
 * @author Johan Henriksson
 *
 */
public class BookmarkModelWindowHook implements Viewer3DWindowHook, ActionListener
	{
	private Viewer3DWindow w;
	private JMenuItem miAddBookmark=new JMenuItem("Add bookmark");
	private JMenu miBookmark=new JMenu("Bookmarks");
	
	public void createHook(Viewer3DWindow w)
		{
		this.w=w;
		w.menuModel.add(miBookmark);
		datachangedEvent();
		}
	
	
	public Collection<BoundingBox3D> adjustScale()
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
		
		for(EvData data:EvDataGUI.openedData)
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
							w.view.camera=new EvGLCamera(m.modelCamera);
						w.repaint();
						}
					});
				}
			if(miData.getItemCount()>0)
				miBookmark.add(miData);
			}
		
		
		
		}
	public void initOpenGL(GL gl)
		{
		}
	public void displayFinal(GL gl, List<TransparentRenderer3D> transparentRenderers)
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
			b.modelCamera=new EvGLCamera(w.view.camera);
			new UndoOpPutObject("Add bookmark "+name, b, container, name);
			}
		}
	
	
	
	
	}