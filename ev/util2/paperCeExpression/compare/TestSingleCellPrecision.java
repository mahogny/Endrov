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
import endrov.lineage.Lineage;
import endrov.lineage.LineageSelParticle;
import endrov.lineage.Lineage.InterpolatedParticle;
import endrov.lineage.util.LineageMergeUtil;
import endrov.lineage.util.LineageVoronoi;
import endrov.lineage.util.MakeParticleContactMap;
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

	public static Lineage getManualAnnot(EvData data)
		{
		Map<EvPath, Lineage> lins = data.getIdObjectsRecursive(Lineage.class);
		for (Map.Entry<EvPath, Lineage> e : lins.entrySet())
			if (e.getKey().getLeafName().startsWith("1") || e.getKey().getLeafName().startsWith("2"))
				{
				System.out.println("found lineage "+e.getKey());
				return e.getValue();
				}
		return null;
		}
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
	
		
		//EvData data=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/AH142_070827.ost"));
		EvData data=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/N2_071114.ost"));
		//EvData data=EvData.loadFile(new File("/Volumes/TBU_main06/ost4dgood/TB2142_071129.ost"));
		Lineage refLin=getManualAnnot(data);
		
		
		Lineage approxLin=LineageMergeUtil.mapModelToRec(refLin, IntegrateAllExp.loadModel());
		
		EvDecimal firstFrame=refLin.firstFrameOfLineage(false).fst();
		EvDecimal lastFrame=refLin.lastFrameOfLineage(false).fst();
		for(EvDecimal curFrame=firstFrame;curFrame.less(lastFrame);curFrame=curFrame.add(new EvDecimal(20)))
			{
			Map<LineageSelParticle, InterpolatedParticle> allInterpRef=refLin.interpolateParticles(curFrame);
			//Map<NucSel, NucInterp> allInterpApprox=approxLin.getInterpNuc(curFrame);
			
			int countNuc=0;
			double sumLengthError=0;
			double sumRadius=0;
			
			
			
			for(LineageSelParticle sel:allInterpRef.keySet())
				{
				InterpolatedParticle interpRef=allInterpRef.get(sel);
				Lineage.Particle nucRef=sel.getParticle();
				if(interpRef.isVisible())
					if(!nucRef.child.isEmpty() || nucRef.pos.lastKey().lessEqual(curFrame))
						{
						Lineage.Particle nucApprox=approxLin.particle.get(sel.snd());
						if(nucApprox!=null)
							{


							InterpolatedParticle interpApprox=nucApprox.interpolatePos(curFrame);
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
				LineageVoronoi vor=MakeParticleContactMap.calcneighOneFrame(approxLin.particle.keySet(), allInterpRef, false);
				//int numCell=vor.nucnames.size();
				for(int i=0;i<vor.nucnames.size();i++)
					if(approxLin.particle.containsKey(vor.nucnames.get(i)))
						{
						Lineage.InterpolatedParticle iA=approxLin.particle.get(vor.nucnames.get(i)).interpolatePos(curFrame);
						if(iA!=null)
							{
							Vector3d va=iA.pos.getPosCopy();
							for(int j:vor.vneigh.dneigh.get(i))
								if(approxLin.particle.containsKey(vor.nucnames.get(j)))
									{
									Lineage.InterpolatedParticle iB=approxLin.particle.get(vor.nucnames.get(j)).interpolatePos(curFrame);
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
