/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineageWindow;

import java.awt.event.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.*;
import endrov.ev.*;
import endrov.lineage.*;
import endrov.lineageWindow.LineageView.ClickRegion;
import endrov.lineageWindow.LineageView.ClickRegionName;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.EvUtilRegexp;
import endrov.util.JImageButton;
import endrov.util.JImageToggleButton;
import endrov.util.SnapBackSlider;
import endrov.util.SnapBackSlider.SnapChangeListener;


/**
 * Lineage Window - an editable tree of the lineage
 * @author Johan Henriksson
 */
public class LineageWindow extends BasicWindow
		implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, TimedDataWindow
	{
	static final long serialVersionUID=0;
	

	public static final ImageIcon iconShowRoot=new ImageIcon(LineageWindow.class.getResource("jhToRoot.png"));
	public static final ImageIcon iconShowSelected=new ImageIcon(LineageWindow.class.getResource("jhToSelected.png"));
	public static final ImageIcon iconZoomAll=new ImageIcon(LineageWindow.class.getResource("jhZoomAll.png"));
	public static final ImageIcon iconShowKeyFrames=new ImageIcon(LineageWindow.class.getResource("jhShowKeyFrames.png"));
	public static final ImageIcon iconShowEvents=new ImageIcon(LineageWindow.class.getResource("jhShowEvents.png"));
	public static final ImageIcon iconShowLabel=new ImageIcon(LineageWindow.class.getResource("jhShowLabel.png"));
	public static final ImageIcon iconShowFrameLines=new ImageIcon(LineageWindow.class.getResource("jhShowFrameLines.png"));
	public static final ImageIcon iconShowVerticalTree=new ImageIcon(LineageWindow.class.getResource("jhShowVerticalTree.png"));

	public static final ImageIcon iconSelectByName=new ImageIcon(LineageWindow.class.getResource("jhSelectByName.png"));

	private final EvHidableSidePaneRight sidePanelSplitPane;

	
	private SnapBackSlider sliderZoomX=new SnapBackSlider(JSlider.HORIZONTAL, -10000,10000); 
	private SnapBackSlider sliderZoomY=new SnapBackSlider(JSlider.VERTICAL, -10000,10000);
	private JButton buttonGoToRoot=new JImageButton(iconShowRoot,"Go to root");
	private JButton buttonGoToSelected=new JImageButton(iconShowSelected,"Go to selected");
	private JButton buttonZoomAll=new JImageButton(iconZoomAll,"Fit everything into screen");
	//private JButton buttonSelectByName=new JImageButton(iconSelectByName,"Select by name of cell, or tissue. Regular expression possible");
	private JToggleButton buttonShowFrameLines=new JImageToggleButton(iconShowFrameLines,"Show frame lines",true);
	private JToggleButton buttonShowKeyFrames=new JImageToggleButton(iconShowKeyFrames,"Show key frames");
	private JToggleButton buttonShowEvents=new JImageToggleButton(iconShowEvents,"Show events");
	private JToggleButton buttonShowLabels=new JImageToggleButton(iconShowLabel,"Show labels",true);
	private JToggleButton buttonShowVerticalTree=new JImageToggleButton(iconShowVerticalTree,"Show vertical tree");
	private LineageView view=new LineageView();
	private FrameControlLineage frameControl=new FrameControlLineage(new ChangeListener()
		{
		public void stateChanged(ChangeEvent e)
			{
			view.setFrame(frameControl.getFrame().doubleValue());
			}
		});
	
	public final EvComboObjectOne<Lineage> objectCombo=new EvComboObjectOne<Lineage>(new Lineage(),false,true);

	/** Last coordinate of the mouse pointer. Used to detect dragging distance. */
	private int mouseLastX=0, mouseLastY=0;
	
	
	public JMenu menuLineage=new JMenu("Lineage");
	public JMenuItem miExportImage=new JMenuItem("Export Image");
	
	public JCheckBoxMenuItem miShowExpDot=new JCheckBoxMenuItem("Show level dots",false);
	public JMenu miShowExp=new JMenu("Level display");
	public JRadioButtonMenuItem miShowExpNone=new JRadioButtonMenuItem("Off",false);
	public JRadioButtonMenuItem miShowExpLine=new JRadioButtonMenuItem("Line",true);
	public JRadioButtonMenuItem miShowExpSolid=new JRadioButtonMenuItem("Filled",false);
	public JCheckBoxMenuItem miShowScale=new JCheckBoxMenuItem("Show scale",view.showScale);
	
	
	
	public JMenuItem miFoldAll=new JMenuItem("Fold all");
	public JMenuItem miUnfoldAll=new JMenuItem("Unfold all");

	
	
	private JComboBox inpSelectByName;
	private JComboBox getInpSelectByName()
		{
		return inpSelectByName;
		}
	private ActionListener listenerSelectByName=new ActionListener()
		{
		public void actionPerformed(ActionEvent e)
			{
			String inp=(String)getInpSelectByName().getSelectedItem();
			if(inp==null || inp.equals("")) 
				return;
			getInpSelectByName().removeActionListener(this);
			getInpSelectByName().setSelectedItem("");
			Set<String> gnucs=Lineage.cellGroups.groups.get(inp);
			Lineage lin=getLineage();
			EvSelection.unselectAll();
			if(gnucs!=null && lin!=null)
				{
				//LinkedList<NucSel> sels=new LinkedList<NucSel>();
				for(String name:gnucs)
					if(lin.particle.containsKey(name))
						EvSelection.select(new LineageSelParticle(lin,name));
				//EvSelection.selectOnly(sels.toArray(new NucSel[]{}));
				}
			
			Pattern match=EvUtilRegexp.wildcardToRegex(inp);
			for(String name:lin.particle.keySet())
				if(match.matcher(name).matches())
					EvSelection.select(new LineageSelParticle(lin,name));
			getInpSelectByName().addActionListener(this);
			BasicWindow.updateWindows();
			}
		};
	

	private LineageExpPanel expPanel=new LineageExpPanel(view);

		
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
		
		Vector<String> groupNames=new Vector<String>();
		groupNames.addAll(Lineage.cellGroups.groups.keySet());
		
		Vector<String> comboNames=new Vector<String>();
		comboNames.add("");
		comboNames.addAll(Lineage.cellGroups.groups.keySet());
		inpSelectByName=new JComboBox(comboNames);
		inpSelectByName.setEditable(true);
		inpSelectByName.addActionListener(listenerSelectByName);
		
		
		//Add listeners
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		view.addMouseWheelListener(this);
		objectCombo.addActionListener(this);
		miShowExpDot.addActionListener(this);
		miShowExpLine.addActionListener(this);
		miShowExpSolid.addActionListener(this);
		miShowExpNone.addActionListener(this);
		miExportImage.addActionListener(this);
		miFoldAll.addActionListener(this);
		miUnfoldAll.addActionListener(this);
		miShowScale.addActionListener(this);


		buttonGoToRoot.addActionListener(this);
		buttonGoToSelected.addActionListener(this);
		buttonZoomAll.addActionListener(this);
		buttonShowFrameLines.addActionListener(this);
		buttonShowKeyFrames.addActionListener(this);
		buttonShowEvents.addActionListener(this);
		buttonShowLabels.addActionListener(this);
		buttonShowVerticalTree.addActionListener(this);
		


		sliderZoomX.addSnapListener(new SnapChangeListener(){public void slideChange(SnapBackSlider source, int change){zoomX(change);repaint();}});
		sliderZoomY.addSnapListener(new SnapChangeListener(){public void slideChange(SnapBackSlider source, int change){zoomY(change);repaint();}});
		
		ButtonGroup expGroup=new ButtonGroup();
		expGroup.add(miShowExpLine);
		expGroup.add(miShowExpSolid);
		expGroup.add(miShowExpNone);
		updateShowExp();
		
		
		sidePanelSplitPane=new EvHidableSidePaneRight(EvSwingUtil.layoutLCR(null, view, sliderZoomY), expPanel, true);
		
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(sidePanelSplitPane,BorderLayout.CENTER);
		
		
		
		JComponent bottomLower = EvSwingUtil.layoutLCR(null, inpSelectByName, null);
		
		JPanel bottomUpper = new JPanel(new GridBagLayout());
//		add(bottomUpper,BorderLayout.SOUTH);
		add(EvSwingUtil.layoutEvenVertical(bottomUpper,bottomLower),BorderLayout.SOUTH);

//		add(sliderZoomY,BorderLayout.EAST);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		bottomUpper.add(objectCombo,c);
		c.gridx++;
		c.weightx=1;
		bottomUpper.add(sliderZoomX,c);
		c.gridx++;
		c.weightx=0;
		c.fill = 0;
		bottomUpper.add(frameControl,c);
		c.gridx++;
		bottomUpper.add(EvSwingUtil.layoutEvenHorizontal(
				buttonGoToRoot, buttonGoToSelected, buttonZoomAll,
				buttonShowVerticalTree, buttonShowFrameLines, buttonShowKeyFrames, buttonShowEvents, buttonShowLabels
				),c);
		c.gridx++;
		
		addMenubar(menuLineage);
		new LineageCommonUI(this, this).addToMenu(menuLineage, true);
		menuLineage.addSeparator();
		menuLineage.add(miExportImage);
		menuLineage.addSeparator();
		menuLineage.add(miShowExpDot);
		menuLineage.add(miShowExp);
		menuLineage.add(miShowScale);
		
		miShowExp.add(miShowExpNone);
		miShowExp.add(miShowExpLine);
		miShowExp.add(miShowExpSolid);
		
		menuLineage.addSeparator();
		menuLineage.add(miFoldAll);
		menuLineage.add(miUnfoldAll);
		
		
		copyShowSettings();	
		
		//Window overall things
		setTitleEvWindow("Lineage Window");
		packEvWindow();
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);

		dataChangedEvent();
		
		}
	

		
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		Element e=new Element("lineagewindow");
		setXMLbounds(e);
		e.setAttribute("group",""+frameControl.getGroup());
		root.addContent(e);
		}

	
	private Lineage getLineage()
		{
		return objectCombo.getSelectedObject();
		}
	
	/**
	 * Copy show settings from toggle buttons to view
	 */
	private void copyShowSettings()
		{
		view.showFrameLines=buttonShowFrameLines.isSelected();
		view.showKeyFrames=buttonShowKeyFrames.isSelected();
		view.showEvents=buttonShowEvents.isSelected();
		view.showLabel=buttonShowLabels.isSelected();
		view.showHorizontalTree=!buttonShowVerticalTree.isSelected();
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==buttonGoToRoot)
			view.goToRoot();
		else if(e.getSource()==buttonGoToSelected)
			view.goToSelected();
		else if(e.getSource()==buttonZoomAll)
			view.zoomAll();
/*		else if(e.getSource()==miExportImage)
			{
			view.saveToDisk();
			}*/
		else if(e.getSource()==buttonShowFrameLines || e.getSource()==buttonShowKeyFrames || e.getSource()==buttonShowEvents ||  
				e.getSource()==buttonShowLabels || e.getSource()==buttonShowVerticalTree)
			{
			copyShowSettings();
			repaint();
			}
		else if(e.getSource()==miUnfoldAll)
			view.unfoldAll();
		else if(e.getSource()==miFoldAll)
			view.foldAll();
		else if(e.getSource()==objectCombo)
			{
			
			dataChangedEvent();
			/*
			//System.out.println("objectcombo");
			view.currentLin=getLineage();
			
			expPanel.placeAllRenderers();
			
			repaint();
			*/
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
	
	
	
	public void updateShowExp()
		{
		view.showExpDot=miShowExpDot.isSelected();
		view.showExpLine=miShowExpLine.isSelected();
		view.showExpSolid=miShowExpSolid.isSelected();
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
			view.pan(dx,dy);
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
		double dist;
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
			{
			dist=e.getUnitsToScroll()/10.0;
			}
		else //if(e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
			{
			dist=e.getUnitsToScroll()/10.0;
			}
		dist=Math.exp(dist);
		view.zoom(dist, e.getX(), dist, e.getY());
		view.repaint();
		}

	
	
	public void zoomX(int numStep)
		{
		double factor=Math.exp(-numStep/1000.0);
		view.zoomX(factor, view.getWidth()/2);
		}
	
	public void zoomY(int numStep)
		{
		double factor=Math.exp(-numStep/1000.0);
		view.zoomY(factor, view.getHeight()/2);
		}
	
	/**
	 * Open popup menu on right-click
	 */
	private void showPopup(MouseEvent e) 
		{
		JPopupMenu popup = new JPopupMenu();
		
		//Round frame to something sensible
		final EvDecimal hoverFrame2=view.getFrameFromCursor(e.getX(),e.getY());
		final EvDecimal hoverFrame=new EvDecimal(hoverFrame2.toBigDecimal().divideToIntegralValue(BigDecimal.ONE));
		
		System.out.println("Hover frame "+hoverFrame);
		popup.add(new JMenuItem("--Frame: "+FrameControl.formatTime(hoverFrame)));
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
					Lineage lin=getLineage();
					if(lin!=null)
						{
						Lineage.InterpolatedParticle inter=lin.particle.get(kf.nuc).interpolatePos(hoverFrame);
						frameControl.setFrameZ(hoverFrame, new EvDecimal(inter.pos.z));
						}
					
					
					}
				});

			
			
			miDelKF.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					final Lineage lin=getLineage();
					final String partName=kf.nuc;
					final EvDecimal frame=kf.frame;
					if(lin!=null)
						{
						new UndoOpLineageReplaceSomeParticle("Delete particle keyframe")
							{
							public void redo()
								{
								keep(lin, partName);
								
								Lineage.Particle particle=lin.particle.get(partName);
								particle.pos.remove(frame);
								if(particle.pos.isEmpty())
									lin.removeParticle(partName);
								BasicWindow.updateWindows();
								}
							}.execute();
						}
					}
				});

			miSplit.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					final Lineage lin=getLineage();
					final String oldName=kf.nuc;
					final EvDecimal frame=kf.frame;
					if(lin!=null)
						{
						new UndoOpLineageReplaceSomeParticle("Split particle")
							{
							public void redo()
								{
								Lineage.Particle oldParticle=lin.particle.get(oldName);
								if(!oldParticle.pos.tailMap(frame).isEmpty())
									{
									//Store undo op
									String newName=lin.getUniqueParticleName();
									
									keep(lin, oldName);
									keep(lin, newName);
									for(String cname:oldParticle.child)
										keep(lin, cname);
									
									
									//Create a new particle and put the keyframes *after* this point in it, then keep the rest in the old particle
									Lineage.Particle newParticle=lin.getCreateParticle(newName);
									newParticle.pos.putAll(oldParticle.pos.tailMap(kf.frame));
									oldParticle.pos.keySet().removeAll(newParticle.pos.keySet());

									//Move all children into the new nuc. Update parents
									newParticle.child.addAll(oldParticle.child);
									oldParticle.child.clear();
									for(String cn:newParticle.child)
										{
										lin.particle.get(cn).parents.remove(oldName);
										lin.particle.get(cn).parents.add(newName);
										}

									//Connect old and new nuc
									newParticle.parents.add(oldName);
									oldParticle.child.add(newName);
									BasicWindow.updateWindows();
									}
								}
							}.execute();
						
						
						}
					}
				});

			}
		

		//Actions on a particle
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
					{LineageCommonUI.actionSetStartFrameDialog(Collections.singleton(new LineageSelParticle(getLineage(),nucName)));}
				});
			miSetEndFrame.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{LineageCommonUI.actionSetEndFrameDialog(Collections.singleton(new LineageSelParticle(getLineage(),nucName)));}
				});
			miRename.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{LineageRenameParticleDialog.run(Collections.singleton(new LineageSelParticle(getLineage(),nucName)),null);}
				});
			miSetDesc.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{LineageCommonUI.actionSetDesc(Collections.singleton(new LineageSelParticle(getLineage(),nucName)));}
				});
			miDelete.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{LineageCommonUI.actionRemove(Collections.singleton(new LineageSelParticle(getLineage(),nucName)));}
				});
			miCreateEmptyChild.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{LineageCommonUI.actionCreateEmptyChild(getLineage(), nucName);}
				});
			miCreateAP.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{LineageCommonUI.actionCreateAP(getLineage(), nucName);}
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
		expPanel.setAvailableExpressions(view.collectExpNames());
		expPanel.placeAllRenderers();
		repaint();
		}
	
	public void loadedFile(EvData data)
		{
		dataChangedEvent();
		}
	public void freeResources(){}


	
	public EvDecimal getFrame()
		{
		System.out.println("Frame: "+view.getFrame());
		return view.getFrame();
		}

	public EvContainer getSelectedData()
		{
		return objectCombo.getSelectObjectParent();
		}


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
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
	
	

	}
