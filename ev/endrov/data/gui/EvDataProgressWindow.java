package endrov.data.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import endrov.gui.EvSwingUtil;


/**
 * Window for running a job in the background
 * 
 * @author Johan Henriksson
 */
public class EvDataProgressWindow extends JFrame implements ActionListener, WindowListener
	{
	static final long serialVersionUID=0;
	
	private JProgressBar progressBar = new JProgressBar(0, 100);
	private JLabel lTitle=new JLabel(" ");
	private JButton bCancel=new JButton("Cancel");
	private Task task;
	
	public interface Task
		{
		public void run(EvDataProgressWindow w);
		public void cancel();
		}
	
	public EvDataProgressWindow(String title, final Task task)
		{
		this.task=task;
		
		lTitle=new JLabel(title);
		
		setLayout(new BorderLayout());
		add(lTitle, BorderLayout.CENTER);
		add(EvSwingUtil.layoutACB(null, progressBar, bCancel), BorderLayout.SOUTH);

		bCancel.addActionListener(this);
		addWindowListener(this);
		
//		progressBar.setMinimumSize(new Dimension(600,10));
		setTitle("Endrov");
		pack();
		setSize(600, getHeight());
		
		setLocationRelativeTo(null);
		setVisible(true);
	
		
		
		startThread();
		}
	
	public void setProgress(final int p)
		{
		SwingUtilities.invokeLater(new Runnable()
			{
			public void run()
				{
				progressBar.setValue(p);
				}
			});
		}
	

	public void setStatusText(final String s)
		{
		SwingUtilities.invokeLater(new Runnable()
			{
			public void run()
				{
				if(!s.equals(""))
					lTitle.setText(s);
				}
			});
		}
	
	/**
	 * Run the thread in the background
	 */
	private void startThread()
		{
		Thread thread=new Thread()
			{
			public void run()
				{
				task.run(EvDataProgressWindow.this);
				SwingUtilities.invokeLater(new Runnable()
					{
					public void run()
						{
						jobFinished();
						}
					});
				};
			};
		thread.start();
		}
	
	/**
	 * Invoked when the thread has finished executing
	 */
	private void jobFinished()
		{
		dispose();
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bCancel)
			{
			task.cancel();
			}
		}
	
	
	

	public void windowActivated(WindowEvent e)
		{
		}

	public void windowClosed(WindowEvent e)
		{
		}

	public void windowClosing(WindowEvent e)
		{
		}

	public void windowDeactivated(WindowEvent e)
		{
		}

	public void windowDeiconified(WindowEvent e)
		{
		}

	public void windowIconified(WindowEvent e)
		{
		}

	public void windowOpened(WindowEvent e)
		{
		}

	
	}