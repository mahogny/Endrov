package endrov.customData;

import java.util.*;
import org.jdom.*;

import endrov.data.EvObject;

import javax.swing.table.*;


/**
 * Table interface to an XML-node
 * @author Johan Henriksson
 */
public class CustomTableModel extends AbstractTableModel
	{
	static final long serialVersionUID=0; 
	
	public Vector<String> columnList=new Vector<String>();
	private String elementName="";
	private Element tableRoot=new Element("a");
	private EvObject ob;
	
	public void setRoot(EvObject ob, Element e)
		{
		this.ob=ob;
		if(e==null)
			tableRoot=new Element("empty");
		else
			tableRoot=e;
		collectColumns();
		fireTableStructureChanged();
		}
	
	public Element getRoot()
		{
		return tableRoot;
		}
	
	public void collectColumns()
		{
		columnList.clear();
		List<?> ch=tableRoot.getChildren();
		if(ch.size()>0)
			{
			System.out.println("root "+tableRoot.getName());
			Element e=(Element)ch.get(0);
			elementName=e.getName();
			for(Object o:e.getChildren())
				{
				Element ae=(Element)o;
				columnList.add(ae.getName());
				System.out.println("ae "+ae.getName());
				}
			}
		}
	

	public int getColumnCount()
		{
		return columnList.size();
		}

	public String getColumnName(int arg0)
		{
		return columnList.get(arg0);
		}

	public int getRowCount()
		{
		return tableRoot.getChildren().size();
		}

	public Object getValueAt(int row, int col)
		{
		Element rowe=(Element)tableRoot.getChildren().get(row);
		return getValue(rowe, columnList.get(col)).getText();
		}
	
	private Element getValue(Element e, String column)
		{
		for(Object o:e.getChildren())
			{
			Element ce=(Element)o;
			if(ce.getName().equals(column))
				return ce;
			}
		System.out.println("Warning: element not found");
		return new Element("foo");
		}

	public boolean isCellEditable(int row, int column)
		{
		return true;
		}


	public void setValueAt(Object val, int row, int col)
		{
		Element rowe=(Element)tableRoot.getChildren().get(row);
		Element date=getValue(rowe, columnList.get(col));
		date.removeContent();
		date.addContent((String)val);
		ob.setMetadataModified();
		fireTableCellUpdated(row, col);
		}

	
	public void insertCol(String name)
		{
		for(Object o:tableRoot.getChildren())
			{
			Element e=(Element)o;
			e.addContent(new Element(name));
			}
		collectColumns();
		fireTableStructureChanged();
		}

	
	//Insert empty row. If row is -1, then append
	public void insertRow()
		{
		Element e=new Element(elementName);
		for(String s:columnList)
			e.addContent(new Element(s));
		int row=tableRoot.getChildren().size()-1;
		
//		tableRoot.addContent(e);

		if(row==-1)
			row=tableRoot.getChildren().size();
		Vector<Object> list=new Vector<Object>();
		for(Object o:tableRoot.removeContent())
			list.add(o);
		list.add(row, e);
		for(Object o:list)
			tableRoot.addContent((Content)o);
//		tableRoot.addContent(list);
		
		//fireTableStructureChanged();
//		fireTableRowsUpdated(row, row+1);
		fireTableRowsInserted(row, row+1);
		}
	
	public void removeRow(int row)
		{
		Vector<Object> list=new Vector<Object>();
		for(Object o:tableRoot.getChildren())
			list.add(o);
		list.remove(row);
		tableRoot.removeContent();
		tableRoot.addContent(list);
		ob.setMetadataModified();
		fireTableRowsDeleted(row, row);
		}
	
	public void removeColumn(int col)
		{
		String colName=columnList.get(col);
		for(Object o:tableRoot.getChildren())
			{
			Element oe=(Element)o;
			Vector<Object> list=new Vector<Object>();
			for(Object o2:oe.getChildren())
				if(!((Element)o2).getName().equals(colName))
					list.add(o2);
			oe.removeContent();
			oe.addContent(list);
			}
		collectColumns();
		ob.setMetadataModified();
		fireTableStructureChanged();
		}
	
	
	}
