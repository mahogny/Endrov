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
	/*
	public boolean isDepends=false;
	public boolean isSuggestion=false;
	public boolean isRecommended=false;
	*/
	private DebPackage(){}
	
	
	public enum PkgType{Depends, Suggests, Recommends}
	
	public PkgType type;
	
	/**
	 * Normal dependency
	 * @param name Name of required package
	 * @param linkjar JAR-files to link to in the system
	 * @param provides Files currently in libs/ that can be deleted
	 */
	public DebPackage(String name, String[] linkjar, String[] provides)
		{
		this.type=PkgType.Depends;
		this.name=name;
//		isDepends=true;
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
		pkg.type=PkgType.Suggests;
		//pkg.isSuggestion=true;
		return pkg;
		}

	/**
	 * Recommended package dependency
	 */
	public static DebPackage recommends(String name, String[] linkjar, String[] provides)
		{
		DebPackage pkg=new DebPackage(name, linkjar, provides);
		pkg.type=PkgType.Recommends;
		//pkg.isRecommended=true;
		return pkg;
		}
	
	
	}
