package endrov.ev;

import java.util.Scanner;

/**
 * Information about current build
 * @author Johan Henriksson
 *
 */
public class EvBuild
	{
	public static final String version;
	static
		{
		Scanner scanner = new Scanner(EvBuild.class.getResourceAsStream("version.txt"));
		version=scanner.nextLine();
		}
	}
