package endrov.gui.window;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom.Element;

import endrov.core.EndrovCore;
import endrov.core.EvBuild;
import endrov.core.PersonalConfig;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.JMultilineLabel;

/**
 * Form to fill in information about the user 
 * @author Johan Henriksson
 *
 */
public class EvRegistrationDialog extends JDialog implements ActionListener
	{
	//Registered information
	public static String name="";
	public static String institution="";
	public static String email="";
	
	private static final long serialVersionUID = 1L;
	
	private JButton bOk=new JButton("OK");
	private JButton bLater=new JButton("Enter later");
	
	private JMultilineLabel label=new JMultilineLabel(
			"To enable further development, we collect statistics on who\n" +
			"are using the software. It is used e.g. for grant applications\n" +
			"but also to see which platforms we should prioritize. The\n" +
			"list of users will not be given to third parties."); 
	
	private JTextField tfName=new JTextField(name);
	private JTextField tfInstitute=new JTextField(institution);
	private JTextField tfEmail=new JTextField(email);
	
	private Semaphore lock=new Semaphore(0);
	
	public EvRegistrationDialog()
		{
		setTitle(EndrovCore.programName + " Registration");
		setLayout(new GridLayout(1,1));
		add(EvSwingUtil.layoutACB(
				label, 
				EvSwingUtil.layoutEvenVertical(
						new JLabel(" "),
						EvSwingUtil.withLabel("Name: ", tfName),
						EvSwingUtil.withLabel("Institution: ", tfInstitute),
						EvSwingUtil.withLabel("E-mail: ", tfEmail)
				),
				EvSwingUtil.layoutEvenHorizontal(bOk, bLater)
		));
	
		bOk.addActionListener(this);
		bLater.addActionListener(this);
	
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			name=tfName.getText();
			institution=tfInstitute.getText();
			email=tfEmail.getText();
			}
		else if(e.getSource()==bLater)
			{
			}
		dispose();
		lock.release();
		}
	
	/**
	 * Check if Endrov has been registered already
	 */
	public static boolean hasRegistered()
		{
		return !name.equals("");
		}
	
	/**
	 * Run dialog 
	 */
	public static void runDialog()
		{
		EvRegistrationDialog use=new EvRegistrationDialog();
		try
			{
			use.lock.acquire();
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		}

	/**
	 * Run dialog - to be run from a swing event. if recoded then it can be merged with above function
	 */
	public static void runDialogNoLock()
		{
		new EvRegistrationDialog();
		}

	

	/**
	 * Connect and send information. This will not stall the software in case of a bad connection
	 */
	public static void connectAndRegister(final boolean firstTime)
		{
		new Thread()
			{
			public void run()
				{
				try 
					{
					String localaddr=InetAddress.getLocalHost().getHostAddress();
					for(NetworkInterface iface:Collections.list(NetworkInterface.getNetworkInterfaces()))
						for(InetAddress a:Collections.list(iface.getInetAddresses()))
							{
							String ipa=a.getHostAddress();
							if(!ipa.equals("127.0.0.1") && !ipa.contains("0:0:0:0:0:0:0:1") && !ipa.equals("::1"))
								localaddr=ipa;
							}
	
					HttpClient client = new HttpClient();
					PostMethod method = new PostMethod( "http://www.endrov.net/registerEndrov.php" );
	
					method.addParameter( "javaversion", System.getProperty("java.specification.version") );
					method.addParameter( "arch", System.getProperty("os.arch").toLowerCase() );
					method.addParameter( "OS", System.getProperty("os.name").toLowerCase() );
					method.addParameter( "localaddr", localaddr );
					method.addParameter( "endrovversion", EvBuild.version );
					method.addParameter( "name", name );
					method.addParameter( "institution", institution );
					method.addParameter( "email", email);
					method.addParameter( "firsttime", ""+firstTime);
	
					//Could also do "uname -a" when applicable
	
					int statusCode = client.executeMethod( method );
					if( statusCode != -1 ) 
						{
						//String contents = 
						method.getResponseBodyAsString();
						method.releaseConnection();
						//System.out.println( contents );
						}
					System.out.println("Sent user registration");
					}
				catch(UnknownHostException e)
					{
					System.out.println("Could not look up registration server");
					}
				catch( Exception e ) 
					{
					e.printStackTrace();
					}
				}
			}.run();
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin(){}
	static
	{
	EndrovCore.addPersonalConfigLoader("evregistration", new PersonalConfig()
		{
		public void loadPersonalConfig(Element e)
			{
			name=e.getAttributeValue("name");
			institution=e.getAttributeValue("institution");
			email=e.getAttributeValue("email");
			}
	
		public void savePersonalConfig(Element e)
			{
			Element ne=new Element("evregistration");
			ne.setAttribute("name", name);
			ne.setAttribute("institution", institution);
			ne.setAttribute("email", email);
			
			e.addContent(ne);
			}
		});
	}
	
	public static void main(String[] args)
		{
		EvRegistrationDialog.runDialog();
		connectAndRegister(true);
		}
	
	}