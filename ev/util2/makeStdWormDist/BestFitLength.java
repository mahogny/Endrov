package util2.makeStdWormDist;

import java.util.*;
import javax.vecmath.*;

import util2.makeStdWormDist.NucStats.NucStatsOne;
import static java.lang.Math.*;



public class BestFitLength
	{
	public Map<String,NucStatsOne> nuc;
	public double eps;
	
	
	public void clear()
		{
		nuc=null;
		}
	
	
	
	public void iterate(int minit, int maxit, double okEps)
		{
		for(int i=0;i<maxit;i++)
			{
			doOneIteration();
			if(i>minit && eps<okEps)
				break;
			}
//		System.exit(0);
		}
	
	public void doOneIteration()
		{

		
		
		
		
//		System.out.println("eps: "+eps+"  scale: "+x[7-1]+"    rot   "+x[4-1]+" "+x[5-1]+" "+x[6-1]+"    trans "+x[1-1]+" "+x[2-1]+" "+x[3-1]);
//		System.out.println("diff: scale: "+dx[7-1]+"    rot   "+dx[4-1]+" "+dx[5-1]+" "+dx[6-1]+"    trans "+dx[1-1]+" "+dx[2-1]+" "+dx[3-1]);
		}
	
	private Vector3d total2(
			String iName, String jName, String diffName)
		{
		NucStatsOne ni=nuc.get(iName);
		Double len=ni.avlenthisframe.get(jName);
		if(len==null)
			return new Vector3d();
		else
			{
			NucStatsOne nj=nuc.get(jName);
	
			Vector3d xi=new Vector3d(ni.curpos);
			xi.sub(nj.curpos);
			double inner=xi.length()-len;
			
			
			
			
			
			return inner*inner; //TODO: weight
			}
		}
			
	
	
	private double total2(
			String iName, String jName, 
			String diffName, int diffCoord)
		{
		
		if(diffName==null)
			{
			double gv=g(iName, jName, diffName, diffCoord);
			return gv*gv;
			}
		else
			return 0;
		}
	
	
	
	
	private double g(
			String iName, String jName, 
			String diffName, int diffCoord)
		{
		NucStatsOne ni=nuc.get(iName);
		
		Double len=ni.avlenthisframe.get(jName);
		if(len==null)
			return 0;
		else
			{
			NucStatsOne nj=nuc.get(jName);
	
			if(diffName==null)
				{
				Vector3d xi=new Vector3d(ni.curpos);
				xi.sub(nj.curpos);
				double inner=xi.length()-len;
				return inner*inner; //TODO: weight
				}
			else
				{
				if(diffName.equals(iName))
					{
					
					}
				else if(diffName.equals(jName))
					{
					
					}
				else
					return 0;
				}
	
			}
		
		}
	

	
	
	}
