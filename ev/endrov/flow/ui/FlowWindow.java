package endrov.flow.ui;

import java.awt.BorderLayout;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import endrov.basicWindow.icon.BasicIcon;
import endrov.flow.*;

public class FlowWindow extends JFrame
	{
	static final long serialVersionUID=0;
	FlowPanel fp=new FlowPanel();
	
	JTree unitTree;

	private static ImageIcon iconButtonSwap=new ImageIcon(FlowWindow.class.getResource("labelSwap.png"));
	private static ImageIcon iconButtonPlay=new ImageIcon(FlowWindow.class.getResource("labelPlayForward.png"));
	private static ImageIcon iconButtonStop=new ImageIcon(FlowWindow.class.getResource("labelPlayStop.png"));
	
	JButton bCopy=BasicIcon.getButtonCopy();
	JButton bPaste=BasicIcon.getButtonPaste();
	//JButton bCut=BasicWindow.getButtonCut();
	JButton bDelete=BasicIcon.getButtonDelete();

	JButton bSwap=new JButton(iconButtonSwap);
	JButton bPlay=new JButton(iconButtonPlay);
	JButton bStop=new JButton(iconButtonStop);
	
	
	public FlowWindow()
		{
		bSwap.setToolTipText("Swap position between 2 units");
		bPlay.setToolTipText("Run entire flow");
		bStop.setToolTipText("Stop execution of flow");
		
		
		
		//Sort unit declerations by category & name
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
						{
						unit.x=0;
						unit.y=0;
						wthis.fp.flow.units.add(unit);
						wthis.fp.repaint();
						}
					}
	//			JTree comp=(JTree)e.getSource();
//				System.out.println(""+comp.getModel().g
				}
		});
		//TODO: new tree model, disable multiple selection
		
		
		
		
		JPanel toolbar=new JPanel();
		toolbar.add(bCopy);
		toolbar.add(bPaste);
		toolbar.add(bDelete);
		toolbar.add(bSwap);
		toolbar.add(bPlay);
		toolbar.add(bStop);
		JPanel pTop=new JPanel(new BorderLayout());
		pTop.add(toolbar,BorderLayout.WEST);
		
		
		JComponent unitTreeScroll=new JScrollPane(unitTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setLayout(new BorderLayout());
		add(unitTreeScroll,BorderLayout.WEST);
		add(pTop,BorderLayout.NORTH);
		add(fp,BorderLayout.CENTER);
		
		
		pack();
		setSize(500,300);
		setVisible(true);
		}
	
	
	public EvChannel process(EvChannel ch)
		{
		//...filter code here...
		
		return ch;
		}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		FlowWindow t=new FlowWindow();
		t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		}

	}
