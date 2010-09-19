package endrov.dataBrowser;

import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;

import endrov.data.EvContainer;
import endrov.data.EvData;

/**
 * 
 * @author Johan Henriksson
 *
 */
public class DataBrowserTreeX extends JXTreeTable implements TreeTableModel
	{
	private static final long serialVersionUID = 1L;

	
	
	public DataBrowserTreeX()
		{
		setTreeTableModel(this);
		
		
		// TODO Auto-generated constructor stub
		}



	public Object getValueAt(Object arg0, int arg1)
		{
		// TODO Auto-generated method stub
		return null;
		}



	public boolean isCellEditable(Object arg0, int arg1)
		{
		// TODO Auto-generated method stub
		return false;
		}



	public void setValueAt(Object arg0, Object arg1, int arg2)
		{
		// TODO Auto-generated method stub
		
		}


	private List<TreeModelListener> listeners=new LinkedList<TreeModelListener>();
	
	
	public void addTreeModelListener(TreeModelListener arg0)
		{
		listeners.add(arg0);
		}



	public Object getChild(Object arg0, int arg1)
		{
		// TODO Auto-generated method stub
		return null;
		}



	public int getChildCount(Object arg0)
		{
		// TODO Auto-generated method stub
		return 0;
		}



	public int getIndexOfChild(Object arg0, Object arg1)
		{
		if(arg0==null)
			{
			//It is the root
			return EvData.openedData.indexOf((EvContainer)arg1);
			}
		else
			{
			EvContainer c=(EvContainer)arg0;
//			c.metaObject
	//TODO		
			
			}
		// TODO Auto-generated method stub
		return 0;
		}



	public Object getRoot()
		{
		return null;
		}



	public boolean isLeaf(Object arg0)
		{
		return false;
		}



	public void removeTreeModelListener(TreeModelListener arg0)
		{
		listeners.remove(arg0);
		}



	public void valueForPathChanged(TreePath arg0, Object arg1)
		{
		//TODO this might be useful!
		}



	}
