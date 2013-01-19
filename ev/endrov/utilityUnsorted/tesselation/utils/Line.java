package endrov.utilityUnsorted.tesselation.utils;

import java.util.ArrayList;

import endrov.util.Vector2i;

/**
 * A 2d line definition
 * 
 * @author Javier Fernandez
 *
 */
public class Line
	{
	public Vector2i p1;
	public Vector2i p2;
	public boolean vertical;
	public double slope;
	public int yLength;

	public Line(Vector2i p1, Vector2i p2)
		{
		this.p1 = p1;
		this.p2 = p2;
		if (p2.x-p1.x==0)
			{
			this.vertical = true;
			this.slope = Integer.MAX_VALUE;
			}
		else if (p2.y-p1.y==0)
			{
			this.slope = 0;
			}
		else
			{
			this.slope = ((double) p2.y-p1.y)/((double) p2.x-p1.x);
			}
		this.yLength = Math.abs((p2.y-p1.y)); // Length in y axis
		}

	public double lineLength()
		{
		return Math.sqrt(Math.pow(p2.y-p1.y, 2)+Math.pow(p2.x-p1.x, 2));
		}

	public int getXGivenY(int y)
		{
		if (vertical)
			return p1.x;
		if (slope==0)
			return -1;
		return (int) Math.round((double) (y-p1.y+slope*(p1.x))/slope);
		}

	public int getYGivenX(int x)
		{
		if (slope==0)
			return p1.y;
		if (vertical)
			return -1;
		return (int) Math.round((double) slope*(x-p1.x)+p1.y);
		}

	/**
	 * Calculates the relative position of the pixel with respect to the
	 * width of the image to which it belongs
	 * @param pos
	 * @param w
	 * @return
	 */
	public static int posToPixel(Vector2i pos, int w){
		return ((pos.x)+pos.y*w);
	}
	
	/**
	 * Return the image pixels that conform the line according to the pixel
	 * matcher
	 */
	public ArrayList<Integer> getLinePoints(int imageWidth)
		{

		ArrayList<Integer> points = new ArrayList<Integer>();
		Vector2i temp = new Vector2i();

		if (slope!=0)
			{

			if (Math.abs(slope)>1)
				{ // follow y axis
				int init = p1.y;
				int end = p2.y;
				int step = (p1.y<p2.y) ? 1 : -1;
				int count = 0;
				int total = Math.abs(init-end)+1;
				for (int hy = init; count<total; hy += step)
					{
					temp.x = getXGivenY(hy);
					temp.y = hy;
					points.add(posToPixel(temp,imageWidth));
					count++;
					}
				}
			else
				{// Follow x axis
				int init = p1.x;
				int end = p2.x;
				int step = (p1.x<p2.x) ? 1 : -1;
				int count = 0;
				int total = Math.abs(init-end)+1;
				for (int hx = init; count<total; hx += step)
					{
					temp.x = hx;
					temp.y = getYGivenX(hx);
					points.add(posToPixel(temp,imageWidth));
					count++;
					}
				}
			}
		else
			{ // Draw horizontal line

			int init = p1.x;
			int end = p2.x;
			int step = (p1.x<p2.x) ? 1 : -1;
			int count = 0;
			int total = Math.abs(init-end)+1;
			for (int hx = init; count<total; hx += step)
				{
				temp.x = hx;
				temp.y = p1.y;
				points.add(posToPixel(temp,imageWidth));
				count++;
				}
			}
		return points;
		}

	}

