import java.util.*;


public class DebPackage
	{
	public String name; //debian package file
	public Set<String> linkJars=new HashSet<String>();
	public Set<String> providesFiles=new HashSet<String>();
	
	
	public DebPackage(String name, String[] linkjar, String[] provides)
		{
		this.name=name;
		if(linkjar!=null)
			for(String s:linkjar)
				linkJars.add(s);
		if(provides!=null)
			for(String s:provides)
				providesFiles.add(s);
		
		
		}
	
	}
