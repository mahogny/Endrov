/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
