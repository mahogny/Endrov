package endrov.dataBrowser;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import endrov.data.EvPath;

public class DataBrowserTree extends JTree
	{
	private static final long serialVersionUID = 1L;

	
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
 	
	
	public static class DataTreeModel implements TreeModel
		{
		private List<TreeModelListener> listeners=new LinkedList<TreeModelListener>();
		
		public void addTreeModelListener(TreeModelListener l)
			{
			listeners.add(l);

			
			
			}

		public Object getChild(Object parent, int index)
			{
			if(parent==null)
				{
				//This is the root
				
				
				}
			
			// TODO Auto-generated method stub
			return null;
			}

		public int getChildCount(Object parent)
			{
			// TODO Auto-generated method stub
			return 0;
			}

		public int getIndexOfChild(Object parent, Object child)
			{
			// TODO Auto-generated method stub
			return 0;
			}

		public Object getRoot()
			{
			// TODO Auto-generated method stub
			return null;
			}

		public boolean isLeaf(Object node)
			{
			// TODO Auto-generated method stub
			return false;
			}

		public void removeTreeModelListener(TreeModelListener l)
			{
			listeners.remove(l);
			}

		public void valueForPathChanged(TreePath path, Object newValue)
			{
			}
		}
	
	
	public DataBrowserTree()
		{
		setModel(new DataTreeModel());
		setCellRenderer(new TreeCellRenderer()
			{
				
				public Component getTreeCellRendererComponent(JTree arg0, Object arg1,
						boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6)
					{
					// TODO Auto-generated method stub
					return null;
					}
			});
		
		
		// TODO Auto-generated constructor stub
		
//		EvPath p;
//		p.
		
		
		}
	
	
	}
