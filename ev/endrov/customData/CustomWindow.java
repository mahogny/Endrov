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

import org.jdom.*;


//TODO: auto-replicate down to metadata

/**
 * Adjust Frame-Time mapping
 * @author Johan Henriksson
 */
public class CustomWindow extends BasicWindow 
implements ActionListener, ChangeListener, ObjectCombo.comboFilterMetaObject, TreeSelectionListener, TableModelListener
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
	private ObjectCombo objectCombo=new ObjectCombo(this, true);
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
	public void windowPersonalSettings(Element root)
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
		fillTreeAttributesPane((CustomTreeElement)treeModel.getRoot());
		
		//Window overall things
		setTitleEvWindow("Custom Data");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}

	/**
	 * For combo box - which meta objects to list
	 */
	public boolean comboFilterMetaObjectCallback(EvObject ob)
		{
		return ob instanceof CustomObject;
		}
	/**
	 * Add special options for the combo box, every object
	 */
	public ObjectCombo.Alternative[] comboAddObjectAlternative(final ObjectCombo combo, final EvData meta)
		{
		ObjectCombo.Alternative a=new ObjectCombo.Alternative(meta, null, "<New object>", new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				String type=JOptionPane.showInputDialog("Type of new object");
				if(type!=null)
					{
					meta.addMetaObject(new CustomObject(new Element(type)));
					BasicWindow.updateWindows();
					}
				}
			});
		return new ObjectCombo.Alternative[]{a};
		}
	/**
	 * Add special options for the combo box
	 */
	public ObjectCombo.Alternative[] comboAddAlternative(final ObjectCombo combo)
		{
		return new ObjectCombo.Alternative[]{};
		}

	
	/**
	 * Callback: selection in tree changed
	 */
	public void valueChanged(TreeSelectionEvent e2)
		{
		TreePath p=tree.getSelectionPath();
		if(p==null)
			{
			fillTreeAttributesPane(null);
			tableModel.setRoot(objectCombo.getObject(), null);
			}
		else
			{
			CustomTreeElement e=(CustomTreeElement)p.getLastPathComponent();
			fillTreeAttributesPane(e);
			tableModel.setRoot(objectCombo.getObject(), e.e);
			}
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
			JTextField cf=new JTextField(e.e.getText());
			p2.add(new JLabel("Value:"));
			p2.add(cf,BorderLayout.CENTER);
			treeFields.add(cf);
			
			//Every attribute
			for(Object o:attr)
				{
				final Attribute a=(Attribute)o;
				JPanel p=new JPanel(new BorderLayout());
				p.add(new JLabel(a.getName()+":"),BorderLayout.WEST);
				JTextField tf=new JTextField(a.getValue());
				p.add(tf,BorderLayout.CENTER);
				JButton bremove=new JButton("X");
				p.add(bremove, BorderLayout.EAST);
				treeFields.add(p);
				
				bremove.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e2)
						{
						TreePath p=e.getPath();
						e.e.removeAttribute(a.getName());
						treeModel.updateElement(e);
						tree.setSelectionPath(p);    //should not be needed
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
						TreePath p=e.getPath();
						treeModel.addChild(e, new Element(name));
						tree.setSelectionPath(p);    //should not be needed
						objectCombo.getObject().metaObjectModified=true;
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
						TreePath p=e.getPath();
						treeModel.updateElement(e);
						tree.setSelectionPath(p);    //should not be needed
						objectCombo.getObject().metaObjectModified=true;
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
						tree.setSelectionPath(parent.getPath());
						objectCombo.getObject().metaObjectModified=true;
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
			treeModel.setMetaObject((CustomObject)objectCombo.getObject());
			
			}
		else if(e.getSource()==btInsertEntry)
			{
			tableModel.insertRow();
			}
		else if(e.getSource()==btRemoveEntry)
			{
			int row=table.getSelectedRow();
			if(row!=-1)
				tableModel.removeRow(row);
			}
		else if(e.getSource()==btInsertColumn)
			{
			String name=JOptionPane.showInputDialog("Name of column");
			if(name!=null)
				tableModel.insertCol(name);
			}
		else if(e.getSource()==btRemoveColumn)
			{
			int col=table.getSelectedColumn();
			if(col!=-1)
				tableModel.removeColumn(col);
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
				
				String elementName=JOptionPane.showInputDialog("Name of new elements");
				if(elementName==null || elementName.equals(""))
					return;
				
				try
					{
					if(filename.getName().endsWith(".xml"))
						{
						//TODO
						}
					else
						{
						ImportTable imp=new ImportTable();
						if(filename.getName().endsWith(".csv"))
							imp.importCSV(filename.getAbsolutePath(), ',', '\"');
						else
							imp.importExcel(filename.getAbsolutePath());

						//imp.show();
						//A preview might be nice?
						
						imp.intoXML(tableModel.getRoot(), elementName);
						}
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
				return f.getName().toLowerCase().endsWith(".xml") || f.getName().toLowerCase().endsWith(".xls") || f.getName().toLowerCase().endsWith(".csv");
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
		
//		fillGraphpart();
		}
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		objectCombo.updateObjectList();
		//copy list
		fillTreeAttributesPane((CustomTreeElement)treeModel.getRoot());
		}



	public void tableChanged(TableModelEvent e)
		{
		if(e.getType()==TableModelEvent.INSERT)
			{
			
			}
		else if(e.getType()==TableModelEvent.DELETE)
			{
			
			}
		else if(e.getType()==TableModelEvent.UPDATE)
			{
			
			}
		//TODO: update tree
		}
	
	
	public void loadedFile(EvData data){}
	public void freeResources(){}

	
	}
