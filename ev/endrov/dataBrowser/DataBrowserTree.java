package endrov.dataBrowser;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DropMode;
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
	
	private static class Node
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
		}
	
	
	
	
	//public class DataTreeModel 
	
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
	    setTransferHandler(new TreeTransferHandler());  
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	
	
	private class TreeDragSource implements DragSourceListener, DragGestureListener
		{
		DragSource source;
		DragGestureRecognizer recognizer;
		TransferableTreeNode transferable;
		Node oldNode;
		JTree sourceTree;

		public TreeDragSource(JTree tree, int actions)
			{
			sourceTree = tree;
			source = new DragSource();
			recognizer = source.createDefaultDragGestureRecognizer(sourceTree, actions, this);
			}

		public void dragGestureRecognized(DragGestureEvent dge)
			{
			TreePath path = sourceTree.getSelectionPath();
			if ((path==null)||(path.getPathCount()<=1))
				{
				// We can't move the root node or an empty selection
				return;
				}
			oldNode = (Node) path.getLastPathComponent();
			transferable = new TransferableTreeNode(path);
			source.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable, this);

			// If you support dropping the node anywhere, you should probably
			// start with a valid move cursor:
			// source.startDrag(dge, DragSource.DefaultMoveDrop, transferable, this);
			}

		public void dragEnter(DragSourceDragEvent dsde)
			{
			}

		public void dragExit(DragSourceEvent dse)
			{
			}

		public void dragOver(DragSourceDragEvent dsde)
			{
			}

		public void dropActionChanged(DragSourceDragEvent dsde)
			{
			System.out.println("Action: "+dsde.getDropAction());
			System.out.println("Target Action: "+dsde.getTargetActions());
			System.out.println("User Action: "+dsde.getUserAction());
			}

		public void dragDropEnd(DragSourceDropEvent dsde)
			{
			System.out.println("Drop Action: "+dsde.getDropAction());
			if (dsde.getDropSuccess() && dsde.getDropAction()==DnDConstants.ACTION_MOVE)
				{
				
				//////// TODO
				
				
//				((DataBrowserTree) sourceTree.getModel()).removeNodeFromParent(oldNode);
				}
			}
		}

	// TreeDropTarget.java
	// A quick DropTarget that's looking for drops from draggable JTrees.
	//

	class TreeDropTarget implements DropTargetListener
		{

		DropTarget target;

		JTree targetTree;

		public TreeDropTarget(JTree tree)
			{
			targetTree = tree;
			target = new DropTarget(targetTree, this);
			}


		private Node getNodeForEvent(DropTargetDragEvent dtde)
			{
			Point p = dtde.getLocation();
			DropTargetContext dtc = dtde.getDropTargetContext();
			JTree tree = (JTree) dtc.getComponent();
			TreePath path = tree.getClosestPathForLocation(p.x, p.y);
			return (Node) path.getLastPathComponent();
			}

		public void dragEnter(DropTargetDragEvent dtde)
			{
			Node node = getNodeForEvent(dtde);
			if(node==root)
			//if (node.isLeaf())
				{
				dtde.rejectDrag();
				}
			else
				{
				// start by supporting move operations
				// dtde.acceptDrag(DnDConstants.ACTION_MOVE);
				dtde.acceptDrag(dtde.getDropAction());
				}
			}

		public void dragOver(DropTargetDragEvent dtde)
			{
			Node node = getNodeForEvent(dtde);
			if(node==root)
			//if (node.isLeaf())
				{
				dtde.rejectDrag();
				}
			else
				{
				// start by supporting move operations
				// dtde.acceptDrag(DnDConstants.ACTION_MOVE);
				dtde.acceptDrag(dtde.getDropAction());
				}
			}

		public void dragExit(DropTargetEvent dte)
			{
			}

		public void dropActionChanged(DropTargetDragEvent dtde)
			{
			}

		public void drop(DropTargetDropEvent dtde)
			{
			Point pt = dtde.getLocation();
			DropTargetContext dtc = dtde.getDropTargetContext();
			JTree tree = (JTree) dtc.getComponent();
			TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
			Node parent = (Node) parentpath.getLastPathComponent();
			if(parent==root)
			//if (parent.isLeaf())
				{
				dtde.rejectDrop();
				return;
				}

			try
				{
				Transferable tr = dtde.getTransferable();
				DataFlavor[] flavors = tr.getTransferDataFlavors();
				for (int i = 0; i<flavors.length; i++)
					{
					if (tr.isDataFlavorSupported(flavors[i]))
						{
						dtde.acceptDrop(dtde.getDropAction());
						TreePath p = (TreePath) tr.getTransferData(flavors[i]);
						Node node = (Node) p.getLastPathComponent();
						DataBrowserTree model = (DataBrowserTree) tree.getModel();
						
						/////TODO 
						
//						model.insertNodeInto(node, parent, 0);
						
						System.out.println("insert "+node+" "+parent);
						
						dtde.dropComplete(true);
						return;
						}
					}
				dtde.rejectDrop();
				}
			catch (Exception e)
				{
				e.printStackTrace();
				dtde.rejectDrop();
				}
			}
		}

	public static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class, "Tree Path");

	// TransferableTreeNode.java
	// A Transferable TreePath to be used with Drag & Drop applications.
	//
	private class TransferableTreeNode implements Transferable
		{
		DataFlavor flavors[] = { TREE_PATH_FLAVOR };

		TreePath path;

		public TransferableTreeNode(TreePath tp)
			{
			path = tp;
			}

		public synchronized DataFlavor[] getTransferDataFlavors()
			{
			return flavors;
			}

		public boolean isDataFlavorSupported(DataFlavor flavor)
			{
			return (flavor.getRepresentationClass()==TreePath.class);
			}

		public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
			{
			if (isDataFlavorSupported(flavor))
				return (Object) path;
			else
				throw new UnsupportedFlavorException(flavor);
			}
		}
	
	*/

	
	
	
	
	
	
	
	class TreeTransferHandler extends TransferHandler
		{
		private static final long serialVersionUID = 1L;
		private DataFlavor nodesFlavor;
		private DataFlavor[] flavors = new DataFlavor[1];
		private Node[] nodesToRemove;

		public TreeTransferHandler()
			{
			try
				{
				String mimeType = DataFlavor.javaJVMLocalObjectMimeType+";class=\""+Node[].class.getName()+"\"";
				nodesFlavor = new DataFlavor(mimeType);
				flavors[0] = nodesFlavor;
				}
			catch (ClassNotFoundException e)
				{
				System.out.println("ClassNotFound: "+e.getMessage());
				}
			}

		public boolean canImport(TransferHandler.TransferSupport support)
			{
			if (!support.isDrop())
				{
				return false;
				}
			support.setShowDropLocation(true);
			System.out.println("----1");
			if (!support.isDataFlavorSupported(nodesFlavor))
				{
				return false;
				}
			
			System.out.println("----2");
			
			//TODO 
			
			/*
			// Do not allow a drop on the drag source selections.
			JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			JTree tree = (JTree) support.getComponent();
			int dropRow = tree.getRowForPath(dl.getPath());
			int[] selRows = tree.getSelectionRows();
			for (int i = 0; i<selRows.length; i++)
				{
				if (selRows[i]==dropRow)
					{
					System.out.println("### "+dropRow+"   "+i);
					return false;
					}
				}
				*/
			
			System.out.println("----3");

			//TODO can several items be moved at the same time? check!
			
			/*
			// Do not allow MOVE-action drops if a non-leaf node is
			// selected unless all of its children are also selected.
			int action = support.getDropAction();
			if (action==MOVE)
				{
				return haveCompleteNode(tree);
				}*/
			
			//TODO
			/*
			
			// Do not allow a non-leaf node to be copied to a level
			// which is less than its source level.
			TreePath dest = dl.getPath();
			Node target = (Node) dest.getLastPathComponent();
			TreePath path = tree.getPathForRow(selRows[0]);
			Node firstNode = (Node) path.getLastPathComponent();
			if (firstNode.getChildCount()>0 && target.isAbove(firstNode))
				return false;*/
			return true;
			}

		/*
		private boolean haveCompleteNode(JTree tree)
			{
			int[] selRows = tree.getSelectionRows();
			TreePath path = tree.getPathForRow(selRows[0]);
			Node first = (Node) path.getLastPathComponent();
			int childCount = first.getChildCount();
			// first has children and no children are selected.
			if (childCount>0&&selRows.length==1)
				return false;
			// first may have children.
			for (int i = 1; i<selRows.length; i++)
				{
				path = tree.getPathForRow(selRows[i]);
				Node next = (Node) path.getLastPathComponent();
				if (first.isNodeChild(next))
					{
					// Found a child of first.
					if (childCount>selRows.length-1)
						{
						// Not all children of first are selected.
						return false;
						}
					}
				}
			return true;
			}*/

		protected Transferable createTransferable(JComponent c)
			{
			JTree tree = (JTree) c;
			TreePath[] paths = tree.getSelectionPaths();
			if (paths!=null)
				{
				
				
				//TODO filter out top-level components. No need to do real clones!
				
				
				// Make up a node array of copies for transfer and
				// another for/of the nodes that will be removed in
				// exportDone after a successful drop.
				List<Node> copies = new ArrayList<Node>();
				List<Node> toRemove = new ArrayList<Node>();
				Node node = (Node) paths[0].getLastPathComponent();
				Node copy = copy(node);
				copies.add(copy);
				toRemove.add(node);
				for (int i = 1; i<paths.length; i++)
					{
					Node next = (Node) paths[i].getLastPathComponent();
					// Do not allow higher level nodes to be added to list.
					if (next.isAbove(node))
						{
						break;
						}
					else if (node.isAbove(next))
						{ // child node
						//copy.add(copy(next));
						
						
						//TODO
						
						System.out.println("add child to copy "+copy(next));
						
						// node already contains child
						}
					else
						{ // sibling
						copies.add(copy(next));
						toRemove.add(next);
						}
					}
				Node[] nodes = copies.toArray(new Node[copies.size()]);
				nodesToRemove = toRemove.toArray(new Node[toRemove.size()]);
				return new NodesTransferable(nodes);
				}
			return null;
			}

		/** Defensive copy used in createTransferable. */
		
		private Node copy(Node node)
			{
			return new Node(node);
			}

		protected void exportDone(JComponent source, Transferable data, int action)
			{
			if ((action&MOVE)==MOVE)
				{
				JTree tree = (JTree) source;
				DataBrowserTree model = (DataBrowserTree) tree.getModel();
				// Remove nodes saved in nodesToRemove in createTransferable.
				for (int i = 0; i<nodesToRemove.length; i++)
					{
					
					
					System.out.println("Remove nodes...."+nodesToRemove[i]);
					//TODO
//					model.removeNodeFromParent(nodesToRemove[i]);
					}
				}
			}

		public int getSourceActions(JComponent c)
			{
			return COPY_OR_MOVE;
			}

		public boolean importData(TransferHandler.TransferSupport support)
			{
			if (!canImport(support))
				{
				return false;
				}
			// Extract transfer data.
			Node[] nodes = null;
			try
				{
				Transferable t = support.getTransferable();
				nodes = (Node[]) t.getTransferData(nodesFlavor);
				}
			catch (UnsupportedFlavorException ufe)
				{
				System.out.println("UnsupportedFlavor: "+ufe.getMessage());
				}
			catch (java.io.IOException ioe)
				{
				System.out.println("I/O error: "+ioe.getMessage());
				}
			// Get drop location info.
			JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			int childIndex = dl.getChildIndex();
			TreePath dest = dl.getPath();
			Node parent = (Node) dest.getLastPathComponent();
			JTree tree = (JTree) support.getComponent();
			DataBrowserTree model = (DataBrowserTree) tree.getModel();
			// Configure for drop mode.
			int index = childIndex; // DropMode.INSERT
			if (childIndex==-1)
				{ // DropMode.ON
				index = parent.getChildCount();
				}
			// Add data to model.
			for (int i = 0; i<nodes.length; i++)
				{
				
				//TODO
				
				System.out.println("Insert node "+nodes[i]);
				
				//model.insertNodeInto(nodes[i], parent, index++);
				}
			return true;
			}

		public String toString()
			{
			return getClass().getName();
			}

		public class NodesTransferable implements Transferable
			{
			Node[] nodes;

			public NodesTransferable(Node[] nodes)
				{
				this.nodes = nodes;
				}

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException
				{
				if (!isDataFlavorSupported(flavor))
					throw new UnsupportedFlavorException(flavor);
				return nodes;
				}

			public DataFlavor[] getTransferDataFlavors()
				{
				return flavors;
				}

			public boolean isDataFlavorSupported(DataFlavor flavor)
				{
				return nodesFlavor.equals(flavor);
				}
			}
		}
	
	
	}
