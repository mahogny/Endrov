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

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;
import util2.paperCeExpression.integrate.IntExp;

import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.frameTime.FrameTime;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;

/**
 * Pairwise comparison of recordings
 * @author Johan Henriksson
 *
 */
public class AssembleSingleCell
	{
	public final static File cachedValuesFileT=new File(CompareAll.outputBaseDir,"comparisonSS.xml");

	public static NucLineage getSingleCellLin(EvData data)
		{
		Map<EvPath, NucLineage> lins = data.getIdObjectsRecursive(NucLineage.class);
		for (Map.Entry<EvPath, NucLineage> e : lins.entrySet())
			if (e.getKey().getLeafName().startsWith("estcell"))
				{
				System.out.println("found lineage "+e.getKey());
				return e.getValue();
				}
		return null;
		}

	public static NucLineage getInLinT(EvData data)
		{
		Map<EvPath, NucLineage> lins = data.getIdObjectsRecursive(NucLineage.class);
		for (Map.Entry<EvPath, NucLineage> e : lins.entrySet())
			if (e.getKey().getLeafName().startsWith("AP1-GFP"))
				{
				System.out.println("found lineage "+e.getKey());
				return e.getValue();
				}
		return null;
		}

	
	public static NucLineage getInLinAPT(EvData data)
		{
		Map<EvPath, NucLineage> lins = data.getIdObjectsRecursive(NucLineage.class);
		for (Map.Entry<EvPath, NucLineage> e : lins.entrySet())
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
	
	
	public static void assembleSingleCellLin(NucLineage recLin, NucLineage totLin, File in, String totExpName)
		{
		
		if(recLin==null)
			{
			System.out.println("Not done!!!!!!           "+in);
			return;
//			continue;
			}
		
		String recExpName=CompareAll.expName;
		
		//For all nuclei
		for(Map.Entry<String, NucLineage.Nuc> recNucE:recLin.nuc.entrySet())
			{
			NucLineage.Nuc recNuc=recNucE.getValue();
			NucLineage.Nuc totNuc=totLin.nuc.get(recNucE.getKey());
			
			//Do this nucleus if it exists in the reference
			if(totNuc!=null && !recNuc.pos.isEmpty() && !totNuc.pos.isEmpty())
				{
				NucExp totExp=totNuc.getCreateExp(totExpName);
				totExp.level.clear(); //Not needed at the moment since it is assembled "de novo"

				//Prepare to remap time on local cell level. Note that if a cell is the last cell then remapping can be tricky. this is not considered here.
				EvDecimal recFirstFrame=recNuc.getFirstFrame();
				EvDecimal recLastFrame=recNuc.getLastFrame();
				if(recLastFrame==null)
					recLastFrame=recNuc.pos.lastKey();
				EvDecimal recDiff=recLastFrame.subtract(recFirstFrame);
				
				EvDecimal totFirstFrame=totNuc.getFirstFrame();
				EvDecimal totLastFrame=totNuc.getLastFrame();
				if(totLastFrame==null)
					totLastFrame=totNuc.pos.lastKey();
				EvDecimal totDiff=totLastFrame.subtract(totFirstFrame);
				
				//Transfer levels. Remap time
				NucExp recExp=recNuc.exp.get(recExpName);
				if(recExp!=null)
					for(Map.Entry<EvDecimal, Double> e:recExp.level.entrySet())
						{
						EvDecimal totFrame=(e.getKey().subtract(recFirstFrame).multiply(totDiff).divide(recDiff)).add(totFirstFrame);
						totExp.level.put(totFrame, e.getValue());
						}
				}
			}
		
		}
	
	
	public static void assembleAPT(String label, NucLineage aptLin, NucLineage recSingleCell, NucLineage refLin, NucLineage outLin, File in, String totExpName)
		{
		if(aptLin==null)
			{
			System.out.println("APT/T Not done!!!!!!           "+in);
			return;
			}

		FrameTime ftRef=CompareAll.buildFrametime(refLin);
		FrameTime ftRec=CompareAll.buildFrametime(recSingleCell);

		String recExpName=CompareAll.expName;
		
		//For all nuclei
		for(Map.Entry<String, NucLineage.Nuc> recNucE:aptLin.nuc.entrySet())
			{
			NucLineage.Nuc recNuc=recNucE.getValue();
			NucLineage.Nuc outNuc=outLin.getCreateNuc(/*label+" "+*/recNucE.getKey());
			
			NucExp totExp=outNuc.getCreateExp(totExpName);
			totExp.level.clear(); //Not needed at the moment since it is assembled "de novo"

			//Transfer levels. Remap time
			NucExp recExp=recNuc.exp.get(recExpName);
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
		EvLog.listeners.add(new EvLogStdout());
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
				if(IntExp.isDone(f))
					datas2.add(f);
			datas=datas2;
			}

		System.out.println(datas);
		System.out.println("Number of annotated strains: "+datas.size());

		EvData totalData=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/celegans2008.2.ost"));
		final NucLineage totLinSingleCell=totalData.getIdObjectsRecursive(NucLineage.class).values().iterator().next();

		NucLineage totLinAPT=new NucLineage();
		NucLineage totLinT=new NucLineage();
		
		for(File in:datas)
			{
			System.out.println("starting      "+in);
			if(CompareAll.ensureCalculated(in))
				{
				EvData dataFile=EvData.loadFile(in);
				NucLineage inLinSingleCell=getSingleCellLin(dataFile);

				String totExpName=PaperCeExpressionUtil.getGeneName(in)+"_"+in.getName();
				//Do if not done. do single-cell
				assembleSingleCellLin(inLinSingleCell, totLinSingleCell, in, totExpName);
				
				//Do the rest
				NucLineage inLinAPT=getInLinAPT(dataFile);
				NucLineage inLinT=getInLinT(dataFile);
				assembleAPT("apt", inLinAPT, inLinSingleCell, totLinSingleCell, totLinAPT, in, totExpName);
				assembleAPT("t", inLinT, inLinSingleCell, totLinSingleCell, totLinT, in, totExpName);
				}
			}
		
		
		//Save down
		try
			{
			EvData out=new EvData();
			out.metaObject.put("ss", totLinSingleCell);
			out.metaObject.put("apt", totLinAPT);
			out.metaObject.put("t", totLinT);
			out.saveDataAs(new File("/Volumes/TBU_main06/summary2.ost"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		
		System.exit(0);
		
		}
	
	
	}
