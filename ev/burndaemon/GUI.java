package burndaemon;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;

import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class GUI extends JFrame implements ActionListener, WindowListener, DaemonListener
	{
	public static final long serialVersionUID=1;

	JTextArea logPanel=new JTextArea();	
	JButton bQuit=new JButton("Quit");
	JButton bConfig=new JButton("Read config");
	JButton bStartstop=new JButton("Start");

	/** Set to true when program should exit after daemon has quit */
	private boolean toQuit=false;

	Daemon daemon=new Daemon(this);

	
	/**
	 * Set up GUI
	 */
	public GUI()
		{
		setTitle("BurnDaemon");

		setLayout(new BorderLayout());

		JScrollPane sp=new JScrollPane(logPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel bp=new JPanel(new GridLayout(1,3));		
		bp.add(bStartstop);
		bp.add(bConfig);
		bp.add(bQuit);

		add(sp,BorderLayout.CENTER);
		add(bp,BorderLayout.SOUTH);

		bQuit.addActionListener(this);
		bConfig.addActionListener(this);
		bStartstop.addActionListener(this);

		pack();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setBounds(100, 50, 600, 500);
		setVisible(true);

		daemon.readConfig("burndaemon/config.txt");
		daemon.start();

		}



	/**
	 * Receiver of daemon log events
	 */
	public void daemonLog(String s)
		{
		int textLen=50*500;
		
		Calendar cal=new GregorianCalendar();
		int year = cal.get(Calendar.YEAR);             
    int month = cal.get(Calendar.MONTH);           
    int day = cal.get(Calendar.DAY_OF_MONTH);      
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int min = cal.get(Calendar.MINUTE);
    int sec = cal.get(Calendar.SECOND);
		String date=""+year+GUI.pad(month,2)+GUI.pad(day,2)+" "+
			GUI.pad(hour,2)+":"+GUI.pad(min,2)+":"+GUI.pad(sec,2);
    
		String newText=date+": "+s+"\n"+logPanel.getText();
		if(newText.length()>textLen)
			newText=newText.substring(0,textLen);
		logPanel.setText(newText);
		//System.out.println(s);
		if(s.equals("Stopped"))
			{
			bStartstop.setText("Start");
			if(toQuit)
				System.exit(0);
			}
		}


	/**
	 * Receiver of daemon incremental log events
	 * NOT USED
	 */
	public void daemonLogAttention(String s)
		{
		daemonLog(s);
		//JOptionPane.showMessageDialog(this, s);
		}

	
	/**
	 * Receiver of daemon error events
	 */
	public void daemonError(String s, Exception e)
		{
		String total="";
		if(s!=null)
			total=s+"\n";
		if(e!=null)
			{
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			total=total+e.getMessage()+"\n"+sw.toString();
			System.out.println(e.getMessage());
			e.printStackTrace();
			}
		daemonLog(total);
		}


	/**
	 * Quit dialog
	 */
	public void quit()
		{
		int answer=JOptionPane.showConfirmDialog(null, "Do you really want to quit?", "Confirm", JOptionPane.YES_NO_OPTION);
		if(answer==JOptionPane.YES_OPTION)
			{
			if(!daemon.isRunning())
				System.exit(0);
			else
				{
				toQuit=true;
				daemon.shutDown();
				}
			}
		}

	public void windowClosing(WindowEvent e) {quit();}
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}




	/**
	 * Swing event handler
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bQuit)
			quit();
		else if(e.getSource()==bConfig)
			{

			JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    	{
	    	String filename=chooser.getSelectedFile().getAbsolutePath();
	    	daemon.readConfig(filename);
	    	}
			}
		else if(e.getSource()==bStartstop)
			{
			if(daemon.isRunning())
				{
				bStartstop.setText("Shutting down");
				daemon.shutDown();
				}
			else
				{
				daemon.go();
				bStartstop.setText("Stop");
				}
			}		
		}

	

	/**
	 * Pad an integer up to # digits
	 */
	public static String pad(int i, int pad)
		{
		String s=""+i;
		while(s.length()<pad)
			s="0"+s;
		return s;
		}



	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		new GUI();
		}

	}
