package endrov.recording.recmetMultidim;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;

/**
 * Multi-dimension acquisition
 * @author Johan Henriksson 
 */
public class RecWindowMultiDim extends BasicWindow
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
					JMenuItem mi=new JMenuItem("Multi-dim acq Window",new ImageIcon(getClass().getResource("tangoCamera.png")));
					mi.addActionListener(this);
					w.addMenuWindow(mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowMultiDim();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		}
	
	

	public RecWindowMultiDim()
		{
		this(new Rectangle(400,300));
		}
	
	public RecWindowMultiDim(Rectangle bounds)
		{
		///////////////// Z settings ///////////////////////////////////////
		SpinnerSimpleEvDecimal spStartZ=new SpinnerSimpleEvDecimal();
		SpinnerSimpleEvDecimal spEndZ=new SpinnerSimpleEvDecimal();
		JRadioButton rbTotSlices=new JRadioButton("#Z");
		rbTotSlices.setToolTipText("Specify number of slices in Z-direction");
		JRadioButton rbDZ=new JRadioButton("dZ");
		rbDZ.setToolTipText("Specify spacing beetween slices in micrometer");
		JRadioButton rbOneZ=new JRadioButton("Once",true);
		rbDZ.setToolTipText("Do not acquire multiple slices");
		ButtonGroup bgDZgroup=new ButtonGroup();
		bgDZgroup.add(rbTotSlices);
		rbTotSlices.add(rbDZ);
		rbTotSlices.add(rbOneZ);
		JButton bSetStartZ=new JButton("Set");
		JButton bSetEndZ=new JButton("Set");
		SpinnerSimpleInteger spNumZ=new SpinnerSimpleInteger();
		SpinnerSimpleEvDecimal spDZ=new SpinnerSimpleEvDecimal();
		JComponent zpanel=
		EvSwingUtil.withTitledBorder("Slices",
			EvSwingUtil.layoutTableCompactWide(
					new JLabel("Z start"), EvSwingUtil.layoutLCR(null, spStartZ, bSetStartZ),
					new JLabel("Z end"), EvSwingUtil.layoutLCR(null, spEndZ, bSetEndZ),
					rbTotSlices, spNumZ,
					rbDZ, spDZ,
					rbOneZ, new JLabel("")
			));
		
		
		
		///////////////// Time settings ///////////////////////////////////////
		
		
		SpinnerSimpleEvFrame spDt=new SpinnerSimpleEvFrame();
		spDt.setFrame("1s");
		JRadioButton rbNumFrames=new JRadioButton("#t");
		JRadioButton rbTotT=new JRadioButton("Tot.t");
		JRadioButton rbOneT=new JRadioButton("Once",true);
		rbNumFrames.setToolTipText("Specify number of frames to capture");
		rbTotT.setToolTipText("Specify total acquisition time");
		rbOneT.setToolTipText("Acquire a single time point");
		ButtonGroup bgDtGroup=new ButtonGroup();
		bgDtGroup.add(rbNumFrames);
		bgDtGroup.add(rbTotT);
		bgDtGroup.add(rbOneT);
		SpinnerSimpleInteger spNumFrames=new SpinnerSimpleInteger();
		SpinnerSimpleEvFrame spTotTime=new SpinnerSimpleEvFrame();
		
		JComponent tpanel=
		EvSwingUtil.withTitledBorder("Time",
			EvSwingUtil.layoutTableCompactWide(
					new JLabel("dt"), spDt,
					rbNumFrames, spNumFrames,
					rbTotT, spTotTime,
					rbOneT, new JLabel("")
			));
		
		
		
		///////////////// Order settings ///////////////////////////////////////
		

		///////////////// Position settings ///////////////////////////////////////


		///////////////// Channel settings ///////////////////////////////////////

		
		///////////////// Acquire ///////////////////////////////////////

		
		JButton bAcquire=new JImageButton(BasicIcon.iconButtonRecord,"Start acquisition");
		JLabel labelToAcq=new JLabel("");
		JLabel labelAcqStat=new JLabel("");
		JComponent pAcq=EvSwingUtil.withTitledBorder("Acquire",
				EvSwingUtil.layoutCompactVertical(
					EvSwingUtil.layoutTableCompactWide(
							new JLabel("To acquire:"), labelToAcq,
							new JLabel("Progress:"), labelAcqStat),
							bAcquire
							)
				);
		
		
		
		
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutCompactVertical(zpanel,tpanel,pAcq),
				BorderLayout.CENTER);
//		add(new JLabel(""),BorderLayout.CENTER);
		
		
		
		
		
		//Window overall things
		setTitleEvWindow("Multidimensional acquisition");
		packEvWindow();
		setVisibleEvWindow(true);
//		setBoundsEvWindow(bounds);
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
		}
	
	public static void main(String[] args)
		{
		new RecWindowMultiDim();
		
		}
	
	}
