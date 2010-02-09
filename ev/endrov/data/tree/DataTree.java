/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data.tree;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import endrov.basicWindow.icon.BasicIcon;
import endrov.bookmark.Bookmark;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;

/**
 * Tree representation of Endrov container hierarchy
 * @author Johan Henriksson
 *
 */
public class DataTree extends JTree
	{
	private static final long serialVersionUID = 1L;

	public DataTree()
		{
		super(new DataTreeModel());
		setCellRenderer(new MyRenderer());
		setShowsRootHandles(true);
		setRootVisible(false);
		}
	
	/******************************************************************************************************
	 *                               Custom appearance of tree nodes                                      *
	 *****************************************************************************************************/

	private static class MyRenderer extends DefaultTreeCellRenderer
		{
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) 
			{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,	hasFocus);
			
			DataTreeElement elem=(DataTreeElement)value;
			if(elem.isRoot)
				setIcon(BasicIcon.iconEndrov);
			else
				{
				setIcon(elem.getLeaf().getContainerIcon());
//				setIcon(BasicIcon.iconData);
				
				//Need generic object icon
				
				}
//			setToolTipText(decl.description);
	//		setToolTipText(null);
			
			return this;
			}
		}
	
	
	/**
	 * The tree has been updated. should be passed through listeners rather, as it allows for a
	 * more clever update.
	 */
	public void dataUpdated()
		{
		getModel().dataUpdated();
		}
	
	public DataTreeModel getModel()
		{
		return (DataTreeModel)super.getModel(); 
		}
	
	public static void main(String[] args)
		{
		JFrame frame=new JFrame();
		frame.setSize(200, 200);

		EvData d=new EvData();
		Bookmark b=new Bookmark();
		d.metaObject.put("foo", b);

		EvData.registerOpenedData(d);
		EvData.registerOpenedData(new EvData());

	//	JTree tree=new JTree(new DataTreeModel());
		DataTree tree=new DataTree();
		frame.add(tree);
//		tree.setCellRenderer(new MyRenderer());
		//Tree must listen on data updates
		
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		}

	
	
	
	/**
	 * Get the paths to all selected objects
	 */
	public List<EvPath> getSelectedPaths()
		{
		TreePath[] sel=getSelectionPaths();
		LinkedList<EvPath> paths=new LinkedList<EvPath>();
		for(TreePath o:sel)
			paths.add(((DataTreeElement)o.getLastPathComponent()).getPath());
		return paths;
		}

	/**
	 * Get all selected objects
	 */
	public List<EvContainer> getSelectedObjects()
		{
		TreePath[] sel=getSelectionPaths();
		LinkedList<EvContainer> paths=new LinkedList<EvContainer>();
		for(TreePath o:sel)
			paths.add(((DataTreeElement)o.getLastPathComponent()).getLeaf());
		return paths;
		}

	
	}
