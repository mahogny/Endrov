package endrov.typeLineage.expression;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.EvComboObjectOne;
import endrov.gui.window.EvBasicWindow;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.gui.EvComboChannel;
import endrov.typeLineage.Lineage;

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
public class ParticleDialogIntegrate extends EvBasicWindow implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	
	public EvComboChannel comboChannel=new EvComboChannel(false, false);
	public EvComboObjectOne<Lineage> comboLin=new EvComboObjectOne<Lineage>(new Lineage(), false, false);
	public JTextField tfExpName=new JTextField("exp");
	public JCheckBox cbUseRadius=new JCheckBox();
	
	private JLabel lStatus=new JLabel("");
	private JButton bIntegrate=new JButton("Start");
	
	public ParticleDialogIntegrate()
		{
		setLayout(new BorderLayout());
		
		add(EvSwingUtil.layoutTableCompactWide(
				new JLabel("Channel: "), comboChannel,
				new JLabel("Lineage: "), comboLin,
				new JLabel("Exp name: "), tfExpName,
				new JLabel("Integrate only within cell radius: "), cbUseRadius,
				new JLabel("Status: "), lStatus
				), BorderLayout.CENTER);
		
		add(bIntegrate, BorderLayout.SOUTH);
		
		bIntegrate.addActionListener(this);
		cbUseRadius.addActionListener(this);
		
		setTitleEvWindow("Integrate expression");
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
	public void windowFreeResources()
		{
		}

	@Override
	public void windowEventUserLoadedFile(EvData data)
		{
		}

	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}

	
	
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
			Lineage lin=comboLin.getSelectedObject();
			EvContainer imset=comboChannel.getSelectObjectParent();
			//Imageset imset=comboChannel.getImageset();
			EvChannel ch=comboChannel.getSelectedObject();
			
			boolean useCellRadius=cbUseRadius.isSelected();
			
			//This writes exp immediately - it is not totally thread safe
			IntegrateExp.integrateSingleCell(lin, imset, ch, tfExpName.getText(),	this, useCellRadius);
			if(!stop){}
			ParticleDialogIntegrate.this.currentCallback=null;
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
