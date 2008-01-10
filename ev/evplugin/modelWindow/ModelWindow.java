package evplugin.modelWindow;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdom.*;

import evplugin.basicWindow.*;
import evplugin.consoleWindow.*;
import evplugin.ev.*;
import evplugin.imageset.EmptyImageset;
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
					m.setShowGrid(e.getAttribute("showGrid").getBooleanValue());
					m.frameControl.setGroup(e.getAttribute("group").getIntValue());
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

	public JCheckBoxMenuItem miShowAllNucNames=new JCheckBoxMenuItem("Show all nuclei names"); //move
	public JCheckBoxMenuItem miShowSelectedNucNames=new JCheckBoxMenuItem("Show names of selected nuclei"); //move
	public JMenuItem miShowSelectedNuc=new JMenuItem("Show all selected nuclei"); //move
	public JMenuItem miHideSelectedNuc=new JMenuItem("Hide all selected nuclei"); //move
	
	
	public JCheckBoxMenuItem miShowGrid=new JCheckBoxMenuItem("Show grid"); 

	
	
	private static class OneImageChannel extends JPanel
		{
		static final long serialVersionUID=0;
		private static ImageIcon iconLabelFS=new ImageIcon(ModelWindow.class.getResource("labelFS.png"));
		JButton bFs=new JButton(iconLabelFS);
		ChannelCombo channelCombo=new ChannelCombo(new EmptyImageset(),true);
		
		public OneImageChannel(String name)
			{
			setLayout(new BorderLayout());
			add(new JLabel(name),BorderLayout.WEST);
			add(channelCombo,BorderLayout.CENTER);
			add(bFs,BorderLayout.EAST);
			}
		
		
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
		menuModel.add(miShowSelectedNuc);
		menuModel.add(miHideSelectedNuc);
		
		menuModel.add(miShowGrid);
		
		
		
		miViewTop.addActionListener(this);
		miViewLeft.addActionListener(this);
		miViewFront.addActionListener(this);
		miViewBack.addActionListener(this);
		miViewBottom.addActionListener(this);
		miViewRight.addActionListener(this);
		
		miShowAllNucNames.addActionListener(this);
		miShowSelectedNuc.addActionListener(this);
		miHideSelectedNuc.addActionListener(this);

		miShowGrid.addActionListener(this);

		buttonCenter.addActionListener(this);
		metaCombo.addActionListener(this);

		//Put GUI together
		setLayout(new BorderLayout());
		add(view,BorderLayout.CENTER);

		JPanel bottomTotal=new JPanel(new GridLayout(2,1));
		add(bottomTotal,BorderLayout.SOUTH);

		JPanel bottomMain=new JPanel(new GridBagLayout());
		bottomTotal.add(bottomMain);
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

		
		JPanel bottomChannels=new JPanel(new GridLayout(1,3));
		bottomTotal.add(bottomChannels);
		
		OneImageChannel icR=new OneImageChannel("R");
		OneImageChannel icG=new OneImageChannel("G");
		OneImageChannel icB=new OneImageChannel("B");
		bottomChannels.add(icR);
		bottomChannels.add(icG);
		bottomChannels.add(icB);
		
		//View settings
		setShowGrid(true);
		
		//Window overall things
		setTitle(EV.programName+" Model Window");
		pack();
		setVisible(true);
		setBounds(x,y,w,h);
		view.showGrid=miShowGrid.isSelected();
		}
	
	/**
	 * View setting: display grid?
	 */
	public void setShowGrid(boolean b)
		{
		miShowGrid.setSelected(b);
		view.showGrid=b;
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
		e.setAttribute("showGrid",""+miShowGrid.isSelected());
		e.setAttribute("group",""+frameControl.getGroup());
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
		else if(e.getSource()==miShowSelectedNuc)
			{
			for(evplugin.nuc.NucPair p:NucLineage.selectedNuclei)
				NucLineage.hiddenNuclei.remove(p);
			view.repaint();
			//totally misplaced
			}
		else if(e.getSource()==miHideSelectedNuc)
			{
			for(evplugin.nuc.NucPair p:NucLineage.selectedNuclei)
				NucLineage.hiddenNuclei.add(p);
			view.repaint();
			//totally misplaced
			}
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
				view.pan(0,0,e.getUnitsToScroll()*20.0);
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
	

	public void finalize()
		{
		System.out.println("removing model window");
		}
	
	}
