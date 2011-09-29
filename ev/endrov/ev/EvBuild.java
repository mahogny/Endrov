package endrov.ev;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Information about this version of the software
 * @author Johan Henriksson
 *
 */
public class EvBuild
	{
	public static String version;
	public static String timestamp;
	
	static
		{
		InputStream is=EV.class.getResourceAsStream("timestamp.txt");
		if(is!=null)
			{
			Scanner scanner = new Scanner(is);
			timestamp=scanner.nextLine();
			}
		else
			timestamp="0";
		
		Scanner scanner = new Scanner(EV.class.getResourceAsStream("version.txt"));
		version=scanner.nextLine()+"."+timestamp;
		}
	}
