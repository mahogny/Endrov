/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi.window;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import org.jdom.*;

import endrov.core.observer.SimpleObserver;
import endrov.data.*;
import endrov.gui.component.EvComboObjectOne;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.roi.*;
import endrov.typeImageset.Imageset;



//what about selection from multiple imagesets?


/**
 * ROI Window - List and edit all ROIs
 * 
 * @author Johan Henriksson
 */
public class WindowROI extends EvBasicWindow implements ActionListener, TreeSelectionListener, TreeExpansionListener
	{
	static final long serialVersionUID=0;

	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static class ThisBasicHook implements EvBasicWindowHook, ActionListener
		{
		public void createMenus(EvBasicWindow w)
			{
			JMenuItem mi=new JMenuItem("ROI",new ImageIcon(getClass().getResource("iconWindow.png")));
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			WindowROI.getRoiWindow();
			}
		public void buildMenu(EvBasicWindow w){}
		}
	
	

	
	/**
	 * Get current ROI window or open one
	 */
	public static WindowROI getRoiWindow()
		{
		for(EvBasicWindow w:EvBasicWindow.getWindowList())
			if(w instanceof WindowROI)
				return (WindowROI)w;
		return new WindowROI();
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private JButton bDelete=BasicIcon.getButtonDelete();
	private JPanel upperPanel=new JPanel(new GridLayout(2,1));
	private EvComboObjectOne<Imageset> metaCombo=new EvComboObjectOne<Imageset>(new Imageset(),false,false);
	public boolean comboFilterMetadataCallback(EvData meta)
		{
		return true;
		}
	private ROITreeModel treeModel=new ROITreeModel();
	private JTree tree=new JTree(treeModel);

	
	SimpleObserver.Listener listenSelection=new SimpleObserver.Listener()
		{
		public void observerEvent(Object src)
			{
			//System.out.println("observe");
			traverseSelection();
			updateLayout();
			}
		};

	
	
	/**
	 * Make a new window
	 */
	public WindowROI()
		{		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		
		Vector<JButton> newButtons=new Vector<JButton>();
		for(final ROI.ROIType rt:ROI.getTypes())
			{
			if(rt.isCompound() && rt.getIcon()!=null)
				{
				JButton button=new JButton(rt.getIcon());
				button.addActionListener(new ActionListener()
					{public void actionPerformed(ActionEvent e)
						{
						makeCompoundROI((CompoundROI)rt.makeInstance());
						}});
				newButtons.add(button);
				}
			}
		
		//Put GUI together		
		JPanel bp=new JPanel(new GridLayout(1,1+newButtons.size()));
		for(JButton b:newButtons)
			bp.add(b);
		bp.add(bDelete);

		upperPanel.add(metaCombo);
		upperPanel.add(bp);

		//Init selection
		metaCombo.updateList();
		treeModel.setMetaObject(metaCombo.getSelectedObject());
		traverseSelection();
		
		addTreeListeners();
		ROI.selectionChanged.addWeakListener(listenSelection);
		

		bDelete.addActionListener(this);
		metaCombo.addActionListener(this);

		updateLayout();
		
		//Window overall things
		setTitleEvWindow("ROI");
		packEvWindow();
		setBoundsEvWindow(600,200,400,500);
	//	dataChangedEvent();
		setVisibleEvWindow(true);
		}
	
	private void addTreeListeners()
		{
		tree.addTreeSelectionListener(this);
		tree.addTreeExpansionListener(this);
		}

	private void removeTreeListeners()
		{
		tree.removeTreeExpansionListener(this);
		tree.removeTreeSelectionListener(this);
		}

	
	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}

	
	
	/**
	 * Listener: tree selection changed
	 */
	public void valueChanged(TreeSelectionEvent e)
		{
//		System.out.println("valuechanged");
		HashSet<ROI> selected=new HashSet<ROI>();
		TreePath[] selection=tree.getSelectionPaths();
		System.out.println("---");
		if(selection!=null)
			for(TreePath tp:selection)
				{
				Object o=tp.getLastPathComponent();
				ROITreeElement to=(ROITreeElement)o;
				if(to.e instanceof ROI)
					selected.add((ROI)to.e);
				System.out.println(to.e);
				}
		removeTreeListeners();
		ROI.setSelected(selected);
		addTreeListeners();
		}
	
	
	
	public void treeCollapsed(TreeExpansionEvent e)
		{
//		System.out.println("collapsed");
		TreePath path=e.getPath();
		ROITreeElement parent=(ROITreeElement)path.getLastPathComponent();
		int numc=treeModel.getChildCount(parent);
		removeTreeListeners();
		for(int i=0;i<numc;i++)
			{
			ROITreeElement child=(ROITreeElement)treeModel.getChild(parent, i);
			tree.removeSelectionPath(child.getPath());
			}
		addTreeListeners();
		}
	public void treeExpanded(TreeExpansionEvent e)
		{
	//	System.out.println("expanded");
		TreePath path=e.getPath();
		ROITreeElement parent=(ROITreeElement)path.getLastPathComponent();
		removeTreeListeners();
		traverseSelection(parent,false);
		addTreeListeners();
		}
	
	public void traverseSelection()
		{
		removeTreeListeners();
		ROITreeElement root=(ROITreeElement)treeModel.getRoot();
		traverseSelection(root,false);
		addTreeListeners();
		}
	public void traverseSelection(ROITreeElement e, boolean removeAll)
		{
		TreePath path=e.getPath();
		ROITreeElement parent=(ROITreeElement)path.getLastPathComponent();
		int numc=treeModel.getChildCount(parent);
		for(int i=0;i<numc;i++)
			{
			ROITreeElement child=(ROITreeElement)treeModel.getChild(parent, i);
			ROI childroi=child.getROI();
			if(childroi!=null)
				{
				boolean iss=ROI.isSelected(childroi);
				if(removeAll || !iss)
					tree.removeSelectionPath(child.getPath());
				else if(iss)
					tree.addSelectionPath(child.getPath());
				}
			}
		
		}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==metaCombo)
			{
			treeModel.setMetaObject(metaCombo.getSelectedObject());
			///channelCombo.setImageset(metaCombo.getImageset());
			}
		else if(e.getSource()==bDelete)
			ROI.deleteSelected();
		
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
//		System.out.println("all changed");
		metaCombo.updateList();
		treeModel.setMetaObject(metaCombo.getSelectedObject());
		treeModel.emitAllChanged(); //overkill?
		traverseSelection();
		}
	
	
	


	private void makeCompoundROI(CompoundROI croi)
		{
		EvContainer data=metaCombo.getSelectedObject();
		if(data!=null)
			CompoundROI.makeCompoundROI(data,croi);
		}

	
	
	/**
	 * Insert ROI component to edit.
	 * Should one allow several to be open?
	 */
	private void updateLayout()
		{
		setLayout(new BorderLayout());
		removeAll();
		add(upperPanel, BorderLayout.NORTH);
		add(tree, BorderLayout.CENTER);
		
		Collection<ROI> rois=ROI.getSelected();
		if(rois.size()==1)
			{
			ROI roi=rois.iterator().next();
			
			JPanel editpanel=new JPanel(new GridLayout(1,1));
			editpanel.setBorder(BorderFactory.createTitledBorder("Edit "+roi.getMetaTypeDesc()));
			JComponent d=roi.getROIWidget();
			add(editpanel,BorderLayout.SOUTH);
			if(d==null)
				d=new JLabel("There are no options");
			editpanel.add(d);
			}
		validate();
		}

	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}


	@Override
	public String windowHelpTopic()
		{
		return null;
		}	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()	{}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(EvBasicWindow w)
				{
				w.addHook(this.getClass(),new ThisBasicHook());
				}
			});
		}
	
	}
