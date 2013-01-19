package endrov.typeWorms.javier.fit;

import java.util.ArrayList;

import endrov.typeWorms.javier.WormDescriptor;
import endrov.util.math.Vector2i;

/**
 * Minimization method based on finding the best individual in the neighborhood until
 * a local or global optimum is reached
 * 
 * @author Javier Fernandez
 */
public class BestNeighborOptimization extends WormShapeOptimization
	{
		
	public BestNeighborOptimization()
		{
		super();
		}
	

	@Override
	public double run(WormDescriptor wd)
		{
		int[] wormAngles = new int[wd.getWprof().thickness.length];
		// Initialize neighbor array
		Vector2i[] auxNeighArray = new Vector2i[(wd.getWprof().thickness.length*4)-8];
		for (int i = 0; i<auxNeighArray.length; i++)
			{
			auxNeighArray[i] = new Vector2i();
			}
		double best = Integer.MAX_VALUE;
		boolean rast = true;
		ArrayList<Integer> rastShape = new ArrayList<Integer>();
		try
			{
			rastShape = wd.rasterizeWorm();
			}
		catch (RuntimeException e)
			{
			rast = false;
			}
		if (rast)
			{
			best = WormShapeOptimization.objFunction(rastShape, wd.getDtArray());
			}
		boolean newBend = true;
		double currentValue = -1;
		Vector2i bestPert = new Vector2i();
		int cp;
	//	int iter = 1;
		// Start minimization
		while (newBend)
			{
//			iter++;
			newBend = false;
			int dist = -1;
			int newpos = -1;
			int oldpos = -1;
			int bestPos = -1;

			WormShapeOptimization.getNeighborhood(wormAngles, wd, auxNeighArray);

			for (int i = 0; i<auxNeighArray.length; i++)
				{
				cp = auxNeighArray[i].x;
				// System.out.println("Trying: "+auxNeighArray[i]);

				if (auxNeighArray[i].y<0)
					{
					dist = -auxNeighArray[i].y;
					newpos = wd.angleSouthLine[cp][dist];
					wd.updateCP(cp, newpos);
					}
				else
					{
					dist = auxNeighArray[i].y;
					newpos = wd.angleNorthLine[cp][dist];
					wd.updateCP(cp, newpos);
					}

				rast = true;
				try
					{
					rastShape = wd.rasterizeWorm();
					}
				catch (RuntimeException e)
					{
					rast = false;
					currentValue = Integer.MAX_VALUE;
					}
				if (rast)
					{
					currentValue = WormShapeOptimization.objFunction(rastShape, wd
							.getDtArray());
					}
				if (currentValue<best)
					{
					best = currentValue;
					bestPert = new Vector2i(auxNeighArray[i].x, auxNeighArray[i].y);
					bestPos = newpos;
					newBend = true;
					}
				// return change
				if (wormAngles[cp]<0)
					{
					dist = -wormAngles[cp];
					oldpos = wd.angleSouthLine[cp][dist];
					}
				else
					{
					dist = wormAngles[cp];
					oldpos = wd.angleNorthLine[cp][dist];
					}
				wd.updateCP(cp, oldpos);
				}
			if (newBend)
				{
				wormAngles[bestPert.x] = bestPert.y;
				wd.updateCP(bestPert.x, bestPos);
				}
			}

		return best;
		}
	}
