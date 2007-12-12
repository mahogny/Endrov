package evplugin.roi.window;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.roi.*;
import evplugin.roi.primitive.BoxROI;
import evplugin.roi.primitive.UnionROI;
//import evplugin.imageset.*;
import evplugin.basicWindow.*;
import org.jdom.*;

/**
 * ROI Window - Display all ROIs
 * 
 * @author Johan Henriksson
 */
public class WindowROI extends BasicWindow implements ActionListener, MetaCombo.comboFilterMetadata, TreeSelectionListener
	{
	static final long serialVersionUID=0;
	

	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static class ThisBasicHook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("ROI");
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			new WindowROI();
			}
		public void buildMenu(BasicWindow w){}
		}
	
	
	public static void initPlugin()	{}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new ThisBasicHook());
				}
			});
		}
	
	private static ImageIcon iconDiff=new ImageIcon(WindowROI.class.getResource("iconDiff.png"));
	private static ImageIcon iconIntersect=new ImageIcon(WindowROI.class.getResource("iconIntersect.png"));
	private static ImageIcon iconSub=new ImageIcon(WindowROI.class.getResource("iconSub.png"));
	private static ImageIcon iconUnion=new ImageIcon(WindowROI.class.getResource("iconUnion.png"));
	private static ImageIcon iconDelete=new ImageIcon(WindowROI.class.getResource("iconDelete.png"));
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private JButton bNewDiff=new JButton(iconDiff);
	private JButton bNewIntersect=new JButton(iconIntersect);
	private JButton bNewSub=new JButton(iconSub);
	private JButton bNewUnion=new JButton(iconUnion);
	private JButton bNewDelete=new JButton(iconDelete);
	
	
	private MetaCombo metaCombo=new MetaCombo(this, false);
	public boolean comboFilterMetadataCallback(EvData meta)
		{
		return true;
		}

	private ROITreeModel treeModel=new ROITreeModel();
	private JTree tree=new JTree(treeModel);
	
	
	
	/**
	 * Make a new window at default location
	 */
	public WindowROI()
		{
		this(600,300,500,300);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public WindowROI(int x, int y, int w, int h)
		{
		metaCombo.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
	
		JPanel bottom=new JPanel(/*new GridLayout(1,4)*/);
		add(metaCombo,BorderLayout.NORTH);
		add(tree, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		bottom.add(bNewDiff);
		bottom.add(bNewIntersect);
		bottom.add(bNewSub);
		bottom.add(bNewUnion);
		bottom.add(bNewDelete);
		
		
		bNewUnion.addActionListener(this);
		
		//bottom.add(new JLabel("Start frame:"));
		
		tree.addTreeSelectionListener(this);
		
		//A tree is probably suitable here
		
		
		//Window overall things
		setTitle(EV.programName+" ROI");
		pack();
		setBounds(x,y,w,h);
		setVisible(true);
		}
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		}

	
	

	public void valueChanged(TreeSelectionEvent e)
		{
		ROI.selected.clear(); //note! what about other windows?
		TreePath[] selection=tree.getSelectionPaths();
		if(selection!=null)
			for(TreePath tp:selection)
				{
				Object o=tp.getLastPathComponent();
				ROITreeElement to=(ROITreeElement)o;
				System.out.println(" "+o.toString());
				if(to.e instanceof ROI)
					ROI.selected.add((ROI)to.e);
			//	if(o instanceof ROI)
				//	ROI.selected.add(o);
				}
		System.out.println("Selected1: "+ROI.selected.size());
		BasicWindow.updateWindows(this);
		System.out.println("Selected2: "+ROI.selected.size());
			
	//		ROITreeElement e=
			
		ROI.selected.contains(new BoxROI());
		ROI.selected.contains(new UnionROI());
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==metaCombo)
			{
			treeModel.setMetaObject(metaCombo.getMeta());
			///channelCombo.setImageset(metaCombo.getImageset());
			}
		else if(e.getSource()==bNewUnion)
			makeUnion();
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		metaCombo.updateList();
		treeModel.emitAllChanged(); //overkill?
		}
	
	
	public void makeUnion()
		{
		EvData data=metaCombo.getMeta();
		if(data!=null)
			{
			System.out.println("Selected: "+ROI.selected.size());
			for(ROI roi:ROI.selected)
				System.out.println("- "+roi.getROIDesc());
			Set<ROI> rois=collectRecursiveROI(data);
			System.out.println("Coll: "+rois.size());

			UnionROI union=new UnionROI();
			data.addMetaObject(union);

			
			for(ROI roi:rois)
				union.subRoi.add(roi);
			BasicWindow.updateWindows();
			
			}
		}
	
	
	
	public Set<ROI> collectRecursiveROI(Object parent)
		{
		HashSet<ROI> hs=new HashSet<ROI>();
		if(parent instanceof EvData)
			{
			EvData data=(EvData)parent;
			for(EvObject ob:data.metaObject.values())
				if(ob instanceof ROI)
					{
					if(ROI.selected.contains((ROI)ob))
						{
						ROI.selected.remove((ROI)ob);
						hs.add((ROI)ob);
						}
					else
						hs.addAll(collectRecursiveROI(ob));
					}
			}
		else if(parent instanceof CompoundROI)
			{
			for(ROI roi:((CompoundROI)parent).subRoi)
				{
				if(ROI.selected.contains(roi))
					{
					ROI.selected.remove(roi);
					hs.add((ROI)roi);
					}
				else
					hs.addAll(collectRecursiveROI(roi));
				}
			}
		return hs;
		}
	
	}
