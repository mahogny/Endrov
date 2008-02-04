package evplugin.roi;



public class DebugLineIterator extends LineIterator
	{
	LineIterator it;
	String name;
	public DebugLineIterator(String name,LineIterator it)
		{
		this.it=it;
		this.name=name;
		System.out.println("debug iterator");
		}
	
	public boolean next()
		{
		boolean ret=it.next();
		y=it.y;
		z=it.z;
		ranges=it.ranges;
		
		System.out.println(name+" y:"+y+" z:"+z);
		for(LineRange r:ranges)
			System.out.println(name+"  "+r.start+"#"+r.end);
		System.out.println(name+" toreturn:"+ret);
		
		return ret;
		}

	}
