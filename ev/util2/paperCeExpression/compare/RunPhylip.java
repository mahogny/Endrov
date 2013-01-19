/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.compare;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.util.EvFileUtil;
import endrov.util.Tuple;

public class RunPhylip
	{

	/*
	 * 
	 *  For the file of interest, run
	 *  /usr/lib/phylip/bin/kitsch infile
	 * 
	 *  then sh fix.sh
	 *  Then use a viewer to render the tree
	 * 
	 */
	
	
	public static void doFor(File comparisonFile, String base)
		{
		System.out.println("profile -------------------------   "+comparisonFile);
		
		//Load similarities
		final Map<Tuple<File,File>, ColocCoefficients> comparison;
		comparison=CompareAll.loadCache(null, comparisonFile);
		//comparison=CompareAll.loadCache(datas, CompareAll.cachedValuesFileT);

		//Find out which files there are
		Set<File> datas=PaperCeExpressionUtil.getAnnotated();//new TreeSet<File>();//.getAnnotated();
		/*
		for(Tuple<File,File> t:comparison.keySet())
			IntExpFileUtil.getAnnotated()
			if(t.fst().exists())
				datas.add(t.fst());*/

		//Only keep those actually calculated
		Set<File> found=new TreeSet<File>();
		for(Tuple<File,File> a:comparison.keySet())
			found.add(a.fst());
		datas.retainAll(found);
		
		

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
			
			String newName=PaperCeExpressionUtil.getGeneName(fa)+" "+fa.getName();
			
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
						System.out.println("-----------------Null for "+fa+"     "+fb);

					Double v=null;
					if(type.equals("pearson"))
						{
						
						v=1.0-Math.abs(coef.getPearson());
						if(fa.equals(fb))
							System.out.println("should be 0: "+v); //may need to force 0
						if(v==null || Double.isInfinite(v) || Double.isNaN(v))
							v=0.0;
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
				File basedir=new File(new File(CompareAll.outputBaseDir,"phylip"),base+"-"+type);
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
		
		
		}
	

	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later
		
		doFor(CompareAll.cachedValuesFileT,"t");
		doFor(CompareAll.cachedValuesFileAP,"apt");
		doFor(CompareAll.cachedValuesFileXYZ,"xyz");          
		doFor(CompareAll.cachedValuesFileSS,"ss");
		
		
		
		System.exit(0);
		}
	}
