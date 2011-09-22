package endrov.worms.javier.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import endrov.util.Vector2i;

public class GreedyNonBipartiteAssignment
	{

	private static class CmpVector implements Comparable<CmpVector>
		{
		double value;
		int index;

		private CmpVector(double value, int index)
			{
			this.value = value;
			this.index = index;
			}

		public int compareTo(CmpVector o)
			{
			return Double.compare(value, o.value);
			}

		public String toString()
			{
			return ""+value;
			}

		}

	/**
	 * Returns the best index pair assignment from the match matrix, minimizing
	 * the cost and maximizing the number of pairs.
	 * 
	 * @param matchMatrix
	 *          matrix filled in the upper-diagonal region with the distance value
	 *          corresponding to the match between the endpoints row-column. The
	 *          last row (already described endpoint) is discarded, though it must
	 *          be included
	 */
	public static ArrayList<Vector2i> findBestAssignment(double[][] matchMatrix)
		{
		ArrayList<Vector2i> best = new ArrayList<Vector2i>();
		ArrayList<Integer> generalRowList = new ArrayList<Integer>();
		for (int r = 0; r<matchMatrix.length-1; r++)
			generalRowList.add(r);
		for (int row = 0; row<matchMatrix.length-1; row++)
			{

			double[][] matchCopy = new double[matchMatrix.length][matchMatrix[0].length];
			for (int r = 0; r<matchMatrix.length; r++)
				for (int j = r+1; j<matchMatrix[0].length; j++)
					matchCopy[r][j] = matchMatrix[r][j];

			ArrayList<Integer> rowList = new ArrayList<Integer>(generalRowList);
			ArrayList<Vector2i> res = inteligentTurning(matchCopy, row, rowList);
			if (res.size()>best.size())
				{
				best = res;
				}
			else if (res.size()==best.size())
				{
				if (totalCost(best, matchMatrix)>totalCost(res, matchMatrix))
					best = res;
				}
			}
		return best;
		}

	private static double totalCost(ArrayList<Vector2i> sol,
			double[][] matchMatrix)
		{
		double totalCost = 0;
		Iterator<Vector2i> sit = sol.iterator();
		Vector2i n;
		while (sit.hasNext())
			{
			n = sit.next();
			totalCost += matchMatrix[n.x][n.y];
			}
		return totalCost;
		}

	//TODO never used
	public static ArrayList<Vector2i> turningProcessing(double[][] matchMatrix,
			int row)
		{
		int loop = matchMatrix.length-1;
		ArrayList<Vector2i> matchList = new ArrayList<Vector2i>();
		while (loop>0)
			{
			Vector2i newMatch = findMarkMin(matchMatrix, row);
			if (newMatch!=null)
				{
				matchList.add(newMatch);
				}
			row += 1;
			// Never reach the last row
			row %= matchMatrix.length-1;
			loop -= 1;
			}
		return matchList;
		}

	private static ArrayList<Vector2i> inteligentTurning(double[][] matchMatrix,
			int row, ArrayList<Integer> rowList)
		{
		ArrayList<Vector2i> matchList = new ArrayList<Vector2i>();
		// find every time the next row with lowest value
		while ((!rowList.isEmpty())&&row!=-1)
			{
			Vector2i newMatch = findMarkMin(matchMatrix, row);
			if (newMatch!=null)
				{
				matchList.add(newMatch);
				}
			rowList.remove((Integer) row);
			row = getNextRow(matchMatrix, rowList);
			}
		return matchList;
		}

	/**
	 * finds the row that contains the minimun value
	 */
	private static int getNextRow(double[][] matchMatrix,
			ArrayList<Integer> remainingList)
		{
		ArrayList<CmpVector> listMins = new ArrayList<CmpVector>();

		for (int row : remainingList)
			{
			// find the minimum in the list
			double min = Double.MAX_VALUE;
			int minIndex = -1;

			for (int j = row+1; j<matchMatrix[0].length; j++)
				{
				// System.out.print(matchMatrix[row][j]+" ");
				if (matchMatrix[row][j]!=-1&&matchMatrix[row][j]<min)
					{
					min = matchMatrix[row][j];
					minIndex = j;
					}
				}
			if (minIndex!=-1)
				listMins.add(new CmpVector(min, row));
			}
		Collections.sort(listMins);
		if (!listMins.isEmpty())
			return listMins.get(0).index;
		else
			return -1;
		}

	private static Vector2i findMarkMin(double[][] matchMatrix, int row)
		{
		double min = Integer.MAX_VALUE;
		int minIndex = -1;
		for (int j = row+1; j<matchMatrix[0].length; j++)
			{
			if (matchMatrix[row][j]<min&&matchMatrix[row][j]!=-1)
				{
				min = matchMatrix[row][j];
				minIndex = j;
				}
			}
		// System.out.println();
		if (minIndex!=-1)
			{
			// update columns
			for (int r = 0; r<matchMatrix.length; r++)
				matchMatrix[r][minIndex] = -1;
			// update whole row for selected column
			for (int c = 0; c<matchMatrix[0].length; c++)
				matchMatrix[minIndex][c] = -1;
			// update the column of the selected row
			for (int r = 0; r<matchMatrix.length; r++)
				matchMatrix[r][row] = -1;

			// row/col
			return new Vector2i(row, minIndex);
			}
		return null;
		}

	public static void printMatchMatrix(double[][] matchMatrix)
		{
		for (int i = 0; i<matchMatrix.length; i++)
			{
			for (int j = 0; j<matchMatrix[0].length; j++)
				System.out.print(matchMatrix[i][j]+" ");
			System.out.println();
			}
		}

	}
