package endrov.recording.camWindow;


import java.awt.BorderLayout;
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
import endrov.recording.CameraImage;
import endrov.recording.HWCamera;

/**
 * Camera live-feed window
 * @author Johan Henriksson 
 */
public class CamWindow extends BasicWindow
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
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
				JMenuItem mi=new JMenuItem("Camera Window",new ImageIcon(getClass().getResource("tangoCamera.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new CamWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});
		
		}
	
	public static TreeMap<String,Extension> extensions=new TreeMap<String,Extension>();
	public static void addMicroscopeWindowExtension(String name, Extension e)
		{
		extensions.put(name,e);
		}

	/******************************************************************************************************
	 *                               Extension                                                            *
	 *****************************************************************************************************/

	public static interface Extension
		{
		public JComponent addControls();
		
		}

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	private CamWindow This=this;
	private JComboBox mcombo=new JComboBox();
	private BufferedImage fromCam=null;
	
	private JPanel drawArea=new JPanel(){
		static final long serialVersionUID=0;
		protected void paintComponent(Graphics g)
			{
			BufferedImage im=This.fromCam;
			if(im!=null)
				{
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
	
	


	private ActionListener listener=new ActionListener()
		{
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==This.timer)
				{
				//this does not work later. have to synchronize all calls for an image
				//so all targets gets it.
				
				DevicePath camname=(DevicePath)This.mcombo.getSelectedItem();
				if(camname!=null)
					{
					HWCamera cam=(HWCamera)EvHardware.getDevice(camname);
					CameraImage cim=cam.snap();
					BufferedImage im=This.fromCam=cim.getPixels().quickReadOnlyAWT();
					Rectangle dbounds=This.drawArea.getBounds();
					if(im!=null)
						if(im.getWidth()!=dbounds.getWidth() ||
								im.getHeight()!=dbounds.getHeight())
							{
							Rectangle bounds=This.getBoundsEvWindow();
							This.setBoundsEvWindow(new Rectangle(
									bounds.x,bounds.y,
									(int)(bounds.getWidth()+(im.getWidth()-dbounds.getWidth())),
									(int)(bounds.getHeight()+(im.getHeight()-dbounds.getHeight()))
									));
							}

					
					//Here one could mark totally black or saturated areas
						
					drawArea.repaint();
					//System.out.println("do repaint");
					}

				
				
				}
			}
		};
		
	//Update timer, busy loop for now. replace later by camera event listener	
	javax.swing.Timer timer=new javax.swing.Timer(10,listener);



	public CamWindow()
		{
		this(new Rectangle(400,300));
		}
	
	public CamWindow(Rectangle bounds)
		{

		
		
		mcombo=new JComboBox(new Vector<DevicePath>(EvHardware.getDeviceMap(HWCamera.class).keySet()));

		
		
		setLayout(new BorderLayout());
		add(mcombo,BorderLayout.SOUTH);
		add(drawArea,BorderLayout.CENTER);
		
		
		
		//Window overall things
		setTitleEvWindow("Camera Control");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		timer.start();
		setResizable(false);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		
		}

	public void loadedFile(EvData data){}

	public void windowPersonalSettings(Element e)
		{
		
		} 
	public void freeResources()
		{
		timer.stop();
		}
	
	
	}
