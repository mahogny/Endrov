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

import endrov.basicWindow.BasicWindow;
import endrov.hardwareFrivolous.FrivolousModel;
import endrov.util.EvSwingUtil;

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
				FrivolousStage stage = new FrivolousStage(frivolous);
				cam.seqAcqThread.start();
				frivolous.hw.put("cam", cam);
				frivolous.hw.put("stage", stage);
				frivolous.hw.put("autofocus", new FrivolousAutofocus(stage));

				BasicWindow.updateWindows();
				
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
