package evplugin.modelWindow;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import evplugin.basicWindow.*;
import evplugin.consoleWindow.*;
import evplugin.ev.*;
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
			public void loadPersonalConfig(Vector<String> arg)
				{
				new ModelWindow(
						Integer.parseInt(arg.get(1)), Integer.parseInt(arg.get(2)),
						Integer.parseInt(arg.get(3)),Integer.parseInt(arg.get(4)));
				}
			public String savePersonalConfig(){return "";}
			});
		}
	
	public static Vector<ModelWindowExtension> modelWindowExtensions=new Vector<ModelWindowExtension>();

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private int mouseLastX, mouseLastY;
	
	public final Vector<ModelWindowHook> modelWindowHooks=new Vector<ModelWindowHook>();
	
	public final ModelView view;
	public final FrameControlModel frameControl;
	public final MetaCombo metaCombo=new MetaCombo(null,false);
	public JButton buttonCenter=new JButton("Center");
	
	private JMenu menuModel=new JMenu("ModelWindow");
	
	private JMenu miView=new JMenu("Default views");
	private JMenuItem miViewFront=new JMenuItem("Front");
	private JMenuItem miViewBack=new JMenuItem("Back");
	private JMenuItem miViewTop=new JMenuItem("Top");
	private JMenuItem miViewBottom=new JMenuItem("Bottom");
	private JMenuItem miViewLeft=new JMenuItem("Left");
	private JMenuItem miViewRight=new JMenuItem("Right");

	public JCheckBoxMenuItem miShowAllNucNames=new JCheckBoxMenuItem("Show all nuc names"); //move
	public JCheckBoxMenuItem miShowSelectedNucNames=new JCheckBoxMenuItem("Show names of select nuc"); //move
	public JCheckBoxMenuItem miShowGrid=new JCheckBoxMenuItem("Show grid",true); 

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
		
		for(ModelWindowExtension e:modelWindowExtensions)
			e.newModelWindow(this);
		
		//Listeners
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		view.addKeyListener(this);
		view.addMouseWheelListener(this);
		view.setFocusable(true);
		setFocusable(true);
		addKeyListener(this);

		addMenubar(menuModel);
		menuModel.add(miView);
		miView.add(miViewFront);
		miView.add(miViewBack);
		miView.add(miViewTop);
		miView.add(miViewBottom);
		miView.add(miViewLeft);
		miView.add(miViewRight);
		menuModel.add(miShowAllNucNames);
		menuModel.add(miShowSelectedNucNames);
		menuModel.add(miShowGrid);
		
		
		
		miViewTop.addActionListener(this);
		miViewLeft.addActionListener(this);
		miViewFront.addActionListener(this);
		miViewBack.addActionListener(this);
		miViewBottom.addActionListener(this);
		miViewRight.addActionListener(this);
		
		miShowAllNucNames.addActionListener(this);
		miShowGrid.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(view,BorderLayout.CENTER);
		
		JPanel bottom=new JPanel(new GridBagLayout());
		add(bottom,BorderLayout.SOUTH);

		
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
		
		bottom.add(frameControl,constrFrame);
		bottom.add(buttonCenter,constrCenter);
		bottom.add(metaCombo,constrCombo);
		
		buttonCenter.addActionListener(this);
		metaCombo.addActionListener(this);
		
		
		
		//Window overall things
		setTitle(EV.programName+" Model Window");
		pack();
		setVisible(true);
		setBounds(x,y,w,h);
		view.showGrid=miShowGrid.isSelected();
		}
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public String windowPersonalSettings()
		{
		Rectangle r=getBounds();
		return "modelwindow "+r.x+" "+r.y+" "+r.width+" "+r.height+";";
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
		else if(e.getSource()==miShowAllNucNames)
			{
			view.repaint();
			//totally misplaced
			}
		else if(e.getSource()==miShowSelectedNucNames)
			{
			view.repaint();
			//totally misplaced
			}
		else if(e.getSource()==miShowGrid)
			{
			view.showGrid=miShowGrid.isSelected();
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
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
		{
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
			ConsoleWindow.focusConsole(this, view);
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
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
		{
		//Clicking a nucleus selects it
		if(SwingUtilities.isLeftMouseButton(e))
			{
			NucLineage.mouseSelectNuc(NucLineage.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
			}
		
		
		
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
		{
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
		{
		mouseLastX = e.getX();
		mouseLastY = e.getY();
		view.mouseX=mouseLastX;
		view.mouseY=mouseLastY;
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
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e)
		{
		view.mouseX=-1;
		view.mouseY=-1;
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e)
		{
		final int dx=e.getX()-mouseLastX;
		final int dy=e.getY()-mouseLastY;

		if((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK)!=0)
			{
			//TODO: left=also move center
			view.pan(-dx,+dy,0);
			}
		else if((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0)
			{
			//TODO: left=also move center
			view.pan(0,0,dy);
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
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e)
		{
		view.mouseX=e.getX();
		view.mouseY=e.getY();
		view.repaint();
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
				view.camera.rotateCamera(0, 0, e.getUnitsToScroll()/300.0);
			else
				view.pan(0,0,e.getUnitsToScroll());
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
		if(frameControl!=null && view!=null)
			{
			view.frame=frameControl.getFrame();
			view.repaint();
			}
		}
	

	}
