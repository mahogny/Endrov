package util2.integrateExpression.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Set;

import endrov.util.EvFileUtil;

/**
 * Transform data and render it with GNUplot
 * @author Johan Henriksson
 *
 */
public class NewRenderHTML
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
			ap3dTotTemplate=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateAP3dIndex.html"));
			ap3dCellTemplate=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateAP3dCell.html"));
			ap2dTotTemplate=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateAP2dIndex.html"));
			ap2dCellTemplate=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateAP2dCell.html"));
			tTotTemplate=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateTIndex.html"));
			tCellTemplate=EvFileUtil.readStream(NewRenderHTML.class.getResourceAsStream("templateTCell.html"));
			
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
	
	
	
	
/*
	public static double[] squeezeDim1(double[][] arr)
		{
		double[] ret=new double[arr.length];
		LinkedList<Double> arr=new LinkedList<Double>();
		for(int i=0;i<arr.length;i++)
			ret[i]=arr[i][0];
		return ret;
		}*/
	
	
	public static void toTimage(double[][] exp, File ostFile, String title) throws IOException
		{
		StringBuffer sbData=new StringBuffer();
		
		for(int row=0;row<exp.length;row++)
			{
			double[] line=exp[row];
			if(line!=null)
				for(int col=0;col<line.length;col++)
					sbData.append(col+"\t"+line[col]+"\n"); //x y
			sbData.append("\n");
			}
//		for(int col=0;col<exp.length;col++)
//			sbData.append(col+"\t"+exp[col]+"\n"); //x y?
	
		File tempdatFile=File.createTempFile("surface", ".dat");
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

	
	
	
	
	
	/**
	 * Make the summary
	 */
	public static void makeSummary(File htmlOutdir, Set<File> datas)
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
			//File htmlOutdir=new File("/Volumes/TBU_main06/userdata/henriksson/geneProfilesAPT");
			htmlOutdir.mkdirs();
			
			
			
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
