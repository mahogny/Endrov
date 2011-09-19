package endrov.ev;

import java.util.Scanner;

public class EvBuild
	{
	public static String version;
	static
		{
		Scanner scanner = new Scanner(EV.class.getResourceAsStream("version.txt"));
		version=scanner.nextLine();
		}
	}
