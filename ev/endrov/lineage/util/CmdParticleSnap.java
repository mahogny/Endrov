/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage.util;

import javax.vecmath.*;

import endrov.basicWindow.*;
import endrov.lineage.Lineage;
import endrov.lineage.LineageCommonUI;
import endrov.lineage.LineageSelParticle;


/**
 * Snap to line
 * @author Johan Henriksson
 */
public class CmdParticleSnap 
	{
	public int numArg()	{return 0;}
	public void exec() throws Exception
		{
		for(LineageSelParticle nucPair:LineageCommonUI.getSelectedParticles())
			{
			Lineage lin=nucPair.fst();
			Lineage.Particle n=lin.particle.get(nucPair.snd());
			
			Lineage.ParticlePos pos=n.pos.get(n.pos.firstKey());
			
			Lineage.Particle n1=lin.particle.get(Lineage.connectNuc[0]);
			Lineage.ParticlePos p1=n1.pos.get(n1.pos.firstKey());
			Lineage.Particle n2=lin.particle.get(Lineage.connectNuc[1]);
			Lineage.ParticlePos p2=n2.pos.get(n2.pos.firstKey());

			//Vector out of axis, base in p1
			Vector3d axisBase=new Vector3d(p1.x,p1.y,p1.z);
			Vector3d axisVec =new Vector3d(p2.x,p2.y,p2.z);
			axisVec.sub(axisBase);
			
			//Vector out of position, base in p1
			Vector3d posVec  =new Vector3d(pos.x, pos.y, pos.z);
			posVec.sub( axisBase);

			//Snap
			double len=axisVec.dot(posVec)/axisVec.lengthSquared();
			posVec.set(axisVec);
			posVec.scale(len);
			
			//Vector back to position
			posVec.add(axisBase);
			pos.x=posVec.x;
			pos.y=posVec.y;
			pos.z=posVec.z;
			}
		BasicWindow.updateWindows();
		}	
	}
