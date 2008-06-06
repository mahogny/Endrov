package evplugin.modelWindow;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.jdom.*;

import OSTdaemon.Xml;

import evplugin.basicWindow.*;
import evplugin.consoleWindow.*;
import evplugin.data.EvData;
import evplugin.data.EvObject;
import evplugin.ev.*;
import evplugin.keyBinding.*;
import evplugin.keyBinding.NewBinding.EvBindKeyEvent;
import evplugin.keyBinding.NewBinding.EvBindStatus;
import evplugin.modelWindow.basicExt.CrossHandler;

//TODO drag and drop of a file with # in the name fails on linux

/**
 * Model window - displays a navigatable 3d model
 * @author Johan Henriksson
 */
public class ModelWindow extends BasicWindow
		implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, ChangeListener, NewBinding.EvBindListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	
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
	
	//public JProgressBar progress=new JProgressBar(0,1000);
	private JPanel progress=new JPanel(); 
	
	public final ModelView view;
	public final FrameControlModel frameControl;
	private final MetaCombo metaCombo=new MetaCombo(null,false);
	private final JButton buttonCenter=new JButton("Center");
	private final EvHidableSidePane sidePanelSplitPane;
	
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
		updateToolPanels();
		
		sidePanelSplitPane = new EvHidableSidePane(view, toolPanel, true);
		
		setLayout(new BorderLayout());
		add(sidePanelSplitPane,BorderLayout.CENTER);
		add(bottomPanel,BorderLayout.SOUTH);

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
		
		//TODO dangerous, might be called before constructed
		NewBinding.attachBindAxisListener(this);
		}
	
	
	
	/**
	 * Clear tool panels and readd items
	 */
	public void updateToolPanels()
		{
		sidePanelItems.clear();
		bottomPanelItems.clear();
		bottomPanelItems.add(bottomMain);
		bottomPanelItems.add(progress); //Since it belongs to voxel, maybe keep it there?
		for(ModelWindowHook h:modelWindowHooks)
			h.fillModelWindomMenus();
		
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
	public void windowPersonalSettings(Element root)
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
			windowPersonalSettings(root);
			try
				{
				String out=Xml.xmlToString(new Document((Element)root.getChild("modelwindow").clone()));
				setClipBoardString(out);
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
				setPersonalConfig(Xml.stringToXml(getClipBoardString()));
				}
			catch (Exception e1)
				{
				e1.printStackTrace();
				}
			repaint();
			}
		}
	
	/**
	 * Get string from clipboard, never null
	 */
	public static String getClipBoardString()
		{
		try
			{
			return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
			}
		catch(Exception e2)
			{
			System.out.println("Failed to get text from clipboard");
			}
		return "";
		}
	
	
	public static void setClipBoardString(String s)
		{
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), 
				new ClipboardOwner(){
				public void lostOwnership(Clipboard aClipboard, Transferable aContents){}
				});
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
		if(e.getKeyChar()=='a')
			frameControl.stepBack(1.0);
		else if(e.getKeyChar()=='d')
			frameControl.stepForward(1.0);	
		else if(e.getKeyChar()=='w')
			frameControl.stepForward(0.2);	
		else if(e.getKeyChar()=='s')
			frameControl.stepBack(0.2);
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
		objectDisplayList.setData(metaCombo.getMeta());
		objectDisplayList.updateList();
		for(ModelWindowHook h:modelWindowHooks)
			h.datachangedEvent();
		if(frameControl!=null && view!=null)
			{
			view.frame=frameControl.getFrame();
			view.repaint(); //TODO modw repaint
			}
		}

	/**
	 * Get the EvData currently selected
	 */
	public EvData getSelectedData()
		{
		return metaCombo.getMeta();
		}
	

	/**
	 * Called whenever a new file is loaded, and this window should change active set
	 */
	public void loadedFile(EvData data)
		{
		metaCombo.setMeta(data);
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
			SwingUtilities.invokeLater(new Runnable(){public void run(){pbar.setValue(v);}});
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
		private JProgressBar pbar=new JProgressBar(0,1000);
		}
	/**
	 * Update progressbar panel
	 */
	private void updateProgressMeter()
		{
		synchronized(progressMeters)
			{
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
	private static final int AXIS_PAN_X=KeyBinding.register(new KeyBinding("Model Window","Axis pan X",new KeyBinding.TypeJInput("x",0)));
	private static final int AXIS_PAN_Y=KeyBinding.register(new KeyBinding("Model Window","Axis pan Y",new KeyBinding.TypeJInput("y",0)));
	private static final int AXIS_PAN_Z=KeyBinding.register(new KeyBinding("Model Window","Axis pan z",new KeyBinding.TypeJInput("rz",0)));
	private static final int AXIS_ROT=KeyBinding.register(new KeyBinding("Model Window","Axis rot",new KeyBinding.TypeJInput("z",0)));


	public void bindAxisPerformed(EvBindStatus status)
		{
		float axismulxy=10;
		float axismulz=30;
		float rotmulxy=0.05f;
		float rotmulz=0.1f;
		float axisx=KeyBinding.get(AXIS_PAN_X).getAxis(status);
		float axisy=KeyBinding.get(AXIS_PAN_Y).getAxis(status);
		float axisz=KeyBinding.get(AXIS_PAN_Z).getAxis(status);
		
		axisx=0;
		axisy=0;
		
		float roty=KeyBinding.get(AXIS_PAN_X).getAxis(status);
		float rotx=KeyBinding.get(AXIS_PAN_Y).getAxis(status);
		
		float rotz=KeyBinding.get(AXIS_ROT).getAxis(status);
		if(axisx!=0 || axisy!=0 || axisz!=0 || rotx!=0 || roty!=0 || rotz!=0) //null? or is 0 good?
			{
			
//			view.camera.rotateCamera(-dy/400.0, -dx/400.0, 0);
			
			view.pan(axisx*axismulxy, -axisy*axismulxy, axisz*axismulz, false); //true
			view.repaint();
			view.camera.rotateCenter(rotx*rotmulxy, roty*rotmulxy, rotz*rotmulz); //vs rotateCamera
			System.out.println("hej "+axisx+" "+axisy);
			}
		}


	public void bindKeyPerformed(EvBindKeyEvent e, EvBindStatus status)
		{
		// TODO Auto-generated method stub
		
		}
	
	
	
	
	
	
	
	
	
	
	
	
	}
