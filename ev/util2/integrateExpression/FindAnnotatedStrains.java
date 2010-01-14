/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import endrov.util.Tuple;

/**
 * Find strains that has not been annotated: tagDone4d
 * @author Johan Henriksson
 *
 */
public class FindAnnotatedStrains
	{

	public static Set<File> getAnnotated()
		{
		Set<File> doneStrains=new TreeSet<File>();
		for(File parent:new File[]{new File("/Volumes/TBU_main06/ost4dgood")})
			for(File f:parent.listFiles())
				if(f.getName().endsWith(".ost"))
					{
					if(new File(f,"tagDone4d.txt").exists())
						doneStrains.add(f);
					}
		return doneStrains;
		}

	
	
	
	public static void main(String[] args)
		{
		Set<String> strains=new TreeSet<String>();
		Set<String> doneStrains=new TreeSet<String>();
		
		
		for(File parent:new File[]{
				new File("/Volumes/TBU_main01/ost4dgood"),
				new File("/Volumes/TBU_main02/ost4dgood"),
				new File("/Volumes/TBU_main03/ost4dgood"),
				new File("/Volumes/TBU_main04/ost4dgood"),
		})
			for(File f:parent.listFiles())
				if(f.getName().endsWith(".ost"))
					{
					
					Tuple<String,String> nameDate=ExpUtil.nameDateFromOSTName(f.getName());
					
					String strainName=nameDate.fst();
					
					strains.add(strainName);
					
					if(new File(f,"tagDone4d.txt").exists())
						doneStrains.add(strainName);
					}

		for(String strain:strains)
			if(!doneStrains.contains(strain))
				System.out.println("Appears missing: "+strain);
		System.exit(0);
		}
	
	}
