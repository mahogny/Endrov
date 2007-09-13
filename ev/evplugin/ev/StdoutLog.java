package evplugin.ev;

/**
 * Log listener: Print to console
 * 
 * @author Johan Henriksson
 */
public class StdoutLog extends Log
	{
	/**
	 * Log debugging information
	 */
	public void listenDebug(String s)
		{
		System.out.println(s);
		}

	/**
	 * Log an error
	 * @param s Human readable description, may be null
	 * @param e Low-level error, may be null
	 */
	public void listenError(String s, Exception e)
		{
		if(s!=null)
			System.out.println(s);
		if(e!=null)
			{
			System.out.println("Exception message: "+e.getMessage());
			e.printStackTrace();
			}
		
		/*
		StringWriter sw=new StringWriter();
		PrintWriter s=new PrintWriter(sw);
		e.printStackTrace(s);
		s.flush();
		JOptionPane.showMessageDialog(null, sw.toString());
		*/
		}

	/**
	 * Log normal/expected message
	 */
	public void listenLog(String s)
		{
		System.out.println(s);
		}
	
	}
