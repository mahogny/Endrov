/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi;

import java.util.LinkedList;

//or should one at least supply scanlines? much less overhead. channel,frame,z,y -> [x]
public abstract class LineIterator
	{
	public int y;
	public double z;
	
	//new elements must be generated for every next
	public LinkedList<LineRange> ranges=new LinkedList<LineRange>();
	
	public abstract boolean next();
	
	public static class LineRange
		{
		public LineRange(int start, int end)
			{
			this.start=start;
			this.end=end;
			}
		public LineRange()
			{
			}
		public int start, end;
		}
	}
