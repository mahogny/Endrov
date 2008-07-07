package qhull;

import java.io.*;
import java.util.Vector;

import javax.vecmath.Vector3d;

public class Voronoi
	{
	public Voronoi(Vector3d[] points) throws Exception
		{
		File dir=new File(Voronoi.class.getResource(".").getFile());
		File executable=new File(new File(dir.getParentFile(),"linux32"),"qvoronoi");
		
		System.out.println();
		
		//ProcessBuilder pb=new ProcessBuilder(executable.toString());
		
		int nump=points.length;
		
		Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec(executable.toString()+" s o");
    
    //PrintWriter pw=new PrintWriter(process.getOutputStream());
    PrintWriter pw=new PrintWriter(System.out);
    
    pw.println("3");
    pw.println(""+nump);
    for(Vector3d p:points)
    	pw.println("\t"+p.x+"\t"+p.y+"\t"+p.z);
    //pw.append(Character.)
    pw.flush();
    pw.close();
    
    InputStream is = process.getInputStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    System.out.println("read:");
    String line;
    while ((line = br.readLine()) != null) 
      System.out.println(line);

    System.out.println("enread:");
		
		
		//pb.start();
		
		}
	
	
	public static void main(String[] arg)
		{
		Vector<Vector3d> vec=new Vector<Vector3d>();
		vec.add(new Vector3d(0,0,0));
		vec.add(new Vector3d(1,0,1));
		vec.add(new Vector3d(0,0,1));
		vec.add(new Vector3d(0,1,1));
		vec.add(new Vector3d(1,1,1));
		
		try
			{
			Voronoi v=new Voronoi(vec.toArray(new Vector3d[]{}));
			System.out.println(""+v);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	}
