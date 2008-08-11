package qhull;

import java.io.*;
import java.util.*;

import javax.vecmath.Vector3d;

/**
 * Interface to Qhull - Voronoi
 * @author Johan Henriksson
 */
public class Voronoi
	{
  public List<Vector3d> vvert=new Vector<Vector3d>();
  public List<int[]> vsimplex=new Vector<int[]>();
	
	public Voronoi(Vector3d[] points) throws Exception
		{
		String platform;
		if(endrov.ev.EV.isMac())
			platform="mac";
		else if(endrov.ev.EV.isWindows())
			throw new Exception("QHULL Platform not supported");
		else //assume linux?
			platform="linux32";
		
		File dir=new File(Voronoi.class.getResource(".").getFile());
		File executable=new File(new File(dir,platform),"qvoronoi");
		
		int nump=points.length;
		
    Process process = Runtime.getRuntime().exec(executable.toString()+" o");
    
    PrintWriter pw=new PrintWriter(process.getOutputStream());
    //PrintWriter pw=new PrintWriter(System.out);
    pw.println("3");
    pw.println(Integer.toString(nump));
    for(Vector3d p:points)
    	pw.println("\t"+p.x+"\t"+p.y+"\t"+p.z);
    pw.flush();
    pw.close();
    
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
    
    int outNumDim=Integer.parseInt(br.readLine());
    if(outNumDim!=3)
    	throw new Exception("Returned dim!=3");
    
    String line2=br.readLine();
    int numVert=Integer.parseInt(new StringTokenizer(line2).nextToken());
    
    //Vertices
    br.readLine();//Skip infinity
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
				new VoronoiNeigh(v,false);
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
