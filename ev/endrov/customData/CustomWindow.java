/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.customData;


import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;


import endrov.basicWindow.*;
import endrov.data.*;
import endrov.ev.*;
import endrov.util.EvXmlUtil;

import org.jdom.*;


//TODO: auto-replicate down to metadata

/**
 * Adjust Frame-Time mapping
 * @author Johan Henriksson
 */
public class CustomWindow extends BasicWindow 
implements ActionListener, ChangeListener, TreeSelectionListener, TableModelListener
	{
	static final long serialVersionUID=0;

	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new CustomBasic());
		
		EV.personalConfigLoaders.put("CustomWindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try	{new CustomWindow(BasicWindow.getXMLbounds(e));}
				catch (Exception e1) {e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		
		}
	
	//GUI components
	private EvComboObjectOne<CustomObject> objectCombo=new EvComboObjectOne<CustomObject>(new CustomObject(),false,true);
	private CustomTreeModel treeModel=new CustomTreeModel();
	private JTree tree=new JTree(treeModel);
	private JPanel treeFields=new JPanel();
	
	private CustomTableModel tableModel=new CustomTableModel();
	private JTable table=new JTable(tableModel);
	
	private JButton btRemoveEntry=new JButton("Remove row");
	private JButton btInsertEntry=new JButton("Insert row");
	private JButton btRemoveColumn=new JButton("Remove column");
	private JButton btInsertColumn=new JButton("Insert column");
	
	private JButton bImport=new JButton("Import");
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		Element e=new Element("CustomWindow");
		setXMLbounds(e);
		root.addContent(e);
		}

	

	/**
	 * Make a new window at default location
	 */
	public CustomWindow()
		{
		this(new Rectangle(100,100,1000,600));
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public CustomWindow(Rectangle bounds)
		{		
		objectCombo.addActionListener(this);
		tree.addTreeSelectionListener(this);

		btInsertColumn.addActionListener(this);
		btRemoveColumn.addActionListener(this);
		btInsertEntry.addActionListener(this);
		btRemoveEntry.addActionListener(this);
		bImport.addActionListener(this);
		
		JScrollPane treeScroll=new JScrollPane(tree);
		JPanel treePanel=new JPanel(new BorderLayout());
		treePanel.add(treeScroll,BorderLayout.CENTER);
		treePanel.add(treeFields,BorderLayout.SOUTH);


		JPanel upper=new JPanel(new GridLayout(1,2));
		upper.add(objectCombo);
		upper.add(bImport);
		
		JPanel tablePanel=new JPanel(new BorderLayout());
		JScrollPane tableScroll=new JScrollPane(table);
		JPanel tableBottom=new JPanel(new GridLayout(1,4));
		tableBottom.add(btInsertEntry);
		tableBottom.add(btRemoveEntry);
		tableBottom.add(btInsertColumn);
		tableBottom.add(btRemoveColumn);
		tablePanel.add(tableScroll,BorderLayout.CENTER);
		tablePanel.add(tableBottom,BorderLayout.SOUTH);
		
		setLayout(new BorderLayout());
		JTabbedPane tabs=new JTabbedPane();
		tabs.addTab("Tree", treePanel);
		tabs.addTab("Table", tablePanel);
		
		
		add(upper, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		
		//Update GUI
		treeModel.setMetaObject(objectCombo.getSelectedObject());
		fillTreeAttributesPane();
//		fillTreeAttributesPane((CustomTreeElement)treeModel.getRoot());
		
		//Window overall things
		setTitleEvWindow("Custom Data");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}


	
	/**
	 * Callback: selection in tree changed
	 */
	public void valueChanged(TreeSelectionEvent e2)
		{
		TreePath p=tree.getSelectionPath();
		if(p==null)
			{
			//fillTreeAttributesPane(null);
			tableModel.setRoot(objectCombo.getSelectedObject());
			}
		else
			{
			//CustomTreeElement e=(CustomTreeElement)p.getLastPathComponent();
			//fillTreeAttributesPane(e);
			tableModel.setRoot(objectCombo.getSelectedObject());
			}
		fillTreeAttributesPane();
		}

	
	
	public void fillTreeAttributesPane()
		{
		TreePath path=tree.getSelectionPath();
		if(path!=null)
			fillTreeAttributesPane((CustomTreeElement)path.getLastPathComponent());
		else
			fillTreeAttributesPane(null);
		
		}
	
	/**
	 * Update GUI: attribute pane for tree
	 */
	public void fillTreeAttributesPane(final CustomTreeElement e)
		{
		treeFields.removeAll();
		if(e!=null && e.e!=null)
			{
			java.util.List<?> attr=e.e.getAttributes();
			treeFields.setLayout(new GridLayout(attr.size()+2,1));
			
			//The value
			//what about trimming?
			JPanel p2=new JPanel(new BorderLayout());
			final JTextField cf=new JTextField(e.e.getText());
			p2.add(new JLabel("Value:"));
			p2.add(cf,BorderLayout.CENTER);
			treeFields.add(cf);
			
			cf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				e.e.setText(cf.getText());
				treeModel.updateElement(e);
				tableModel.fireTableStructureChanged();
				}
			});
			
			//Every attribute
			for(Object o:attr)
				{
				final Attribute a=(Attribute)o;
				JPanel p=new JPanel(new BorderLayout());
				p.add(new JLabel(a.getName()+":"),BorderLayout.WEST);
				final JTextField tf=new JTextField(a.getValue());
				p.add(tf,BorderLayout.CENTER);
				JButton bremove=new JButton("X");
				p.add(bremove, BorderLayout.EAST);
				treeFields.add(p);
				
				bremove.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e2)
						{
						e.e.removeAttribute(a.getName());
						treeModel.updateElement(e);
						tableModel.fireTableStructureChanged();
						}
					});
				
				tf.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0)
						{
						e.e.setAttribute(a.getName(), tf.getText());
						treeModel.updateElement(e);
						tableModel.fireTableStructureChanged();
						}
				});
				}

			
			//Buttons below
			JPanel p3=new JPanel(new GridLayout(1,3));
			JButton bNewField=new JButton("Add field");
			JButton bNewChild=new JButton("Add child");
			JButton bRemove=new JButton("Remove element");
			p3.add(bNewChild);
			p3.add(bNewField);
			p3.add(bRemove);
			treeFields.add(p3);
			
			bNewChild.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e2)
					{
					String name=JOptionPane.showInputDialog("Name of child");
					if(name!=null)
						{
						//TreePath p=e.getPath();
						treeModel.addChild(e, new Element(name));
						tableModel.fireTableStructureChanged();
						//tree.setSelectionPath(p);    //should not be needed
						objectCombo.getSelectedObject().setMetadataModified();
						}
					}
				});
				
			bNewField.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e2)
					{
					String name=JOptionPane.showInputDialog("Name of field");
					if(name!=null)
						{
						e.e.setAttribute(name, "");
						treeModel.updateElement(e);
						tableModel.fireTableStructureChanged();
						fillTreeAttributesPane();
						objectCombo.getSelectedObject().setMetadataModified();
						}
					}
				});
			
			bRemove.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e2)
					{
					CustomTreeElement parent=e.parent;
					if(parent!=null)
						{
						parent.e.removeContent(e.e);
						treeModel.emitAllChanged();
						tableModel.fireTableStructureChanged();
						tree.setSelectionPath(parent.getPath());
						objectCombo.getSelectedObject().setMetadataModified();
						}
					}
				});
			
			}
		setVisibleEvWindow(true);
		}
	
	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==objectCombo)
			{
			CustomObject cust=objectCombo.getSelectedObject();
			treeModel.setMetaObject(cust);
			//tableModel.setRoot(cust, treeModel.getRoot())
			tableModel.setRoot(cust);
			fillTreeAttributesPane();
			}
		else if(e.getSource()==btInsertEntry)
			{
			tableModel.insertRow();
			treeModel.emitAllChanged();
			objectCombo.getSelectedObject().setMetadataModified();
			}
		else if(e.getSource()==btRemoveEntry)
			{
			int row=table.getSelectedRow();
			if(row!=-1)
				{
				tableModel.removeRow(row);
				treeModel.emitAllChanged();
				objectCombo.getSelectedObject().setMetadataModified();
				}
			}
		else if(e.getSource()==btInsertColumn)
			{
			String name=JOptionPane.showInputDialog("Name of column");
			if(name!=null)
				{
				tableModel.insertCol(name);
				treeModel.emitAllChanged();
				objectCombo.getSelectedObject().setMetadataModified();
				}
			}
		else if(e.getSource()==btRemoveColumn)
			{
			int col=table.getSelectedColumn();
			if(col!=-1)
				{
				tableModel.removeColumn(col);
				treeModel.emitAllChanged();
				objectCombo.getSelectedObject().setMetadataModified();
				}
			}
		else if(e.getSource()==bImport)
			{
			JFileChooser fc=getFileChooser();
			fc.setCurrentDirectory(EvData.getLastDataPath());
			int ret=fc.showOpenDialog(null);
			if(ret==JFileChooser.APPROVE_OPTION)
				{
				EvData.setLastDataPath(fc.getSelectedFile().getParentFile());
				File filename=fc.getSelectedFile();
				
				try
					{
					if(filename.getName().endsWith(".xml"))
						{
						Document doc=EvXmlUtil.readXML(filename);
						CustomObject ob=objectCombo.getSelectedObject();
						if(ob!=null)
							ob.xml=doc.getRootElement();
						}
					else
						{
						String elementName=JOptionPane.showInputDialog("Name of new elements");
						if(elementName==null || elementName.equals(""))
							return;

						ImportTable imp=new ImportTable();
						if(filename.getName().endsWith(".csv"))
							imp.importCSV(filename.getAbsolutePath(), ',', '\"');
						else
							imp.importExcel(filename.getAbsolutePath());

						//imp.show();
						//A preview might be nice?
						CustomObject ob=objectCombo.getSelectedObject();
						if(ob!=null)
							imp.intoXML(ob.xml, elementName);
						}
					objectCombo.getSelectedObject().setMetadataModified();
					dataChangedEvent();
					treeModel.emitAllChanged();
					tableModel.fireTableStructureChanged();
					}
				catch (Exception e1)
					{
					e1.printStackTrace();
					}
				}			
			
			}
		}
	
	/**
	 * Get a file chooser for import
	 */
	private static JFileChooser getFileChooser()
		{
		JFileChooser fc=new JFileChooser();
		fc.setFileFilter(new FileFilter()
			{
			public boolean accept(File f)
				{
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml") || f.getName().toLowerCase().endsWith(".xls") || f.getName().toLowerCase().endsWith(".csv");
				}
			public String getDescription()
				{
				return "XML and table files (.xml/.xls/.csv)";
				}
			});
		return fc;
		}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		}
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		objectCombo.updateList();
		CustomObject ob=objectCombo.getSelectedObject();
		treeModel.setMetaObject(ob);
		tableModel.setRoot(ob);
		fillTreeAttributesPane();
		}



	public void tableChanged(TableModelEvent e)
		{
		if(e.getType()==TableModelEvent.INSERT);
		else if(e.getType()==TableModelEvent.DELETE);
		else if(e.getType()==TableModelEvent.UPDATE);
		treeModel.emitAllChanged();
		}
	
	
	public void loadedFile(EvData data){}
	public void freeResources(){}

	
	}
