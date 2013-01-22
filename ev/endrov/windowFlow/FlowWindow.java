/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowFlow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.flow.*;
import endrov.flowBasic.objects.FlowUnitObjectReference;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.EvComboObjectOne;
import endrov.gui.component.JImageButton;
import endrov.gui.component.JImageToggleButton;
import endrov.gui.component.datatree.JEvDataTree;
import endrov.gui.component.datatree.JEvDataTreeElement;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;

/**
 * Window for editing Flows
 * @author Johan Henriksson
 *
 */
public class FlowWindow extends EvBasicWindow implements ActionListener, KeyListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	private static ImageIcon iconButtonSwap=new ImageIcon(FlowWindow.class.getResource("labelSwap.png"));
	private static ImageIcon iconButtonPlayOnce=new ImageIcon(FlowWindow.class.getResource("labelPlayForward.png"));
	//private static ImageIcon iconButtonStop=new ImageIcon(FlowWindow.class.getResource("labelPlayStop.png"));
	private static ImageIcon iconAlignRight=new ImageIcon(FlowWindow.class.getResource("labelAlignRight.png"));
	private static ImageIcon iconAlignVert=new ImageIcon(FlowWindow.class.getResource("labelAlignVert.png"));
	private static ImageIcon iconAlignHoriz=new ImageIcon(FlowWindow.class.getResource("labelAlignHoriz.png"));
	private static ImageIcon iconButtonPlayCont=new ImageIcon(FlowWindow.class.getResource("labelRepeat.png"));
	
	private static ImageIcon iconStdFlow=new ImageIcon(FlowWindow.class.getResource("standardFlowIcon.png"));

	
	
	/******************************************************************************************************
	 *                               Custom appearance of tree nodes                                      *
	 *****************************************************************************************************/

	private static class MyRenderer extends DefaultTreeCellRenderer 
		{
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) 
			{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,	hasFocus);
			if(leaf)
				{
				FlowUnitDeclaration decl=(FlowUnitDeclaration)((DefaultMutableTreeNode)value).getUserObject();
				if(decl.icon!=null)
					setIcon(decl.icon);
				else
					setIcon(iconStdFlow);
				setToolTipText(decl.description);
				} 
			else 
				setToolTipText(null);
			return this;
			}
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	private FlowView fp=new FlowView();
	
	private JTree unitTree;
	
	private JEvDataTree dataTree=new JEvDataTree(true);
	
	private JButton bCopy=BasicIcon.getButtonCopy();
	private JButton bPaste=BasicIcon.getButtonPaste();
	//JButton bCut=BasicWindow.getButtonCut();
	private JButton bDelete=BasicIcon.getButtonDelete();

	private JButton bSwap=new JImageButton(iconButtonSwap,"Swap position between 2 units");
	private JButton bPlayOnce=new JImageButton(iconButtonPlayOnce,"Evaluate flow once");
	private JToggleButton bRepeat=new JImageToggleButton(iconButtonPlayCont,"Evaluate flow continuously");
	//private JButton bStop=new JImageButton(iconButtonStop,"Stop execution of flow");
	private JButton bAlignRight=new JImageButton(iconAlignRight,"Align right");
	private JButton bAlignVert=new JImageButton(iconAlignVert,"Align vertical");
	private JButton bAlignHoriz=new JImageButton(iconAlignHoriz,"Align horizontal");
	
	private EvComboObjectOne<Flow> objectCombo=new EvComboObjectOne<Flow>(new Flow(),false,true);
	
	


	
	public FlowWindow()
		{
		this(new Rectangle(500,400));
		}
	public FlowWindow(Rectangle bounds)
		{
		
		//Sort unit declarations by category & name
		Map<String, Map<String,FlowUnitDeclaration>> decls=new TreeMap<String, Map<String,FlowUnitDeclaration>>();
		for(FlowUnitDeclaration u:Flow.unitDeclarations)
			{
			Map<String,FlowUnitDeclaration> cat=decls.get(u.category);
			if(cat==null)
				decls.put(u.category, cat=new TreeMap<String,FlowUnitDeclaration>());
			cat.put(u.name, u);
			}
		
		//Build tree from unit declarations
		DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Units");
		for(final Map.Entry<String, Map<String,FlowUnitDeclaration>> catEntry:decls.entrySet())
			{
			DefaultMutableTreeNode catNode=new DefaultMutableTreeNode(catEntry.getKey());
			treeRoot.add(catNode);
			for(Map.Entry<String,FlowUnitDeclaration> declEntry:catEntry.getValue().entrySet())
				{
				DefaultMutableTreeNode declNode=new DefaultMutableTreeNode();
				declNode.setUserObject(declEntry.getValue());
				catNode.add(declNode);
				}
			}
		
		unitTree=new JTree(treeRoot);
		unitTree.setCellRenderer(new MyRenderer());
		unitTree.setShowsRootHandles(true);
		unitTree.setRootVisible(false);
		ToolTipManager.sharedInstance().registerComponent(unitTree);

		
		final FlowWindow wthis=this;
		
		unitTree.addTreeExpansionListener(new TreeExpansionListener(){
			public void treeCollapsed(TreeExpansionEvent event)	{treeExpanded(event);}
			public void treeExpanded(TreeExpansionEvent event) {wthis.validate();}
		});
		unitTree.addTreeSelectionListener(new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent e)
				{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
				
				EvPath flowPath=objectCombo.getSelectedPath();
				if(flowPath!=null)
					{
					if(node!=null && node.isLeaf() && e!=null)
						{
						FlowUnitDeclaration decl=(FlowUnitDeclaration)node.getUserObject();
						System.out.println(decl);
						FlowUnit unit=decl.createInstance();
						if(unit!=null)
							wthis.fp.setUnitToPlace(unit);
						unitTree.setSelectionPath(null);
						}
					}
				else
					EvBasicWindow.showErrorDialog("Create a new flow object first");
				}
		});
		//TODO: new tree model, disable multiple selection
		
		
		dataTree.addTreeSelectionListener(new TreeSelectionListener(){
		public void valueChanged(TreeSelectionEvent e)
			{
			JEvDataTreeElement node = (JEvDataTreeElement)e.getPath().getLastPathComponent();
			EvPath flowPath=objectCombo.getSelectedPath();
			if(flowPath!=null)
				{
				if(flowPath!=null)
					{
					String path=node.getPath().getStringPathRelativeTo(flowPath.getParent());
					FlowUnitObjectReference unit=new FlowUnitObjectReference(path);
					
					if(unit!=null)
						wthis.fp.setUnitToPlace(unit);
					dataTree.setSelectionPath(null);
					}
				}
			else
				EvBasicWindow.showErrorDialog("Create a new flow object first");
			}
		});
		
		
		JComponent toolbar=EvSwingUtil.layoutCompactHorizontal(bCopy,bPaste,bDelete,bSwap,bAlignRight,bAlignVert,bAlignHoriz,bPlayOnce,bRepeat);
		JPanel pTop=new JPanel(new BorderLayout());
		pTop.add(objectCombo,BorderLayout.CENTER);
		pTop.add(toolbar,BorderLayout.EAST);
		
		bCopy.addActionListener(this);
		bPaste.addActionListener(this);
		bDelete.addActionListener(this);
		bSwap.addActionListener(this);
		bAlignRight.addActionListener(this);
		bAlignVert.addActionListener(this);
		bAlignHoriz.addActionListener(this);
		bPlayOnce.addActionListener(this);
		bRepeat.addActionListener(this);
		objectCombo.addActionListener(this);
		

		JComponent unitTreeScroll=new JScrollPane(unitTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JComponent dataTreeScroll=new JScrollPane(dataTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel leftPanel=new JPanel(new GridLayout(2,1));
		leftPanel.add(unitTreeScroll);
		leftPanel.add(dataTreeScroll);

		setLayout(new BorderLayout());
		add(leftPanel,BorderLayout.WEST);
		add(pTop,BorderLayout.NORTH);
		add(fp,BorderLayout.CENTER);
		
		addKeyListener(this);
		setEnabled(true);
		setFocusable(true);
		fp.setFocusable(true);
		fp.addKeyListener(this);
		
		//Window overall things
		setTitleEvWindow("Flow scripter");
		packEvWindow();
		dataChangedEvent();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bAlignRight)
			fp.alignRight(fp.selectedUnits);
		else if(e.getSource()==bAlignVert)
			fp.alignVert(fp.selectedUnits);
		else if(e.getSource()==bAlignHoriz)
			fp.alignHoriz(fp.selectedUnits);
		else if(e.getSource()==bDelete)
			fp.delete();
		else if(e.getSource()==bSwap)
			fp.swap();
		else if(e.getSource()==objectCombo)
			loadData();
		else if(e.getSource()==bCopy)
			fp.copy();
		else if(e.getSource()==bPaste)
			fp.paste();
		else if(e.getSource()==bPlayOnce)
			fp.evaluateAll();
		}
	
	public void dataChangedEvent()
		{
		objectCombo.updateList();
		loadData();  //TODO like here
		}

	public void loadData()
		{
		//TODO could be called at strange times
		
		dataTree.dataUpdated();
		
		fp.setFlow(objectCombo.getSelectedObject(), objectCombo.getData(), objectCombo.getRoot(),	objectCombo.getSelectedPath());
		fp.repaint();
		}
	
	public void windowEventUserLoadedFile(EvData data){}

	public void windowSavePersonalSettings(Element root)
		{
/*		Element e=new Element(pcWindowName);
		setXMLbounds(e);
		root.addContent(e);*/
		}
	
	@Override
	public void windowLoadPersonalSettings(Element e)
		{
		}

	
	
	public void windowFreeResources(){}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		new FlowWindow();
		}

	

	/**
	 * Keyboard button pressed
	 */
	public void keyPressed(KeyEvent e)
		{
		}

	/**
	 * Keyboard button released
	 */
	public void keyReleased(KeyEvent e)
		{
		//TODO put in menu instead as accelerator
		if(e.getKeyCode()==KeyEvent.VK_DELETE)
			fp.delete();
		/*
		if(e.getKeyCode()==KeyEvent.VK_C && (e.getModifiersEx()&KeyEvent.META_DOWN_MASK)!=0)
			{
			System.out.println("copy ctrl+c");
			fp.copy();
			}*/
		}

	/**
	 * Keyboard button typed
	 */
	public void keyTyped(KeyEvent e)
		{
		}

	@Override
	public String windowHelpTopic()
		{
		return "Scripting with flows";
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(EvBasicWindow w)
				{
				w.addHook(this.getClass(),new Hook());
				}
			class Hook implements EvBasicWindowHook, ActionListener
			{
			public void createMenus(EvBasicWindow w)
				{
				JMenuItem mi=new JMenuItem("Flow scripter",new ImageIcon(FlowWindow.class.getResource("labelFS.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new FlowWindow();
				}

			public void buildMenu(EvBasicWindow w){}
			}
			});
		}
	
	
	}
