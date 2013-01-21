/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowDataBrowser;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.gui.EvDataGUI;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.windowDataBrowser.DataBrowserTree.Node;

/**
 * Data browsing - work on objects
 * @author Johan Henriksson
 *
 */
public class DataBrowserWindow extends EvBasicWindow implements MouseListener, TreeSelectionListener
	{
	private static final long serialVersionUID = 1L;

	private DataBrowserTree tree=new DataBrowserTree(); 
	

	
	
	public DataBrowserWindow()
		{
		setLayout(new GridLayout(1,1));
		
		JComponent mid=new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		add(mid);
		
		
		tree.addMouseListener(this);
		
		tree.addTreeSelectionListener(this);
		
		setTitleEvWindow("Data Browser");
		
		
		packEvWindow();
		setBoundsEvWindow(null, 400);
		
		
		setVisibleEvWindow(true);
		}
	
	/**
	 * Essentially: only the data tree. allow new operations to be registered.
	 * either on right-click or by clicking on a button. or menu?
	 * 
	 * actually, EvSelection can point to EvContainer. then operations can go into data menu
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	@Override
	public void dataChangedEvent()
		{
		
		tree.dataChangedEvent();
		}

	@Override
	public void windowFreeResources()
		{
		}

	@Override
	public void windowEventUserLoadedFile(EvData data)
		{
		dataChangedEvent();
		}

	@Override
	public void windowSavePersonalSettings(Element root)
		{
		}
	public void windowLoadPersonalSettings(Element e)
		{
		}
	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new DataBrowserBasic());
		}
	
	
	/**
	 * Extension to BasicWindow
	 * 
	 * @author Johan Henriksson
	 */
	private static class DataBrowserBasic implements EvBasicWindowExtension
		{
		public void newBasicWindow(EvBasicWindow w)
			{
			w.basicWindowExtensionHook.put(this.getClass(),new Hook());
			}
		private class Hook implements EvBasicWindowHook, ActionListener
			{
			public void createMenus(EvBasicWindow w)
				{
				JMenuItem mi=new JMenuItem("Data Browser",new ImageIcon(getClass().getResource("iconBrowser.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}
			
			public void actionPerformed(ActionEvent e) 
				{
				new DataBrowserWindow();
				}
			
			public void buildMenu(EvBasicWindow w){}
			}
		}


	public void mouseClicked(MouseEvent e)
		{
		if(SwingUtilities.isRightMouseButton(e))
			{
			TreePath[] paths=tree.getSelectionPaths();
			if(paths!=null)
				{
				JPopupMenu menu=new JPopupMenu();
				
				////// Rename entry
				if(paths.length==1)
					{
					final DataBrowserTree.Node n=(DataBrowserTree.Node)paths[0].getLastPathComponent();
					//Can only rename objects, not evdata
					if(n.parent!=null)
						{
						JMenuItem miRename=new JMenuItem("Rename "+n.name);
						miRename.addActionListener(new ActionListener()
							{
							public void actionPerformed(ActionEvent e)
								{
								String input=JOptionPane.showInputDialog(DataBrowserWindow.this, "New name for "+n.name, n.name);
								if(input!=null)
									{
									if(n.parent.con.metaObject.containsKey(input))
										showErrorDialog("Name already taken");
									else
										{
										n.parent.con.metaObject.remove(n.name);
										n.parent.con.metaObject.put(input, (EvObject)n.con);
										EvBasicWindow.updateWindows();
										}
									}
								}
							});
						menu.add(miRename);
						}
					}
				
				////// Delete entry
				final Set<DataBrowserTree.Node> toDelete=new HashSet<Node>();
				final Set<String> names=new TreeSet<String>();
				for(TreePath path:paths)
					{
					Node n=(Node)path.getLastPathComponent();
					if(n!=DataBrowserTree.root)
						{
						toDelete.add(n);
						names.add(n.name);
						}
					}
				if(!toDelete.isEmpty())
					{
					JMenuItem miDelete=new JMenuItem("Delete");
					miDelete.addActionListener(new ActionListener()	{
						public void actionPerformed(ActionEvent e)
							{
							if(showConfirmYesNoDialog(
									"Do you really want to delete these "+names.size()+" object(s)?"))
								{
								for(Node n:toDelete)
									{
									EvContainer parent=n.parent.con;
									if(parent==null)
										EvDataGUI.openedData.remove(n.con);
									else
										n.parent.con.metaObject.remove(n.name);
									}
								EvBasicWindow.updateWindows();
								}
							}
						});
					menu.add(miDelete);
					}
				
				if(menu.getComponentCount()!=0)
					menu.show(tree, e.getX(), e.getY());
				}
			

			}
		repaint();    //This is an ugly hack to fix the selection-does-not-get-updated-problem
		}

	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void mousePressed(MouseEvent e)
		{
		}

	public void mouseReleased(MouseEvent e)
		{
		}

	
	public void valueChanged(TreeSelectionEvent e)
		{
		/*
		//Find all selected
		HashSet<EvSelectable> sels=new HashSet<EvSelectable>(); 
		for(TreePath p:tree.getSelectionPaths())
			{
			DataBrowserTree.Node n=(DataBrowserTree.Node)p.getLastPathComponent();
			sels.add(new EvSelectObject<EvContainer>(n.con));
			}
		
		//Compare with current global selection
		if(!EvSelection.selected.equals(sels))
			EvSelection.selectOnly(sels);
			*/
		}

	}
