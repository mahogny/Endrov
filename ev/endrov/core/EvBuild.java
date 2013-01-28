package endrov.core;

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
		InputStream isTimestamp=EndrovCore.class.getResourceAsStream("timestamp.txt");
		if(isTimestamp!=null)
			{
			Scanner scanner = new Scanner(isTimestamp);
			timestamp=scanner.nextLine();
			scanner.close();
			}
		else
			timestamp="0";
		
		Scanner scannerVersion = new Scanner(EndrovCore.class.getResourceAsStream("version.txt"));
		version=scannerVersion.nextLine()+"."+timestamp;
		scannerVersion.close();

		InputStream isGit=EndrovCore.class.getResourceAsStream("githash.txt");
		if(isGit!=null)
			{
			Scanner scanner = new Scanner(isGit);
			githash=scanner.nextLine();
			scanner.close();
			}
		else
			githash="-";
		}
	}
