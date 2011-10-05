package endrov.dataBrowser;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;

public class DataBrowserTree extends JTree implements TreeModel
	{
	private static final long serialVersionUID = 1L;
	
	private List<TreeModelListener> listeners=new LinkedList<TreeModelListener>();
	
	
	//TreeDragSource ds;
	//TreeDropTarget dt;

	/**
	 * 
	 * 
	//	JXTreeTable   from swingx? 
	 * 
	 * columns can be shown if wanted. mod date.
	 * 
	 * column for visibility?
	 * 
	 * new voxel renderer: do all textures at the same time! sorting problem solved
	 * * find closest vertex
	 * * find furthest vertex
	 * * loop through, generate planes
	 * can even make bounded squares! drop values inside shader.
	 * ? support for rendering sub-volumes. work for marcus...
	 * 
	 *
	 */
	
	public static class Node //implements Transferable
		{
		Node parent;
		EvContainer con;
		ArrayList<Node> children=new ArrayList<Node>();
		String name;
		
		public Node()
			{
			}
		
		public Node(Node n)
			{
			parent=n.parent;
			con=n.con;
			children.addAll(n.children); //TODO is this a good thing? recurse?
			name=n.name;
			}
		
		@Override
		public String toString()
			{
			if(parent==null)
				return "Loaded files";
			else
				{
				return name;
				}
			}
		
		
		public boolean isAbove(Node other)
			{
			//TODO
			return true;
			}
		
		
		public void updateChildren()
			{
			//TODO reuse existing children!
			children.clear();
			
			if(con==null)
				{
				//List opened data files
				for(EvData d:EvData.openedData)
					{
					System.out.println("adding "+d);
					
					Node n=new Node();
					n.parent=this;
					n.con=d;
					n.name=d.getMetadataName();
					/*
					if(d.io!=null)
						n.name=d.io.getMetadataName();
					else
						n.name="<unnamed>";
						*/

					children.add(n);

					//Recurse
					n.updateChildren();
					
					}
		
		
				}
			else
				{
				//List sub-objects
				for(String name:con.metaObject.keySet())
					{
					
					Node n=new Node();
					n.parent=this;
					n.con=con.metaObject.get(name);
					n.name=name;
					
					n.updateChildren();
					
					children.add(n);
					}
				
				}
			}



		public int getChildCount()
			{
			return children.size();
			}

		
		/*
		public static final DataFlavor DnDNode_FLAVOR = new DataFlavor(Node.class, "Drag and drop Node");
		protected static DataFlavor[] flavors = { Node.DnDNode_FLAVOR };


		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
			{
			if (this.isDataFlavorSupported(flavor))
				return this;
			else
				throw new UnsupportedFlavorException(flavor);
			}

		public DataFlavor[] getTransferDataFlavors()
			{
			return flavors;
			}

		public boolean isDataFlavorSupported(DataFlavor flavor)
			{
			DataFlavor[] flavs = this.getTransferDataFlavors();
			for (int i = 0; i < flavs.length; i++)
				if (flavs[i].equals(flavor))
					return true;
			return false;
			}
*/
		}
	
	
	private Node root=new Node();
	
	
	

	public DataBrowserTree()
		{
		root.updateChildren();
		setModel(this);
	
		
		setCellRenderer(new DefaultTreeCellRenderer(){
			private static final long serialVersionUID = 1L;
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus)
				{
				super.getTreeCellRendererComponent(
            tree, value, selected,
            expanded, leaf, row,
            hasFocus);

				Node n=(Node)value;
				if(n.con!=null)
					{
					setIcon(n.con.getContainerIcon());
					if(n.con instanceof EvObject)
						setToolTipText(((EvObject)n.con).getMetaTypeDesc());
					}
				return this;
				}
			});
	
		
		// ds = new TreeDragSource(this, DnDConstants.ACTION_COPY_OR_MOVE);
	    //dt = new TreeDropTarget(this);
	    
	    setDragEnabled(true);  
	    //setDropMode(DropMode.ON_OR_INSERT);  
	    setTransferHandler(new DataBrowserTreeDnD.TreeTransferHandler());  
		}
	
	
	
	public Object getValueAt(Object node, int row)
		{
		Node n=(Node)node;
		return n.children.get(row);
		}
	
	
	
	public boolean isCellEditable(Object arg0, int arg1)
		{
		return false;
		}
	
	
	
	public void setValueAt(Object arg0, Object arg1, int arg2)
		{
		}
	
	
	
	
	public void addTreeModelListener(TreeModelListener arg0)
		{
		listeners.add(arg0);
		}
	
	
	
	public Object getChild(Object parent, int row)
		{
		Node parentn=(Node)parent;
		return parentn.children.get(row);
		}
	
	
	
	public int getChildCount(Object parent)
		{
		Node parentn=(Node)parent;
		return parentn.children.size();
		}
	
	
	
	public int getIndexOfChild(Object parent, Object ob)
		{
		Node n=(Node)parent;
		return n.children.indexOf(ob);
		}
	
	
	
	public Object getRoot()
		{
		return root;
		}
	
	
	
	public boolean isLeaf(Object ob)
		{
		Node n=(Node)ob;
		return n.children.isEmpty();
		}
	
	
	
	public void removeTreeModelListener(TreeModelListener arg0)
		{
		listeners.remove(arg0);
		}
	
	
	
	public void valueForPathChanged(TreePath arg0, Object arg1)
		{
		//TODO this might be useful!
		}
	
	
	public void dataChangedEvent()
		{
		root.updateChildren();
		for(TreeModelListener l:listeners)
			l.treeStructureChanged(new TreeModelEvent(this, new Object[]{root}));
		}
	
	
	
	

	
	
	
		
	
	}
