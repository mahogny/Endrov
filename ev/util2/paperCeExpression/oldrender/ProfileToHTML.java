/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.oldrender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import util2.paperCeExpression.integrate.ExpUtil;


import endrov.util.EvFileUtil;
import endrov.util.Tuple;

/**
 * Turn profiles into HTML
 * @author Johan Henriksson
 *
 */
public class ProfileToHTML
	{
	public static String ap3dTotTemplate;
	public static String ap3dCellTemplate;
	public static String ap2dTotTemplate;
	public static String ap2dCellTemplate;
	public static String tTotTemplate;
	public static String tCellTemplate;
	
	public static String gnuplotAP3d;
	public static String gnuplotAP2d;
	public static String gnuplotT;
	
	static
		{
		try
			{
			ap3dTotTemplate=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("ap3d.html"));
			ap3dCellTemplate=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("ap3dcell.html"));
			ap2dTotTemplate=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("ap2d.html"));
			ap2dCellTemplate=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("ap2dcell.html"));
			tTotTemplate=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("t.html"));
			tCellTemplate=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("tcell.html"));
			
			gnuplotAP3d=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("ap3d.gnu"));
			gnuplotAP2d=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("ap2d.gnu"));
			gnuplotT=EvFileUtil.readStream(ProfileToHTML.class.getResourceAsStream("t.gnu"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			throw new RuntimeException("Problem loading resource");
			}
		}
	
	
	
	
	public static boolean isNumber(String s)
		{
		for(char c:s.toCharArray())
			if(!Character.isDigit(c))
				return false;
		return true;
		}
	
	public static void makeTempSurfaceFile(File input, File output)
		{
		try
			{
			BufferedReader br=new BufferedReader(new FileReader(input));
			
			StringBuffer sb=new StringBuffer();
			
			String line;
			while((line=br.readLine())!=null)
				{
				StringTokenizer st=new StringTokenizer(line);
				//String frame=
				st.nextToken();
				
				
				while(st.hasMoreElements())
					{
					sb.append(st.nextToken());
					sb.append("\t");
					}
				sb.append("\n");
				/*
				int i=0;
				while(st.hasMoreElements())
					{
					sb.append(i);
					sb.append("\t");
					sb.append(frame);
					sb.append("\t");
					sb.append(st.nextToken());
					sb.append("\n");
					i++;
					}
					*/
				}
			br.close();
			
			EvFileUtil.writeFile(output, sb.toString());
			}
		catch (FileNotFoundException e)
			{
			e.printStackTrace();
			System.exit(1);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			System.exit(1);
			}
		
		}
	
	public static void gnuplot(String s) throws IOException
		{
		try
			{
			Process process = Runtime.getRuntime().exec("/usr/bin/gnuplot");
			    
			PrintWriter pw=new PrintWriter(process.getOutputStream());
			pw.println(s);
			pw.flush();
			pw.close();
			
			String retLine;
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((retLine=br.readLine())!=null)
				System.out.println(retLine);
				;
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	public static String genotype(String strain)
		{
		return null;
		}
	
	

	
	/**
	 * Alternative table format
	 * 
	 * http://t16web.lanl.gov/Kawano/gnuplot/gallery/cal6.html
	 * 
	 * file: x\ty\tz\n
	 */
	
	
	private static void plotOne(File f, 
			StringBuffer sbAp2dCells, StringBuffer sbAp3dCells, StringBuffer sbTCells) throws IOException
	{
	File APfile=new File(new File(f,"data"),"AP20-GFPb");
	//	File APfile=new File(new File(f,"data"),"AP20-GFP");
	Tuple<String, String> nameDate=ExpUtil.nameDateFromOSTName(f.getName());

	////////////////////////// AP-profile //////////////////////////////
	if(APfile.exists())
		{
		System.out.println(APfile);
		System.out.println(nameDate);

		File tempdatFile=File.createTempFile("surface", ".dat");
		System.out.println("tempfile: "+tempdatFile);

		//Turn matrix into GNU dat-file
		makeTempSurfaceFile(APfile,tempdatFile);

		String imgap3d=APfile.toString()+"b.png";
		String thisCmd=gnuplotAP3d
		.replace("TITLE", nameDate.fst())
		.replace("#INFILE", tempdatFile.toString())
		.replace("#START","set terminal png\nset output '"+imgap3d+"'\n");
		//		.replace("#START","set terminal png\nset output '"+APfile.toString()+".png'\n");
		//		System.out.println(thisCmd);
		gnuplot(thisCmd);

		String imgap2d=APfile.toString()+"b.2d.png";
		String thisCmd2d=gnuplotAP2d
		.replace("TITLE", nameDate.fst())
		.replace("#INFILE", tempdatFile.toString())
		.replace("#START","set terminal png size 128,350\nset output '"+imgap2d+"'\n");
		//		.replace("#START","set terminal png size 128,350\nset output '"+APfile.toString()+".2d.png'\n");
		//		System.out.println(thisCmd2d);
		gnuplot(thisCmd2d);


		sbAp3dCells.append(ap3dCellTemplate
				.replace("STRAIN",nameDate.fst())
				.replace("OSTURL", f.toString())
				.replace("IMGURL",imgap3d));

		sbAp2dCells.append(ap2dCellTemplate
				.replace("STRAIN",nameDate.fst())
				.replace("OSTURL", f.toString())
				.replace("IMGURL",imgap2d));

		}


	//////////////////////// T-profile ////////////////////////////
	File Tfile=new File(new File(f,"data"),"AP1-GFPb");
	if(Tfile.exists())
		{

		String imgt=Tfile.toString()+"b.png";
		String thisCmd=gnuplotT
		.replace("TITLE", nameDate.fst())
		.replace("#INFILE", Tfile.toString())
		.replace("#START","set terminal png\nset output '"+imgt+"'\n");
		//		System.out.println(thisCmd);
		gnuplot(thisCmd);


		sbTCells.append(tCellTemplate
				.replace("STRAIN",nameDate.fst())
				.replace("OSTURL", f.toString())
				.replace("IMGURL",imgt));

		}
	}

	
	public static void main(String[] args)
		{
		StringBuffer sbAp3dCells=new StringBuffer();
		StringBuffer sbAp2dCells=new StringBuffer();
		StringBuffer sbTCells=new StringBuffer();
		
		
//		Log.listeners.add(new StdoutLog());
//		EV.loadPlugins();
		
	//	EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
		//doProfile(data);

		try
			{
			File htmlOutdir=new File("/Volumes/TBU_main06/userdata/henriksson/geneProfilesAPT");
			htmlOutdir.mkdirs();
			
			for(File parent:new File[]{
					new File("/Volumes/TBU_main06/ost4dgood"),
			})
			for(File f:parent.listFiles())
				if(f.getName().endsWith(".ost")) 
					if(new File(f,"tagDone4d.txt").exists())
						plotOne(f, sbAp2dCells, sbAp3dCells, sbTCells);
			
			EvFileUtil.writeFile(new File(htmlOutdir,"ap3d.html"), ap3dTotTemplate
					.replace("TABLECONTENT", sbAp3dCells.toString()));
			EvFileUtil.writeFile(new File(htmlOutdir,"ap2d.html"), ap2dTotTemplate
					.replace("TABLECONTENT", sbAp2dCells.toString()));
			EvFileUtil.writeFile(new File(htmlOutdir,"t.html"), tTotTemplate
					.replace("TABLECONTENT", sbTCells.toString()));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		System.out.println("done");
		System.exit(0);
		}
	}
