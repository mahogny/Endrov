/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package qhull;

import java.io.*;
import java.util.*;

import javax.vecmath.Vector3d;

import endrov.util.io.EvFileUtil;

/**
 * Interface to Qhull - Voronoi
 * 
 * TODO must be installed in a path without spaces
 * 
 * @author Johan Henriksson
 */
public class Voronoi
	{
	public Vector3d[] center;
  public List<Vector3d> vvert=new Vector<Vector3d>();
  public List<int[]> vsimplex=new Vector<int[]>();
	
	public Voronoi(Vector3d[] points) throws Exception
		{
		center=points;
		
		String platform;
		if(endrov.starter.EvSystemUtil.isMac())
			platform="mac";
		else if(endrov.starter.EvSystemUtil.isWindows())
			throw new Exception("QHULL Platform not supported");
		else //assume linux?
			{
			platform="linux";
			}
		
		//File dir=new File(Voronoi.class.getResource(".").getFile());
		File dir=EvFileUtil.getFileFromURL(Voronoi.class.getResource(".").toURI().toURL());
		
		File executable=new File(new File(dir,"bin_"+platform),"qvoronoi");
		
		File sysExecutable=new File("/usr/bin/qvoronoi");
		if(sysExecutable.exists())
			executable=sysExecutable;
		
		//String execString=executable.toString();
		//execString="."+File.separator+"qhull"+File.separator+"bin_"+platform+File.separator+"qvoronoi";
		
		// /usr/bin/qvoronoi
		
		//int nump=points.length;
		//might need to replace / for \ on windows
		//Process process = Runtime.getRuntime().exec(execString,"o"});
    Process process = Runtime.getRuntime().exec(executable.toString()+" o");
    
    PrintWriter pw=new PrintWriter(process.getOutputStream());
    vectors2pw(pw, points);
    
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    
    ///////////
/*    BufferedReader brerr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    String line3;
    while((line3 = br.readLine())!=null)
    	System.out.println(line3);
    System.out.println("---");
    while((line3 = brerr.readLine())!=null)
    	System.out.println(line3);
    System.out.println("---err");*/
    /////////////
    
    String firstLine=br.readLine();
    if(firstLine==null)
    	{
    	System.out.println("--- output to qvoronoi ---");
    	vectors2pw(new PrintWriter(System.out), points);
    	System.out.println("---- end output ---");
    	throw new Exception("No input back");
    	}
    int outNumDim=Integer.parseInt(firstLine);
    if(outNumDim!=3)
    	throw new Exception("Returned dim!=3");
    
    String line2=br.readLine();
    int numVert=Integer.parseInt(new StringTokenizer(line2).nextToken());
    
    //Vertices
    br.readLine();//Skip infinity       (how does this affect indexing?)
    for(int i=1;i<numVert;i++)
    	{
    	StringTokenizer st=new StringTokenizer(br.readLine());
    	Vector3d v=new Vector3d(
    			Double.parseDouble(st.nextToken()),
    			Double.parseDouble(st.nextToken()),
    			Double.parseDouble(st.nextToken()));
      vvert.add(v);
    	}

    //Simplexes
    String line;
    while((line = br.readLine())!=null)
    	{
    	StringTokenizer st=new StringTokenizer(line);
    	Vector<Integer> simplexv=new Vector<Integer>();
    	st.nextToken(); //Skip number of vertices
    	while(st.hasMoreTokens())
    		simplexv.add(Integer.parseInt(st.nextToken())-1);
    	int[] fa=new int[simplexv.size()];
    	for(int i=0;i<fa.length;i++)
    		fa[i]=simplexv.get(i);
    	vsimplex.add(fa);
    	}
		}
	
	private static void vectors2pw(PrintWriter pw, Vector3d[] points)
		{
    pw.println("3");
    pw.println(Integer.toString(points.length));
    for(Vector3d p:points)
    	pw.println("\t"+p.x+"\t"+p.y+"\t"+p.z);
    pw.flush();
    pw.close();
		}
	

	public String toString()
		{
		StringBuffer bf=new StringBuffer();
		bf.append(vvert);
		bf.append("\n");
		for(int[] f:vsimplex)
			{
			for(int i:f)
				bf.append(" "+i);
			bf.append("\n");
			}
		return bf.toString();
		}
	
	/**
	 * Does a simplex extend to infinity?
	 */
	public boolean isAtInfinity(int i)
		{
		for(int vi:vsimplex.get(i))
			if(vi==-1)
				return true;
		return false;
		}
	

	/**
	 * Mark all vertices in a cell as at infinity
	 */
	public void setInfinityCell(Collection<Integer> infinityCell)
		{
		HashSet<Integer> infVert=new HashSet<Integer>();
		for(int i:infinityCell)
			{
			for(int vi:vsimplex.get(i))
				infVert.add(vi);
			vsimplex.set(i, new int[]{});
			}
		
		for(int i=0;i<vsimplex.size();i++)
			{
			boolean hasInf=false;
			LinkedList<Integer> newlist=new LinkedList<Integer>();
			for(int vi:vsimplex.get(i))
				if(vi==-1 || infVert.contains(vi))
					hasInf=true;
				else
					newlist.add(vi);
			if(hasInf)
				newlist.add(-1);
			int[] newa=new int[newlist.size()];
			for(int k=0;k<newlist.size();k++)
				newa[k]=newlist.get(k);
			vsimplex.set(i,newa);
			}
		
		}
	
	
	/**
	 * test
	 */
	
	public static void main(String[] arg)
		{
		Vector<Vector3d> vec=new Vector<Vector3d>();
		for(int i=0;i<200;i++)
			vec.add(new Vector3d(Math.random(),Math.random(),Math.random()));
/*		vec.add(new Vector3d(0,0,0));
		vec.add(new Vector3d(1,0,1));
		vec.add(new Vector3d(0,0,1));
		vec.add(new Vector3d(0,1,1));
		vec.add(new Vector3d(1,1,1));
	*/	
		try
			{
			long t=System.currentTimeMillis();
			
			int num=100;
			for(int i=0;i<num;i++)
				{
				Voronoi v=new Voronoi(vec.toArray(new Vector3d[]{}));
				new VoronoiNeigh(v,false,new HashSet<Integer>());
//				v.foo();
//				System.out.println(""+v);
				
				}
			System.out.println("time "+((System.currentTimeMillis()-t)/(double)num));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	}
