package util2.paperCeExpression.compare;

import java.io.File;
import java.util.Map;

import javax.vecmath.Vector3d;

import util2.paperCeExpression.IntegrateAllExp;


import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.nuc.NucLineage;
import endrov.nuc.NucRemapUtil;
import endrov.nuc.NucSel;
import endrov.nuc.NucVoronoi;
import endrov.nuc.NucLineage.NucInterp;
import endrov.nuc.ccm.MakeCellContactMap;
import endrov.util.EvDecimal;

/**
 * Test how well it works to map single-cell model onto 4 cells + time points.
 * Turns out to work very well! 
 * 
 * TODO voronoi stuff. torbjörn.
 * ceh37 finish lineaging
 * ceh30 example of cell migration
 * 
 * @author Johan Henriksson
 *
 */
public class TestSingleCellPrecision
	{

	public static NucLineage getManualAnnot(EvData data)
		{
		Map<EvPath, NucLineage> lins = data.getIdObjectsRecursive(NucLineage.class);
		for (Map.Entry<EvPath, NucLineage> e : lins.entrySet())
			if (e.getKey().getLeafName().startsWith("1") || e.getKey().getLeafName().startsWith("2"))
				{
				System.out.println("found lineage "+e.getKey());
				return e.getValue();
				}
		return null;
		}
	
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
	
		
		//EvData data=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/AH142_070827.ost"));
		EvData data=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/N2_071114.ost"));
		//EvData data=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/TB2142_071129.ost"));
		NucLineage refLin=getManualAnnot(data);
		
		
		NucLineage approxLin=NucRemapUtil.mapModelToRec(refLin, IntegrateAllExp.loadModel());
		
		EvDecimal firstFrame=refLin.firstFrameOfLineage().fst();
		EvDecimal lastFrame=refLin.lastFrameOfLineage().fst();
		for(EvDecimal curFrame=firstFrame;curFrame.less(lastFrame);curFrame=curFrame.add(new EvDecimal(20)))
			{
			Map<NucSel, NucInterp> allInterpRef=refLin.getInterpNuc(curFrame);
			//Map<NucSel, NucInterp> allInterpApprox=approxLin.getInterpNuc(curFrame);
			
			int countNuc=0;
			double sumLengthError=0;
			double sumRadius=0;
			
			
			
			for(NucSel sel:allInterpRef.keySet())
				{
				NucInterp interpRef=allInterpRef.get(sel);
				NucLineage.Nuc nucRef=sel.getNuc();
				if(interpRef.isVisible())
					if(!nucRef.child.isEmpty() || nucRef.pos.lastKey().lessEqual(curFrame))
						{
						NucLineage.Nuc nucApprox=approxLin.nuc.get(sel.snd());
						if(nucApprox!=null)
							{


							NucInterp interpApprox=nucApprox.interpolatePos(curFrame);
							if(interpApprox!=null && interpApprox.isVisible())
								{
								
								if(!nucApprox.child.isEmpty() || nucApprox.pos.lastKey().lessEqual(curFrame))
									{
									countNuc++;
									
									Vector3d va=interpApprox.pos.getPosCopy();
									Vector3d vb=interpRef.pos.getPosCopy();
									
									va.sub(vb);
									sumLengthError+=va.length();
									sumRadius+=interpApprox.pos.r;
									}
								
								}
							}
						
						
						
						}
				
				
				
				
				}
			
			//average cell distance in model, is the best reference. voronoi...
			double sumAllLength=0;
			double meanAllLength=0;
			try
				{
				int countAllLength=0;
				NucVoronoi vor=MakeCellContactMap.calcneighOneFrame(approxLin.nuc.keySet(), allInterpRef, false);
				//int numCell=vor.nucnames.size();
				for(int i=0;i<vor.nucnames.size();i++)
					if(approxLin.nuc.containsKey(vor.nucnames.get(i)))
						{
						NucLineage.NucInterp iA=approxLin.nuc.get(vor.nucnames.get(i)).interpolatePos(curFrame);
						if(iA!=null)
							{
							Vector3d va=iA.pos.getPosCopy();
							for(int j:vor.vneigh.dneigh.get(i))
								if(approxLin.nuc.containsKey(vor.nucnames.get(j)))
									{
									NucLineage.NucInterp iB=approxLin.nuc.get(vor.nucnames.get(j)).interpolatePos(curFrame);
									if(iB!=null)
										{
										Vector3d vb=iB.pos.getPosCopy();
										vb.sub(va);
										sumAllLength+=vb.length();
										countAllLength++;
										}
									}
							}
						}
				meanAllLength=sumAllLength/countAllLength;
				//NeighMap ccm=MakeCellContactMap.calculateCellMap(approxLin, approxLin.nuc.keySet(), curFrame, curFrame, null);
				//System.out.println(ccm.neighmap);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			
			double avgLengthError=sumLengthError/countNuc;
			double avgRadius=sumRadius/countNuc;
			
			System.out.println(curFrame+"\t"+countNuc+"\t"+avgLengthError+"\t"+meanAllLength+"\t"+avgRadius);
			
			//System.out.println("curframe "+curFrame+"    "+count+"     "+avgLength+"    "+avgRadius+"    "+(avgLength/avgRadius)+"   "+meanAllLength);
			
			}
		
		data.metaObject.get("im").metaObject.put("estcell", approxLin);
		data.saveData();
		
		System.exit(0);
		
		
		
		}
	}
