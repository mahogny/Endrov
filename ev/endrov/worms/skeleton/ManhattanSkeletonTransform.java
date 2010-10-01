package endrov.worms.skeleton;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.util.Vector2i;

/**
 * Tools for skeleton manipulation based on Manhattan Distance Transform
 * 
 * @author Javier Fernandez
 *
 */
public final class ManhattanSkeletonTransform extends SkeletonTransform
	{
	@Override
	public int[] getNeighbors(int position, int w)
		{
		int neighbors[] = new int[4];
		neighbors[0] = position-w; // Up
		neighbors[1] = position+1; // Right
		neighbors[2] = position+w; // Down
		neighbors[3] = position-1; // Left

		return neighbors;
		}

	public ArrayList<Vector2i> getDirectionalNeighbors(int[] imageArray, int w,
			int currentPixel, int neighborMovement)
		{
		int nList[] = new int[6];

		switch (neighborMovement)
			{
			case 0: // up
				nList[0] = currentPixel-w;
				nList[1] = 0; // up
				nList[2] = currentPixel-w+1;
				nList[3] = 5; // up-right
				nList[4] = currentPixel-w-1;
				nList[5] = 4; // up-left
				break;
			case 1: // right
				nList[0] = currentPixel+1;
				nList[1] = 1; // right
				nList[2] = currentPixel-w+1;
				nList[3] = 5; // right-up
				nList[4] = currentPixel+w+1;
				nList[5] = 7;// right-down
				break;
			case 2: // down
				nList[0] = currentPixel+w;
				nList[1] = 2; // down
				nList[2] = currentPixel+w-1;
				nList[3] = 6;// down-left
				nList[4] = currentPixel+w+1;
				nList[5] = 7; // down-right
				break;
			case 3: // left
				nList[0] = currentPixel-1;
				nList[1] = 3; // left
				nList[2] = currentPixel-1+w;
				nList[3] = 6;// left-down
				nList[4] = currentPixel-1-w;
				nList[5] = 4;// left-up
				break;
			case 4: // up-left
				nList[0] = currentPixel-w-1;
				nList[1] = 4;// up-left
				nList[2] = currentPixel-1;
				nList[3] = 3;// left
				nList[4] = currentPixel-w;
				nList[5] = 0;// up
				break;
			case 5: // up-right
				nList[0] = currentPixel-w+1;
				nList[1] = 5;// up-right
				nList[2] = currentPixel+1;
				nList[3] = 1;// right
				nList[4] = currentPixel-w;
				nList[5] = 0;// up
				break;
			case 6: // down-left
				nList[0] = currentPixel+w-1;
				nList[1] = 6;// down-left
				nList[2] = currentPixel+w;
				nList[3] = 2;// down
				nList[4] = currentPixel-1;
				nList[5] = 3;// left
				break;
			case 7: // down-right
				nList[0] = currentPixel+w+1;
				nList[1] = 7;// down-right
				nList[2] = currentPixel+1;
				nList[3] = 1; // right
				nList[4] = currentPixel+w;
				nList[5] = 2;// down
				break;
			}
		ArrayList<Vector2i> neighbors = new ArrayList<Vector2i>(3);
		for (int i = 0; i<6; i += 2)
			{
			Vector2i n = new Vector2i(nList[i], nList[i+1]);
			neighbors.add(n);
			}
		return neighbors;
		}

	/**
	 * Returns the neighbor that corresponds to the maximum directional movement
	 * from previousPixel to currentPixel, performing the movement
	 * neighborMovement.
	 */
	public Vector2i getMaxDirectionalNeighbor(int[] imageArray, int w,
			int currentPixel, int neighborMovement)
		{
		ArrayList<Vector2i> neighbors = getDirectionalNeighbors(imageArray, w,
				currentPixel, neighborMovement);
		// get the max directional neighbor and its direction

		Vector2i maxVector = neighbors.get(0);
		int max = imageArray[maxVector.x];

		Iterator<Vector2i> it = neighbors.iterator();
		it.next();
		Vector2i n;
		while (it.hasNext())
			{
			n = it.next();
			if (imageArray[n.x]>max)
				{
				max = imageArray[n.x];
				maxVector = n;
				}
			}
		return maxVector;
		}

	/**
	 * Checks whether pixel is a connected pixel in skeleton. A connected pixel is
	 * such that exists two neighbors of the pixel that connect in any direction.
	 * This is diagonal connection or vertical/horizontal connection.
	 */
	public boolean nonConnectedPixel(boolean[] skeleton, int w, int pixel)
		{

		EuclideanSkeletonTransform euclideanSk = new EuclideanSkeletonTransform();
		int neighbors[] = euclideanSk.getNeighbors(pixel, w); // Obtain 8 neighbors
		int length = neighbors.length;
		for (int i = 0; i<4; i++)
			{
			// Check the connection positions for every neighbor
			int p2;
			switch (i)
				{
				case 0:// up
					p2 = 5;
					if (bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// right
					p2 = 4;
					if (bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// left
					p2 = 1;
					if (bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// down-right
					p2 = 3;
					if (bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// down-left
					break;
				case 1:// right
					p2 = 5;
					if (p2<length&&p2>=0&&bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// up
					p2 = 7;
					if (p2<length&&p2>=0&&bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// down
					p2 = 2;
					if (p2<length&&p2>=0&&bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// down-left
					break;
				case 2:// down
					p2 = 7;
					if (p2<length&&p2>=0&&bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// right
					p2 = 6;
					if (p2<length&&p2>=0&&bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// left
					p2 = 3;
					if (p2<length&&p2>=0&&bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// up-left
					break;
				case 3:// left
					p2 = 4;
					if (p2<length&&p2>=0&&bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// up
					p2 = 6;
					if (p2<length&&p2>=0&&bothTrue(skeleton, neighbors[i], neighbors[p2]))
						return false;// down
					break;
				}
			}
		return true;
		}

	/**
	 * True if the positions p1 and p2 are both true in the boolean array 'array'
	 */
	public boolean bothTrue(boolean[] array, int p1, int p2)
		{
		if (p1<0||p2<0)
			return false;
		return (array[p1]&&array[p2]);
		}

	}
