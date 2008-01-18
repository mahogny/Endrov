package evplugin.roi.window;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import org.jdom.*;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.roi.*;
import evplugin.roi.primitive.*;
import evplugin.basicWindow.*;



//what about selection from multiple imagesets?


/**
 * ROI Window - Display all ROIs
 * 
 * @author Johan Henriksson
 */
public class WindowROI extends BasicWindow implements ActionListener, MetaCombo.comboFilterMetadata, TreeSelectionListener, TreeExpansionListener
	{
	static final long serialVersionUID=0;

	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static class ThisBasicHook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("ROI");
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			WindowROI.getRoiWindow();
			//new WindowROI();
			}
		public void buildMenu(BasicWindow w){}
		}
	
	
	public static void initPlugin()	{}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new ThisBasicHook());
				}
			});
		}
	
	private static ImageIcon iconDiff=new ImageIcon(WindowROI.class.getResource("iconDiff.png"));
	private static ImageIcon iconIntersect=new ImageIcon(WindowROI.class.getResource("iconIntersect.png"));
	private static ImageIcon iconSub=new ImageIcon(WindowROI.class.getResource("iconSub.png"));
	private static ImageIcon iconUnion=new ImageIcon(WindowROI.class.getResource("iconUnion.png"));

	
	/**
	 * Get current ROI window or open one
	 */
	public static WindowROI getRoiWindow()
		{
		for(BasicWindow w:BasicWindow.getWindowList())
			if(w instanceof WindowROI)
				return (WindowROI)w;
		return new WindowROI();
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private JButton bNewDiff=new JButton(iconDiff);
	private JButton bNewIntersect=new JButton(iconIntersect);
	private JButton bNewSub=new JButton(iconSub);
	private JButton bNewUnion=new JButton(iconUnion);
	private JButton bDelete=new JButton(getIconDelete());
	private JPanel upperPanel=new JPanel(new GridLayout(2,1));
	private MetaCombo metaCombo=new MetaCombo(this, false);
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
	 * Make a new window at default location
	 */
	public WindowROI()
		{
		this(600,200,400,500);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public WindowROI(int x, int y, int w, int h)
		{		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		//Put GUI together		
		JPanel bp=new JPanel(new GridLayout(1,5));
		bp.add(bNewDiff);
		bp.add(bNewIntersect);
		bp.add(bNewSub);
		bp.add(bNewUnion);
		bp.add(bDelete);

		upperPanel.add(metaCombo);
		upperPanel.add(bp);

		//Init selection
		metaCombo.updateList();
		treeModel.setMetaObject(metaCombo.getMeta());
		traverseSelection();
		
		addTreeListeners();
		ROI.selectionChanged.addWeakListener(listenSelection);
		
		bNewDiff.addActionListener(this);
		bNewIntersect.addActionListener(this);
		bNewSub.addActionListener(this);
		bNewUnion.addActionListener(this);
		bDelete.addActionListener(this);
		metaCombo.addActionListener(this);

		updateLayout();
		
		//Window overall things
		setTitle(EV.programName+" ROI");
		pack();
		setBounds(x,y,w,h);
	//	dataChangedEvent();
		setVisible(true);
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

	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		}

	
	
	/**
	 * Listener: tree selection changed
	 */
	public void valueChanged(TreeSelectionEvent e)
		{
//		System.out.println("valuechanged");
		HashSet<ROI> selected=new HashSet<ROI>();
		TreePath[] selection=tree.getSelectionPaths();
		if(selection!=null)
			for(TreePath tp:selection)
				{
				Object o=tp.getLastPathComponent();
				ROITreeElement to=(ROITreeElement)o;
				if(to.e instanceof ROI)
					selected.add((ROI)to.e);
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
			treeModel.setMetaObject(metaCombo.getMeta());
			///channelCombo.setImageset(metaCombo.getImageset());
			}
		else if(e.getSource()==bNewUnion)
			makeCompoundROI(new UnionROI());
		else if(e.getSource()==bNewIntersect)
			makeCompoundROI(new IntersectROI());
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
//		System.out.println("all changed");
		metaCombo.updateList();
		treeModel.setMetaObject(metaCombo.getMeta());
		treeModel.emitAllChanged(); //overkill?
		traverseSelection();
		}
	
	

	private void makeCompoundROI(CompoundROI croi)
		{
		EvData data=metaCombo.getMeta();
		if(data!=null)
			{
			Set<ROI> rois=collectRecursiveROI(data);
			data.addMetaObject(croi);
			for(ROI roi:rois)
				croi.subRoi.add(roi);
			BasicWindow.updateWindows();
			}
		}
	
	
	/**
	 * Remove all selected ROIs recursively from their parents and return them
	 */
	private Set<ROI> collectRecursiveROI(Object parent)
		{
		HashSet<ROI> hs=new HashSet<ROI>();
		if(parent instanceof EvData)
			{
			EvData data=(EvData)parent;
			Set<Integer> toremove=new HashSet<Integer>();
			for(int key:data.metaObject.keySet())
				{
				EvObject ob=data.metaObject.get(key);
				if(ob instanceof ROI)
					{
					if(ROI.isSelected((ROI)ob))
						{
						toremove.add(key);
						hs.add((ROI)ob);
						}
					else
						hs.addAll(collectRecursiveROI(ob));
					}
				}
			for(int key:toremove)
				data.metaObject.remove(key);
			}
		else if(parent instanceof CompoundROI)
			{
			Set<ROI> toremove=new HashSet<ROI>();
			for(ROI roi:((CompoundROI)parent).subRoi)
				{
				if(ROI.isSelected(roi))
					{
					toremove.add(roi);
					hs.add((ROI)roi);
					}
				else
					hs.addAll(collectRecursiveROI(roi));
				}
			((CompoundROI)parent).subRoi.removeAll(toremove);
			}
		return hs;
		}
	
	
	
	/**
	 * Insert ROI component to edit.
	 * Should one allow several to be open?
	 */
	private void updateLayout()
		{
		Container c=getContentPane();
		
		c.setLayout(new BorderLayout());
		c.removeAll();
		c.add(upperPanel, BorderLayout.NORTH);
		c.add(tree, BorderLayout.CENTER);
		
		Collection<ROI> rois=ROI.getSelected();
		if(rois.size()==1)
			{
			ROI roi=rois.iterator().next();
			
			JPanel editpanel=new JPanel(new GridLayout(1,1));
			editpanel.setBorder(BorderFactory.createTitledBorder("Edit "+roi.getMetaTypeDesc()));
			JComponent d=roi.getROIWidget();
			c.add(editpanel,BorderLayout.SOUTH);
			if(d==null)
				d=new JLabel("There are no options");
			editpanel.add(d);
			}
		c.validate();
		}

	

	
	
	
	
	
	}
