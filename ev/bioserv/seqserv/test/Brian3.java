package bioserv.seqserv.test;


import java.io.*;
import java.util.StringTokenizer;

import bioserv.seqserv.io.*;


public class Brian3
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		try
			{
			for(String otherOrg:new String[]{"ppatens","creinhardtii"})
				{
				for(File bfile:new File("/home/tbudev3/bioinfo/brian/blast",otherOrg+".reverse").listFiles())
					{
					System.out.println(bfile);
					Blast2 b=Blast2.readModeXML(bfile);

					String wbGene=bfile.getName();
					
					//What is the rank?
					int rank=0;
					for(Blast2.Entry e:b.entry)
						{
						//TODO: how to parse?
						StringTokenizer stok=new StringTokenizer(e.subjectid,"|");  //WBGene00007201|exos-4.1
						String thisWbGene=stok.nextToken();
						if(thisWbGene.equals(wbGene))
							{
							System.out.println("Found "+wbGene+" rank# "+rank);
							
							break;
							}
						rank++;
						}
					
					
					}
					
					
					
					}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("main done");
		}

	}
