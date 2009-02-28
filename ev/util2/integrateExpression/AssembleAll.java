package util2.integrateExpression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.StringTokenizer;

import org.apache.poi.hssf.record.formula.functions.Replace;

import endrov.util.EvFileUtil;
import endrov.util.Tuple;

/**
 * Take all OSTs with generated profiles and generate assemblies
 * @author Johan Henriksson
 *
 */
public class AssembleAll
	{
	
	public static boolean isNumber(String s)
		{
		for(char c:s.toCharArray())
			if(!Character.isDigit(c))
				return false;
		return true;
		}
	
	public static Tuple<String, String> nameDateFromOSTName(String n)
		{
		//String orig=n;
		n=n.substring(0,n.indexOf(".ost"));
		
		int u1=n.indexOf('_');
		String strainName;
		if(u1==-1)
			{
			strainName=n;
			n="";
			}
		else
			{
			strainName=n.substring(0, u1);
			n=n.substring(u1+1);
			}
		
		int u2=n.indexOf('_');
		String date;
		if(u2==-1)
			{
			date=n;
			n="";
			}
		else
			{
			date=n.substring(0, u2);
			}
		
		
		if(isNumber(date) && date.length()==6)
			{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(2);
			int year=2000+Integer.parseInt(date.substring(0,2));
			int month=Integer.parseInt(date.substring(2,4));
			int day=Integer.parseInt(date.substring(4,6));

			date=year+""+nf.format(month)+""+nf.format(day);
			}
		else
			date="20060101";
		

		//TODO
		//Fallback: actually name of gene
		
		return new Tuple<String, String>(strainName,date);
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
			
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while(br.readLine()!=null);
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
	
	
	
	
	public static void main(String[] args)
		{
		
		
		File htmlOutdir=new File("/Volumes2/TBU_main03/userdata/henriksson/geneProfilesAPT");
		htmlOutdir.mkdirs();
		
		
//		Log.listeners.add(new StdoutLog());
//		EV.loadPlugins();
		
	//	EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
		//doProfile(data);

		try
			{
			String apTotTemplate=EvFileUtil.readStream(AssembleAll.class.getResourceAsStream("ap3d.html"));
			String apCellTemplate=EvFileUtil.readStream(AssembleAll.class.getResourceAsStream("ap3dcell.html"));
			String tTotTemplate=EvFileUtil.readStream(AssembleAll.class.getResourceAsStream("t.html"));
			String tCellTemplate=EvFileUtil.readStream(AssembleAll.class.getResourceAsStream("tcell.html"));
			StringBuffer sbApCells=new StringBuffer();
			StringBuffer sbTCells=new StringBuffer();
			
			String gnuplotAP3d=EvFileUtil.readStream(AssembleAll.class.getResourceAsStream("ap3d.gnu"));
			String gnuplotAP2d=EvFileUtil.readStream(AssembleAll.class.getResourceAsStream("ap2d.gnu"));
			String gnuplotT=EvFileUtil.readStream(AssembleAll.class.getResourceAsStream("t.gnu"));
			
			File tempdatFile=File.createTempFile("surface", ".dat");
			System.out.println(tempdatFile);
			
			for(File f:new File("/Volumes2/TBU_main01/ost4dgood").listFiles())
				if(f.getName().endsWith(".ost")) 
					{
					File APfile=new File(new File(f,"data"),"AP20-GFPb");
//					File APfile=new File(new File(f,"data"),"AP20-GFP");
					Tuple<String, String> nameDate=nameDateFromOSTName(f.getName());
					
					////////////////////////// AP-profile //////////////////////////////
					if(APfile.exists())
						{
						System.out.println(APfile);
						System.out.println(nameDate);
						
						//Turn matrix into GNU dat-file
						makeTempSurfaceFile(APfile,tempdatFile);
						
						String imgap3d=APfile.toString()+"b.png";
						String thisCmd=gnuplotAP3d
						.replace("TITLE", nameDate.fst())
						.replace("#INFILE", tempdatFile.toString())
						.replace("#START","set terminal png\nset output '"+imgap3d+"'\n");
//						.replace("#START","set terminal png\nset output '"+APfile.toString()+".png'\n");
//						System.out.println(thisCmd);
						gnuplot(thisCmd);

						String thisCmd2d=gnuplotAP2d
						.replace("TITLE", nameDate.fst())
						.replace("#INFILE", tempdatFile.toString())
						.replace("#START","set terminal png size 128,350\nset output '"+APfile.toString()+"b.2d.png'\n");
//						.replace("#START","set terminal png size 128,350\nset output '"+APfile.toString()+".2d.png'\n");
//						System.out.println(thisCmd2d);
						gnuplot(thisCmd2d);

						
						sbApCells.append(apCellTemplate
								.replace("STRAIN",nameDate.fst())
								.replace("OSTURL", f.toString())
								.replace("IMGURL",imgap3d));
						
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
//						System.out.println(thisCmd);
						gnuplot(thisCmd);
						
						
						sbTCells.append(tCellTemplate
								.replace("STRAIN",nameDate.fst())
								.replace("OSTURL", f.toString())
								.replace("IMGURL",imgt));
						
						}
					}
			EvFileUtil.writeFile(new File(htmlOutdir,"ap3d.html"), apTotTemplate
					.replace("TABLECONTENT", sbApCells.toString()));
			EvFileUtil.writeFile(new File(htmlOutdir,"t.html"), tTotTemplate
					.replace("TABLECONTENT", sbTCells.toString()));
			}
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		System.out.println("done");
		System.exit(0);
		
		}
	}
