/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage.expression;

import java.util.Map;

import endrov.util.EvDecimal;
import endrov.util.ImVector2;
import endrov.util.ImVector3d;

/**
 * Integrate expression along AP-axis
 * @author Johan Henriksson
 *
 */
public class IntegratorSliceAP extends IntegratorSlice 
	{

	public IntegratorSliceAP(IntegrateExp integrator, /*String newLinName,*/ int numSubDiv, Map<EvDecimal, Double> bg)
		{
		super(integrator, /*newLinName,*/ numSubDiv, bg);
		}
	
	//Normalized with inverse length of axis 
	public ImVector3d getDirVec()
		{
		double axisLength=2*shell.major;
		ImVector2 dirvec=ImVector2.polar(shell.major, shell.angle).normalize().mul(-1.0/axisLength);
		return new ImVector3d(dirvec.x, dirvec.y, 0);
		}
	
	public ImVector3d getMidPos()
		{
		return new ImVector3d(shell.midx, shell.midy, shell.midz); 
		}	
	}
