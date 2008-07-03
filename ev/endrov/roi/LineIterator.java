package endrov.roi;

import java.util.LinkedList;

//or should one at least supply scanlines? much less overhead. channel,frame,z,y -> [x]
public abstract class LineIterator
	{
	public int y,z;
	
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
