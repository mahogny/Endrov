package endrov.applet;

import javax.swing.JApplet;
import javax.swing.JTextField;

//http://java.sun.com/sfaq/

/*
 *     *  applets loaded via the file system are allowed to read and write files
    * applets loaded via the file system are allowed to load libraries on the client
    * applets loaded via the file system are allowed to exec processes
    * applets loaded via the file system are allowed to exit the virtual machine
    * applets loaded via the file system are not passed through the byte code verifier 
 */

//class-file is in applet

//<param name=file value="/etc/inet/hosts">

public class AppletTest extends JApplet
	{
	static final long serialVersionUID=0; 
	
	//Called when this applet is loaded into the browser.
	public void init()
		{
		JTextField fi=new JTextField();
		getContentPane().add(fi);
		
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
