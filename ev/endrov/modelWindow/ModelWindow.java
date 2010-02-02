/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindow;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.jdom.*;


import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.consoleWindow.*;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.ev.*;
import endrov.keyBinding.*;
import endrov.modelWindow.basicExt.CrossHandler;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.EvXmlUtil;
import endrov.util.SnapBackSlider;

//TODO drag and drop of a file with # in the name fails on linux

/**
 * Model window - displays a navigatable 3d model
 * @author Johan Henriksson
 */
public class ModelWindow extends BasicWindow
		implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, ChangeListener, JinputListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	
	public static Vector<ModelWindowExtension> modelWindowExtensions=new Vector<ModelWindowExtension>();

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	private void setPersonalConfig(Element e)
		{
		try
			{
			Rectangle r=BasicWindow.getXMLbounds(e);
			setBoundsEvWindow(r);
			
			frameControl.setGroup(e.getAttribute("group").getIntValue());
			
			for(ModelWindowHook hook:modelWindowHooks)
				hook.readPersonalConfig(e);

			sidePanelSplitPane.setPanelVisible(e.getAttribute("sidePanelVisible").getBooleanValue());
			}
		catch (Exception e1)
			{
			e1.printStackTrace();
			}
		}

	
	private int mouseLastX, mouseLastY;
	
	public final Vector<ModelWindowHook> modelWindowHooks=new Vector<ModelWindowHook>();	
	public final Vector<JComponent> sidePanelItems=new Vector<JComponent>();
	private final JPanel sidePanel=new JPanel(new GridBagLayout());
	public final Vector<JComponent> bottomPanelItems=new Vector<JComponent>();
	private final JPanel bottomPanel=new JPanel(new GridBagLayout());
	private JPanel bottomMain=new JPanel(new GridBagLayout());
	
	private JProgressBar progress=new JProgressBar(0,1000);
	
	public final ModelView view;
	public final FrameControlModel frameControl;
	private final EvComboObject metaCombo=new EvComboObject(new LinkedList<EvObject>(),true,false)
		{
		static final long serialVersionUID=0;
		public boolean includeObject(EvContainer cont)
			{
			return true;
			}
		};
	private final JButton buttonCenter=new JButton("Center");
	private final EvHidableSidePaneRight sidePanelSplitPane;
	
	public JMenu menuModel=new JMenu("ModelWindow");
	
	private JMenu miView=new JMenu("Default views");
	private JMenuItem miViewFront=new JMenuItem("Front");
	private JMenuItem miViewBack=new JMenuItem("Back");
	private JMenuItem miViewTop=new JMenuItem("Top");
	private JMenuItem miViewBottom=new JMenuItem("Bottom");
	private JMenuItem miViewLeft=new JMenuItem("Left");
	private JMenuItem miViewRight=new JMenuItem("Right");

	private JMenu miWindowState=new JMenu("Window State");
	private JMenuItem miCopyState=new JMenuItem("Copy");
	private JMenuItem miPasteState=new JMenuItem("Paste");

	private JCheckBoxMenuItem miShowAxis=new JCheckBoxMenuItem("Show axis directions");

	private JMenu miSetBGColor=makeSetBGColorMenu();

	private SnapBackSlider barZoom=new SnapBackSlider(JScrollBar.VERTICAL,0,1000);
	private SnapBackSlider barRotate=new SnapBackSlider(JScrollBar.VERTICAL,0,1000);
	
	private ObjectDisplayList objectDisplayList=new ObjectDisplayList();
	
	public final CrossHandler crossHandler=new CrossHandler();
	
	
	private List<ModelWindowMouseListener> modelWindowMouseListeners=new LinkedList<ModelWindowMouseListener>();
	public void addModelWindowMouseListener(ModelWindowMouseListener li)
		{
		modelWindowMouseListeners.add(li);
		}
	
	
	/**
	 * Is an object supposed to be visible?
	 */
	public boolean showObject(EvObject ob)
		{
		return objectDisplayList.toDisplay(ob);
		}
	

	
	/**
	 * Make a new window at default location
	 */
	public ModelWindow()
		{
		this(new Rectangle(0,50,1000,800));
		}
	
	/**
	 * Make a new window at some location
	 */
	public ModelWindow(Rectangle bounds)
		{
		view=new ModelView(this);
		frameControl=new FrameControlModel(this);

		//Add hooks
		for(ModelWindowExtension e:modelWindowExtensions)
			e.newModelWindow(this);
				
		//Add listeners to view
		view.addMouseMotionListener(this);
		view.addMouseListener(this);
		view.addMouseWheelListener(this);
		view.addKeyListener(this);
		view.setFocusable(true);
		setFocusable(true);
		addKeyListener(this);

		//Build view menu
		addMenubar(menuModel);
		menuModel.add(miView);
		miView.add(miViewFront);
		miView.add(miViewBack);
		miView.add(miViewTop);
		miView.add(miViewBottom);
		miView.add(miViewLeft);
		miView.add(miViewRight);
		
		//Build window state menu
		menuModel.add(miWindowState);
		miWindowState.add(miCopyState);
		miWindowState.add(miPasteState);
		
		//Build other menu entries
		menuModel.add(miSetBGColor);
		menuModel.add(miShowAxis);
		
		//Add action listeners
		miViewTop.addActionListener(this);
		miViewLeft.addActionListener(this);
		miViewFront.addActionListener(this);
		miViewBack.addActionListener(this);
		miViewBottom.addActionListener(this);
		miViewRight.addActionListener(this);
		buttonCenter.addActionListener(this);
		metaCombo.addActionListener(this);
		miCopyState.addActionListener(this);
		miPasteState.addActionListener(this);
		miShowAxis.addActionListener(this);
		
		//Add change listeners
		objectDisplayList.addChangeListener(this);

		//Special: cross
		addModelWindowMouseListener(crossHandler.crossMListener);
		
		//Put GUI together
		GridBagConstraints constrFrame=new GridBagConstraints();
		constrFrame.gridx=0;
		constrFrame.weightx=1;
		constrFrame.fill=GridBagConstraints.HORIZONTAL;
		GridBagConstraints constrCenter=new GridBagConstraints();
		constrCenter.gridx=1;
		constrFrame.weightx=1;
		GridBagConstraints constrCombo=new GridBagConstraints();
		constrCombo.gridx=2;
		constrFrame.weightx=3;
		bottomMain.add(frameControl,constrFrame);
		bottomMain.add(buttonCenter,constrCenter);
		bottomMain.add(metaCombo,constrCombo);		

		JPanel inToolpane=new JPanel(new BorderLayout());
		inToolpane.setMinimumSize(new Dimension(250,20));
		inToolpane.setMaximumSize(new Dimension(250,20));
		inToolpane.add(sidePanel,BorderLayout.NORTH);
		JScrollPane toolPanel=new JScrollPane(inToolpane,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		toolPanel.setMinimumSize(new Dimension(250,20));
		toolPanel.setMaximumSize(new Dimension(250,20));
//		updateToolPanels();

		JPanel zoomrotPanel=new JPanel(new GridLayout(2,1));
		zoomrotPanel.add(EvSwingUtil.layoutACB(new JLabel(BasicIcon.iconLabelZoom), barZoom, null));
		zoomrotPanel.add(EvSwingUtil.layoutACB(new JLabel(BasicIcon.iconLabelRotate), barRotate, null));

		
		barZoom.addSnapListener(new SnapBackSlider.SnapChangeListener(){
			public void slideChange(int change)
				{
				view.pan(0,0,change*5,false);
				view.repaint();
				}
		});
		barRotate.addSnapListener(new SnapBackSlider.SnapChangeListener(){
			public void slideChange(int change)
				{
				view.camera.rotateCamera(0, 0, change/200.0);
				view.repaint();
				}
		});


		
		sidePanelSplitPane = new EvHidableSidePaneRight(view, toolPanel, true);
		
		setLayout(new BorderLayout());
		add(sidePanelSplitPane,BorderLayout.CENTER);
		add(bottomPanel,BorderLayout.SOUTH);
		add(zoomrotPanel,BorderLayout.EAST);
		
		
		//Trigger all events to build dynamic parts of window
		updateToolPanels();
		dataChangedEvent();
		
		//Window overall things
		setTitleEvWindow("Model Window");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		attachDragAndDrop(this);
		view.autoCenter();
		view.renderAxisArrows=miShowAxis.isSelected();
		
		//TODO dangerous, might be called before constructed
		attachJinputListener(this);
		}
	
	
	
	
	private JMenu makeSetBGColorMenu()
		{
		JMenu m=new JMenu("Set background color");
		for(final EvColor c:EvColor.colorList)
			{
			JMenuItem mi=new JMenuItem(c.name);
			mi.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
					{
					view.bgColor=c.c;
					view.repaint();
					}
			});
			m.add(mi);
			}
		return m;
		}

	
	
	
	
	
	
	
	
	
	/**
	 * Clear tool panels and readd items
	 */
	public void updateToolPanels()
		{
		System.out.println("updating tool panels");
		sidePanelItems.clear();
		bottomPanelItems.clear();
		bottomPanelItems.add(bottomMain);
		bottomPanelItems.add(progress); //Since it belongs to voxel, maybe keep it there?
		for(ModelWindowHook h:modelWindowHooks)
			h.fillModelWindowMenus();
		
		//Assemble side panel
		int counta=0;
		sidePanel.removeAll();
		for(JComponent c:sidePanelItems)
			{
			GridBagConstraints cr=new GridBagConstraints();	cr.gridy=counta;	cr.fill=GridBagConstraints.HORIZONTAL;
			cr.weightx=1;
			sidePanel.add(c,cr);
			counta++;
			}
		GridBagConstraints cg=new GridBagConstraints();	cg.gridy=counta;	cg.fill=GridBagConstraints.HORIZONTAL;	cg.weightx=1;
		sidePanel.add(objectDisplayList,cg);
		
		//Assemble bottom panel
		int countb=0;
		bottomPanel.removeAll();
		for(JComponent c:bottomPanelItems)
			{
			GridBagConstraints cr=new GridBagConstraints();	cr.gridy=countb;	cr.fill=GridBagConstraints.HORIZONTAL;
			cr.weightx=1;
			bottomPanel.add(c,cr);
			countb++;
			}
		
		sidePanel.revalidate();
		bottomPanel.revalidate();
		}
	



	
	
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		Element e=new Element("modelwindow");
		setXMLbounds(e);
		e.setAttribute("group",""+frameControl.getGroup());
		e.setAttribute("sidePanelVisible",""+sidePanelSplitPane.getPanelVisible());
		
		for(ModelWindowHook hook:modelWindowHooks)
			hook.savePersonalConfig(e);
		
		root.addContent(e);
		}

	

	
	/**
	 * Action listener
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==metaCombo)
			dataChangedEvent();
		else if(e.getSource()==buttonCenter)
			{
			view.autoCenter();
			dataChangedEvent();
			}
		else if(e.getSource()==miViewFront)
			setPresetView(0, 0, 0);
		else if(e.getSource()==miViewBack)
			setPresetView(0, +Math.PI, 0);
		else if(e.getSource()==miViewLeft)
			setPresetView(0, +Math.PI/2, 0);
		else if(e.getSource()==miViewRight)
			setPresetView(0, -Math.PI/2, 0);
		else if(e.getSource()==miViewTop)
			setPresetView(+Math.PI/2, 0, 0);
		else if(e.getSource()==miViewBottom)
			setPresetView(-Math.PI/2, 0, 0);
		else if(e.getSource()==miCopyState)
			{
			Element root=new Element("w");
			windowSavePersonalSettings(root);
			try
				{
				String out=EvXmlUtil.xmlToString(new Document((Element)root.getChild("modelwindow").clone()));
				EvSwingUtil.setClipBoardString(out);
				}
			catch (Exception e1)
				{
				e1.printStackTrace();
				}
			}
		else if(e.getSource()==miPasteState)
			{
			try
				{
				setPersonalConfig(EvXmlUtil.stringToXml(EvSwingUtil.getClipBoardString()));
				}
			catch (Exception e1)
				{
				e1.printStackTrace();
				}
			repaint();
			}
		else if(e.getSource()==miShowAxis)
			{
			view.renderAxisArrows=miShowAxis.isSelected();
			repaint();
			}
		}
	
	/**
	 * Set a preset view
	 */
	private void setPresetView(double x, double y, double z)
		{
		view.camera.setRotation(x, y, z);
		view.autoCenter();
		}
	
	public void keyPressed(KeyEvent e)
		{
		if(!ScriptBinding.runScriptKey(e))
			{
			if(KeyBinding.get(KEY_GETCONSOLE).typed(e))
				ConsoleWindow.focusConsole(this, view);
			}
		}
	public void keyReleased(KeyEvent e)
		{
		}
	public void keyTyped(KeyEvent e)
		{	
		//TODO not hardcode! then also GP bind
		if(e.getKeyChar()=='a')
			frameControl.stepBack(new EvDecimal(1.0));
		else if(e.getKeyChar()=='d')
			frameControl.stepForward(new EvDecimal(1.0));	
		else if(e.getKeyChar()=='w')
			frameControl.stepForward(new EvDecimal(0.2));	
		else if(e.getKeyChar()=='s')
			frameControl.stepBack(new EvDecimal(0.2));
		}
	public void mouseClicked(MouseEvent e)
		{
		for(ModelWindowMouseListener list:modelWindowMouseListeners)
			list.mouseClicked(e);
		view.requestFocus();
		}
	public void mousePressed(MouseEvent e)
		{
		mouseLastX = e.getX();
		mouseLastY = e.getY();
		view.mouseX=mouseLastX;
		view.mouseY=mouseLastY;
		for(ModelWindowMouseListener list:modelWindowMouseListeners)
			list.mousePressed(e);
		}
	public void mouseReleased(MouseEvent e)
		{
		for(ModelWindowMouseListener list:modelWindowMouseListeners)
			list.mouseReleased(e);
		}
	public void mouseEntered(MouseEvent e)
		{
		for(ModelWindowMouseListener list:modelWindowMouseListeners)
			list.mouseEntered(e);
		}
	public void mouseExited(MouseEvent e)
		{
		for(ModelWindowMouseListener list:modelWindowMouseListeners)
			list.mouseExited(e);
		view.mouseX=-1;
		view.mouseY=-1;		
		}
	public void mouseMoved(MouseEvent e)
		{
		view.mouseX=e.getX();
		view.mouseY=e.getY();
		for(ModelWindowMouseListener list:modelWindowMouseListeners)
			list.mouseMoved(e);
		view.repaint(); //Needed to update selection etc //TODO modw 
		}
	public void mouseDragged(MouseEvent e)
		{
		final int dx=e.getX()-mouseLastX;
		final int dy=e.getY()-mouseLastY;

		boolean moveAccepted=false;
		for(ModelWindowMouseListener list:modelWindowMouseListeners)
			moveAccepted|=list.mouseDragged(e,dx,dy);

		if(!moveAccepted)
			{
			if((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK)!=0 ||
					SwingUtilities.isMiddleMouseButton(e))
				{
				view.pan(-dx,+dy,0,true);
				}
			else if((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0)
				{
				view.pan(0,0,dy,false);
				}
			else
				{
				if(SwingUtilities.isLeftMouseButton(e))
					view.camera.rotateCamera(-dy/400.0, -dx/400.0, 0);
				else if(SwingUtilities.isRightMouseButton(e))
					view.camera.rotateCenter(-dy/400.0, -dx/400.0, 0);
				}
			}
		dataChangedEvent();

		mouseLastX = e.getX();
		mouseLastY = e.getY();
		view.mouseX=mouseLastX;
		view.mouseY=mouseLastY;
		}
	/**
	 * Handle mouse scroll wheel
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
		{
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
			{
			if((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK)!=0)
				//view.pan(0,0,e.getUnitsToScroll()/5.0);
				view.camera.rotateCamera(0, 0, e.getUnitsToScroll()/20.0);
			else
				view.pan(0,0,e.getUnitsToScroll()*20.0,(e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
			dataChangedEvent();
			}
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		dataChangedEvent();
		}

	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		metaCombo.updateList();
		objectDisplayList.setData(metaCombo.getSelectedObject());
		objectDisplayList.updateList();
		for(ModelWindowHook h:modelWindowHooks)
			h.datachangedEvent();
		if(frameControl!=null && view!=null)
			{
			view.frame=frameControl.getFrame();
			view.repaint(); //TODO modw repaint
			//System.out.println("repainting");
			}
		}

	/**
	 * Get the EvContainer currently selected, never null
	 */
	public EvContainer getSelectedData()
		{
		EvContainer d=getSelectedDataNull();
		if(d==null)
			return new EvContainer();
		else
			return d;
		}

	/**
	 * Get the EvData currently selected, can be null
	 */
	public EvContainer getSelectedDataNull()
		{
		return metaCombo.getSelectedObject();
		}


	/**
	 * Called whenever a new file is loaded, and this window should change active set
	 */
	public void loadedFile(EvData data)
		{
		metaCombo.setSelectedObject(data);
		//metaCombo.setMeta(data);
		}
	
	public void finalize()
		{
		System.out.println("removing model window");
		}
	
	

	
	private List<ProgressMeter> progressMeters=new LinkedList<ProgressMeter>();
	/**
	 * Progress meters
	 */
	public class ProgressMeter
		{
		public void init()
			{
			SwingUtilities.invokeLater(new Runnable(){public void run(){updateProgressMeter();}});
			}
		/** Set progress, 0-1000 */
		public void set(final int v)
			{
			progress=v;	SwingUtilities.invokeLater(new Runnable(){public void run(){updateProgressMeter();}});
//			SwingUtilities.invokeLater(new Runnable(){public void run(){pbar.setValue(v);}});
			}
		/** Remove progress bar. update view */
		public void done()
			{
			final ProgressMeter mthis=this;
			SwingUtilities.invokeLater(new Runnable(){public void run()
				{
				progressMeters.remove(mthis);
				updateProgressMeter();
				view.repaint(); //TODO modw repaint. w.repaint does not do the job in this case!
				}});
			}
		private int progress=0; 
//		private JProgressBar pbar=new JProgressBar(0,1000);
		}
	
	/**
	 * Update progressbar panel
	 */
	private void updateProgressMeter()
		{
		synchronized(progressMeters)
			{
//			int tot=progressMeters.size()*1000;
			int sum=0;
			for(ProgressMeter pm:progressMeters)
				sum+=pm.progress;
			if(progressMeters.isEmpty())
				progress.setValue(1000);
			else
				progress.setValue(sum/progressMeters.size());
			/*
			if(progressMeters.isEmpty())
				{
				progress.setVisible(false);
				progress.removeAll();
				}
			else
				{
				progress.removeAll();
				progress.setLayout(new GridLayout(1,progressMeters.size()));
				for(ProgressMeter pm:progressMeters)
					progress.add(pm.pbar);
				progress.revalidate();
				progress.setVisible(true);
				}
				*/
			}
		}
	
	/**
	 * Create a new progress meter, add it, and return 
	 */
	public ProgressMeter createProgressMeter()
		{
		synchronized(progressMeters)
			{
			ProgressMeter m=new ProgressMeter();
			m.init();
			progressMeters.add(m);
			return m;
			}
		}

	//
	private static final int AXIS_ROTX=KeyBinding.register(new KeyBinding("Model Window","Rot X",new KeyBinding.TypeJInput("y",0)));
	private static final int AXIS_ROTY=KeyBinding.register(new KeyBinding("Model Window","Rot Y",new KeyBinding.TypeJInput("x",0)));
	private static final int AXIS_PANZ=KeyBinding.register(new KeyBinding("Model Window","Axis pan Z",new KeyBinding.TypeJInput("rz",0)));
	private static final int AXIS_ROTZ=KeyBinding.register(new KeyBinding("Model Window","Axis rot",new KeyBinding.TypeJInput("z",0)));

	private static final int ALTERNATIVECONTROLS=KeyBinding.register(new KeyBinding("Model Window","Toggle Alternative",new KeyBinding.TypeJInput("5",1)));

	private static final int ALT_FORWARD=KeyBinding.register(new KeyBinding("Model Window","Alt/Forward",new KeyBinding.TypeJInput("4",1)));
	private static final int ALT_BACKWARD=KeyBinding.register(new KeyBinding("Model Window","Alt/Backward",new KeyBinding.TypeJInput("6",1)));
	private static final int AXIS_ALTPANX=KeyBinding.register(new KeyBinding("Model Window","Alt/Pan X",new KeyBinding.TypeJInput("x",0)));
	private static final int AXIS_ALTPANY=KeyBinding.register(new KeyBinding("Model Window","Alt/Pan Y",new KeyBinding.TypeJInput("y",0)));
	private static final int AXIS_ALTROTX=KeyBinding.register(new KeyBinding("Model Window","Alt/Rot X",new KeyBinding.TypeJInput("rz",0)));
	private static final int AXIS_ALTROTY=KeyBinding.register(new KeyBinding("Model Window","Alt/Rot Y",new KeyBinding.TypeJInput("z",0)));

	private static final int KEY_NEXTFRAME=KeyBinding.register(new KeyBinding("Model Window","Next frame",new KeyBinding.TypeJInput("2",1)));
	private static final int KEY_PREVFRAME=KeyBinding.register(new KeyBinding("Model Window","Prev frame",new KeyBinding.TypeJInput("0",1)));

	public void bindAxisPerformed(JInputManager.EvJinputStatus status)
		{
		float axismulxy=10;
		float axismulz=30;
		float rotmulxy=0.05f;
		float rotmulz=0.1f;

		float axisx=0,axisy=0,axisz=0, rotx=0,roty=0,rotz=0, rotcx=0,rotcy=0,rotcz=0;
		boolean update=false;
		
		if(KeyBinding.get(ALTERNATIVECONTROLS).held(status))
			{
			axisx=KeyBinding.get(AXIS_ALTPANX).getAxis(status);
			axisy=KeyBinding.get(AXIS_ALTPANY).getAxis(status);
			rotcx=KeyBinding.get(AXIS_ALTROTX).getAxis(status);
			rotcy=KeyBinding.get(AXIS_ALTROTY).getAxis(status);
			
			if(KeyBinding.get(ALT_FORWARD).held(status))axisz=0.5f;
			if(KeyBinding.get(ALT_BACKWARD).held(status))axisz=-0.5f;
			
			//update=true;
			}
		else
			{
			rotx=KeyBinding.get(AXIS_ROTX).getAxis(status);
			roty=KeyBinding.get(AXIS_ROTY).getAxis(status);
			rotz=KeyBinding.get(AXIS_ROTZ).getAxis(status);
			axisz=KeyBinding.get(AXIS_PANZ).getAxis(status);
			
		//	update=true;
			}
		
		if(axisx!=0 || axisy!=0 || axisz!=0 || rotx!=0 || roty!=0 || rotz!=0 || rotcx!=0 || rotcy!=0 || rotcz!=0)
			update=true;
		
		if(update)
			{
			
//			view.camera.rotateCamera(-dy/400.0, -dx/400.0, 0);
			
			view.pan(axisx*axismulxy, -axisy*axismulxy, axisz*axismulz, false); //true
			view.repaint();
			view.camera.rotateCenter(-rotx*rotmulxy, -roty*rotmulxy, rotz*rotmulz); 
			view.camera.rotateCamera(rotcx*rotmulxy, rotcy*rotmulxy, rotcz*rotmulz); 
			}
		}


	public void bindKeyPerformed(JInputManager.EvJinputButtonEvent e)
		{
		if(KeyBinding.get(KEY_PREVFRAME).typed(e))
			frameControl.stepBack(new EvDecimal(1.0));	
		if(KeyBinding.get(KEY_NEXTFRAME).typed(e))
			frameControl.stepForward(new EvDecimal(1.0));	
		}
	
	
	
	public void freeResources(){}

	
	public <E extends EvObject> Collection<E> getVisibleObjects(Class<E> c)
		{
		List<E> v=new LinkedList<E>();
		EvContainer metadata=getSelectedData();
		if(metadata!=null)
			{
			for(E ob:metadata.getObjects(c))
				if(showObject(ob))
					v.add(ob);
			}
		else
			if(EV.debugMode)
				System.out.println("No meta");
		return v;
		}

	
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new ModelWindowBasic());
		EV.personalConfigLoaders.put("modelwindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					Rectangle r=BasicWindow.getXMLbounds(e);
					ModelWindow m=new ModelWindow(r);
					m.setPersonalConfig(e);
					}
				catch (Exception e1)
					{
					e1.printStackTrace();
					}
				}
			public void savePersonalConfig(Element e){}
			});
		}
	
	}
