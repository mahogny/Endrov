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
	public static String githash;
	public static String timestamp;
	
	static
		{
		InputStream isTimestamp=EV.class.getResourceAsStream("timestamp.txt");
		if(isTimestamp!=null)
			{
			Scanner scanner = new Scanner(isTimestamp);
			timestamp=scanner.nextLine();
			}
		else
			timestamp="0";
		
		Scanner scannerVersion = new Scanner(EV.class.getResourceAsStream("version.txt"));
		version=scannerVersion.nextLine()+"."+timestamp;

		InputStream isGit=EV.class.getResourceAsStream("githash.txt");
		if(isGit!=null)
			{
			Scanner scanner = new Scanner(isGit);
			githash=scanner.nextLine();
			}
		else
			githash="-";
		}
	}
