package util2.integrateExpression;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.NumberFormat;

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
	
	public static void main(String[] args)
		{
		
		File outdir=new File("/Volumes/TBU_main03/userdata/henriksson/geneProfilesAPT");
		outdir.mkdirs();
		
//		Log.listeners.add(new StdoutLog());
//		EV.loadPlugins();
		
	//	EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
		//doProfile(data);

		try
			{
			final String gpCmd=EvFileUtil.readStream(AssembleAll.class.getResourceAsStream("ap.gnu"));
			
			for(File f:new File("/Volumes/TBU_main01/ost4dgood").listFiles())
				if(f.getName().endsWith(".ost")) 
					{
					File APfile=new File(new File(f,"data"),"AP20-GFP");
					if(APfile.exists())
						{
						System.out.println(APfile);
						Tuple<String, String> nameDate=nameDateFromOSTName(f.getName());
						System.out.println(nameDate);
						
						String thisCmd=gpCmd
						.replace("#INFILE", APfile.toString())
						.replace("#START","set terminal png\nset output '"+APfile.toString()+".png'\n");
						System.out.println(thisCmd);
						gnuplot(thisCmd);
						
						
						}
					}
			}
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		System.exit(0);
		
		}
	}
