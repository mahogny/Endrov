package endrov.typeWorms.javier.fit;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.typeWorms.javier.WormDescriptor;
import endrov.util.math.Vector2i;

/**
 * Abstract definition of optimization methods for
 * worm shape deformations
 * 
 * @author Javier Fernandez
 */

public abstract class WormShapeOptimization
	{

	public abstract double run(WormDescriptor wd);

	/**
	 * Calculates the cost of the objective function for a given worm profile
	 * rasterization. Objective Function is: The sum of the rasterized pixels that
	 * are background
	 */
	public static double objFunction(ArrayList<Integer> rastShape, int[] dtArray)
		{
		if (rastShape==null)
			return Double.MAX_VALUE;
		int background = 0;
		int foreground = 0;
		Iterator<Integer> it = rastShape.iterator();
		while (it.hasNext())
			{
			if (dtArray[it.next()]==0)
				background++;
			else
				foreground++;
			}
		return (double)background/(double)(foreground+background);
		}

	public static void getNeighborhood(int[] wormAngles, WormDescriptor wd,
			Vector2i[] auxNeighArray)
		{
		/*
		 * Change +1 / -1 in the given direction generates thickness.length
		 * neighbors Neighbors are a value indicating the displacement. Positive
		 * corresponds to North and Negative to South
		 */
		// Avoid endpoints and calculate all possible +1 neighborhoods
		int nCount = 0;
		for (int i = 1; i<wormAngles.length-1; i++, nCount++)
			{
			if ((wormAngles[i]+1)<=wd.angleNorthLine[i].length-1)
				{
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i]+1;
				}
			else
				{
				// dont perturbate
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i];
				}
			}

		for (int i = 1; i<wormAngles.length-1; i++, nCount++)
			{
			if ((wormAngles[i]-1)>=-1*(wd.angleSouthLine[i].length-1))
				{
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i]-1;
				}
			else
				{
				// dont perturbate
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i];
				}
			}

		for (int i = 1; i<wormAngles.length-1; i++, nCount++)
			{
			if ((wormAngles[i]+2)<=wd.angleNorthLine[i].length-1)
				{
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i]+2;
				}
			else
				{
				// dont perturbate
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i];
				}
			}

		for (int i = 1; i<wormAngles.length-1; i++, nCount++)
			{
			if ((wormAngles[i]-2)>=-1*(wd.angleSouthLine[i].length-1))
				{
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i]-2;
				}
			else
				{
				// dont perturbate
				auxNeighArray[nCount].x = i;
				auxNeighArray[nCount].y = wormAngles[i];
				}
			}
		}

	}
