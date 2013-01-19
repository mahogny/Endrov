package endrov.hardwareFrivolous.devices;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import endrov.gui.EvSwingUtil;
import endrov.gui.window.EvBasicWindow;
import endrov.hardware.EvHardware;
import endrov.hardwareFrivolous.FrivolousModel;

/**
 * Configuration window
 */
class FrivolousConfigWindow extends JFrame implements ActionListener 
	{
	private static final long serialVersionUID = 1L;
	private JButton bStartStop;
	private JTextField tfFileName=new JTextField();
	private JButton bBrowse=new JButton("Browse");
	
	private FrivolousDeviceProvider frivolous;
	
	public FrivolousConfigWindow(FrivolousDeviceProvider frivolous)
		{
		super("Frivolous configuration");
		this.frivolous=frivolous;

		tfFileName.setText(FrivolousModel.getStandardExperiment().getAbsolutePath());

		bStartStop = new JButton((frivolous.model==null ? "Start" : "Stop"));
		bStartStop.addActionListener(this);

		bBrowse.addActionListener(this);
		
		setLayout(new GridLayout(1,1));
		add(EvSwingUtil.layoutEvenVertical(
				EvSwingUtil.layoutLCR(new JLabel("Experiment"), tfFileName, bBrowse),
				bStartStop
				));
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
		} 

	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bStartStop)
			{
			if (frivolous.model==null)
				{
				File f=new File(tfFileName.getText());
				frivolous.model = new FrivolousModel(f);

				FrivolousCamera cam = new FrivolousCamera(frivolous);
				FrivolousXYStage stageXY = new FrivolousXYStage(frivolous);
				FrivolousZStage stageZ = new FrivolousZStage(frivolous);
				cam.seqAcqThread.start();
				frivolous.hw.put("cam", cam);
				frivolous.hw.put("xystage", stageXY);
				frivolous.hw.put("zstage", stageZ);
				frivolous.hw.put("autofocus", new FrivolousAutofocus(stageZ));

				EvHardware.updateAvailableDevices();
				EvBasicWindow.updateWindows();
				
				bStartStop.setText("Stop");
				}
			else
				{
				frivolous.model.stop();
				frivolous.model = null;
				bStartStop.setText("Start");
				}
			}
		else if(e.getSource()==bBrowse)
			{
			JFileChooser fc=new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int ret=fc.showOpenDialog(this);
			if(ret==JFileChooser.APPROVE_OPTION)
				{
				File f=fc.getSelectedFile();
				tfFileName.setText(f.getAbsolutePath());
				}
			}
		}

	}
