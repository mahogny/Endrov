package endrov.roi.window;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.*;
import endrov.ev.*;
import endrov.roi.*;



//what about selection from multiple imagesets?


/**
 * ROI Window - List and edit all ROIs
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
			JMenuItem mi=new JMenuItem("ROI",new ImageIcon(getClass().getResource("iconWindow.png")));
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			WindowROI.getRoiWindow();
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
		treeModel.setMetaObject(metaCombo.getMeta());
		traverseSelection();
		
		addTreeListeners();
		ROI.selectionChanged.addWeakListener(listenSelection);
		

		bDelete.addActionListener(this);
		metaCombo.addActionListener(this);

		updateLayout();
		
		//Window overall things
		setTitleEvWindow("ROI");
		packEvWindow();
		setBoundsEvWindow(x,y,w,h);
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
		treeModel.setMetaObject(metaCombo.getMeta());
		treeModel.emitAllChanged(); //overkill?
		traverseSelection();
		}
	
	
	


	private void makeCompoundROI(CompoundROI croi)
		{
		EvData data=metaCombo.getMeta();
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

	
	public void loadedFile(EvData data){}

	
	}
