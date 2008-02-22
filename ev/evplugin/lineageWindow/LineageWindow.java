package evplugin.lineageWindow;

import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jdom.*;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.basicWindow.*;
import evplugin.nuc.*;


/**
 * Lineage Window - an editable tree of the lineage
 * @author Johan Henriksson
 */
public class LineageWindow extends BasicWindow
		implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener/*, KeyListener*/, ChangeListener
	{
	static final long serialVersionUID=0;
	
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new ExtBasic());
		EV.personalConfigLoaders.put("lineagewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					Rectangle r=BasicWindow.getXMLbounds(e);
					new LineageWindow(r);
					}
				catch(Exception e1)
					{
					e1.printStackTrace();
					}
				}
			public void savePersonalConfig(Element e){}
			});
		}
	
	
	
	private JSlider sliderFrameDist=new JSlider(1,200,100); 
	private JButton buttonGoRoot=new JButton("=> root");
	private JButton buttonGoSelected=new JButton("=> selected");
	private LineageView view;
	private FrameControlLineage frameControl=new FrameControlLineage(new ChangeListener()
		{
		public void stateChanged(ChangeEvent e)
			{
			view.goFrame((int)frameControl.getFrame());
			}
		});
	
	public final ObjectCombo objectCombo=new ObjectCombo(new ObjectCombo.comboFilterMetaObject()
		{
		public ObjectCombo.Alternative[] comboAddObjectAlternative(ObjectCombo ob, EvData meta)	{return new ObjectCombo.Alternative[]{};}
		public boolean comboFilterMetaObjectCallback(EvObject ob)	{return ob instanceof NucLineage;}
		public ObjectCombo.Alternative[] comboAddAlternative(final ObjectCombo combo)	{return new ObjectCombo.Alternative[]{};}
		},false);

	/** Last coordinate of the mouse pointer. Used to detect dragging distance. */
	private int mouseLastX=0, mouseLastY=0;
	
	
	public JMenu menuLineage=new JMenu("Lineage");
	public JMenuItem miRename=new JMenuItem("Rename nucleus...");
	public JMenuItem miMerge=new JMenuItem("Merge nuclei");
	public JMenuItem miPC=new JMenuItem("Associate parent");
	public JMenuItem miUnparent=new JMenuItem("Unassociate from parent");
	public JMenuItem miSwapChildren=new JMenuItem("Swap children names*");
	public JMenuItem miFate=new JMenuItem("Set fate");
	public JMenuItem miEndFrame=new JMenuItem("Set end frame...");
	public JMenuItem miRemoveNucleus=new JMenuItem("Remove nucleus");
	public JMenuItem miExportImage=new JMenuItem("Export Image*");
	public JMenuItem miSelectChildren=new JMenuItem("Select children");
	public JMenuItem miRotate=new JMenuItem("Rotate tree");
	public JCheckBoxMenuItem miShowFrameLines=new JCheckBoxMenuItem("Show frame lines",true);
	public JCheckBoxMenuItem miShowKeyFrames=new JCheckBoxMenuItem("Show key frames",true);

	public JMenuItem miFoldAll=new JMenuItem("Fold all");
	public JMenuItem miUnfoldAll=new JMenuItem("Unfold all");

	/**
	 * Make window with standard geometry
	 */
	public LineageWindow()
		{
		this(new Rectangle(0,25,800,650));
		}
	
	
	
	/**
	 * Make a new window at some location
	 */
	public LineageWindow(Rectangle bounds)
		{
		view=new LineageView();
		
		//Add listeners
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		view.addMouseWheelListener(this);
		buttonGoRoot.addActionListener(this);
		buttonGoSelected.addActionListener(this);
		sliderFrameDist.addChangeListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(view,BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new GridBagLayout());
		add(bottom,BorderLayout.SOUTH);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		bottom.add(objectCombo,c);
		c.gridx++;
		c.weightx=1;
		bottom.add(sliderFrameDist,c);
		c.gridx++;
		c.weightx=0;
		c.fill = 0;
		bottom.add(frameControl,c);
		c.gridx++;
		bottom.add(buttonGoRoot,c);
		c.gridx++;
		bottom.add(buttonGoSelected,c);
		c.gridx++;
		
		addMenubar(menuLineage);
		menuLineage.add(miRename);
		menuLineage.add(miMerge);
		menuLineage.add(miPC);
		menuLineage.add(miUnparent);
		menuLineage.add(miSwapChildren);
		menuLineage.add(miFate);
		menuLineage.add(miEndFrame);
		menuLineage.add(miRemoveNucleus);
		menuLineage.addSeparator();
		menuLineage.add(miExportImage);
		menuLineage.addSeparator();
		menuLineage.add(miShowFrameLines);
		menuLineage.add(miShowKeyFrames);
		menuLineage.add(miRotate);
		menuLineage.addSeparator();
		menuLineage.add(miFoldAll);
		menuLineage.add(miUnfoldAll);
		menuLineage.add(miSelectChildren);
		
		miRename.setAccelerator(KeyStroke.getKeyStroke("R"));  //'R',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		miPC.setAccelerator(KeyStroke.getKeyStroke("P"));  //'P',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		miRemoveNucleus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		
		miRename.addActionListener(this);
		miMerge.addActionListener(this);
		miPC.addActionListener(this);
		miUnparent.addActionListener(this);
		miSwapChildren.addActionListener(this);
		miExportImage.addActionListener(this);
		miFate.addActionListener(this);
		miEndFrame.addActionListener(this);
		miRemoveNucleus.addActionListener(this);
		miShowFrameLines.addActionListener(this);
		miShowKeyFrames.addActionListener(this);
		miFoldAll.addActionListener(this);
		miUnfoldAll.addActionListener(this);
		miSelectChildren.addActionListener(this);
		miRotate.addActionListener(this);
		
		//Window overall things
		setTitle(EV.programName+" Lineage Window");
		pack();
		setBounds(bounds);
		setVisible(true);

		stateChanged(null);
		dataChangedEvent();
		}
	

		
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		Element e=new Element("lineagewindow");
		setXMLbounds(e);
		root.addContent(e);
		}

	

	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==buttonGoRoot)
			view.goRoot();
		else if(e.getSource()==buttonGoSelected)
			view.goSelected();
		else if(e.getSource()==miRotate)
			{
			view.displayHorizontalTree=!view.displayHorizontalTree;
			view.repaint();
			}
		else if(e.getSource()==miRename)
			{
			RenameDialog.run(this);
			}
		else if(e.getSource()==miMerge)
			{
			if(!NucLineage.selectedNuclei.isEmpty())
				{
				Iterator<NucPair> nucit=NucLineage.selectedNuclei.iterator();
				NucPair target=nucit.next();
				while(nucit.hasNext())
					{
					NucPair source=nucit.next();
					if(target.getLeft()==source.getLeft())
						(target.getLeft()).mergeNuclei(source.getRight(), target.getRight());
					}
				BasicWindow.updateWindows();
				}
			else
				JOptionPane.showMessageDialog(this, "Select nuclei & have lineage object");
			}
		else if(e.getSource()==miPC)
			{
			if(!NucLineage.selectedNuclei.isEmpty())
				NucLineage.createParentChildSelected();
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miUnparent)
			{
			for(Pair<NucLineage,String> nuc:NucLineage.selectedNuclei)
				(nuc.getLeft()).removeParentReference(nuc.getRight());
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSwapChildren)
			{
			
			}
		else if(e.getSource()==miExportImage)
			{
			
			}
		else if(e.getSource()==miFate)
			{
			if(NucLineage.selectedNuclei.size()==1)
				new FateDialog(this, NucLineage.selectedNuclei.iterator().next());
			else
				JOptionPane.showMessageDialog(this, "Select 1 nucleus first");
			}
		else if(e.getSource()==miEndFrame)
			{
			if(!NucLineage.selectedNuclei.isEmpty())
				{
				String ends=JOptionPane.showInputDialog("End frame or empty for none");
				if(ends!=null)
					{
					for(NucPair nucPair:NucLineage.selectedNuclei)
						{
						String nucName=nucPair.getRight();
						NucLineage.Nuc n=nucPair.getLeft().nuc.get(nucName);
						if(n!=null)
							{
							if(ends.equals(""))
								n.end=null;
							else
								{
								int end=Integer.parseInt(ends);
								n.end=end;
								//r.getModifyingNucPos(); //Make a key frame for the sake of keeping interpolation?
								nucPair.getLeft().removePosAfterEqual(NucLineage.currentHover.getRight(), end+1);
								}
							}
						}
					BasicWindow.updateWindows();
					}
				}
			}
		else if(e.getSource()==miRemoveNucleus)
			{
			if(!NucLineage.selectedNuclei.isEmpty())
				{
				String nucNames="";
				for(NucPair nucName:NucLineage.selectedNuclei)
					nucNames=nucNames+nucName.getRight()+" ";
				int option = JOptionPane.showConfirmDialog(null, "Really want to delete: "+nucNames, "Remove?", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION)
					for(NucPair nucPair:NucLineage.selectedNuclei)
						nucPair.getLeft().removeNuc(nucPair.getRight());
				BasicWindow.updateWindows();
				}
			}
		else if(e.getSource()==miShowFrameLines)
			{
			view.showFrameLines=miShowFrameLines.isSelected();
			repaint();
			}
		else if(e.getSource()==miShowKeyFrames)
			{
			view.showKeyFrames=miShowKeyFrames.isSelected();
			repaint();
			}
		else if(e.getSource()==miUnfoldAll)
			view.unfoldAll();
		else if(e.getSource()==miFoldAll)
			view.foldAll();
		else if(e.getSource()==miSelectChildren)
			{
			Set<NucPair> parents=new HashSet<NucPair>(NucLineage.selectedNuclei);
			for(NucPair p:parents)
				recursiveSelect(p.getLeft(), p.getRight());
			BasicWindow.updateWindows();
			}
		}
	

	/**
	 * Recursively select children
	 */
	public static void recursiveSelect(NucLineage lin, String nucName)
		{
		NucLineage.Nuc nuc=lin.nuc.get(nucName);
		NucLineage.selectedNuclei.add(new NucPair(lin, nucName));
		for(String childName:nuc.child)
			recursiveSelect(lin, childName);
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
		{
		view.clickRegion(e);
		view.requestFocus();
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
		{
		mouseLastX=e.getX();
		mouseLastY=e.getY();		
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
		{
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e)
		{
		int dx=e.getX()-mouseLastX;
		int dy=e.getY()-mouseLastY;
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		if(SwingUtilities.isRightMouseButton(e))
			{
			view.pan(dx,dy);
			view.repaint();
			}
		
		}
	

	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
		{
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
			sliderFrameDist.setValue(sliderFrameDist.getValue()+e.getUnitsToScroll());
		else if(e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
			sliderFrameDist.setValue(sliderFrameDist.getValue()+e.getUnitsToScroll());
		view.repaint();
		}

	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		view.setFrameDist(sliderFrameDist.getValue()/10);
		repaint();
		}

	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		objectCombo.updateObjectList();
		view.currentFrame=frameControl.getFrame();
		view.lin=(NucLineage)objectCombo.getObject();
		repaint();
		}
	
	
	public void mouseMoved(MouseEvent e) {}
	public void mouseExited(MouseEvent e)	{}
	public void mouseEntered(MouseEvent e) {}
	}
