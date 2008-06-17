package imserv;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * ImServ server-side GUI
 * 
 * @author Johan Henriksson
 */
public class GUI extends JFrame implements DaemonListener,ActionListener
	{
	static final long serialVersionUID=0;
	Daemon daemon=new Daemon();
	
	JTextArea textLog=new JTextArea();
	JButton bQuit=new JButton("Quit");
	JTabbedPane tabbedPane = new JTabbedPane();
	JList repList=new JList();
	
	public GUI()
		{
		setTitle("Endrov ImServ");
		
		textLog.setEditable(false);
		
		bQuit.addActionListener(this);
		
		
		tabbedPane.addTab("Log", textLog);
		tabbedPane.addTab("Repositories",repList);
		
		
		JPanel bp=new JPanel(new GridLayout(1,1));
		bp.add(bQuit);
		
		add(tabbedPane,BorderLayout.CENTER);
		add(bp,BorderLayout.SOUTH);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setSize(300, 200);
		setLocationRelativeTo(null);
		setVisible(true);
		
		daemon.run();
		}
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bQuit)
			System.exit(0);
		}
	
	
	public void log(final String s)
		{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
				{
				textLog.setText(textLog.getText()+s+"\n");
				}
		});
		}






	public void repListUpdated()
		{
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
			{
			//
			}
		});
		}






	public void sessionListUpdated()
		{
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
			{
			//
			}
		});
		}






	public static void main(String args[]) 
		{
		new GUI();
		}
	}
