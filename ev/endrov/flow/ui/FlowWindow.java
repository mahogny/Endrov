package endrov.flow.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;
import endrov.basicWindow.EvComboObjectOne;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;
import endrov.data.tree.DataTree;
import endrov.data.tree.DataTreeElement;
import endrov.ev.EV;
import endrov.ev.PersonalConfig;
import endrov.flow.*;
import endrov.flowBasic.objects.FlowUnitObjectIO;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;
import endrov.util.JImageToggleButton;

/**
 * Window for editing Flows
 * @author Johan Henriksson
 *
 */
public class FlowWindow extends BasicWindow implements ActionListener
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
	

	private static String pcWindowName="flowwindow";

	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new Hook());
				}
			class Hook implements BasicWindowHook, ActionListener
			{
			public void createMenus(BasicWindow w)
				{
				JMenuItem mi=new JMenuItem("Flow",new ImageIcon(getClass().getResource("labelFS.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new FlowWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});

		EV.personalConfigLoaders.put(pcWindowName,new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					Rectangle r=BasicWindow.getXMLbounds(e);
					/*FlowWindow w=*/new FlowWindow(r);
					}
				catch(Exception e1)
					{
					e1.printStackTrace();
					}
				}
			public void savePersonalConfig(Element e){}
			});
		}
	
	
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
	
	
	private FlowPanel fp=new FlowPanel();
	
	private JTree unitTree;
	
	private DataTree dataTree=new DataTree();
	
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
		});
		//TODO: new tree model, disable multiple selection
		
		
		dataTree.addTreeSelectionListener(new TreeSelectionListener(){
		public void valueChanged(TreeSelectionEvent e)
			{
			DataTreeElement node = (DataTreeElement)e.getPath().getLastPathComponent();
			if(node!=null && !node.isRoot && e!=null)
				{
				FlowUnitObjectIO unit=new FlowUnitObjectIO(node.getPath());
				
				//TODO relative path or absolute path
				
				System.out.println("path "+node.getPath());
				if(unit!=null)
					wthis.fp.setUnitToPlace(unit);
				dataTree.setSelectionPath(null);
				}
			}
		});
		
		
		JComponent toolbar=EvSwingUtil.layoutCompactHorizontal(bCopy,bPaste,bDelete,bSwap,bAlignRight,bAlignVert,bAlignHoriz,bPlayOnce,bRepeat);
		JPanel pTop=new JPanel(new BorderLayout());
		pTop.add(objectCombo,BorderLayout.CENTER);
		pTop.add(toolbar,BorderLayout.WEST);
		
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
		
		//Window overall things
		setTitleEvWindow("Flow");
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
			{
			fp.getFlow().removeUnits(fp.selectedUnits);
			fp.repaint();
			}
		else if(e.getSource()==objectCombo)
			loadData();
		else if(e.getSource()==bCopy)
			fp.copy();
		else if(e.getSource()==bPaste)
			fp.paste();
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
		
		fp.setFlow(objectCombo.getSelectedObject(), objectCombo.getData(), objectCombo.getRoot(), 
				objectCombo.getSelectedRelativePath().getParent());
		fp.repaint();
		}
	
	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element root)
		{
		Element e=new Element(pcWindowName);
		setXMLbounds(e);
		root.addContent(e);
		}
	
	
	public void freeResources(){}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		new FlowWindow();
		}

	
	
	
	}
