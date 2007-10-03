package evplugin.nuc;

import java.util.*;
import javax.vecmath.*;

import evplugin.basicWindow.*;
import evplugin.script.*;


/**
 * Snap to line
 * @author Johan Henriksson
 */
public class CmdNucsnap extends Command
	{
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		for(NucPair nucPair:NucLineage.selectedNuclei)
			{
			NucLineage lin=nucPair.getLeft();
			NucLineage.Nuc n=lin.nuc.get(nucPair.getRight());
			
			NucLineage.NucPos pos=n.pos.get(n.pos.firstKey());
			
			NucLineage.Nuc n1=lin.nuc.get(NucLineage.connectNuc[0]);
			NucLineage.NucPos p1=n1.pos.get(n1.pos.firstKey());
			NucLineage.Nuc n2=lin.nuc.get(NucLineage.connectNuc[1]);
			NucLineage.NucPos p2=n2.pos.get(n2.pos.firstKey());

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
		return null;
		}	
	}
