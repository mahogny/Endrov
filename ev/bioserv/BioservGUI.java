package bioserv;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;



/**
 * ImServ server-side GUI
 * 
 * @author Johan Henriksson
 */
public class BioservGUI extends JFrame implements BioservDaemon.DaemonListener,ActionListener,WindowListener
	{
	static final long serialVersionUID=0;
	BioservDaemon daemon=new BioservDaemon();
	
	private JTextArea textLog=new JTextArea();
	private JButton bQuit=new JButton("Quit");
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	
	/**
	 * Construct GUI
	 */
	public BioservGUI()
		{
		//Load configuration
		Config.readConfig(daemon);

		//Start up daemon
		daemon.addListener(this);
//		daemon.start();
		
		
		setTitle("Endrov ImServ");
		textLog.setEditable(false);		
		bQuit.addActionListener(this);
		
		//Tabs
		tabbedPane.addTab("Log", new JScrollPane(textLog));
		for(BioservModule module:daemon.modules)
			{
			JComponent c=module.getBioservModuleSwingComponent(this);
			tabbedPane.addTab(c.toString(),c);
//			tabbedPane.addTab(module.getBioservModuleName(),);
			}
		//tabbedPane.addTab("Sessions",sessionList);

		//Total layout
		JPanel totalBP=new JPanel(new GridLayout(1,1));
		totalBP.add(bQuit);

		add(tabbedPane,BorderLayout.CENTER);
		add(totalBP,BorderLayout.SOUTH);
		
		//Final settings
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		pack();
		setSize(400, 300);
		setLocationRelativeTo(null);
		setVisible(true);
		
		
		
		
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
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bQuit)
			dispose();
		}
	
	

	
	/**
	 * Callback: session list updated
	 */
	public void sessionListUpdated()
		{
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
			{
			//hashset, need to sort somehow
//			daemon.sessions.keySet()
			
//			sessionList.getModel().
			//
			}
		});
		}

	
	






	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e)
		{
		Config.saveConfig(daemon);
		System.exit(0);
		}
	public void windowClosing(WindowEvent e)
		{
		dispose();
		}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}




	public static void main(String args[]) 
		{
		BioservGUI gui=new BioservGUI();
		gui.daemon.start();
		}
	}
