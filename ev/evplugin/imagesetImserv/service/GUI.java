package evplugin.imagesetImserv.service;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;

/**
 * ImServ server-side GUI
 * 
 * @author Johan Henriksson
 */
public class GUI extends JFrame implements DaemonListener,ActionListener,WindowListener
	{
	static final long serialVersionUID=0;
	Daemon daemon=new Daemon();
	
	private JTextArea textLog=new JTextArea();
	private JButton bQuit=new JButton("Quit");
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JList repList=new JList();
	private JList sessionList=new JList();
	
	private JButton bRepAdd=new JButton("Add");
	private JButton bRepRemove=new JButton("Remove");
	
	/**
	 * Construct GUI
	 */
	public GUI()
		{
		setTitle("Endrov ImServ");
		
		textLog.setEditable(false);
		
		bQuit.addActionListener(this);
		bRepAdd.addActionListener(this);
		bRepRemove.addActionListener(this);
		
		JPanel repPanelButtons=new JPanel(new GridLayout(1,2));
		repPanelButtons.add(bRepAdd);
		repPanelButtons.add(bRepRemove);
		
		JPanel repPanel=new JPanel(new BorderLayout());
		repPanel.add(new JScrollPane(repList),BorderLayout.CENTER);
		repPanel.add(repPanelButtons,BorderLayout.SOUTH);
		
		JPanel authPanel=new JPanel(new BorderLayout());

		//Tabs
		tabbedPane.addTab("Log", new JScrollPane(textLog));
		tabbedPane.addTab("Repositories",repPanel);
		tabbedPane.addTab("Users",authPanel);
		tabbedPane.addTab("Sessions",sessionList);

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
		
		//Start up daemon
		daemon.start();
		daemon.addListener(this);
		
		//Load configuration
		Config.readConfig(daemon);
		
		
//		daemon.addRepository(new File("/Volumes/TBU_main02/fakeost/"));

		/*
		daemon.addRepository(new File("/Volumes/TBU_main01/ost3dgood"));
		daemon.addRepository(new File("/Volumes/TBU_main01/ost3dfailed"));
		daemon.addRepository(new File("/Volumes/TBU_main01/ost4dgood"));
		daemon.addRepository(new File("/Volumes/TBU_main01/ost4dfailed"));
		daemon.addRepository(new File("/Volumes/TBU_main01/ostxml"));

		daemon.addRepository(new File("/Volumes/TBU_main02/ost3dgood"));
		daemon.addRepository(new File("/Volumes/TBU_main02/ost3dfailed"));
		daemon.addRepository(new File("/Volumes/TBU_main02/ost4dgood"));
		daemon.addRepository(new File("/Volumes/TBU_main02/ost4dfailed"));
		daemon.addRepository(new File("/Volumes/TBU_main02/ostxml"));
		
		daemon.addRepository(new File("/Volumes/TBU_main03/ost3dgood"));
		daemon.addRepository(new File("/Volumes/TBU_main03/ost3dfailed"));
		daemon.addRepository(new File("/Volumes/TBU_main03/ost4dgood"));
		daemon.addRepository(new File("/Volumes/TBU_main03/ost4dfailed"));
		daemon.addRepository(new File("/Volumes/TBU_main03/ostxml"));
		
		daemon.addRepository(new File("/home/mahogny/_imagedata/ost"));
		*/
		
		}
	
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bQuit)
			System.exit(0);
		else if(e.getSource()==bRepAdd)
			{
			JFileChooser fc=new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int rval=fc.showOpenDialog(this);
			if(rval==JFileChooser.APPROVE_OPTION)
				daemon.addRepository(fc.getSelectedFile());
			}
		else if(e.getSource()==bRepRemove)
			{
			File file=(File)repList.getSelectedValue();
			if(file!=null)
				daemon.removeRepository(file);
			}
		
		
		}
	
	
	/**
	 * Callback: log message
	 */
	public void log(final String s)
		{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
				{
				textLog.setText(textLog.getText()+s+"\n");
				}
		});
		}





	/**
	 * Callback: repository list updated
	 */
	public void repListUpdated()
		{
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
			{
			repList.setModel(new ListModel(){
				public void addListDataListener(ListDataListener arg0){}
				public void removeListDataListener(ListDataListener arg0){}
				public Object getElementAt(int i)
					{
					return daemon.reps.get(i).dir;
//					return daemon.reps.get(i).dir.toString();
					}
				public int getSize()
					{
					return daemon.reps.size();
					}
			});
			}
		});
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
		System.out.println("here");
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
		new GUI();
		}
	}
