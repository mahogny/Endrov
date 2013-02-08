package endrov.windowConsole;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.Calendar;

import endrov.core.log.EvLog;

/**
 * Take new log events and put them in console history
 * 
 * @author Johan Henriksson
 */
public class ConsoleLogger extends EvLog
	{
	private static ConsoleLogger logger=new ConsoleLogger();
	
	
	public static void install()
		{
		EvLog.addListener(logger);
		}
	
	public static void uninstall()
		{
		EvLog.removeListener(logger);
		}
	
	
	private String getDate()
		{
		NumberFormat nf=NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(2);
		Calendar c=Calendar.getInstance();
		return "["+nf.format(c.get(Calendar.HOUR_OF_DAY))+":"+nf.format(c.get(Calendar.MINUTE))+":"+nf.format(c.get(Calendar.SECOND))+"] ";
		}
	
	public void listenDebug(String s)
		{
		ConsoleWindow w=ConsoleWindow.getConsole();
		if(w!=null)
			w.addHistory(getDate()+s+"\n");
		}

	public void listenError(String s, Throwable e)
		{
		ConsoleWindow w=ConsoleWindow.openConsole();
		if(s!=null)
			{
			w.addHistory(getDate()+"\n");
			}
		if(e!=null)
			{
			StringWriter sw=new StringWriter();
			PrintWriter s2=new PrintWriter(sw);
			e.printStackTrace(s2);
			s2.flush();

			w.addHistory(getDate()+"Exception message: \n"
					+ sw.toString());
			if(e instanceof OutOfMemoryError)
				w.addHistory("Out of memory; visit http://www.endrov.net/wiki/index.php?title=Configuring_available_memory\n");
			}
		}

	public void listenLog(String s)
		{
		ConsoleWindow w=ConsoleWindow.getConsole();
		if(w!=null)
			w.addHistory(getDate()+s+"\n");
		}

	}
