package endrov.nuc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.EvComboObjectOne;
import endrov.data.EvData;
import endrov.imageset.EvChannel;
import endrov.imageset.EvComboChannel;
import endrov.imageset.Imageset;
import endrov.nuc.integrate.IntegrateExp;
import endrov.util.EvSwingUtil;

/**
 * Dialog to run integrator.
 * Need channel, imset, lineage.
 * able to abort?
 * 
 * BasicWindow would be nice to extend with a more common dialog system that allows macros, and can be headless. Imagej-style.
 * 
 * @author Johan Henriksson
 *
 */
public class NucDialogIntegrate extends BasicWindow implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	
	public EvComboChannel comboChannel=new EvComboChannel(null, false);
	public EvComboObjectOne<NucLineage> comboLin=new EvComboObjectOne<NucLineage>(new NucLineage(), false, false);
	public JTextField tfExpName=new JTextField("exp");
	private JLabel lStatus=new JLabel("");
	
	private JButton bIntegrate=new JButton("Start");
	
	public NucDialogIntegrate()
		{
		setLayout(new BorderLayout());
		
		add(EvSwingUtil.layoutTableCompactWide(
				new JLabel("Channel: "), comboChannel,
				new JLabel("Lineage: "), comboLin,
				new JLabel("Exp name: "), tfExpName,
				new JLabel("Status: "), lStatus
				), BorderLayout.CENTER);
		
		add(bIntegrate, BorderLayout.SOUTH);
		
		bIntegrate.addActionListener(this);
		
		setTitleEvWindow("Integreate expression");
		packEvWindow();
		setVisibleEvWindow(true);
		}

	@Override
	public void dataChangedEvent()
		{
		comboChannel.updateList();
		comboLin.updateList();
		}

	@Override
	public void freeResources()
		{
		}

	@Override
	public void loadedFile(EvData data)
		{
		}

	@Override
	public void windowSavePersonalSettings(Element root)
		{
		}

	
	
	private void updateStatus(final String text)
		{
		try
			{
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run()
					{
					lStatus.setText(text);
					}});
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	
	private class Callback extends Thread implements IntegrateExp.IntegratorCallback
		{
		@Override
		public void run()
			{
			NucLineage lin=comboLin.getSelectedObject();
			Imageset imset=comboChannel.getImageset();
			EvChannel ch=comboChannel.getChannel();
			
			//This writes exp immediately - it is not totally thread safe
			IntegrateExp.integrateSingleCell(lin, imset, ch, tfExpName.getText(),	this);
			if(!stop){}
			NucDialogIntegrate.this.currentCallback=null;
			if(!lStatus.getText().contains("Error"))
				updateStatus("");
			bIntegrate.setText("Start");
			}
		
		public boolean stop=false;
		
		public boolean status(IntegrateExp integrator)
			{
			updateStatus(""+integrator.frame+" / "+integrator.ch.getLastFrame());
			return !stop;
			}

		public void fail(Exception e)
			{
			updateStatus("Error: "+e.getMessage());
			}
		}
	
	
	
	private Callback currentCallback=null;
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bIntegrate)
			{
			if(currentCallback!=null)
				currentCallback.stop=true;
			else
				{
				bIntegrate.setText("Stop");
				currentCallback=new Callback();
				currentCallback.start();
				}

			}
		
		}
	
	}
