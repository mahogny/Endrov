package evplugin.imagesetImserv.service;


//-Dsun.io.serialization.extendedDebugInfo=true

//javac *.java
//rmic HelloImpl
//java -Djava.security.policy=policy HelloImpl
//java HelloClient (run in another window)

//-Djavax.net.ssl.keyStore=testkeys -Djavax.net.ssl.keyStorePassword=passphrase


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import evplugin.ev.BrowserControl;
import evplugin.ev.EV;
import evplugin.ev.EvSwingTools;


/**
 * Interface to imserv: a viewer with capability to select and load . Meant to be integrated in 
 * 
 * @author Johan Henriksson
 *
 */
public class ImservClientPane extends JPanel implements ActionListener, ListSelectionListener
	{
	public static final long serialVersionUID=0;
	
	public ImservConnection conn;
	
	public JList tagList=new JList(new String[]{});
	public DataIconPane pane;
	public JTextField searchField=new JTextField();
	public JButton bHelp=new JButton("Help");
	public JButton bNewTag=new JButton("New Tag");
	public JButton bToTrash=new JButton("=> Trash");
	public JLabel status=new JLabel("");
	private Timer timer=new Timer(1000,this);
	private Date lastUpdate=new Date();
	
	private TagList taglist=new TagList();

	private JTable table=new JTable(new TableModel(){
		public void addTableModelListener(TableModelListener l){}
		public Class<?> getColumnClass(int columnIndex){return String.class;}
		public int getColumnCount(){return 3;}
		public String getColumnName(int columnIndex){return "";}
		public int getRowCount(){return 10;}
		public Object getValueAt(int rowIndex, int columnIndex)
			{
			return ""+rowIndex+"asasdasdasdasd";
			}
		public boolean isCellEditable(int rowIndex, int columnIndex){return false;}
		public void removeTableModelListener(TableModelListener l){}
		public void setValueAt(Object aValue, int rowIndex, int columnIndex){}
	});

	private HashMap<Integer, JButton> tablePlusMap=new HashMap<Integer, JButton>();
	private JButton getTablePlusButton(int row)
		{
		JButton b=tablePlusMap.get(row);
		if(b==null)
			tablePlusMap.put(row, b=new JButton("+"));
		return b;
		}
	private HashMap<Integer, JButton> tableMinusMap=new HashMap<Integer, JButton>();
	private JButton getTableMinusButton(int row)
		{
		JButton b=tableMinusMap.get(row);
		if(b==null)
			tableMinusMap.put(row, b=new JButton("-"));
		return b;
		}
	
	/**
	 * Constructor
	 */
	public ImservClientPane(ImservConnection conn)
		{
		this.conn=conn;
		pane=new DataIconPane(conn);

//		table.setShowGrid(true); 
		
		table.setShowHorizontalLines(true);
		table.setTableHeader(null);
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		//+++
		TableColumn plusColumn=table.getColumnModel().getColumn(0);
//		plusColumn.setPreferredWidth(30);
//		plusColumn.setMinWidth(30);
//		plusColumn.setMaxWidth(30);
		plusColumn.setCellEditor(new TableCellEditor(){
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col)
				{return getTablePlusButton(row);}
			public void addCellEditorListener(CellEditorListener arg0){}
			public void cancelCellEditing(){}
			public Object getCellEditorValue(){return null;}
			public boolean isCellEditable(EventObject arg0){return true;}
			public void removeCellEditorListener(CellEditorListener arg0){}
			public boolean shouldSelectCell(EventObject arg0){return false;}
			public boolean stopCellEditing(){return true;}
		});
		plusColumn.setCellRenderer(new TableCellRenderer(){
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
				{return getTablePlusButton(row);}
		});
		
		//---
		TableColumn minusColumn=table.getColumnModel().getColumn(1);
//		plusColumn.setPreferredWidth(30);
//		minusColumn.setMinWidth(30);
//		minusColumn.setMaxWidth(30);
		minusColumn.setCellEditor(new TableCellEditor(){
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col)
			{return getTableMinusButton(row);}
		public void addCellEditorListener(CellEditorListener arg0){}
		public void cancelCellEditing(){}
		public Object getCellEditorValue(){return null;}
		public boolean isCellEditable(EventObject arg0){return true;}
		public void removeCellEditorListener(CellEditorListener arg0){}
		public boolean shouldSelectCell(EventObject arg0){return false;}
		public boolean stopCellEditing(){return true;}
		});
		minusColumn.setCellRenderer(new TableCellRenderer(){
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
			{return getTableMinusButton(row);}
		});
		
		table.getColumnModel().getColumn(2).setPreferredWidth(10);
		table.doLayout();
		System.out.println(""+table.getPreferredSize()+" "+table.getMinimumSize()+" "+table.getMaximumSize());
		
		bHelp.addActionListener(this);
		bToTrash.addActionListener(this);
		bNewTag.addActionListener(this);
		searchField.addActionListener(this);
		tagList.addListSelectionListener(this);
		
		JPanel listBottom=new JPanel(new GridLayout(3,1));
		listBottom.add(bNewTag);
		listBottom.add(bHelp);
		listBottom.add(bToTrash);
		
		JPanel right=new JPanel(new BorderLayout());
		right.add(EvSwingTools.borderLR(status,searchField,null),BorderLayout.SOUTH);
		right.add(pane,BorderLayout.CENTER);

		//JPanel foo=new JPanel(new GridLayout(1,1));
	//	foo.add(table);
		
		JPanel left=new JPanel(new BorderLayout());
//		left.add(new JScrollPane(tagList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
		left.add(new JScrollPane(taglist,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
//		left.add(new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
//		left.add(new JScrollPane(foo,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
//		left.add(table,BorderLayout.CENTER);
		left.add(listBottom,BorderLayout.SOUTH);
		
		setLayout(new BorderLayout());
		add(left,BorderLayout.WEST);
		add(right,BorderLayout.CENTER);

		
		setConnection(conn);
		timer.start();
		}
	
	
	public void setConnection(ImservConnection conn)
		{
		this.conn=conn;
		pane.setConn(conn);
		updateTagList();
		updateCount();
		}
	
	
	/**
	 * Update the list of tags by querying the server
	 */
	public void updateTagList()
		{
		try
			{
			final ArrayList<Object> list=new ArrayList<Object>();
			list.add(ListDescItem.makeMatchAll());
			if(conn!=null)
				{
				for(String s:conn.imserv.getTags())
					list.add(ListDescItem.makeTag(s));
				for(String s:conn.imserv.getObjects())
					list.add(ListDescItem.makeObj(s));
				for(String s:conn.imserv.getChannels())
					list.add(ListDescItem.makeChan(s));
				}
			
			ListModel lm=new ListModel(){
				public void addListDataListener(ListDataListener l){}
				public void removeListDataListener(ListDataListener l){}
				public Object getElementAt(int index){return list.get(index);}
				public int getSize(){return list.size();}
			};
			tagList.setModel(lm);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	private void setTag(String tag, boolean enable)
		{
		if(!pane.selectedId.isEmpty())
			{
			try
				{
				conn.imserv.setTag(pane.selectedId.toArray(new String[]{}), tag, enable);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			pane.update();
			updateCount();
			}
		else
			JOptionPane.showMessageDialog(this, "No datasets selected");
		}
	
	
	/**
	 * Handle events
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bHelp)
			BrowserControl.displayURL(EV.website+"Organizing_with_ImServ");
		else if(e.getSource()==searchField)
			pane.setFilter(searchField.getText());
		else if(e.getSource()==bNewTag)
			{
			String tag=JOptionPane.showInputDialog(this,"Enter new tag");
			if(tag!=null)
				setTag(tag,true);
			}
		else if(e.getSource()==bToTrash)
			{
			if(JOptionPane.showConfirmDialog(this, "Confirm", "Do you really want to move the selected datasets to trash?", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				setTag("trash",true);
			}
		else if(e.getSource()==timer)
			{
			try
				{
				Date lastUpdate=conn.imserv.getLastUpdate();
				if(!lastUpdate.equals(this.lastUpdate))
					{
					this.lastUpdate=lastUpdate;
					updateTagList();
					pane.update();
					updateCount();
					}
				}
			catch (Exception e1){}
			}
		}
	
	
	/**
	 * Handle selections in tag list
	 */
	public void valueChanged(ListSelectionEvent e)
		{
		StringBuffer crit=new StringBuffer();
		Object[] sels=tagList.getSelectedValues();
		for(Object sel:sels)
			{
			ListDescItem item=(ListDescItem)sel;
			if(crit.length()!=0)
				crit.append(" and ");
			crit.append(item.toString());
			}
		if(sels.length==0)
			crit.append("*");
		searchField.setText(crit.toString());
		pane.setFilter(crit.toString());
		updateCount();
		try{lastUpdate=conn.imserv.getLastUpdate();}
		catch (Exception e1){}
		}
	
	
	
	
	private void updateCount()
		{
		status.setText(""+pane.obList.size());
		}
	
	
	
	
	
	
	
	
	
	}
