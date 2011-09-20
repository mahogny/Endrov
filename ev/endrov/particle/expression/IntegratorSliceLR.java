/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.particle.expression;

import java.util.Map;

import javax.vecmath.Vector3d;

import endrov.particle.Lineage;
import endrov.particle.util.LineageMergeUtil;
import endrov.util.EvDecimal;
import endrov.util.ImVector2;
import endrov.util.ImVector3d;

/**
 * Integrate expression along LR-axis
 * @author Johan Henriksson
 *
 */
public class IntegratorSliceLR extends IntegratorSlice 
	{

	Vector3d axis;
	
	public IntegratorSliceLR(IntegrateExp integrator, int numSubDiv, Map<EvDecimal, Double> bg)
		{
		super(integrator, numSubDiv, bg);
		}
	
	//Normalized with inverse length of axis 
	public ImVector3d getDirVec()
		{
		return new ImVector3d(axis.x,axis.y,axis.z);
		}
	
	public ImVector3d getMidPos()
		{
		return new ImVector3d(shell.midx, shell.midy, shell.midz); 
		}
	
	

	
	/**
	 * Set up coordinate system, return if successful
	 */
	public boolean setupCS(Lineage refLin)
		{
		//NucLineage.Nuc nucABp = refLin.nuc.get("ABp");
		Lineage.Particle nucEMS = refLin.particle.get("EMS");

		Vector3d posABp=LineageMergeUtil.getLastPosABp(refLin);
		
		if (posABp==null||nucEMS==null||nucEMS.pos.isEmpty())
			{
			System.out.println("Does not have enough cells marked, will not produce LR");
			return false;
			}
		else
			System.out.println("Will do LR");

//		Vector3d posABp = nucABp.pos.get(nucABp.pos.lastKey()).getPosCopy();
		Vector3d posEMS = nucEMS.pos.get(nucEMS.pos.lastKey()).getPosCopy();

		ImVector2 dirvec=ImVector2.polar(shell.major, shell.angle);
		Vector3d axisAP=new Vector3d(dirvec.x, dirvec.y, 0);

		//Approximate axis - but I want it perpendicular to the AP-axis defined by the shell
		Vector3d approxDV = new Vector3d();
		approxDV.sub(posEMS, posABp);
		
		Vector3d axisLR = new Vector3d();
		axisLR.cross(approxDV, axisAP);
		
		axisLR.normalize();
		double axisLength=2*shell.minor;
		axisLR.scale(1.0/axisLength);   // + or -?
		this.axis=axisLR;
		
		return true;
		}
	
	}
