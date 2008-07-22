package endrov.flow.ui;

import java.awt.BorderLayout;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import endrov.basicWindow.BasicWindow;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;

public class FlowWindow extends JFrame
	{
	static final long serialVersionUID=0;
	FlowPanel fp=new FlowPanel();
	
	JTree unitTree;

	Vector<FlowUnitDeclaration> unitDeclarations=new Vector<FlowUnitDeclaration>();
	
	//Map<String, Map<String,FlowUnitDeclaration>> unitDeclarations=new TreeMap<String, Map<String,FlowUnitDeclaration>>();
	
	
	private static ImageIcon iconButtonSwap=new ImageIcon(FlowWindow.class.getResource("labelSwap.png"));
	public static JButton getButtonSwap()
		{
		JButton b=new JButton(iconButtonSwap);
		b.setToolTipText("Swap position between 2 units");
		return b;
		}
	
	
	JButton bCopy=BasicWindow.getButtonCopy();
	JButton bPaste=BasicWindow.getButtonPaste();
	//JButton bCut=BasicWindow.getButtonCut();
	JButton bDelete=BasicWindow.getButtonDelete();
	JButton bSwap=getButtonSwap();
	
	
	
	public FlowWindow()
		{
		unitDeclarations.add(new FlowUnitDeclaration(){
			public FlowUnit createInstance(){return null;}
			public String getCategory(){return "Basic";}
			public String getName(){return "If";}
		});
		unitDeclarations.add(new FlowUnitDeclaration(){
		public FlowUnit createInstance(){return null;}
		public String getCategory(){return "Basic";}
		public String getName(){return "Map";}
		});
		unitDeclarations.add(new FlowUnitDeclaration(){
		public FlowUnit createInstance(){return null;}
		public String getCategory(){return "Line";}
		public String getName(){return "getLength";}
		});

		
		//Sort unit declerations by category & name
		Map<String, Map<String,FlowUnitDeclaration>> decls=new TreeMap<String, Map<String,FlowUnitDeclaration>>();
		for(FlowUnitDeclaration u:unitDeclarations)
			{
			Map<String,FlowUnitDeclaration> cat=decls.get(u.getCategory());
			if(cat==null)
				decls.put(u.getCategory(), cat=new TreeMap<String,FlowUnitDeclaration>());
			cat.put(u.getName(), u);
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
		
		//TODO need to redo layout when tree expanded
		
		
		JPanel toolbar=new JPanel();
		toolbar.add(bCopy);
		toolbar.add(bPaste);
		toolbar.add(bDelete);
		toolbar.add(bSwap);
		JPanel pTop=new JPanel(new BorderLayout());
		pTop.add(toolbar,BorderLayout.WEST);
		
		
		
		setLayout(new BorderLayout());
		add(new JScrollPane(unitTree,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.WEST);
		add(pTop,BorderLayout.NORTH);
		add(fp,BorderLayout.CENTER);
		
		
		pack();
		setSize(500,300);
		setVisible(true);
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
