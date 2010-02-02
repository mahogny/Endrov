/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.camWindow;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.hardware.*;
import endrov.imageset.EvPixels;
import endrov.recording.CameraImage;
import endrov.recording.HWCamera;
import endrov.util.EvSwingUtil;

/**
 * Camera live-feed window
 * @author Johan Henriksson 
 */
public class CamWindow extends BasicWindow implements ActionListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;
	
	/*
	public static TreeMap<String,Extension> extensions=new TreeMap<String,Extension>();
	public static void addMicroscopeWindowExtension(String name, Extension e)
		{
		extensions.put(name,e);
		}
*/
	
	/******************************************************************************************************
	 *                               Extension                                                            *
	 *****************************************************************************************************/

	/*
	public static interface Extension
		{
		public JComponent addControls();
		
		}*/

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	private CamWindow This=this;
	private JComboBox mcombo=new JComboBox();
	
	
	private EvPixels fromCam=null;
	
	/**
	 * Image area. Should show under- and over exposed regions
	 */
	private JPanel drawArea=new JPanel(){
		static final long serialVersionUID=0;
		protected void paintComponent(Graphics g)
			{
			g.setColor(new Color(0.3f, 0.1f, 0.3f));
			g.fillRect(0, 0, getWidth(), getHeight());
			
			EvPixels p=This.fromCam;
			if(p!=null)
				{
				
				
				
				BufferedImage im=This.fromCam.quickReadOnlyAWT();
				
				
				
				//TODO show under and over exposure
				
				g.drawImage(im, 0, 0, null);
				//System.out.println("isrepaint");
				SwingUtilities.invokeLater(new Runnable(){
					public void run()
						{
						//This actually slows it down!
						//This.drawArea.repaint();
						}
				});
				}
			}
	};
	
	

	
	public void actionPerformed(ActionEvent e) 
		{
		if(e.getSource()==This.timer && This.tUpdateView.isSelected())
			{
			snapCamera();
			}
		}
		
		
	/**
	 * Take one picture from the camera	
	 */
	private void snapCamera()
		{
		//this does not work later. have to synchronize all calls for an image
		//so all targets gets it.
		
		DevicePath camname=(DevicePath)This.mcombo.getSelectedItem();
		if(camname!=null)
			{
			HWCamera cam=(HWCamera)EvHardware.getDevice(camname);
			CameraImage cim=cam.snap();
			//EvPixels oldp=fromCam;
			//EvPixels p=
			fromCam=cim.getPixels();
			/*
					
			//Update size of this window if camera area size changes
			if(oldp!=null && p!=null)
				if(oldp.getWidth()!=p.getWidth() ||
						oldp.getHeight()!=p.getHeight())
					{
					//Might not work anymore!
					Rectangle bounds=getBoundsEvWindow();
					setBoundsEvWindow(new Rectangle(
							bounds.x,bounds.y,
							(int)(bounds.getWidth()+(oldp.getWidth()-p.getWidth())),
							(int)(bounds.getHeight()+(oldp.getHeight()-p.getHeight()))
							));
					}
*/
			
			//Here one could mark totally black or saturated areas
				
			drawArea.repaint();
			//System.out.println("do repaint");
			}
		
		}
		
		

	//Update timer, busy loop for now. replace later by camera event listener	
	private javax.swing.Timer timer=new javax.swing.Timer(10,this);

	private JCheckBox tAutoRange=new JCheckBox("Auto", true);
	private JButton bSetFullRange=new JButton("Full");
	private CameraHistogramView histoView=new CameraHistogramView();
	private JCheckBox tUpdateView=new JCheckBox("Update", true);
	private JCheckBox tLive=new JCheckBox("Live", false);
	private JButton bSnap=new JButton("Snap");
	private JCheckBox tHistoView=new JCheckBox("Histo", true);



	public CamWindow()
		{
		this(new Rectangle(400,300));
		}

	
	public CamWindow(Rectangle bounds)
		{

		
		
		mcombo=new JComboBox(new Vector<DevicePath>(EvHardware.getDeviceMap(HWCamera.class).keySet()));

		
		
		tLive.setToolTipText("Continuously take pictures");
		bSnap.setToolTipText("Manually take a picture and update");
		tHistoView.setToolTipText("Show histogram controls");
		tAutoRange.setToolTipText("Automatically adjust visible range");
		bSetFullRange.setToolTipText("Set visible range of all of camera range");
		
		JPanel pHisto=new JPanel(new BorderLayout());
		pHisto.setBorder(BorderFactory.createTitledBorder("Range adjustment"));
		pHisto.add(
				EvSwingUtil.layoutCompactVertical(tAutoRange, bSetFullRange),
				BorderLayout.WEST);
		pHisto.add(histoView, BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		
		
		add(EvSwingUtil.layoutCompactHorizontal(mcombo, bSnap, tLive, tUpdateView, tHistoView)
				,BorderLayout.SOUTH);
		add(drawArea,BorderLayout.CENTER);
		
		add(pHisto,BorderLayout.NORTH);
		
		
		
		
		
		//Window overall things
		setTitleEvWindow("Camera Control");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		timer.start();
		//setResizable(false);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		
		} 
	public void freeResources()
		{
		timer.stop();
		}
	

	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new Hook());
				}
			class Hook implements BasicWindowHook, ActionListener
			{
			public void createMenus(BasicWindow w)
				{
				JMenuItem mi=new JMenuItem("Live Camera",new ImageIcon(getClass().getResource("tangoCamera.png")));
				mi.addActionListener(this);
				BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new CamWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});
		
		}
	
	

	public static void main(String[] args)
		{
		
		new CamWindow();
		
		}
	}
