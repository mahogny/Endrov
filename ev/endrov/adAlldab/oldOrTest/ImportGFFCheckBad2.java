package endrov.adAlldab.oldOrTest;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


/**
 * For importing a genome annotation, chromosome GFF
 * 
 * @author Johan Henriksson
 */
public class ImportGFFCheckBad2
	{
	
	public static interface GffRecordHandler
		{
		public void gffRecord();
		}

	private static Pattern ptab=Pattern.compile("\t");
	private static Pattern pcit=Pattern.compile("\"");

	public static String getUntilCitation(String s)
		{
		String inp[]=pcit.split(s,2);
		return inp[0];
		}
	
	
	
	
	public static class Annotation
	{
	public int annotid;
	public String source;
	public String feature;
	public int startpos;
	public int endpos;
	public String seqdesc;
	
	public String toString()
		{
		return "annotid:"+annotid+" source:"+source+" feature:"+feature+" start:"+startpos+" end:"+endpos+" desc:"+seqdesc;
		}
	
	//can be improved
	public boolean getAttributes() 
		{
		LinkedList<String> attr=new LinkedList<String>();
		
//		StringReader reader=new StringReader(seqdesc);
		
		StringBuffer curs=new StringBuffer();
		boolean inCite=false;

		for(char c:seqdesc.toCharArray())
			{
			if(c=='"')
				{
				inCite=!inCite;
				curs.append(c);
				}
			else if(c==';' && !inCite)
				{
				String s=curs.toString().trim();
				if(s.length()!=0)
					attr.add(s);
				curs=new StringBuffer();
				}
			else
				curs.append(c);
			}
		if(inCite)
			{
			System.out.println(seqdesc);
			return true;
			}
		else
			{
			String s=curs.toString().trim();
			if(s.length()!=0)
				attr.add(s);
			curs=new StringBuffer();
			}			
		return false;
		}
	
	//Can potentially query for additional information on-the-fly if not all columns are asked for. This lazy approach
	//is slower if all data is wanted but can be faster if only a sparse set is needed.
	}
	
	
	public static void importGFF(File infile, GffRecordHandler h) throws IOException
		{
		BufferedReader input = new BufferedReader(new FileReader(infile));
		int curline=0;
		String line = null;
		while (( line = input.readLine()) != null)
			{
			try
				{
				String inp[]=ptab.split(line, 10);
				Integer.parseInt(inp[3]);
				Integer.parseInt(inp[4]);

				
				Annotation ann=new Annotation();
				ann.seqdesc=inp[8];
				if(ann.getAttributes())
					System.out.println(line);

				}
			catch (Exception e)
				{
				e.printStackTrace();
				System.out.println("bad line:"+line);
				System.out.println("on line "+curline+": "+e.getMessage());
				}
			if(curline%100000==0)
				System.out.println("curline "+curline);
			curline++;
			}
		}
	
	
	
	public static int annotid=0;
	
	public static void main(String[] args)
		{
		
		try 
			{

			File infile=new File("/Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegansWS183.gff");
			importGFF(infile, new GffRecordHandler()
				{
				public void gffRecord()
					{
					}
				});
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.print("Done");
		}
	}
