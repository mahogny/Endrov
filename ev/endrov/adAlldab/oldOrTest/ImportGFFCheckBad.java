/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.adAlldab.oldOrTest;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


/**
 * For importing a genome annotation, chromosome GFF
 * 
 * @author Johan Henriksson
 */
public class ImportGFFCheckBad
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

				int cnt=0;
				for(int i=0;i<inp[8].length();i++)
					if(inp[8].charAt(i)=='\"')
						cnt++;
				if(cnt%2!=0)
					System.out.println("\" mismatch : "+ line);

				StringTokenizer sta=new StringTokenizer(inp[8],";"); // "; "
				while(sta.hasMoreTokens())
					{
					String at=sta.nextToken().trim();

					if(at.equals(""))
						System.out.println("Empty attribute (has weird ;) : "+ line);
						
					}

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
