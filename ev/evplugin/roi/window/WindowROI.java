package evplugin.roi.window;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.roi.*;
import evplugin.roi.primitive.*;
//import evplugin.imageset.*;
import evplugin.basicWindow.*;
import org.jdom.*;

/**
 * ROI Window - Display all ROIs
 * 
 * @author Johan Henriksson
 */
public class WindowROI extends BasicWindow implements ActionListener, MetaCombo.comboFilterMetadata, TreeSelectionListener
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
			new WindowROI();
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
	private static ImageIcon iconDelete=new ImageIcon(WindowROI.class.getResource("iconDelete.png"));
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private JButton bNewDiff=new JButton(iconDiff);
	private JButton bNewIntersect=new JButton(iconIntersect);
	private JButton bNewSub=new JButton(iconSub);
	private JButton bNewUnion=new JButton(iconUnion);
	private JButton bDelete=new JButton(iconDelete);
	
	
	private MetaCombo metaCombo=new MetaCombo(this, false);
	public boolean comboFilterMetadataCallback(EvData meta)
		{
		return true;
		}

	private ROITreeModel treeModel=new ROITreeModel();
	private JTree tree=new JTree(treeModel);
	
	
	
	/**
	 * Make a new window at default location
	 */
	public WindowROI()
		{
		this(600,300,500,300);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public WindowROI(int x, int y, int w, int h)
		{		
		//Put GUI together
		setLayout(new BorderLayout());
	
		JPanel bottom=new JPanel(/*new GridLayout(1,4)*/);
		add(metaCombo,BorderLayout.NORTH);
		add(tree, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		bottom.add(bNewDiff);
		bottom.add(bNewIntersect);
		bottom.add(bNewSub);
		bottom.add(bNewUnion);
		bottom.add(bDelete);
		
		bNewDiff.addActionListener(this);
		bNewIntersect.addActionListener(this);
		bNewSub.addActionListener(this);
		bNewUnion.addActionListener(this);
		bDelete.addActionListener(this);
		tree.addTreeSelectionListener(this);
		metaCombo.addActionListener(this);
		
		//Window overall things
		setTitle(EV.programName+" ROI");
		pack();
		setBounds(x,y,w,h);
		dataChangedEvent();
		setVisible(true);
		}
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		}

	
	

	public void valueChanged(TreeSelectionEvent e)
		{
		ROI.selected.clear(); //note! what about other windows?
		TreePath[] selection=tree.getSelectionPaths();
		if(selection!=null)
			for(TreePath tp:selection)
				{
				Object o=tp.getLastPathComponent();
				ROITreeElement to=(ROITreeElement)o;
				if(to.e instanceof ROI)
					ROI.selected.add((ROI)to.e);
				}
		BasicWindow.updateWindows(this);
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
		metaCombo.updateList();
		treeModel.emitAllChanged(); //overkill?
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
					if(ROI.selected.contains((ROI)ob))
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
				if(ROI.selected.contains(roi))
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
	
	}
