package evapplet;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

//http://java.sun.com/sfaq/

//requires signing or to be run locally

/*
 *     *  applets loaded via the file system are allowed to read and write files
    * applets loaded via the file system are allowed to load libraries on the client
    * applets loaded via the file system are allowed to exec processes
    * applets loaded via the file system are allowed to exit the virtual machine
    * applets loaded via the file system are not passed through the byte code verifier 
 */

//class-file is in applet

//<param name=file value="/etc/inet/hosts">

public class SecureAppletTest extends JApplet
	{
	static final long serialVersionUID=0; 
	
	//Called when this applet is loaded into the browser.
	public void init()
		{
		
		JFrame frame=new JFrame();
		frame.setSize(100, 100);
		frame.setVisible(true);
		
		JTextField fi=new JTextField();
		getContentPane().add(fi);
		JOptionPane.showMessageDialog(null, "hello");
		
		}
	
	/*
	 * <applet code="AppletTest.class" 
	        codebase="examples/"
	        archive="tumbleClasses.jar, tumbleImages.jar"
	        width="600" height="95">
	    <param name="maxwidth" value="120">
	    <param name="nimgs" value="17">
	    <param name="offset" value="-57">
	    <param name="img" value="images/tumble">
	
	Your browser is completely ignoring the &lt;APPLET&gt; tag!
	</applet>
	 */
	
	}
