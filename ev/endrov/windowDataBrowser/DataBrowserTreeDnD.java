package endrov.windowDataBrowser;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.gui.window.BasicWindow;
import endrov.windowDataBrowser.DataBrowserTree.Node;


//if (support.getDropAction()==MOVE)


/**
 * Drag and drop support
 */
public class DataBrowserTreeDnD
	{
	
	public static class TreeTransferHandler extends TransferHandler
		{
		private static final long serialVersionUID = 1L;
		private DataFlavor nodesFlavor;
		private DataFlavor[] flavors = new DataFlavor[1];
		
		public TreeTransferHandler()
			{
			try
				{
				String mimeType = DataFlavor.javaJVMLocalObjectMimeType+";class=\""+Node[].class.getName()+"\"";
				nodesFlavor = new DataFlavor(mimeType);
				flavors[0] = nodesFlavor;
				
				System.out.println("---- here!!!!");
				
				}
			catch (ClassNotFoundException e)
				{
				System.out.println("ClassNotFound: "+e.getMessage());
				}
			}

		
		public boolean canDrop(Node[] nodes, Node dropNode)
			{
			//Do not allow moving a node to a sub-node
			if(selectionCoversNodes(nodes).contains(dropNode))
				{
				System.out.println("Trying to drop within itself");
				return false;
				}
			return true;
			}
		
		/**
		 * Check if it is ok to import. 
		 * TODO importData, can some logic be moved here?
		 */
		public boolean canImport(TransferHandler.TransferSupport support)
			{
			if (!support.isDrop())
				return false;
			support.setShowDropLocation(true);
			if (!support.isDataFlavorSupported(nodesFlavor))
				return false;

			JTree tree=(JTree)support.getComponent();
			
			//Ensure that the nodes to move are not located in each other
			if(!selectionIndependent(tree))
				{
				System.out.println("Components to move not independent");
				return false;
				}
			
			//Cannot move top-nodes
			for(Node n:getNodes(tree))
				if(n.parent==null)
					{
					System.out.println("Trying to move evdata or root");
					return false;
					}
		
			return true;
			}

		
		/**
		 * Check that the selected nodes are not overlapping 
		 */
		public boolean selectionIndependent(JTree tree)
			{
			Set<Node> nodes=new HashSet<Node>(); 
			for(Node next:getNodes(tree))
				if(!selectionIndependent(nodes, next))
					return false;
			return true;
			}
		public boolean selectionIndependent(Set<Node> nodes, Node next)
			{
			if(nodes.contains(next))
				return false;
			nodes.add(next);
			for(Node n:next.children)
				if(!selectionIndependent(nodes, n))
					return false;
			return true;
			}
		
		private List<Node> getNodes(JTree c)
			{
			TreePath[] paths = c.getSelectionPaths();
			
			if (paths!=null)
				{
				List<Node> copies = new ArrayList<Node>();
				for(TreePath p:paths)
					{
					Node next = (Node) p.getLastPathComponent();
					copies.add(next);
					}
				return copies;
				}
			return null;
			}
		

		protected Transferable createTransferable(JComponent c)
			{
			JTree tree = (JTree) c;
			List<Node> copies=getNodes(tree);
			if (copies!=null)
				return new NodesTransferable(copies.toArray(new Node[copies.size()]));
			else
				return null;
			}

		
		protected void exportDone(JComponent source, Transferable data, int action)
			{
			//TODO move or copy?
			}

		public int getSourceActions(JComponent c)
			{
			return COPY_OR_MOVE;
			}

		public boolean importData(TransferHandler.TransferSupport support)
			{
			//Get nodes to extract
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
			
			//Get drop location info
			JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
			TreePath dest = dl.getPath();
			Node parent = (Node) dest.getLastPathComponent();

			//Check if this is ok
			if (!canDrop(nodes, parent))
				{
				System.out.println("Cannot drop");
				return false;
				}

			//Remove nodes from old parents
			for(Node n:nodes)
				n.parent.children.remove(n.name);
			
			//Add data to model
			for(Node n:nodes)
				{
				EvContainer parentCon=parent.con;
				parentCon.metaObject.put(n.name, (EvObject)n.con);
				}
			
			BasicWindow.updateWindows();
			return true;
			}

		public String toString()
			{
			return getClass().getName();
			}

		

		

		private void selectionCoversNodes(Set<Node> nodeset, Node next)
			{
			nodeset.add(next);
			for(Node n:next.children)
				selectionCoversNodes(nodeset, n);
			}
		private Set<Node> selectionCoversNodes(Node[] nodes)
			{
			Set<Node> nodeset=new HashSet<Node>(); 
			for(Node n:nodes)
				selectionCoversNodes(nodeset, n);
			return nodeset;
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
