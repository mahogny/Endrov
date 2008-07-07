package endrov.imagesetImserv;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvSwingTools;
import endrov.ev.PersonalConfig;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.*;

import org.jdom.Element;

import bioserv.BioservDaemon;
import bioserv.imserv.ImservClientPane;
import bioserv.imserv.ImservConnection;


/**
 * Endrov specific ImServ connection window
 * @author Johan Henriksson
 */
public class ImservWindow extends BasicWindow implements ActionListener
	{
	public static final long serialVersionUID=0;
	
	public static void initPlugin() {}
	static
		{
		EV.personalConfigLoaders.put("imservwindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
//					System.out.println("load");
					Rectangle r=getXMLbounds(e);
					new ImservWindow(r);
					}
				catch (Exception e1)
					{
					e1.printStackTrace();
					}
				}
			public void savePersonalConfig(Element e){}
			});
		}
	
	
	
	
	private ImservClientPane pane=new ImservClientPane(null);
	private JComboBox sCombo=new JComboBox();
	private JButton bLogin=new JButton("Login");
	private JButton bLogout=new JButton("Logout");
	private JPanel sComboPanel=new JPanel(new GridLayout(1,1));

	/**
	 * Embed sessions for the combo with a sane string representation
	 */
	private static class SessionCombo
		{
		public final EvImserv.EvImservSession sess;
		public SessionCombo(EvImserv.EvImservSession sess)
			{
			this.sess=sess;
			}
		public String toString()
			{
			ImservConnection conn=sess.conn;
			return conn.user+"@"+conn.host+":"+conn.port;
			}
		}

	

	
	
	
	public ImservWindow()
		{
		this(new Rectangle(100,100,600,600));
		}
	
	
	public ImservWindow(Rectangle bounds)
		{
		System.setProperty("javax.net.ssl.keyStore",BioservDaemon.class.getResource("imservkeys").getFile());
		System.setProperty("javax.net.ssl.keyStorePassword","passphrase");
		System.setProperty("javax.net.ssl.trustStore",BioservDaemon.class.getResource("cacerts").getFile());
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");
		
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
		
		
		
		add(EvSwingTools.borderLR(null, sComboPanel, EvSwingTools.borderLR(null, bLogout, bLogin)),BorderLayout.SOUTH);
		
		bLogin.addActionListener(this);
		bLogout.addActionListener(this);
		
		updateSessionList();
		
		setTitleEvWindow("ImServ");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}

	private void updateSessionList()
		{
		LinkedList<SessionCombo> sesslist=new LinkedList<SessionCombo>();
		for(EvImserv.EvImservSession sess:EvImserv.sessions)
			sesslist.add(new SessionCombo(sess));
		sCombo.removeActionListener(this);
		sComboPanel.removeAll();
		sCombo=new JComboBox(sesslist.toArray(new SessionCombo[]{}));
		sCombo.addActionListener(this);
		sComboPanel.add(sCombo);
		sessionToPane();
		revalidate();
		}
	
	private void sessionToPane()
		{
		SessionCombo c=(SessionCombo)sCombo.getSelectedItem();
		if(c!=null)
			pane.setConnection(c.sess.conn);
		else
			pane.setConnection(null);
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bLogin)
			{
			DialogOpenDatabase dia=new DialogOpenDatabase(null);
			try
				{
				EvImserv.EvImservSession ome=dia.run();
				if(ome!=null)
					{
					EvImserv.sessions.add(ome);
					updateSessionList();
					BasicWindow.updateWindows();
					}
				}
			catch (Exception e1)
				{
				JOptionPane.showMessageDialog(this, "Failed to connect to server:\n"+e1.getMessage());
				e1.printStackTrace();
				}
			}
		else if(e.getSource()==bLogout)
			{
			SessionCombo c=(SessionCombo)sCombo.getSelectedItem();
			if(c!=null)
				{
//			evimsession.disconnect(); //TODO!!!
				EvImserv.sessions.remove(c.sess);
				}
			updateSessionList();
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==sCombo)
			{
			sessionToPane();
			}
		}
	
	public void dataChangedEvent()
		{
		}

	public void loadedFile(EvData data)
		{
		}

	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
//		System.out.println("store");
		Element e=new Element("imservwindow");
		setXMLbounds(e);
		root.addContent(e);
		}
	
	
	
	
	}
