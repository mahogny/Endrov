/**
 * 
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

import endrov.basicWindow.BasicWindow;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.modelWindow.Camera;
import endrov.modelWindow.ModelWindow;
import endrov.modelWindow.ModelWindowHook;
import endrov.modelWindow.TransparentRender;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

public class BookmarkModelWindowHook implements ModelWindowHook, ActionListener
	{
	private final ModelWindow w;
	private JMenuItem miAddBookmark=new JMenuItem("New");
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
	public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
		{
		return Collections.emptySet();
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
		
		for(EvData data:EvData.metadata)
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
						if(w.frameControl!=null)
							w.frameControl.setFrame(m.frame);
						if(m.modelCamera!=null)
							w.view.camera=new Camera(m.modelCamera);
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
		EvContainer data=w.getSelectedData();
		if(data==null)
			BasicWindow.showErrorDialog("No container selected");
		else
			{
			String name=JOptionPane.showInputDialog(w, "Name of bookmark");
			if(name!=null)
				{
				if(data.metaObject.containsKey(name))
					BasicWindow.showErrorDialog("Object with this name exists already");
				else
					{
					Bookmark b=new Bookmark();
					b.frame=w.frameControl.getFrame();
					b.modelCamera=new Camera(w.view.camera);
					
					data.metaObject.put(name, b);
					BasicWindow.updateWindows();
					}
				}
			
			
			}
		}
	
	
	
	
	}