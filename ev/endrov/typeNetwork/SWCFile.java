package endrov.typeNetwork;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import endrov.core.EvBuild;


/*
example file: http://neuromorpho.org/neuroMorpho/dableFiles/dendritica/CNG%20version/v_e_purk1.CNG.swc 
 
The three dimensional structure of a neuron can be represented in a SWC format (Cannon et al., 1998). SWC is a simple Standardized format. Each line has 7 fields encoding data for a single neuronal compartment:

* an integer number as compartment identifier
* type of neuronal compartment
     0 - undefined
     1 - soma
     2 - axon
     3 - basal dendrite
     4 - apical dendrite

* x coordinate of the compartment
* y coordinate of the compartment
* z coordinate of the compartment
* radius of the compartment
* parent compartment (-1 means no parent)

Every compartment has only one parent and the parent compartment for the first point in each file is always -1 (if the file does not include the soma information then the originating point of the tree will be connected to a parent of -1). The index for parent compartments are always less than child compartments. Loops and unconnected branches are excluded. All trees should originate from the soma and have parent type 1 if the file includes soma information. Soma can be a single point or more than one point. When the soma is encoded as one line in the SWC, it is interpreted as a "sphere". When it is encoded by more than 1 line, it could be a set of tapering cylinders (as in some pyramidal cells) or even a 2D projected contour ("circumference").

 
 Simple neurite tracer also has: 
  - fork point
  - end point
  - custom
Maybe try to stay compatible?
 
*/

public class SWCFile
	{
	private static Map<String, Integer> mapTypeToID=new HashMap<String, Integer>();
	static
		{
		mapTypeToID.put("undefined", 0);
		mapTypeToID.put("soma", 1);
		mapTypeToID.put("axon", 2);
		mapTypeToID.put("basal dendrite", 3);
		mapTypeToID.put("apical dendrite", 4);
		}
	
	/**
	 * Export a network frame. Note that this file format does not support cycles so not all networks can be exported 
	 */
	public static void write(File f, Network.NetworkFrame nf) throws IOException
		{
		PrintWriter w=new PrintWriter(new FileWriter(f));

		w.println("# Exported from Endrov "+EvBuild.version);
		
		//This is to make the IDs increase like 1,2,3...
		Map<Integer,Integer> fromPidToSWC=new HashMap<Integer, Integer>();
		int nextID=1;
		
		//Generate a neighbour list, and types
		Map<Integer,Set<Integer>> neighbours=new HashMap<Integer, Set<Integer>>();
		Map<Integer, Network.Segment> pointSegment=new HashMap<Integer, Network.Segment>();
		for(int pid:nf.points.keySet())
			neighbours.put(pid, new HashSet<Integer>());
		for(Network.Segment s:nf.segments)
			{
			for(int i=0;i<s.points.length-1;i++)
				{
				neighbours.get(s.points[i]).add(s.points[i+1]);
				neighbours.get(s.points[i+1]).add(s.points[i]);
				}
			for(int i=0;i<s.points.length;i++)
				pointSegment.put(s.points[i], s);
			}
		
		//Find a starting point
		if(!nf.points.isEmpty())
			{
			Set<Integer> donePoints=new HashSet<Integer>();
			LinkedList<Integer> todo=new LinkedList<Integer>();
			
			int startPointID=nf.points.keySet().iterator().next();
			
			todo.add(startPointID);
			while(!todo.isEmpty())
				{
				int thisPid=todo.poll();
				if(!donePoints.contains(thisPid))
					{
					Network.Point p=nf.points.get(thisPid);
					
					//Recurse neighbours later
					for(int otherPid:neighbours.get(thisPid))
						todo.add(otherPid);

					//Check which segment the point belongs to
					Network.Segment segment=pointSegment.get(thisPid);
					if(segment!=null)
						{
						
						//Figure out radius. Use 1 if none given
						double r=1;
						if(p.r!=null)
							r=p.r;
						
						//Figure out parent
						int parent=-1;
						for(int otherPid:neighbours.get(thisPid))
							if(donePoints.contains(otherPid))
								parent=fromPidToSWC.get(otherPid);
						
						//Figure out type
						int typeNum=0;
						if(segment.type!=null)
							{
							Integer tryType=mapTypeToID.get(segment.type);
							if(tryType!=null)
								typeNum=tryType;
							}
							

						//Allocate a SWC id
						//int thisSWCid=nextID;
						fromPidToSWC.put(thisPid, nextID);
						nextID++;
						
						//Add entry to file
						w.println(""+fromPidToSWC.get(thisPid)+" "+typeNum+" "+p.x+" "+p.y+" "+p.z+" "+r+" "+parent);
						donePoints.add(thisPid);
						}
					}
				}
			
			}
		
		w.close();
		}
	}
