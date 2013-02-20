package endrov.windowPlateAnalysis;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.core.log.EvLog;
import endrov.data.EvContainer;
import endrov.data.EvPath;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.JImageButton;
import endrov.gui.icon.BasicIcon;
import endrov.typeImageset.EvChannel;
import endrov.typeParticleMeasure.ParticleMeasure;


/**
 * Widget to display a plate analysis batch queue
 * 
 * @author Johan Henriksson
 */
public class PlateAnalysisQueueWidget extends JPanel
	{
	private static final long serialVersionUID = 1L;
	
	private JPanel midpanel=new JPanel();

	private LinkedList<JobWidget> queue=new LinkedList<JobWidget>();
	
	public PlateAnalysisQueueWidget()
		{
		setLayout(new BorderLayout());
	//	JScrollPane scroll=new JScrollPane(midpanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
	//	add(scroll, BorderLayout.CENTER);
		add(midpanel, BorderLayout.CENTER);
		
		updateListLayout();
		}

	private void updateListLayout()
		{
		midpanel.setLayout(new GridBagLayout());
		midpanel.removeAll();
		
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=1;
		c.gridx=0;
		c.gridy=0;
		
		//Add all widgets. Create new if needed
		for(JobWidget job:queue)
			{
			midpanel.add(job,c);
			c.gridy++;
			}
		
		JLabel filler=new JLabel("");
		c.fill=GridBagConstraints.VERTICAL;
		c.weighty=1;
		midpanel.add(filler,c);
		
		setVisible(true);
		revalidate();
		repaint();
		}

	
	private class JobWidget extends JPanel implements ActionListener
		{
		private static final long serialVersionUID = 8963651885605021285L;
		
		private JLabel lSource=new JLabel();
		private JLabel lFlow=new JLabel();
		private JLabel lOutput=new JLabel();
		private JButton bRemove=new JImageButton(BasicIcon.iconRemove,"Remove this job");

		private EvPath pathFlow;
		private EvPath pathOutput;

		private LinkedList<EvPath> wellPathsList=new LinkedList<EvPath>();

		public JobWidget(EvPath pathData, EvPath pathFlow, EvPath pathOutput)
			{
			bRemove.addActionListener(this);
			
			this.pathFlow=pathFlow;
			this.pathOutput=pathOutput;
			
			lSource.setText("From: "+pathData);
			lFlow.setText("Using: "+pathFlow);
			lOutput.setText("To: "+pathOutput);
			
			setLayout(new BorderLayout());
			add(EvSwingUtil.layoutLCR(
					null, 
					EvSwingUtil.layoutEvenVertical(lSource, lFlow, lOutput),
					bRemove),
					BorderLayout.CENTER);
			
			TreeSet<EvPath> wellPaths=new TreeSet<EvPath>();
			EvContainer con=pathData.getObject();
			if(con!=null)
				{
				Map<EvPath, EvChannel> m=con.getIdObjectsRecursive(EvChannel.class);

				//Add wells to panel, from images
				for(EvPath p:m.keySet())
					{
					EvPath path=p.getParent();
					wellPaths.add(path);
					}
				}
			wellPathsList.addAll(wellPaths);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bRemove)
				removeJob();
			}

		
		private void removeJob()
			{
			EvSwingUtil.invokeAndWaitIfNeeded(new Runnable(){
				public void run()
					{
					System.out.println("remove!");
					queue.remove(JobWidget.this);
					updateListLayout();
					}
			});
			}
		
		/**
		 * Get a well to execute, or null if nothing left to do
		 */
		public WellRunnable getNextBackgroundTask() 
			{
			if(wellPathsList.isEmpty())
				{
				removeJob();
				return null;
				}
			else
				{
				return new WellRunnable(this);
				}
			}
		}

	
	public class WellRunnable implements Runnable
		{
		private JobWidget job;

		public EvPath pathToWell;
		
		private WellRunnable(JobWidget job)
			{
			this.job=job;
			pathToWell=job.wellPathsList.getFirst();
			}
		public void run()
			{
			try
				{
				
				ParticleMeasure pm=(ParticleMeasure)job.pathOutput.getObject();
				
				ParticleMeasure wellPM=ParticleMeasureWellFlowExec.execFlowOnWell(pathToWell, job.pathFlow);
				ParticleMeasureWellFlowExec.mergeWellPM(pm, wellPM, pathToWell);
				
				job.wellPathsList.removeFirst();
				if(job.wellPathsList.isEmpty())
					job.removeJob();
				}
			catch (Throwable e)
				{
				EvLog.printError(e);
				}
			}
	
		}


	public void addJob(EvPath pathData, EvPath pathFlow, EvPath pathOutput)
		{
		queue.add(new JobWidget(pathData, pathFlow, pathOutput));
		updateListLayout();
		}

	/**
	 * Get a well to execute, or null if nothing left to do
	 */
	public WellRunnable getNextBackgroundTask()
		{
		while(!queue.isEmpty())
			{
			JobWidget widget=queue.getFirst();

			WellRunnable r=widget.getNextBackgroundTask();
			if(r!=null)
				return r;
			}
		return null;
		}
	
	

	}
