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
import endrov.flow.*;
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
	private static ImageIcon iconButtonPlayCont=new ImageIcon(FlowWindow.class.getResource("labelRepeat.png"));
	
	
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
				if(node!=null && node.isLeaf())//node.getUserObject() instanceof FlowUnitDeclaration)
					{
					FlowUnitDeclaration decl=(FlowUnitDeclaration)node.getUserObject();
					System.out.println(decl);
					FlowUnit unit=decl.createInstance();
					if(unit!=null)
						wthis.fp.placingUnit=unit;
					unitTree.setSelectionPath(null);
					}
				}
		});
		//TODO: new tree model, disable multiple selection
		
		
		
		
		
		JPanel toolbar=new JPanel(new GridLayout(1,8));
		toolbar.add(bCopy);
		toolbar.add(bPaste);
		toolbar.add(bDelete);
		toolbar.add(bSwap);
		toolbar.add(bAlignRight);
		toolbar.add(bAlignVert);
		toolbar.add(bPlayOnce);
		toolbar.add(bRepeat);
		JPanel pTop=new JPanel(new BorderLayout());
		pTop.add(objectCombo,BorderLayout.CENTER);
		pTop.add(toolbar,BorderLayout.WEST);
		
		bCopy.addActionListener(this);
		bPaste.addActionListener(this);
		bDelete.addActionListener(this);
		bSwap.addActionListener(this);
		bAlignRight.addActionListener(this);
		bAlignVert.addActionListener(this);
		bPlayOnce.addActionListener(this);
		bRepeat.addActionListener(this);
		objectCombo.addActionListener(this);
		
		
		JComponent unitTreeScroll=new JScrollPane(unitTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setLayout(new BorderLayout());
		add(unitTreeScroll,BorderLayout.WEST);
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
		else if(e.getSource()==bDelete)
			{
			fp.getFlow().removeUnits(fp.selectedUnits);
			fp.repaint();
			}
		else if(e.getSource()==objectCombo)
			loadData();
		}
	
	public void dataChangedEvent()
		{
		objectCombo.updateList();
		loadData();
		}

	public void loadData()
		{
		fp.setFlow(objectCombo.getSelectedObjectNotNull(), objectCombo.getData(), objectCombo.getRoot());
		fp.repaint();
		}
	
	public void loadedFile(EvData data){}

	public void windowPersonalSettings(Element e)
		{
		// TODO Auto-generated method stub
		
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
