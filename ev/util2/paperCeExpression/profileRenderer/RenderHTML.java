/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.profileRenderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;


import endrov.util.EvFileUtil;

/**
 * Transform data and render it with GNUplot
 * @author Johan Henriksson
 *
 */
public class RenderHTML
	{
	public static String gnuplotSliceTime3d;
	public static String gnuplotSliceTime2d;
	public static String gnuplotTime;
	
	public static String templateRecSliceTime;
	public static String templateIndexSliceTime;


	static
		{
		try
			{
			templateRecSliceTime=EvFileUtil.readStream(RenderHTML.class.getResourceAsStream("templateRecAPT.html"));
			templateIndexSliceTime=EvFileUtil.readStream(RenderHTML.class.getResourceAsStream("templateIndexAPT.html"));
			
			gnuplotSliceTime3d=EvFileUtil.readStream(RenderHTML.class.getResourceAsStream("renderAP3d.gnu"));
			gnuplotSliceTime2d=EvFileUtil.readStream(RenderHTML.class.getResourceAsStream("renderAP2d.gnu"));
			gnuplotTime=EvFileUtil.readStream(RenderHTML.class.getResourceAsStream("renderT.gnu"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			throw new RuntimeException("Problem loading resource");
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
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	
	/**
	 * Turn normalized array of slice-time data into graph
	 */
	public static void toSliceTimeImage(double[][] exp, File ostFile, String title, String prefix) throws IOException
		{
		StringBuffer sbData=new StringBuffer();
		
		for(int row=0;row<exp.length;row++)
			{
			double[] line=exp[row];
			if(line!=null)
				for(int col=0;col<line.length;col++)
					{
					double val=line[col];
					if(Double.isInfinite(val) || Double.isNaN(val))
						val=0;
					sbData.append(col+"\t"+row+"\t"+val+"\n"); //x y z?
					}
			sbData.append("\n");
			}

		
		File tempdatFile=new File("/tmp/surface_"+prefix+".dat");
		//System.out.println(tempdatFile);
//		File tempdatFile=File.createTempFile("surface", ".dat");
		EvFileUtil.writeFile(tempdatFile, sbData.toString());
		File dataDir=new File(ostFile,"data");
		dataDir.mkdirs();
		
		String imgap3d=new File(dataDir,"exp"+prefix+"3d.png").toString();
		String thisCmd3d=gnuplotSliceTime3d
		.replace("TITLE", title)
		.replace("#INFILE", tempdatFile.toString())
		.replace("#START","set terminal png\nset output '"+imgap3d+"'\n");
		gnuplot(thisCmd3d);
		
		String imgap2d=new File(dataDir,"exp"+prefix+"2d.png").toString();
		String thisCmd2d=gnuplotSliceTime2d
		.replace("TITLE", title)
//		.replace("#INFILE", "/home/tbudev3/test.png")
		.replace("#INFILE", tempdatFile.toString())
		.replace("#START","set terminal png\nset output '"+imgap2d+"'\n");
		gnuplot(thisCmd2d);
		}
	
	
	
	
	/**
	 * Turn normalized array of T into graph
	 */
	public static void toTimeImage(double[][] exp, File ostFile, String title) throws IOException
		{
		StringBuffer sbData=new StringBuffer();
		
		for(int row=0;row<exp.length;row++)
			{
			double[] line=exp[row];
			if(line!=null)
				for(int col=0;col<line.length;col++)
					{
					double val=line[col];
					if(Double.isInfinite(val) || Double.isNaN(val))
						val=0;
					sbData.append(row+"\t"+val+"\n"); //x y
					}
			}
	
		File tempdatFile=File.createTempFile("series", ".dat");
		EvFileUtil.writeFile(tempdatFile, sbData.toString());
		File dataDir=new File(ostFile,"data");
		dataDir.mkdirs();
		String imgap3d=new File(dataDir,"expT.png").toString();
		String thisCmd=gnuplotTime
		.replace("TITLE", title)
		.replace("#INFILE", tempdatFile.toString())
		.replace("#START","set terminal png\nset output '"+imgap3d+"'\n");
		gnuplot(thisCmd);
		}

	
	
	
	private static File copyForSummary(File f, File htmlOutdir, File orig) throws IOException
		{
		File newFile=new File(htmlOutdir,f.getName()+"_"+orig.getName());
		EvFileUtil.copy(orig, newFile);
		return newFile;
		}
	
	/**
	 * Make the summary HTML. Assumes all images exist.
	 */
	public static void makeSummaryHTML(File htmlOutdir, Set<File> datas) throws IOException
		{
		htmlOutdir.mkdirs();

		LinkedList<File> sortedDatas=new LinkedList<File>();
		sortedDatas.addAll(datas);
		Collections.sort(sortedDatas, new Comparator<File>(){
			public int compare(File o1, File o2)
				{
				String n1=PaperCeExpressionUtil.getGeneName(o1);
				String n2=PaperCeExpressionUtil.getGeneName(o2);
				return n1.compareTo(n2);
				}
			});
		
		StringBuffer sbAPT=new StringBuffer();
		StringBuffer sbXYZtitles=new StringBuffer();
		StringBuffer sbXYZims=new StringBuffer();
		for(File f:sortedDatas)
			{
			String strainName=PaperCeExpressionUtil.getGeneName(f);
			
			String recStringAPT=templateRecSliceTime
			.replace("STRAIN", strainName)
			.replace("OSTURL", ""+f);
			String recStringXYZtitle=(
					"<td>"+
					"<center>"+
					"STRAIN<br/>"+
					"<small><small>OSTURL</small></small><br/>"+
					"</center>"+
					"</td>"
					)
			.replace("STRAIN", strainName)
			.replace("OSTURL", ""+f);
			String recStringXYZimages="<td><a href=\"IMGURLxyz\"><img src=\"IMGURLxyz\" border=\"0\"/></a></td>";

			////////////////// File 1 /////////////////
			
			File fAP2d=new File(new File(f,"data"),"expAP2d.png");
			recStringAPT=copyAndGetLink(f, fAP2d, htmlOutdir, recStringAPT, "zAPT2d");
			File fAP3d=new File(new File(f,"data"),"expAP3d.png");
			recStringAPT=copyAndGetLink(f, fAP3d, htmlOutdir, recStringAPT, "zAPT3d");

			File fT=new File(new File(f,"data"),"expT.png");
			recStringAPT=copyAndGetLink(f, fT, htmlOutdir, recStringAPT, "IMGURLt");

			File fLR2d=new File(new File(f,"data"),"expLR2d.png");
			recStringAPT=copyAndGetLink(f, fLR2d, htmlOutdir, recStringAPT, "zLRT2d");
			File fLR3d=new File(new File(f,"data"),"expLR3d.png");
			recStringAPT=copyAndGetLink(f, fLR3d, htmlOutdir, recStringAPT, "zLRT3d");

			File fDV2d=new File(new File(f,"data"),"expDV2d.png");
			recStringAPT=copyAndGetLink(f, fDV2d, htmlOutdir, recStringAPT, "zDVT2d");
			File fDV3d=new File(new File(f,"data"),"expDV3d.png");
			recStringAPT=copyAndGetLink(f, fDV3d, htmlOutdir, recStringAPT, "zDVT3d");

			
			sbAPT.append(recStringAPT);
			
			////////////////// File 2 /////////////////
			
			File fXYZ=new File(new File(f,"data"),"expXYZ.png");
			recStringXYZimages=copyAndGetLink(f, fXYZ, htmlOutdir, recStringXYZimages, "IMGURLxyz");  

			sbXYZtitles.append(recStringXYZtitle);
			sbXYZims.append(recStringXYZimages);
			}

		//Write files
		EvFileUtil.writeFile(new File(htmlOutdir,"indexAPT.html"), 
				templateIndexSliceTime.replace("TABLECONTENT", sbAPT.toString()));
		EvFileUtil.writeFile(new File(htmlOutdir,"indexXYZ.html"), 
				(
						"<html>"+
						"<title>Expression patterns overview (XYZ)</title>"+
						"<table>"+
						"<tr>"+sbXYZtitles+"</tr>"+
						"<tr>"+sbXYZims+"</tr>"+
						"</table>"+
						"</html>"		
				));

		}
	
	public static String copyAndGetLink(File f, File fAP2d, File htmlOutdir, String recStringAPT, String toReplace) throws IOException
		{
		if(fAP2d.exists())
			{
			fAP2d=copyForSummary(f, htmlOutdir, fAP2d);
			recStringAPT=recStringAPT.replace(toReplace, fAP2d.getName());
			}
		else
			recStringAPT=recStringAPT.replace(toReplace, "");
		return recStringAPT;
		}
	
	

	
	}
