package endrov.hardware;

import java.util.*;

/**
 * Hardware device property type
 * @author Johan Henriksson
 */
public class PropertyType
	{
	public boolean readOnly=false;
	public TreeSet<String> categories=new TreeSet<String>();
	
	public boolean hasRange=false;
	public double rangeLower, rangeUpper;
	
	public boolean isBoolean=false;
	
	
	
	
	public static PropertyType getEditableBooleanState()
		{
		PropertyType p=new PropertyType();
		p.categories.add("0");
		p.categories.add("1");
		p.isBoolean=true;
		return p;
		}

	public static PropertyType getEditableIntState(int min,int max)
		{
		PropertyType p=new PropertyType();
		for(int i=min;i<=max;i++)
			p.categories.add(""+i);
		return p;
		}

	public static PropertyType getEditableIntState(int[] state)
		{
		PropertyType p=new PropertyType();
		for(int s:state)
			p.categories.add(""+s);
		return p;
		}

	public static PropertyType getEditableCategoryState(String[] stateName)
		{
		PropertyType p=new PropertyType();
		p.categories.addAll(Arrays.asList(stateName));
		return p;
		}

	
	
	}
