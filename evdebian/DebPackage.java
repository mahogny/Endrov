import java.util.*;


public class DebPackage
	{
	public String name; //debian package file
	public String bp; //build-classpath name
	public Set<String> providesFiles=new HashSet<String>();
	
	
	public DebPackage(String name, String bp, String[] provides)
		{
		this.name=name;
		this.bp=bp;
		if(provides!=null)
			for(String s:provides)
				providesFiles.add(s);
		
		
		}
	
	}
