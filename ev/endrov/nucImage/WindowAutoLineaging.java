package endrov.nucImage;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.EvComboObjectOne;
import endrov.basicWindow.SpinnerSimpleEvFrame;
import endrov.data.EvData;
import endrov.nuc.NucLineage;
import endrov.nucImage.LineagingAlgorithm.LineageAlgorithmDef;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * Window letting user control an algorithm for automatic lineaging
 * 
 * @author Johan Henriksson
 *
 */
public class WindowAutoLineaging extends BasicWindow implements LineagingAlgorithm.LineageSession
	{
	private static final long serialVersionUID = 1L;

	private EvComboObjectOne<NucLineage> comboLin=new EvComboObjectOne<NucLineage>(new NucLineage(), true, true);
//	private EvComboObjectOne<EvChannel> comboChan=new EvComboObjectOne<EvChannel>(new EvChannel(), true, false);
	private JComboBox comboAlgo=new JComboBox(LineageAlgorithmDef.listAlgorithms);

	private JPanel panelOptions=new JPanel(new GridLayout(1,1));
	private JLabel panelStatus=new JLabel();
	
	private JButton bStartStop=new JButton("Start");
	private JButton bStep=new JButton("Step");
	private JButton bFlatten=new JButton("Flatten");
	
	private SpinnerSimpleEvFrame frameStart=new SpinnerSimpleEvFrame();
	
	/**
	 * Instance of the current algorithm. Has to be remembered since it might keep state.
	 */
	private LineagingAlgorithm currentAlgo;
	private LineagingAlgorithm getCurrentAlgo()
		{
		return currentAlgo;
		}
	
	
	/**
	 * Create and display window
	 */
	public WindowAutoLineaging()
		{
		setLayout(new GridLayout(1,1));
		/*
		JPanel top=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.gridy=0; top.add(new JLabel("Lineage "),c);
		//c.gridy=1; top.add(new JLabel("Channel "),c);
		c.gridy=1; top.add(new JLabel("Algorithm "),c);
		
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridx=1;
		c.weightx=1;
		
		c.gridy=0; top.add(comboLin,c);
//		c.gridy=1; top.add(comboChan,c);
		c.gridy=1; top.add(comboAlgo,c);
		*/
		JComponent top=EvSwingUtil.layoutTableCompactWide(
				new JLabel("Lineage "),comboLin,
				new JLabel("Algorithm "),comboAlgo
				);
		
		panelOptions.setBorder(BorderFactory.createTitledBorder("Options"));

		
		add(EvSwingUtil.layoutCompactVertical(
				top,
				panelOptions,
				panelStatus,
				EvSwingUtil.withLabel("Frame", frameStart),
				EvSwingUtil.layoutEvenHorizontal(bStartStop,bStep,bFlatten)));
		
		setTitleEvWindow("Auto-lineage");
		updateCurrentAlgo();
		packEvWindow();
		setVisibleEvWindow(true);
		
		
		comboAlgo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){updateCurrentAlgo();}});
		
		bStartStop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){startStop();}});

		bStep.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){step();}});


		bFlatten.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){flatten();}});

		}
	
	
	
	/**
	 * Run the algorithm without interrupting the GUI
	 */
	private class SteppingThread extends Thread
		{
		private boolean toStop=false;
		public void run()
			{
			System.out.println("===start thread===");
			try
				{
				do
					getCurrentAlgo().run(WindowAutoLineaging.this);
//				currentAlgo.run(this);
//					step();
					while(!toStop);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			SwingUtilities.invokeLater(new Runnable(){
			public void run()
				{
				bStartStop.setText("Start");
				}});
			System.out.println("===end thread===");
			}
		}
	private SteppingThread thread=new SteppingThread();
	
	private boolean isRunning()
		{
		return thread.isAlive();
		}
	
	/**
	 * Start or stop calculation, depending on if it is running or not 
	 */
	private void startStop()
		{
		if(currentAlgo!=null)
			{
			if(isRunning())
				{
				bStartStop.setText("Stopping");
				thread.toStop=true;
				currentAlgo.setStopping(true);
				}
			else
				{
				thread=new SteppingThread();
				bStartStop.setText("Stop");
				thread.toStop=false;
				currentAlgo.setStopping(false);
				thread.start();
				}
			}
		}
	
	/**
	 * Run calculation for one frame
	 */
	private void step()
		{
		if(currentAlgo!=null && !isRunning())
			{
			thread=new SteppingThread();
			bStartStop.setText("Stepping");
			thread.toStop=true;
			currentAlgo.setStopping(false);
			thread.start();
//			currentAlgo.setStopping(false);
//			currentAlgo.run(this);
			}
		}
	
	/**
	 * Flatten lineage tree
	 */
	private void flatten()
		{
		NucLineage lineage=comboLin.getSelectedObject();
		if(lineage!=null)
			{
			lineage.flattenSingleChildren();
			BasicWindow.updateWindows();
			}
		}
	
	private void updateCurrentAlgo()
		{
		LineagingAlgorithm.LineageAlgorithmDef def=(LineagingAlgorithm.LineageAlgorithmDef)comboAlgo.getSelectedItem();
		if(def!=null)
			setCurrentAlgo(def.getInstance());
		else
			setCurrentAlgo(null);
		}
	
	
	private void setCurrentAlgo(LineagingAlgorithm algo)
		{
		currentAlgo=algo;
		panelOptions.removeAll();
		if(currentAlgo!=null)
			panelOptions.add(currentAlgo.getComponent());
		invalidate();
		packEvWindow();
//		revalidate();
		}
	
	
	public void dataChangedEvent()
		{
		comboLin.updateList();
		if(currentAlgo!=null)
			currentAlgo.dataChangedEvent();
		}

	public void freeResources()
		{
		}

	public void loadedFile(EvData data)
		{
		}

	public void windowSavePersonalSettings(Element e)
		{
		}


	public NucLineage getLineage()
		{
		return comboLin.getSelectedObject();
		}


	public EvDecimal getStartFrame()
		{
		return frameStart.getDecimalValue();
		}

	/**
	 * Call when frame has been completed and all data written.
	 * Supply the next frame.
	 */
	public void finishedAndNowAtFrame(final EvDecimal f)
		{
		if(SwingUtilities.isEventDispatchThread())
			frameStart.setValue(f);
		else
			{
			try
				{
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run()
						{
						frameStart.setValue(f);
						}});
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		}

	
	
	}
