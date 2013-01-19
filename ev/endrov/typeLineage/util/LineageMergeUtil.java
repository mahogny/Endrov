package endrov.typeLineage.util;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import util2.paperCeExpression.compare.CompareAll;

import endrov.typeCoordinateSystem.CoordinateSystem;
import endrov.typeFrameTime.FrameTime;
import endrov.typeLineage.Lineage;
import endrov.typeLineage.LineageExp;
import endrov.util.math.EvDecimal;

/**
 * Utilities for merging lineages
 * 
 * @author Johan Henriksson
 *
 */
public class LineageMergeUtil
	{
	/**
	 * Take the expression pattern from one lineage to another by remapping the time of each individual cell
	 */
	public static void mapExpression(Lineage fromLin, Lineage toLin, String fromExpName, String toExpName)
		{
		//For all particles
		for(Map.Entry<String, Lineage.Particle> recNucE:fromLin.particle.entrySet())
			{
			Lineage.Particle fromNuc=recNucE.getValue();
			Lineage.Particle toNuc=toLin.particle.get(recNucE.getKey());
			
			//Do this particle if it exists in the reference
			if(toNuc!=null && !fromNuc.pos.isEmpty() && !toNuc.pos.isEmpty())
				{
				LineageExp toExp=toNuc.getCreateExp(toExpName);
				toExp.level.clear(); //Not needed at the moment since it is assembled "de novo"
		
				//Prepare to remap time on local cell level. Note that if a cell is the last cell then remapping can be tricky. this is not considered here.
				EvDecimal recFirstFrame=fromNuc.getFirstFrame();
				EvDecimal recLastFrame=fromNuc.getLastFrame();
				if(recLastFrame==null)
					recLastFrame=fromNuc.pos.lastKey();
				EvDecimal recDiff=recLastFrame.subtract(recFirstFrame);
				
				EvDecimal toFirstFrame=toNuc.getFirstFrame();
				EvDecimal toLastFrame=toNuc.getLastFrame();
				if(toLastFrame==null)
					toLastFrame=toNuc.pos.lastKey();
				EvDecimal toDiff=toLastFrame.subtract(toFirstFrame);
				
				//Transfer levels. Remap time
				LineageExp fromExp=fromNuc.exp.get(fromExpName);
				if(fromExp!=null)
					for(Map.Entry<EvDecimal, Double> e:fromExp.level.entrySet())
						{
						EvDecimal toFrame=(e.getKey().subtract(recFirstFrame).multiply(toDiff).divide(recDiff)).add(toFirstFrame);
						toExp.level.put(toFrame, e.getValue());
						}
				}
			}
		
		}

	/**
	 * Super-impose standard model onto recording by rotation, scaling and changing times 
	 */
	public static Lineage mapModelToRec(Lineage reflin, Lineage intoLin)
		{
		Lineage returnLin=new Lineage();
		
		//Time normalization
		FrameTime ftRef=CompareAll.buildFrametime(reflin);
		FrameTime ftStd=CompareAll.buildFrametime(intoLin);
		
		//Coordinate transform
		CoordinateSystem csStd=LineageMergeUtil.singlecellCSfromLin(intoLin);
		CoordinateSystem csRef=LineageMergeUtil.singlecellCSfromLin(reflin);
		Matrix4d mStd=csStd.getTransformToSystem();   //This should put std into the origo, more or less
		Matrix4d mRef=csRef.getTransformToWorld();    //The inverse of putting ref in the origo
		Matrix4d transform=new Matrix4d();
		transform.mul(mRef, mStd);
		
		//For all particles
		for(String particleName:intoLin.particle.keySet())	
			{
			Lineage.Particle oldParticle=intoLin.particle.get(particleName);
			Lineage.Particle newParticle=returnLin.getCreateParticle(particleName);
			
			newParticle.child.addAll(oldParticle.child);
			newParticle.parents.addAll(oldParticle.parents);

			for(Map.Entry<EvDecimal, Lineage.ParticlePos> e:new HashMap<EvDecimal, Lineage.ParticlePos>(oldParticle.pos).entrySet())
				{
				Lineage.ParticlePos oldNucpos=e.getValue();
				Lineage.ParticlePos newNucpos=new Lineage.ParticlePos();
				
				//Normalize time
				EvDecimal oldframe=e.getKey();
				EvDecimal newframe=ftRef.mapTime2Frame(ftStd.mapFrame2Time(oldframe));
				newParticle.pos.put(newframe, newNucpos);
				//newpos.put(newframe,nucpos);

				//Normalize coordinate
				Vector4d transformPos=new Vector4d(oldNucpos.x,oldNucpos.y,oldNucpos.z,1);
				transform.transform(transformPos);
				newNucpos.x=transformPos.x;
				newNucpos.y=transformPos.y;
				newNucpos.z=transformPos.z;
				newNucpos.r=oldNucpos.r;
	
				}
			}
	
		return returnLin;
		}

	/**
	 * Set up coordinate system
	 */
	public static CoordinateSystem singlecellCSfromLin(Lineage refLin)
		{
		Lineage.Particle nucP2 = refLin.particle.get("P2'");
		Lineage.Particle nucEMS = refLin.particle.get("EMS");
	
		Vector3d posABp = getLastPosABp(refLin);
		Vector3d posABa = getLastPosABa(refLin);
	
		if (nucP2==null||posABa==null||posABp==null||nucEMS==null
				||nucP2.pos.isEmpty()
				||nucEMS.pos.isEmpty())
			{
			System.out.println("Does not have enough cells marked, no single-cell");
			return null;
			}
	
		Vector3d posP2 = nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy();
		Vector3d posEMS = nucEMS.pos.get(nucEMS.pos.lastKey()).getPosCopy();
	
		Vector3d v1 = new Vector3d();
		Vector3d v2 = new Vector3d();
		v1.sub(posABa, posP2);
		v2.sub(posEMS, posABp);
	
		// By using all 4 cells for mid it should be less sensitive to
		// abberrations
		Vector3d mid = new Vector3d();
		mid.add(posABa);
		mid.add(posABp);
		mid.add(posEMS);
		mid.add(posP2);
		mid.scale(0.25);
	
		// Create coordinate system. Enlarge by 20%
		CoordinateSystem cs = new CoordinateSystem();
		double scale = 1.35;
		cs.setFromTwoVectors(v1, v2, v1.length()*scale, v2.length()*scale, v2.length()*scale, mid);
	
		return cs;
		}

	public static Vector3d getLastPosABp(Lineage refLin)
		{
		Lineage.Particle nucABp = refLin.particle.get("ABp");
		if (nucABp!=null && !nucABp.pos.isEmpty())
			{
			Vector3d posABp = nucABp.pos.get(nucABp.pos.lastKey()).getPosCopy();
			return posABp;
			}
		else 
			{
			Lineage.Particle nucABpl = refLin.particle.get("ABpl");
			Lineage.Particle nucABpr = refLin.particle.get("ABpr");
			if (nucABpl!=null && nucABpr!=null  && !nucABpl.pos.isEmpty() && !nucABpr.pos.isEmpty())
				{
				Vector3d a = nucABpl.pos.get(nucABpl.pos.lastKey()).getPosCopy();
				Vector3d b = nucABpr.pos.get(nucABpr.pos.lastKey()).getPosCopy();
				a.add(b);
				a.scale(0.5);
				return a;
				}
			else
				return null;
			}
		}

	public static Vector3d getLastPosABa(Lineage refLin)
		{
		Lineage.Particle nucABa = refLin.particle.get("ABa");
		if (nucABa!=null && !nucABa.pos.isEmpty())
			{
			Vector3d posABa = nucABa.pos.get(nucABa.pos.lastKey()).getPosCopy();
			return posABa;
			}
		else 
			{
			Lineage.Particle nucABal = refLin.particle.get("ABal");
			Lineage.Particle nucABar = refLin.particle.get("ABar");
			if (nucABal!=null && nucABar!=null  && !nucABal.pos.isEmpty() && !nucABar.pos.isEmpty())
				{
				Vector3d a = nucABal.pos.get(nucABal.pos.lastKey()).getPosCopy();
				Vector3d b = nucABar.pos.get(nucABar.pos.lastKey()).getPosCopy();
				a.add(b);
				a.scale(0.5);
				return a;
				}
			else
				return null;
			}
		}

	}
