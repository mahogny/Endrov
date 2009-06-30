package endrov.ev;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

/**
 * Log listener: Print to console
 * 
 * @author Johan Henriksson
 */
public class SwingLog extends EvLog
	{
	/**
	 * Log debugging information
	 */
	public void listenDebug(String s)
		{
		JOptionPane.showMessageDialog(null, s);
		}

	/**
	 * Log an error
	 * @param s Human readable description, may be null
	 * @param e Low-level error, may be null
	 */
	public void listenError(String s, Exception e)
		{
		if(s!=null)
			JOptionPane.showMessageDialog(null, s);
		if(e!=null)
			{
			StringWriter sw=new StringWriter();
			PrintWriter s2=new PrintWriter(sw);
			e.printStackTrace(s2);
			s2.flush();
			JOptionPane.showMessageDialog(null, e.getMessage()+sw.toString());
			}
		}

	/**
	 * Log normal/expected message
	 */
	public void listenLog(String s)
		{
		JOptionPane.showMessageDialog(null, s);
		}
	
	}
