/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.gui.EvDataGUI;

/**
 * Tree of all objects
 * 
 * @author Johan Henriksson
 *
 */
class EvComboObjectTree extends JTree
	{
	static final long serialVersionUID=0;
	
	public EvComboObjectTree()
		{
		super(new MyTreeNode());
		
		
		}
	
	//TODO update tree on object update. probably requires a treemodel
	//TODO drag and drop
	
	public EvContainer getSelectedContainer()
		{
		TreePath p=getSelectionPath();
		if(p!=null)
			{
			MyTreeNode node=(MyTreeNode)p.getLastPathComponent();
			return node.con;
			}
		else
			return null;
		}
	
	
	private static class MyTreeNode implements TreeNode
		{
		private MyTreeNode parent;
		private EvContainer con;
		String name;
		Vector<MyTreeNode> children=new Vector<MyTreeNode>();

		/**
		 * Construct root
		 */
		public MyTreeNode()
			{
			name="";
			for(EvData data:EvDataGUI.openedData)
				children.add(new MyTreeNode(this, data.getMetadataName(),data));
			}
		
		/**
		 * Construct from container
		 */
		public MyTreeNode(MyTreeNode parent, String thisName, EvContainer con)
			{
			this.parent=parent;
			name=thisName;
			this.con=con;
			for(Map.Entry<String, EvObject> entry:con.metaObject.entrySet())
				children.add(new MyTreeNode(this,entry.getKey(),entry.getValue()));
			}
		
		public Enumeration<MyTreeNode> children()
			{
			return children.elements();
			}

		public boolean getAllowsChildren()
			{
			return true;
			}

		public TreeNode getChildAt(int i)
			{
			return children.get(i);
			}

		public int getChildCount()
			{
			return children.size();
			}

		public int getIndex(TreeNode node)
			{
			return children.indexOf(node);
			}

		public TreeNode getParent()
			{
			return parent;
			}

		public boolean isLeaf()
			{
			return children.isEmpty();// && con instanceof EvObject; //More criteria?
			}
		
		public String toString()
			{
			if(con instanceof EvData)
				return name+":data";
			else if(con instanceof EvObject)
				return name+":"+((EvObject)con).getMetaTypeDesc();
			else
				return name;
			}
		
		}
	
	}
