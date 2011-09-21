/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.worms;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.Map;

import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.EvContainer;
import endrov.imageWindow.*;
import endrov.network.Network;
import endrov.util.EvDecimal;

/**
 * Make and edit networks
 *
 * @author Johan Henriksson
 */
public class WormImageTool implements ImageWindowTool, ActionListener
	{
	private final ImageWindow w;
	private final WormImageRenderer r;
	
	private WeakReference<WormFit> editingObject=new WeakReference<WormFit>(null);
	private void setEditObject(WormFit lin)
		{
		editingObject=new WeakReference<WormFit>(lin);
		}
	
	public WormImageTool(final ImageWindow w, WormImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	
	
	
	private void fillMenu(JComponent menu)
		{
		
		
		//menu.add(new JSeparator());
		
		EvContainer ims=w.getRootObject();
		final WeakReference<EvContainer> wims=new WeakReference<EvContainer>(ims);
		if(ims!=null)
			for(Map.Entry<String, WormFit> e:ims.getIdObjects(WormFit.class).entrySet())
				{
				JCheckBoxMenuItem miEdit=new JCheckBoxMenuItem("Edit "+e.getKey());
				miEdit.setActionCommand(e.getKey());
				miEdit.setSelected(editingObject.get()==e.getValue());
				miEdit.addActionListener(this);
				menu.add(miEdit);
				}		
		
		JMenuItem miNew=new JMenuItem("New object");
		miNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				WormFit ob=new WormFit();
				wims.get().addMetaObject(ob);
				setEditObject(ob);
				w.setTool(WormImageTool.this);
				}
		});
		menu.add(miNew);
		
		}

	public JMenuItem getMenuItem()
		{
		JMenu menu=new JMenu("WormFit");
		fillMenu(menu);
		return menu;
		}
	public void actionPerformed(ActionEvent e)
		{
		String id=e.getActionCommand();
		setEditObject((WormFit)w.getRootObject().getMetaObject(id));
		w.setTool(this);
		}
	
	public void deselected()
		{
		}


	public void mouseClicked(MouseEvent e, Component invoker)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			WormFit editNetwork=editingObject.get();
			if(editNetwork!=null)
				{
				//EvDecimal frame=w.getFrame();
				
				
				
				

				BasicWindow.updateWindows();
				}
					
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			
			JPopupMenu menu=new JPopupMenu();

			JMenuItem miRun=new JMenuItem("Run algorithm");
			
			miRun.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						EvDecimal frame=w.getFrame();
						
						WormFit wfit=editingObject.get();
						if(wfit!=null)
							{
							WormAlgo.run(wfit, frame, w.getSelectedChannel());
							BasicWindow.updateWindows();
							}
						}
				});
			
			
			
			menu.add(miRun);
			
			menu.show(e.getComponent(),e.getX(),e.getY());
			
			}
		}
	
	
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		
		}
	
	
	public void mousePressed(MouseEvent e)
		{
	
		}

	
	public void mouseReleased(MouseEvent e)
		{
		
		}

	public void mouseMoved(MouseEvent e, int dx, int dy)
		{
		}

	

	
	public void keyPressed(KeyEvent e)
		{
		
		}


	
	public void paintComponent(Graphics g)
		{

		}
	
	public void keyReleased(KeyEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	
	}
