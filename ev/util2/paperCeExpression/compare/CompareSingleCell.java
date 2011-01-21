/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.compare;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.flowColocalization.ColocCoefficients;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.EvListUtil;
import endrov.util.EvParallel;
import endrov.util.Tuple;

/**
 * Pairwise comparison of recordings
 * @author Johan Henriksson
 *
 */
public class CompareSingleCell
	{

	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later
	
		Set<String> argsSet=new HashSet<String>();
		for(String s:args)
			argsSet.add(s);
	
		//Find recordings to compare
		Set<File> datas=PaperCeExpressionUtil.getAnnotated(); 
		//Set<File> datas=IntExpFileUtil.getTestSet();

		System.out.println(datas);
		System.out.println("Number of annotated strains: "+datas.size());

		EvData totalData=EvData.loadFile(new File("/Volumes/TBU_main06/summary.ost"));
		final NucLineage totLin=totalData.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		
		final Map<Tuple<File,File>, ColocCoefficients> comparisonSS;
		comparisonSS=CompareAll.loadCache(datas, CompareAll.cachedValuesFileSS);

		
		final EvDecimal dt=new EvDecimal("20"); //[s]

		int numThread=8;
		EvParallel.map_(numThread,new LinkedList<Tuple<File,File>>(EvListUtil.productSet(datas, datas)), new EvParallel.FuncAB<Tuple<File,File>,Object>(){
		public Object func(Tuple<File,File> key)
			{
			File ina=key.fst();
			File inb=key.snd();
			
			System.out.println("----- "+ina+"           vs         "+inb);
			String expA=AssembleSingleCell.getExpName(ina);
			String expB=AssembleSingleCell.getExpName(inb);
			
			ColocCoefficients coeff=new ColocCoefficients();
			for(NucLineage.Nuc nuc:totLin.nuc.values())
				if(!nuc.pos.isEmpty())
				{
				EvDecimal endtime=nuc.getLastFrame();
				if(endtime==null)
					endtime=nuc.pos.lastKey();
				
				NucExp expLevelA=nuc.exp.get(expA);
				NucExp expLevelB=nuc.exp.get(expB);
				if(expLevelA!=null && expLevelB!=null && !expLevelA.level.isEmpty() && !expLevelB.level.isEmpty())
					for(EvDecimal time=nuc.getFirstFrame();time.less(endtime);time=time.add(dt))
						{
						double levelA=expLevelA.interpolateLevel(time);
						double levelB=expLevelB.interpolateLevel(time);
						coeff.add(levelA, levelB);
						}
				}
			if(coeff.n!=0)
				synchronized (comparisonSS)
					{
					comparisonSS.put(Tuple.make(ina, inb), coeff);
					}
			
			return null;
			}
		});
		
		CompareAll.storeCache(comparisonSS, CompareAll.cachedValuesFileSS);
		
		System.exit(0);
		
		}
	
	
	}
