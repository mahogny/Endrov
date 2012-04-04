/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;

import java.util.*;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.Vector3i;


/**
 * Identify regions - A pre-step to analyze particles. Each continuous region with the same color
 * will be given an ID. However, it is really meant for binary images.  
 * <br/>
 * The region of value 0 can optionally be ignored
 * <br/>
 * O(w h d)
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpIdentifyParticles3D extends EvOpStack1
	{
	private boolean ignore0;
	private boolean alsoDiagonal;
	
	public EvOpIdentifyParticles3D(boolean ignore0, boolean alsoDiagonal)
		{
		this.ignore0=ignore0;
		this.alsoDiagonal=alsoDiagonal;
		}
	
	
	/**
	 * Helper: see if neighbour is free. then mark and continue
	 */
	private static void tryadd(LinkedList<Vector3i> eqVal, double[][] inarr, double thisValue, int w, int h, int d, int x, int y, int z)
		{
		double newValue=inarr[z][y*w+x];
		if(newValue==thisValue)
			eqVal.add(new Vector3i(x,y,z));
		}

	
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return apply(ph, p[0], ignore0, alsoDiagonal);
		}
	
	public static EvStack apply(ProgressHandle ph, EvStack stack, boolean ignore0, boolean alsoDiagonal)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();
		
		EvStack out=new EvStack();
		out.allocate(w, h, d, EvPixelsType.INT, stack);
		int[][] mark=out.getOrigArraysInt(ph);
		
		//Keep track of pixels included so far
		boolean visited[][][]=new boolean[d][h][w];
		
		int markid=1;
		
		double[][] inarr=stack.getReadOnlyArraysDouble(ph);
		LinkedList<Vector3i> eqVal=new LinkedList<Vector3i>();
		for(int z=0;z<d;z++)
			for(int y=0;y<h;y++)
				for(int x=0;x<w;x++)
					{
					//Dismiss pixels that has been used already
					if(!visited[z][y][x])
						{
						double thisValue=inarr[z][y*w+x];

						int cnt=0;
						
						//Select id to mark with
						int thisMarkID=markid;
						if(thisValue==0 && ignore0)
							thisMarkID=0;
						else
							markid++; //Allocate next id
						
						//Flood fill all pixels with the same color
						eqVal.add(new Vector3i(x,y,z));
						while(!eqVal.isEmpty())
							{
							cnt++;
							
							Vector3i v=eqVal.poll();
							int vx=v.x;
							int vy=v.y;
							int vz=v.z;

							//Ignore already tested pixels
							if(visited[vz][vy][vx])
								continue;
							
							visited[vz][vy][vx]=true;
							mark[vz][vy*w+vx]=thisMarkID;

							if(alsoDiagonal)
								{
								for(int nx=vx-1;nx<=vx+1;nx++)
									for(int ny=vy-1;ny<=vy+1;ny++)
										for(int nz=vz-1;nz<=vz+1;nz++)
											if(nx!=vx || ny!=vy || nz!=vz)
												if(
													nx>=0 && nx<w &&
													ny>=0 && ny<h &&
													nz>=0 && nz<d)
													tryadd(eqVal, inarr, thisValue, w, h, d, nx,ny,nz);
								}
							else
								{
								if(vx>0)
									tryadd(eqVal, inarr, thisValue, w, h, d, vx-1,vy,vz);
								if(vx<w-1)
									tryadd(eqVal, inarr, thisValue, w, h, d, vx+1,vy,vz);
								
								if(vy>0)
									tryadd(eqVal, inarr, thisValue, w, h, d, vx,vy-1,vz);
								if(vy<h-1)
									tryadd(eqVal, inarr, thisValue, w, h, d, vx,vy+1,vz);
								
								if(vz>0)
									tryadd(eqVal, inarr, thisValue, w, h, d, vx,vy,vz-1);
								if(vz<d-1)
									tryadd(eqVal, inarr, thisValue, w, h, d, vx,vy,vz+1);
								}
							
							
							}
						
						}
					}
		
		return out;
		}
	
	
	
	
	}
