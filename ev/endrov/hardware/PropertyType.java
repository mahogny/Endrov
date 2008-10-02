package endrov.hardware;

import java.util.Set;
import java.util.TreeSet;

public class PropertyType
	{
	public boolean readOnly=false;
	public Set<String> categories=new TreeSet<String>();
	
	public boolean hasRange=false;
	public double rangeLower, rangeUpper;
	
	public boolean isBoolean=false;
	
	public int foo;
	}
