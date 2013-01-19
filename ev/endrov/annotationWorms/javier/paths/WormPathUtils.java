package endrov.annotationWorms.javier.paths;

import java.util.ArrayList;

/**
 * Utils for worm path guessing and tracking
 * 
 * @author Javier Fernandez
 *
 */
public class WormPathUtils
	{

	public static String pathToString(ArrayList<Integer> wormPath)
		{
		int base1 = wormPath.get(0);
		int base2 = wormPath.get(wormPath.size()-1);
		int aux;
		if (base1>base2)
			{
			aux = base1;
			base1 = base2;
			base2 = aux;
			}
		return base1+"-"+base2;
		}

	public static String basesPathToString(int base1, int base2)
		{
		int aux;
		if (base1>base2)
			{
			aux = base1;
			base1 = base2;
			base2 = aux;
			}
		return base1+"-"+base2;
		}

	}
