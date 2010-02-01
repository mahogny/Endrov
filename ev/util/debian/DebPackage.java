/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util.debian;
import java.util.*;


public class DebPackage
	{
	public String name; //debian package file
	public Set<String> linkJars=new HashSet<String>();
	public Set<String> providesFiles=new HashSet<String>();
	
	public boolean isDepends=false;
	public boolean isSuggestion=false;
	public boolean isRecommended=false;
	
	private DebPackage(){}
	
	public DebPackage(String name, String[] linkjar, String[] provides)
		{
		this.name=name;
		isDepends=true;
		if(linkjar!=null)
			for(String s:linkjar)
				linkJars.add(s);
		if(provides!=null)
			for(String s:provides)
				providesFiles.add(s);
		
		
		}
	
	public static DebPackage suggest(String name)
		{
		DebPackage pkg=new DebPackage();
		pkg.name=name;
		pkg.isSuggestion=true;
		return pkg;
		}
	
	public static DebPackage recommends(String name)
		{
		DebPackage pkg=new DebPackage();
		pkg.name=name;
		pkg.isRecommended=true;
		return pkg;
		}
	
	
	}
