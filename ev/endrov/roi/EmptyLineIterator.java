package endrov.roi;

//or should one at least supply scanlines? much less overhead. channel,frame,z,y -> [x]
public class EmptyLineIterator extends LineIterator
	{
	public boolean next()
		{
		return false;
		}
	}
