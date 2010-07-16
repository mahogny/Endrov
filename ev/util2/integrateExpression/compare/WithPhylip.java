/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.integrateExpression.compare;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.util.EvFileUtil;
import endrov.util.Tuple;

public class WithPhylip
	{

	/*
	 * 
	 *  /usr/lib/phylip/bin/kitsch
	 * 
	 */
	

	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		new CompareSQL(); //Get password right away so it doesn't stop later
		
		//Load similarities
		final Map<Tuple<File,File>, ColocCoefficients> comparison;
		comparison=CompareAll.loadCache(null, CompareAll.cachedValuesFileAP);
		//comparison=CompareAll.loadCache(datas, CompareAll.cachedValuesFileT);

		//Find out which files there are
		Set<File> datas=new TreeSet<File>();//.getAnnotated();
		for(Tuple<File,File> t:comparison.keySet())
			datas.add(t.fst());


		System.out.println(datas);
		
		Map<File,String> nameMap=new HashMap<File, String>();
		int countName=0;
		StringBuffer sbSed=new StringBuffer();
		for(File fa:datas)
			{
			//Similarity -> disimilarity, just a change of sign
			
			String tempName=("n"+countName+"nnnnnnnnnn").substring(0,10);
			nameMap.put(fa,tempName);
			countName++;
			
			String newName=CompareSQL.getGeneName(fa)+" "+fa.getName();
			
			sbSed.append(" | sed 's/"+tempName+"/"+newName+"/'");
			}
		
		System.out.println("# recordings: "+datas.size());
		
		HashSet<String> strainNames=new HashSet<String>();
		for(File fa:datas)
			{
			String name=fa.getName();
			name=name.substring(0,name.indexOf('_'));
			strainNames.add(name);
			}
		System.out.println("# strains: "+strainNames.size());
		
		for(String type:new String[]{"l2","pearson"})
			{
			StringBuffer sb=new StringBuffer();
			sb.append(""+datas.size()+"\n");
			for(File fa:datas)
				{
				//Similarity -> disimilarity, just a change of sign
				
				String name=nameMap.get(fa);//(fa.getName()+"          ").substring(0,10);
				sb.append(name);
				
				for(File fb:datas)
					{
					ColocCoefficients coef=comparison.get(Tuple.make(fa,fb));
					if(coef==null)
						System.out.println("Null for "+fa+"     "+fb);

					Double v=null;
					if(type.equals("pearson"))
						{
						v=coef.getPearson();
						if(v==null || Double.isInfinite(v) || Double.isNaN(v))
							v=0.0;
						if(fa.equals(fb))
							v=1.0;
						}
					else if(type.equals("l2"))
						{
						v=coef.getL2();
						if(v==null || Double.isInfinite(v) || Double.isNaN(v))
							v=0.5*0.5; //doubtful? but probably not further away
						if(fa.equals(fb))
							v=0.0;
						}
					sb.append("  "+v);
					}
				sb.append("\n");
				}
			
			try
				{
				File basedir=new File(new File(CompareAll.outputBaseDir,"phylip"),type);
				basedir.mkdirs();
				EvFileUtil.writeFile(new File(basedir,"infile"), sb.toString());
				
				String fSed=
				"cat outfile"+sbSed+" > outfile2\n"+
				"cat outtree"+sbSed+" > outtree2\n";
				
				EvFileUtil.writeFile(new File(basedir, "fix.sh"), fSed);
				}
			catch (IOException e)
				{
				e.printStackTrace();
				}
			}
		
		
		
		
		System.exit(0);
		}
	}
