package endrov.lineageWindow;

import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.*;
import endrov.ev.*;
import endrov.lineageWindow.LineageView.ClickRegion;
import endrov.lineageWindow.LineageView.ClickRegionName;
import endrov.nuc.*;
import endrov.util.EvDecimal;
import endrov.util.Tuple;


/**
 * Lineage Window - an editable tree of the lineage
 * @author Johan Henriksson
 */
public class LineageWindow extends BasicWindow
		implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, ChangeListener
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
					LineageWindow w=new LineageWindow(r);
					w.frameControl.setGroup(e.getAttribute("group").getIntValue());
					}
				catch(Exception e1)
					{
					e1.printStackTrace();
					}
				}
			public void savePersonalConfig(Element e){}
			});
		}
	
	
	
	private JSlider sliderFrameDist=new JSlider(-20000,31000,0); 
	private JSlider sliderExpScale=new JSlider(JSlider.VERTICAL, -4000,4000,0); 
	private JButton buttonGoRoot=new JButton("=> root");
	private JButton buttonGoSelected=new JButton("=> selected");
	private LineageView view=new LineageView();
	private FrameControlLineage frameControl=new FrameControlLineage(new ChangeListener()
		{
		public void stateChanged(ChangeEvent e)
			{
			view.setFrame(frameControl.getFrame().doubleValue());
			}
		});
	
	public final EvComboObjectOne<NucLineage> objectCombo=new EvComboObjectOne<NucLineage>(new NucLineage(),false,true);

	/** Last coordinate of the mouse pointer. Used to detect dragging distance. */
	private int mouseLastX=0, mouseLastY=0;
	
	
	public JMenu menuLineage=new JMenu("Lineage");
	public JMenuItem miRename=new JMenuItem("Rename nucleus");
	public JMenuItem miNewNuc=new JMenuItem("Create empty nucleus");
	public JMenuItem miMerge=new JMenuItem("Merge nuclei");
	public JMenuItem miPC=new JMenuItem("Associate parent");
	public JMenuItem miUnparent=new JMenuItem("Unassociate from parent");
	public JMenuItem miSwapChildren=new JMenuItem("Swap children names*");
	public JMenuItem miFate=new JMenuItem("Set fate");
	public JMenuItem miSetDesc=new JMenuItem("Set description");
	public JMenuItem miStartFrame=new JMenuItem("Set override start frame");
	public JMenuItem miEndFrame=new JMenuItem("Set override end frame");
	public JMenuItem miDeleteNucleus=new JMenuItem("Delete nucleus");
	public JMenuItem miExportImage=new JMenuItem("Export Image");
	public JMenuItem miSelectChildren=new JMenuItem("Select children");
	public JMenuItem miSelectParents=new JMenuItem("Select parents");
	public JMenuItem miSelectAll=new JMenuItem("Select all in this lineage");
	public JMenuItem miSelectAllName=new JMenuItem("Select all w/ the same name");
	
	public JMenuItem miRotate=new JMenuItem("Rotate tree");
	public JCheckBoxMenuItem miShowFrameLines=new JCheckBoxMenuItem("Show frame lines",true);
	public JCheckBoxMenuItem miShowKeyFrames=new JCheckBoxMenuItem("Show key frames",true);
	public JCheckBoxMenuItem miShowTreeLabel=new JCheckBoxMenuItem("Show tree label",true);
	public JCheckBoxMenuItem miShowLeafLabel=new JCheckBoxMenuItem("Show leaf label",true);
	
	public JCheckBoxMenuItem miShowExpDot=new JCheckBoxMenuItem("Show level dots",false);
	public JMenu miShowExp=new JMenu("Level display");
	public JRadioButtonMenuItem miShowExpNone=new JRadioButtonMenuItem("Off",false);
	public JRadioButtonMenuItem miShowExpLine=new JRadioButtonMenuItem("Line",true);
	public JRadioButtonMenuItem miShowExpSolid=new JRadioButtonMenuItem("Filled",false);
	public JCheckBoxMenuItem miShowScale=new JCheckBoxMenuItem("Show scale",view.showScale);
	
	
	
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
		//Add listeners
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		view.addMouseWheelListener(this);
		buttonGoRoot.addActionListener(this);
		buttonGoSelected.addActionListener(this);
		sliderFrameDist.addChangeListener(this);
		sliderExpScale.addChangeListener(this);
		objectCombo.addActionListener(this);
		miShowExpDot.addActionListener(this);
		miShowExpLine.addActionListener(this);
		miShowExpSolid.addActionListener(this);
		miShowExpNone.addActionListener(this);
		miShowTreeLabel.addActionListener(this);
		miShowLeafLabel.addActionListener(this);
		miRename.addActionListener(this);
		miNewNuc.addActionListener(this);
		miMerge.addActionListener(this);
		miPC.addActionListener(this);
		miUnparent.addActionListener(this);
		miSwapChildren.addActionListener(this);
		miExportImage.addActionListener(this);
		miFate.addActionListener(this);
		miSetDesc.addActionListener(this);
		miStartFrame.addActionListener(this);
		miEndFrame.addActionListener(this);
		miDeleteNucleus.addActionListener(this);
		miShowFrameLines.addActionListener(this);
		miShowKeyFrames.addActionListener(this);
		miFoldAll.addActionListener(this);
		miUnfoldAll.addActionListener(this);
		miSelectChildren.addActionListener(this);
		miSelectParents.addActionListener(this);
		miSelectAll.addActionListener(this);
		miSelectAllName.addActionListener(this);
		miRotate.addActionListener(this);
		miShowScale.addActionListener(this);
		
		ButtonGroup expGroup=new ButtonGroup();
		expGroup.add(miShowExpLine);
		expGroup.add(miShowExpSolid);
		expGroup.add(miShowExpNone);
		updateShowExp();
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(view,BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new GridBagLayout());
		add(bottom,BorderLayout.SOUTH);

		add(sliderExpScale,BorderLayout.EAST);
		
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
		menuLineage.add(miNewNuc);
		menuLineage.add(miMerge);
		menuLineage.add(miPC);
		menuLineage.add(miUnparent);
		menuLineage.add(miSwapChildren);
		menuLineage.add(miFate);
		menuLineage.add(miSetDesc);
		menuLineage.add(miStartFrame);
		menuLineage.add(miEndFrame);
		menuLineage.add(miDeleteNucleus);
		menuLineage.add(NucLineage.makeSetColorMenu());
		menuLineage.addSeparator();
		menuLineage.add(miExportImage);
		menuLineage.addSeparator();
		menuLineage.add(miShowFrameLines);
		menuLineage.add(miShowKeyFrames);
		menuLineage.add(miShowExpDot);
		menuLineage.add(miShowExp);
		menuLineage.add(miShowScale);
		menuLineage.add(miShowTreeLabel);
		menuLineage.add(miShowLeafLabel);
		menuLineage.add(miRotate);
		
		miShowExp.add(miShowExpNone);
		miShowExp.add(miShowExpLine);
		miShowExp.add(miShowExpSolid);
		
		menuLineage.addSeparator();
		menuLineage.add(miFoldAll);
		menuLineage.add(miUnfoldAll);
		menuLineage.addSeparator();
		menuLineage.add(miSelectChildren);
		menuLineage.add(miSelectParents);
		menuLineage.add(miSelectAll);
		menuLineage.add(miSelectAllName);
		
		miRename.setAccelerator(KeyStroke.getKeyStroke("R"));  //'R',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		miPC.setAccelerator(KeyStroke.getKeyStroke("P"));  //'P',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
		miDeleteNucleus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		
		
		//Window overall things
		setTitleEvWindow("Lineage Window");
		packEvWindow();
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);

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
		e.setAttribute("group",""+frameControl.getGroup());
		root.addContent(e);
		}

	
	private NucLineage getLineage()
		{
		return objectCombo.getSelectedObject();
//		return lin==null?new NucL
//		return objectCombo.getSelectedObjectNotNull();
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
			view.camera.showHorizontalTree=!view.camera.showHorizontalTree;
			view.repaint();
			}
		else if(e.getSource()==miRename)
			NucRenameDialog.run(NucLineage.selectedNuclei, null);
		else if(e.getSource()==miNewNuc)
			{
			String nucName=JOptionPane.showInputDialog("Name of new nucleus");
			if(nucName!=null)
				{
				if(getLineage().nuc.containsKey(nucName))
					showErrorDialog("Name already taken");
				else
					{
					getLineage().getNucCreate(nucName);
					repaint();
					}
				}
			}
		else if(e.getSource()==miMerge)
			{
			if(!NucLineage.selectedNuclei.isEmpty())
				{
				Iterator<NucPair> nucit=NucLineage.selectedNuclei.iterator();
				NucPair target=nucit.next();
				NucLineage theLineage=target.fst();
//				Vector<String> nucToMerge=new Vector<String>();
//				nucToMerge.add(target.getRight());
//				String suggestName=target.getRight();
				while(nucit.hasNext())
					{
					NucPair source=nucit.next();
					if(theLineage==source.fst())
						{
//						nucToMerge.add(source.getRight());
						if(!target.snd().startsWith(":"))
							{
							NucPair temp=target;
							target=source;
							source=temp;
							}
						target.fst().mergeNuclei(source.snd(), target.snd());
						}
					}
//				suggestName=JOptionPane.showInputDialog("Give name of merged nucleus:", suggestName);
//				if(suggestName)
				BasicWindow.updateWindows();
				}
			else
				JOptionPane.showMessageDialog(this, "Must select nuclei first");
			}
		else if(e.getSource()==miPC)
			{
			if(!NucLineage.selectedNuclei.isEmpty())
				NucLineage.createParentChildSelected();
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miUnparent)
			{
			for(Tuple<NucLineage,String> nuc:NucLineage.selectedNuclei)
				(nuc.fst()).removeParentReference(nuc.snd());
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSwapChildren)
			{
			
			}
		else if(e.getSource()==miExportImage)
			{
			view.saveToDisk();
			}
		else if(e.getSource()==miFate)
			{
			if(NucLineage.selectedNuclei.size()==1)
//				new FateDialog(evw, NucLineage.selectedNuclei.iterator().next()); need Frame
				new FateDialog(null, NucLineage.selectedNuclei.iterator().next());
			else
				JOptionPane.showMessageDialog(this, "Select 1 nucleus first");
			}
		else if(e.getSource()==miSetDesc)
			actionSetDesc(NucLineage.selectedNuclei);
		else if(e.getSource()==miStartFrame)
			actionSetStartFrame(NucLineage.selectedNuclei);
		else if(e.getSource()==miEndFrame)
			actionSetEndFrame(NucLineage.selectedNuclei);
		else if(e.getSource()==miDeleteNucleus)
			actionRemove(NucLineage.selectedNuclei);
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
		else if(e.getSource()==miShowTreeLabel)
			{
			view.showTreeLabel=miShowTreeLabel.isSelected();
			repaint();
			}
		else if(e.getSource()==miShowLeafLabel)
			{
			view.showLeafLabel=miShowLeafLabel.isSelected();
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
				recursiveSelectChildren(p.fst(), p.snd());
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSelectParents)
			{
			Set<NucPair> children=new HashSet<NucPair>(NucLineage.selectedNuclei);
			for(NucPair p:children)
				recursiveSelectParent(p.fst(), p.snd());
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSelectAll)
			{
			if(view.currentLin!=null)
				selectAll(view.currentLin);
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSelectAllName)
			{
			HashSet<String> names=new HashSet<String>();
			for(NucPair p:NucLineage.selectedNuclei)
				names.add(p.snd());
			for(EvData data:EvData.openedData)
				for(NucLineage lin:data.getObjects(NucLineage.class))
					for(String n:names)
						if(lin.nuc.containsKey(n))
							NucLineage.selectedNuclei.add(new NucPair(lin,n));
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==objectCombo)
			{
			System.out.println("objectcombo");
			view.currentLin=getLineage();
			repaint();
			}
		else if(e.getSource()==miShowExpDot)
			{
			updateShowExp();
			repaint();
			}
		else if(e.getSource()==miShowScale)
			{
			view.showScale=miShowScale.isSelected();
			repaint();
			}
		else if(e.getSource()==miShowExpLine || e.getSource()==miShowExpSolid || e.getSource()==miShowExpNone)
			{
			updateShowExp();
			repaint();
			}
			
			
		}
	
	
	/**
	 * Set override end frame of nuclei
	 */
	public static void actionSetEndFrame(Collection<NucPair> nucs)
		{
		if(!nucs.isEmpty())
			{
			String sFrame=JOptionPane.showInputDialog("End frame or empty for none");
			if(sFrame!=null)
				{
				for(NucPair nucPair:nucs)
					{
					String nucName=nucPair.snd();
					NucLineage.Nuc n=nucPair.fst().nuc.get(nucName);
					if(n!=null)
						{
						if(sFrame.equals(""))
							n.overrideEnd=null;
						else
							{
							EvDecimal frame=new EvDecimal(sFrame);
							n.overrideEnd=frame;
							nucPair.fst().removePosAfter(NucLineage.currentHover.snd(), frame, false);
							}
						}
					}
				BasicWindow.updateWindows();
				}
			}
		}

	/**
	 * Set override start frame
	 */
	public static void actionSetStartFrame(Collection<NucPair> nucs)
		{
		if(!nucs.isEmpty())
			{
			String sFrame=JOptionPane.showInputDialog("Start frame or empty for none");
			if(sFrame!=null)
				{
				for(NucPair nucPair:nucs)
					{
					String nucName=nucPair.snd();
					NucLineage.Nuc n=nucPair.fst().nuc.get(nucName);
					if(n!=null)
						{
						if(sFrame.equals(""))
							n.overrideStart=null;
						else
							{
							EvDecimal frame=FrameControl.parseTime(sFrame);
							n.overrideStart=frame;
							nucPair.fst().removePosAfter(NucLineage.currentHover.snd(), frame, false);
							}
						}
					}
				BasicWindow.updateWindows();
				}
			}
		}
	
	public static void actionRemove(Collection<NucPair> nucs)
		{
		if(!nucs.isEmpty())
			{
			String nucNames="";
			for(NucPair nucName:nucs)
				nucNames=nucNames+nucName.snd()+" ";
			int option = JOptionPane.showConfirmDialog(null, "Really want to delete: "+nucNames, "Remove?", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION)
				for(NucPair nucPair:nucs)
					nucPair.fst().removeNuc(nucPair.snd());
			BasicWindow.updateWindows();
			}

		}
	
	public static void actionSetDesc(Collection<NucPair> nucs)
		{
		for(NucPair nucPair:nucs)
			{
			String nucName=nucPair.snd();
			NucLineage.Nuc n=nucPair.fst().nuc.get(nucName);
			if(n!=null)
				{
				String newDesc=JOptionPane.showInputDialog("Description for "+nucName);
				if(newDesc!=null)
					{
					if(newDesc.equals(""))
						newDesc=null;
					n.description=newDesc;
					}
				}
			}
		BasicWindow.updateWindows();
		}
	
	public void updateShowExp()
		{
		view.showExpDot=miShowExpDot.isSelected();
		view.showExpLine=miShowExpLine.isSelected();
		view.showExpSolid=miShowExpSolid.isSelected();
		}
	

	/**
	 * Recursively select children
	 */
	public static void recursiveSelectChildren(NucLineage lin, String nucName)
		{
		NucLineage.Nuc nuc=lin.nuc.get(nucName);
		NucLineage.selectedNuclei.add(new NucPair(lin, nucName));
		for(String childName:nuc.child)
			recursiveSelectChildren(lin, childName);
		}
	
	/**
	 * Recursively select parent
	 */
	public static void recursiveSelectParent(NucLineage lin, String nucName)
		{
		String pname=lin.nuc.get(nucName).parent;
		if(pname!=null)
			{
			NucLineage.selectedNuclei.add(new NucPair(lin, pname));
			recursiveSelectParent(lin, pname);
			}
		}

	private static void selectAll(NucLineage lin)
		{
		for(String s:lin.nuc.keySet())
			NucLineage.selectedNuclei.add(new NucPair(lin,s));
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
		{
		view.requestFocus();
		view.clickRegion(e);
		if(SwingUtilities.isRightMouseButton(e))
			showPopup(e);
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
			view.camera.pan(dx,dy);
			view.repaint();
			}
		
		}
	


	public void mouseMoved(MouseEvent e) {}
	public void mouseExited(MouseEvent e)	{}
	public void mouseEntered(MouseEvent e) {}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
		{
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
			sliderFrameDist.setValue(sliderFrameDist.getValue()+e.getUnitsToScroll()*100);
		else if(e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
			sliderFrameDist.setValue(sliderFrameDist.getValue()+e.getUnitsToScroll()*100);
		view.repaint();
		}

	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		view.setFrameDist(Math.pow(10.0,sliderFrameDist.getValue()/30000.0));
		//view.setBranchScale(Math.pow(10.0,sliderBranchScale.getValue()/30000.0));
		view.expScale=Math.pow(10.0,sliderExpScale.getValue()/3000.0);
		repaint();
		}

	
	/**
	 * Open popup menu on right-click
	 */
	private void showPopup(MouseEvent e) 
		{
		JPopupMenu popup = new JPopupMenu();
		
		final EvDecimal hoverFrame=view.camera.getFrameFromCursor(e.getX(), e.getY());
		popup.add(new JMenuItem("--Frame: "+hoverFrame));
		JMenuItem miGoToFrame=new JMenuItem("Go to frame");
		popup.add(miGoToFrame);
		
		miGoToFrame.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				frameControl.setFrame(hoverFrame);
				}
			});
		
		
		final LineageView.KeyFramePos kf=view.getKeyFrame(e.getX(), e.getY());
		if(kf!=null)
			{
			popup.addSeparator();
			popup.add(new JMenuItem("--Keyframe: "+kf.nuc+"/"+kf.frame));
			JMenuItem miGotoFZ=new JMenuItem("Go to frame and z");
			popup.add(miGotoFZ);
			//popup.addSeparator();
			JMenuItem miDelKF=new JMenuItem("Delete keyframe");
			popup.add(miDelKF);
			JMenuItem miSplit=new JMenuItem("Split here");
			popup.add(miSplit);
			
			miGotoFZ.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					NucLineage lin=getLineage();
					if(lin!=null)
						{
						NucLineage.NucInterp inter=lin.nuc.get(kf.nuc).interpolatePos(hoverFrame);
						frameControl.setFrameZ(hoverFrame, new EvDecimal(inter.pos.z));
						}
					
					
					}
				});

			
			
			miDelKF.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					NucLineage lin=getLineage();
					if(lin!=null)
						{
						NucLineage.Nuc nuc=lin.nuc.get(kf.nuc);
						nuc.pos.remove(kf.frame);
						if(nuc.pos.isEmpty())
							lin.removeNuc(kf.nuc);
						BasicWindow.updateWindows();
						}
					}
				});

			miSplit.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					NucLineage lin=getLineage();
					if(lin!=null)
						{
						NucLineage.Nuc nuc=lin.nuc.get(kf.nuc);
						
						if(!nuc.pos.tailMap(kf.frame).isEmpty())
							{
							String newname=lin.getUniqueNucName();
							NucLineage.Nuc newnuc=lin.getNucCreate(newname);
							newnuc.pos.putAll(nuc.pos.tailMap(kf.frame));
							for(EvDecimal key:newnuc.pos.keySet())
								nuc.pos.remove(key);
							
							newnuc.child.addAll(nuc.child);
							nuc.child.clear();
							for(String cn:newnuc.child)
								lin.nuc.get(cn).parent=newname;
							
							newnuc.parent=kf.nuc;
							nuc.child.add(newname);
							BasicWindow.updateWindows();
							}
						}
					}
				});

			}
		

		//Actions on a nucleus
		ClickRegion r=view.getClickRegion(e);
		if(r!=null && r instanceof ClickRegionName)
			{
			final String nucName=((ClickRegionName)r).nucname;
			
			popup.addSeparator();
			popup.add(new JMenuItem("--Nuc: "+nucName));
			JMenuItem miSetStartFrame=new JMenuItem("Set override start frame");
			popup.add(miSetStartFrame);
			JMenuItem miSetEndFrame=new JMenuItem("Set override end frame");
			popup.add(miSetEndFrame);
			JMenuItem miSetDesc=new JMenuItem("Set description");
			popup.add(miSetDesc);
			JMenuItem miCreateEmptyChild=new JMenuItem("Create empty child");
			popup.add(miCreateEmptyChild);
			JMenuItem miCreateAP=new JMenuItem("Create empty AP+start time");
			popup.add(miCreateAP);
			JMenuItem miRename=new JMenuItem("Rename");
			popup.add(miRename);
			JMenuItem miDelete=new JMenuItem("Delete");
			popup.add(miDelete);

			miSetStartFrame.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{actionSetStartFrame(Collections.singleton(new NucPair(getLineage(),nucName)));}
				});
			miSetEndFrame.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{actionSetEndFrame(Collections.singleton(new NucPair(getLineage(),nucName)));}
				});
			miRename.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{NucRenameDialog.run(Collections.singleton(new NucPair(getLineage(),nucName)),null);}
				});
			miSetDesc.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{actionSetDesc(Collections.singleton(new NucPair(getLineage(),nucName)));}
				});
			miDelete.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{actionRemove(Collections.singleton(new NucPair(getLineage(),nucName)));}
				});
			miCreateEmptyChild.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					String cname=JOptionPane.showInputDialog("Name of child");
					if(cname!=null)
						{
						if(getLineage().nuc.containsKey(cname))
							showErrorDialog("Name already taken");
						else
							{
							getLineage().nuc.get(nucName).child.add(cname);
							getLineage().getNucCreate(cname).parent=nucName;
							BasicWindow.updateWindows();
							}
						}
					}
				});
			miCreateAP.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					String nameA=nucName+"a";
					String nameP=nucName+"p";
					if(!getLineage().nuc.containsKey(nameA) && !getLineage().nuc.containsKey(nameP))
						{
						getLineage().nuc.get(nucName).child.add(nameA);
						getLineage().getNucCreate(nameA).parent=nucName;
						getLineage().nuc.get(nucName).child.add(nameP);
						getLineage().getNucCreate(nameP).parent=nucName;
						BasicWindow.updateWindows();
						
						String sFrame=JOptionPane.showInputDialog("Start frame or nothing for none");
						if(sFrame!=null)
							{
							EvDecimal frame=FrameControl.parseTime(sFrame);
							getLineage().nuc.get(nameA).overrideStart=frame;
							getLineage().nuc.get(nameP).overrideStart=frame;
							BasicWindow.updateWindows();
							}
						}
					else
						showErrorDialog("One child already exist");
					}
				});
			
			
			}
		
		popup.show(e.getComponent(),e.getX(), e.getY());
		}
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		objectCombo.updateList();
//			view.currentFrame=frameControl.getFrame();
		view.currentLin=getLineage();
		repaint();
		}
	
	public void loadedFile(EvData data)	{}
	public void freeResources(){}
	}
