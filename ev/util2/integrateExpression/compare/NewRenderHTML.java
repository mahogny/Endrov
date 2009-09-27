package util2.integrateExpression.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;

import util2.integrateExpression.ExpUtil;

import endrov.util.EvFileUtil;

/**
 * Transform data and render it with GNUplot
 * @author Johan Henriksson
 *
 */
public class NewRenderHTML
	{
/*
	public static String ap3dTotTemplate;
	public static String ap3dCellTemplate;
	public static String ap2dTotTemplate;
	public static String ap2dCellTemplate;
	public static String tTotTemplate;
	public static String tCellTemplate;
	*/
	public static String gnuplotAP3d;
	public static String gnuplotAP2d;
	public static String gnuplotT;
	
	
	public static String templateRecAPT;
	public static String templateIndexAPT;

	public static String templateRecXYZ;
	public static String templateIndexXYZ;

	static
		{
		try
			{
			templateRecAPT=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateRecAPT.html"));
			templateIndexAPT=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateIndexAPT.html"));

			templateRecXYZ=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateRecXYZ.html"));
			templateIndexXYZ=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateIndexXYZ.html"));

			gnuplotAP3d=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("renderAP3d.gnu"));
			gnuplotAP2d=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("renderAP2d.gnu"));
			gnuplotT=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("renderT.gnu"));
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
	 * Turn normalized array of AP into graph
	 */
	public static void toAPimage(double[][] exp, File ostFile, String title) throws IOException
		{
		StringBuffer sbData=new StringBuffer();
		
		for(int row=0;row<exp.length;row++)
			{
			double[] line=exp[row];
			if(line!=null)
				for(int col=0;col<line.length;col++)
					sbData.append(col+"\t"+row+"\t"+line[col]+"\n"); //x y z?
			sbData.append("\n");
			}

		File tempdatFile=new File("/tmp/surface.dat");
//		File tempdatFile=File.createTempFile("surface", ".dat");
		EvFileUtil.writeFile(tempdatFile, sbData.toString());
		File dataDir=new File(ostFile,"data");
		dataDir.mkdirs();
		
		String imgap3d=new File(dataDir,"expAP3d.png").toString();
		String thisCmd3d=gnuplotAP3d
		.replace("TITLE", title)
		.replace("#INFILE", tempdatFile.toString())
		.replace("#START","set terminal png\nset output '"+imgap3d+"'\n");
		gnuplot(thisCmd3d);
		
		String imgap2d=new File(dataDir,"expAP2d.png").toString();
		String thisCmd2d=gnuplotAP2d
		.replace("TITLE", title)
//		.replace("#INFILE", "/home/tbudev3/test.png")
		.replace("#INFILE", tempdatFile.toString())
		.replace("#START","set terminal png\nset output '"+imgap2d+"'\n");
		gnuplot(thisCmd2d);
		}
	
	
	
	
	/**
	 * Turn normalized array of T into graph
	 */
	public static void toTimage(double[][] exp, File ostFile, String title) throws IOException
		{
		StringBuffer sbData=new StringBuffer();
		
		for(int row=0;row<exp.length;row++)
			{
			double[] line=exp[row];
			if(line!=null)
				for(int col=0;col<line.length;col++)
					sbData.append(row+"\t"+line[col]+"\n"); //x y
			}
	
		File tempdatFile=File.createTempFile("series", ".dat");
		EvFileUtil.writeFile(tempdatFile, sbData.toString());
		File dataDir=new File(ostFile,"data");
		dataDir.mkdirs();
		String imgap3d=new File(dataDir,"expT.png").toString();
		String thisCmd=gnuplotT
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
	public static void makeSummaryAPT(File htmlOutdir, Set<File> datas) throws IOException
		{
		htmlOutdir.mkdirs();

		StringBuffer sbAPT=new StringBuffer();
		StringBuffer sbXYZ=new StringBuffer();
		for(File f:datas)
			{

			String strainName=ExpUtil.nameDateFromOSTName(f.getName()).fst();
			
			String recStringAPT=templateRecAPT
			.replace("STRAIN", strainName)
			.replace("OSTURL", ""+f);
			String recStringXYZ=templateRecXYZ
			.replace("STRAIN", strainName)
			.replace("OSTURL", ""+f);

			////////////////// File 1 /////////////////
			
			File fAP2d=new File(new File(f,"data"),"expAP2d.png");
			if(fAP2d.exists())
				{
				fAP2d=copyForSummary(f, htmlOutdir, fAP2d);
				recStringAPT=recStringAPT.replace("IMGURL2d", fAP2d.getName());
				}
			else
				recStringAPT=recStringAPT.replace("IMGURL2d", "");

			File fAP3d=new File(new File(f,"data"),"expAP3d.png");
			if(fAP3d.exists())
				{
				fAP3d=copyForSummary(f, htmlOutdir, fAP3d);
				recStringAPT=recStringAPT.replace("IMGURL3d", fAP3d.getName());
				}
			else
				recStringAPT=recStringAPT.replace("IMGURL3d", "");

			File fT=new File(new File(f,"data"),"expT.png");
			if(fT.exists())
				{
				fT=copyForSummary(f, htmlOutdir, fT);
				recStringAPT=recStringAPT.replace("IMGURLt", fT.getName());
				}
			else
				recStringAPT=recStringAPT.replace("IMGURLt", "");

			sbAPT.append(recStringAPT);
			
			////////////////// File 2 /////////////////
			
			File fXYZ=new File(new File(f,"data"),"expXYZ.png");
			if(fXYZ.exists())
				{
				fXYZ=copyForSummary(f, htmlOutdir, fXYZ);
				recStringXYZ=recStringXYZ.replace("IMGURLxyz", fXYZ.getName());
				}
			else
				recStringXYZ=recStringXYZ.replace("IMGURLxyz", "");

			sbXYZ.append(recStringXYZ);

			}

		//Write files
		EvFileUtil.writeFile(new File(htmlOutdir,"indexAPT.html"), 
				templateIndexAPT.replace("TABLECONTENT", sbAPT.toString()));
		EvFileUtil.writeFile(new File(htmlOutdir,"indexXYZ.html"), 
				templateIndexXYZ.replace("TABLECONTENT", sbXYZ.toString()));

		}
	
	
	
	}
