package evplugin.modelWindow;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jdom.*;

import evplugin.basicWindow.*;
import evplugin.consoleWindow.*;
import evplugin.data.EvData;
import evplugin.data.EvObject;
import evplugin.ev.*;
import evplugin.keyBinding.*;
import evplugin.nuc.NucLineage;

/**
 * Model window - displays a navigatable 3d model
 * @author Johan Henriksson
 */
public class ModelWindow extends BasicWindow
		implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, ChangeListener
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
					int x=e.getAttribute("x").getIntValue();
					int y=e.getAttribute("y").getIntValue();
					int w=e.getAttribute("w").getIntValue();
					int h=e.getAttribute("h").getIntValue();
					ModelWindow m=new ModelWindow(x,y,w,h);
					m.frameControl.setGroup(e.getAttribute("group").getIntValue());
					
					for(ModelWindowHook hook:m.modelWindowHooks)
						hook.readPersonalConfig(e);

					m.sidePanelSplitPane.setPanelVisible(e.getAttribute("sidePanelVisible").getBooleanValue());
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


	
	private int mouseLastX, mouseLastY;
	
	public final Vector<ModelWindowHook> modelWindowHooks=new Vector<ModelWindowHook>();	
	public final Vector<JComponent> sidePanelItems=new Vector<JComponent>();
	private final JPanel sidePanel=new JPanel(new GridBagLayout());
	public final Vector<JComponent> bottomPanelItems=new Vector<JComponent>();
	private final JPanel bottomPanel=new JPanel(new GridBagLayout());
	private JPanel bottomMain=new JPanel(new GridBagLayout());
	
	public final ModelView view;
	public final FrameControlModel frameControl;
	public final MetaCombo metaCombo=new MetaCombo(null,false);
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

	private ObjectDisplayList objectDisplayList=new ObjectDisplayList();
	
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
		this(0,50,1000,800);
		}
	
	/**
	 * Make a new window at some location
	 */
	public ModelWindow(int x, int y, int w, int h)
		{
		view=new ModelView(this);
		frameControl=new FrameControlModel(this);

		//Add hooks
		for(ModelWindowExtension e:modelWindowExtensions)
			e.newModelWindow(this);
		
		//Listeners		
		view.addMouseMotionListener(this);
		view.addMouseListener(this);
		view.addMouseWheelListener(this);
		view.addKeyListener(this);
		view.setFocusable(true);
		setFocusable(true);
		addKeyListener(this);
		objectDisplayList.addChangeListener(this);

		addMenubar(menuModel);
		menuModel.add(miView);
		miView.add(miViewFront);
		miView.add(miViewBack);
		miView.add(miViewTop);
		miView.add(miViewBottom);
		miView.add(miViewLeft);
		miView.add(miViewRight);
		
		miViewTop.addActionListener(this);
		miViewLeft.addActionListener(this);
		miViewFront.addActionListener(this);
		miViewBack.addActionListener(this);
		miViewBottom.addActionListener(this);
		miViewRight.addActionListener(this);
		buttonCenter.addActionListener(this);
		metaCombo.addActionListener(this);

	
		
		//Put GUI together
		//JPanel bottomTotal=new JPanel(new GridLayout(2,1));

		
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

		updateToolPanels();
		dataChangedEvent();
		
//		attachDragAndDrop(this); //TODO EXPERIMENTAL! FIX LINUX BEFORE RELEASE!
		
		//Window overall things
		setTitleEvWindow("Model Window");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(x,y,w,h);
		}
	
	
	
	/**
	 * Clear tool panels and readd items
	 */
	public void updateToolPanels()
		{
		sidePanelItems.clear();
		bottomPanelItems.clear();
		bottomPanelItems.add(bottomMain);
		for(ModelWindowHook h:modelWindowHooks)
			h.fillModelWindomMenus();
		
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
		Rectangle r=getBounds();
		Element e=new Element("modelwindow");
		e.setAttribute("x", ""+r.x);
		e.setAttribute("y", ""+r.y);
		e.setAttribute("w", ""+r.width);
		e.setAttribute("h", ""+r.height);
		e.setAttribute("group",""+frameControl.getGroup());
		e.setAttribute("sidePanelVisible",""+sidePanelSplitPane.getPanelVisible());
		
		for(ModelWindowHook hook:modelWindowHooks)
			hook.savePersonalConfig(e);
		
		root.addContent(e);
		}

	

	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==metaCombo)
			view.meta=metaCombo.getMeta();
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
		}
	
	/**
	 * Set a preset view
	 */
	private void setPresetView(double x, double y, double z)
		{
		view.camera.setRotation(x, y, z);
		view.autoCenter();
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
		{
		if(!ScriptBinding.runScriptKey(e))
			{
			if(KeyBinding.get(KEY_GETCONSOLE).typed(e))
				ConsoleWindow.focusConsole(this, view);
			}
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e)
		{
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
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
		//Clicking a nucleus selects it
		if(SwingUtilities.isLeftMouseButton(e))
			{
			NucLineage.mouseSelectNuc(NucLineage.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
			}
//		else if(SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e))
		view.requestFocus();
		}
	
	public void mousePressed(MouseEvent e)
		{
		mouseLastX = e.getX();
		mouseLastY = e.getY();
		view.mouseX=mouseLastX;
		view.mouseY=mouseLastY;
		}
	public void mouseReleased(MouseEvent e)
		{
		}

	
	public void mouseEntered(MouseEvent e)
		{
		}
	public void mouseExited(MouseEvent e)
		{
		view.mouseX=-1;
		view.mouseY=-1;
		}
	
	public void mouseMoved(MouseEvent e)
		{
		view.mouseX=e.getX();
		view.mouseY=e.getY();
		view.repaint();
		}
	public void mouseDragged(MouseEvent e)
		{
		final int dx=e.getX()-mouseLastX;
		final int dy=e.getY()-mouseLastY;

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
		dataChangedEvent();

		mouseLastX = e.getX();
		mouseLastY = e.getY();
		view.mouseX=mouseLastX;
		view.mouseY=mouseLastY;
		}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		dataChangedEvent();
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
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		metaCombo.updateList();
		view.meta=metaCombo.getMeta();
		objectDisplayList.setData(metaCombo.getMeta());
		objectDisplayList.updateList();
		for(ModelWindowHook h:modelWindowHooks)
			h.datachangedEvent();
		if(frameControl!=null && view!=null)
			{
			view.frame=frameControl.getFrame();
			view.repaint();
			}
		}
	


	public void loadedFile(EvData data)
		{
		metaCombo.setMeta(data);
		}
	
	public void finalize()
		{
		System.out.println("removing model window");
		}
	
	}
