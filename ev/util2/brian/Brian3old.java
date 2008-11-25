package util2.brian;


import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.StringTokenizer;

import endrov.util.EvParallel;

import bioserv.seqserv.io.*;


public class Brian3old
{

	public static void main(String[] args)
		{
		System.out.println(BrianSQL.connectPostgres("//193.11.32.108/brian", "postgres", "wuermli"));
	
	
		for(final String otherOrg:new String[]{"ppatens","creinhardtii"})
			{
			EvParallel.map_(Arrays.asList(new File("/home/tbudev3/bioinfo/brian/blast",otherOrg+".reverse").listFiles()), 
					new EvParallel.FuncAB<File, Object>(){
					public Object func(File bfile)
						{
						try
							{
	
							System.out.println(bfile);
							Blast2 b=Blast2.readModeXML(bfile);
	
							String wbGene=bfile.getName();
	
							//What is the rank?
							int rank=0;
							BrianSQL.runUpdate("delete from blastrank where organism='"+otherOrg+"' and cegene='"+wbGene+"'");
							for(Blast2.Entry e:b.entry)
								{
								//TODO: how to parse?
								StringTokenizer stok=new StringTokenizer(e.subjectid,"|");  //WBGene00007201|exos-4.1
								String thisWbGene=stok.nextToken();
								if(thisWbGene.equals(wbGene))
									{
									System.out.println("Found "+wbGene+" rank# "+rank);
	
									BrianSQL.runUpdate("insert into blastrank values('"+otherOrg+"','"+wbGene+"',"+rank+")");
	
									break;
									}
								rank++;
								}
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
	
						return null;
						}
			});
	
	
			/*
					for(File bfile:new File("/home/tbudev3/bioinfo/brian/blast",otherOrg+".reverse").listFiles())
						{
						System.out.println(bfile);
						Blast2 b=Blast2.readModeXML(bfile);
	
						String wbGene=bfile.getName();
	
						//What is the rank?
						int rank=0;
						runUpdate("delete from blastrank where organism='"+otherOrg+"' and cegene='"+wbGene+"'");
						for(Blast2.Entry e:b.entry)
							{
							//TODO: how to parse?
							StringTokenizer stok=new StringTokenizer(e.subjectid,"|");  //WBGene00007201|exos-4.1
							String thisWbGene=stok.nextToken();
							if(thisWbGene.equals(wbGene))
								{
								System.out.println("Found "+wbGene+" rank# "+rank);
	
								runUpdate("insert into blastrank values('"+otherOrg+"','"+wbGene+"',"+rank+")");
	
								break;
								}
							rank++;
							}
	
	
						}
	
	
	
			 */
			}
		System.out.println("main done");
		}
	
	}
