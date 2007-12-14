package evplugin.roi;



public class DebugLineIterator extends LineIterator
	{
	LineIterator it;
	public DebugLineIterator(LineIterator it)
		{
		this.it=it;
		System.out.println("debug iterator");
		}
	
	public boolean next()
		{
		boolean ret=it.next();
		y=it.y;
		z=it.z;
		ranges=it.ranges;
		
		System.out.println("y:"+y+" z:"+z);
		for(LineRange r:ranges)
			System.out.println(" "+r.start+"#"+r.end);
		System.out.println(" toreturn:"+ret);
		
		return ret;
		}

	}
