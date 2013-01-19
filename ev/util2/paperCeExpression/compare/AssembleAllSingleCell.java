/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.compare;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util2.paperCeExpression.IntegrateAllExp;
import util2.paperCeExpression.collectData.PaperCeExpressionUtil;

import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.frameTime.FrameTime;
import endrov.lineage.Lineage;
import endrov.lineage.LineageExp;
import endrov.lineage.util.LineageMergeUtil;
import endrov.util.EvDecimal;

/**
 * Pairwise comparison of recordings
 * @author Johan Henriksson
 *
 */
public class AssembleAllSingleCell
	{
	public final static File cachedValuesFileT=new File(CompareAll.outputBaseDir,"comparisonSS.xml");

	public static Lineage getSingleCellLin(EvData data)
		{
		Map<EvPath, Lineage> lins = data.getIdObjectsRecursive(Lineage.class);
		for (Map.Entry<EvPath, Lineage> e : lins.entrySet())
			if (e.getKey().getLeafName().startsWith("estcell"))
				{
				System.out.println("found lineage "+e.getKey());
				return e.getValue();
				}
		return null;
		}

	public static Lineage getInLinT(EvData data)
		{
		Map<EvPath, Lineage> lins = data.getIdObjectsRecursive(Lineage.class);
		for (Map.Entry<EvPath, Lineage> e : lins.entrySet())
			if (e.getKey().getLeafName().startsWith("AP1-GFP"))
				{
				System.out.println("found lineage "+e.getKey());
				return e.getValue();
				}
		return null;
		}

	
	public static Lineage getInLinAPT(EvData data)
		{
		Map<EvPath, Lineage> lins = data.getIdObjectsRecursive(Lineage.class);
		for (Map.Entry<EvPath, Lineage> e : lins.entrySet())
			if (e.getKey().getLeafName().startsWith("AP20-GFP"))
				{
				System.out.println("found lineage "+e.getKey());
				return e.getValue();
				}
		return null;
		}

	
	public static String getExpName(File in)
		{
		return PaperCeExpressionUtil.getGeneName(in)+"_"+in.getName();
		}
	
	
	public static void assembleAPT(String label, 
			Lineage aptLin, //Data pulled out into APT lineage. Referenced by rec-frame
			Lineage recSingleCell, //The single-cell lineage mapped to recording 
			Lineage refLin, //Reference model
			Lineage outLin, File in, String totExpName)
		{
		if(aptLin==null)
			{
			System.out.println("APT/T Not done!!!!!!           "+in);
			return;
			}
		if(recSingleCell==null)
			throw new RuntimeException("Recording has to single-cell lineage mapped");
		
		
		FrameTime ftRef=CompareAll.buildFrametime(refLin);
		FrameTime ftRec=CompareAll.buildFrametime(recSingleCell);

		String recExpName=CompareAll.expName;
		
		//For all nuclei
		for(Map.Entry<String, Lineage.Particle> recNucE:aptLin.particle.entrySet())
			{
			Lineage.Particle recNuc=recNucE.getValue();
			Lineage.Particle outNuc=outLin.getCreateParticle(/*label+" "+*/recNucE.getKey());
			
			LineageExp totExp=outNuc.getCreateExp(totExpName);
			totExp.level.clear(); //Not needed at the moment since it is assembled "de novo"

			//Transfer levels. Remap time
			LineageExp recExp=recNuc.exp.get(recExpName);
			if(recExp!=null)
				for(Map.Entry<EvDecimal, Double> e:recExp.level.entrySet())
					{
					//Remap time
					EvDecimal totFrame=ftRef.mapTime2Frame(ftRec.mapFrame2Time(e.getKey()));  
					totExp.level.put(totFrame, e.getValue());
					
					//Create time points
					if(outNuc.overrideStart==null || outNuc.overrideStart.greater(totFrame))
						outNuc.overrideStart=totFrame;
					if(outNuc.overrideEnd==null || outNuc.overrideEnd.less(totFrame))
						outNuc.overrideEnd=totFrame;
					}
			}
		
		}
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later
	
		Set<String> argsSet=new HashSet<String>();
		for(String s:args)
			argsSet.add(s);
	
		//Find recordings to compare
		Set<File> datas=PaperCeExpressionUtil.getAnnotated(); 
		//Set<File> datas=IntExpFileUtil.getTestSet();
	
		//Use only test set?
		if(argsSet.contains("test"))
			datas=PaperCeExpressionUtil.getTestSet();
	
		//Use only calculated recordings?
		if(argsSet.contains("onlycalculated"))
			{
			System.out.println("---- only calculated");
			Set<File> datas2=new HashSet<File>();
			for(File f:datas)
				if(IntegrateAllExp.isDone(f))
					datas2.add(f);
			datas=datas2;
			}

		System.out.println(datas);
		System.out.println("Number of annotated strains: "+datas.size());

		final Lineage totLinSingleCell=PaperCeExpressionUtil.loadModel();
		

		Lineage totLinAPT=new Lineage();
		Lineage totLinT=new Lineage();
		
		for(File in:datas)
			{
			System.out.println("starting      "+in);
			if(CompareAll.ensureCalculated(in))
				{
				EvData dataFile=EvData.loadFile(in);
				Lineage inLinSingleCell=getSingleCellLin(dataFile);
				//Can only do the mapping if there is a single-cell lineage 
				if(inLinSingleCell!=null)
					{
					String toExpName=PaperCeExpressionUtil.getGeneName(in)+"_"+in.getName();
					//Do if not done: do single-cell
					if(inLinSingleCell!=null)
						LineageMergeUtil.mapExpression(inLinSingleCell, totLinSingleCell, CompareAll.expName, toExpName);
//					else
	//					System.out.println("Not done!!!!!!           "+in);
					
					//Do the rest
					Lineage inLinAPT=getInLinAPT(dataFile);
					Lineage inLinT=getInLinT(dataFile);
					assembleAPT("apt", inLinAPT, inLinSingleCell, totLinSingleCell, totLinAPT, in, toExpName);
					assembleAPT("t", inLinT, inLinSingleCell, totLinSingleCell, totLinT, in, toExpName);
					}

				}
			}
		
		
		//Save down
		try
			{
			EvData out=new EvData();
			out.metaObject.put("ss", totLinSingleCell);
			out.metaObject.put("apt", totLinAPT);
			out.metaObject.put("t", totLinT);
			out.saveDataAs(new File("/Volumes/TBU_main06/ostmodel/summary2.ost"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		
		System.exit(0);
		
		}
	
	
	}
