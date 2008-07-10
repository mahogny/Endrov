package endrov.nuc;

import java.util.Map;
import java.util.Vector;

import javax.vecmath.Vector3d;

import qhull.Voronoi;
import qhull.VoronoiNeigh;

/**
 * Construct neighbourhood based on voronoi approximation
 * 
 * @author Johan Henriksson
 */
public class NucVoronoi
	{
	public Voronoi vor;
	public Vector<String> nucnames;
	public Vector<Vector3d> nmid;
	public VoronoiNeigh vneigh;
	
	public NucVoronoi(Map<NucPair, NucLineage.NucInterp> inter) throws Exception
		{
		nucnames=new Vector<String>();
		nmid=new Vector<Vector3d>();
		for(Map.Entry<NucPair, NucLineage.NucInterp> entry:inter.entrySet())
			{
			//NucLineage.Nuc nuc=entry.getKey().fst().nuc.get(entry.getKey().snd());
			//int curframe=100;
			//if(nuc.firstFrame()<=curframe && nuc.lastFrame()>=curframe)
			if(entry.getValue().isVisible())
				{
				nucnames.add(entry.getKey().snd());
				nmid.add(entry.getValue().pos.getPosCopy());
				}
			}
		vor=new Voronoi(nmid.toArray(new Vector3d[]{}));
		//System.out.println(vor.toString());
		vneigh=new VoronoiNeigh(vor);
		}
	}
