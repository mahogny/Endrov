package util2.brian;


import java.io.*;
import java.sql.*;
import java.util.Arrays;

import endrov.util.EvFileUtil;
import endrov.util.EvParallel;


public class BrianUp
{

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		System.out.println(BrianSQL.connectPostgres("//193.11.32.108/brian", "postgres", "wuermli"));
	
	
		for(final String otherOrg:new String[]{"ppatens","creinhardtii"})
			{
			EvParallel.map_(Arrays.asList(new File("/home/tbudev3/bioinfo/brian/blast",otherOrg+".reverse").listFiles()), 
//			EvParallel.map_(Arrays.asList(new File("/home/tbudev3/bioinfo/brian/blast",otherOrg/*+".reverse"*/).listFiles()), 
					new EvParallel.FuncAB<File, Object>(){
					public Object func(File bfile)
						{
						try
							{
							String content=EvFileUtil.readFile(bfile);
							String wbGene=bfile.getName();
							
							
							PreparedStatement ps=BrianSQL.conn.prepareStatement("insert into reverseblast values(?,?,?)");
							//PreparedStatement ps=conn.prepareStatement("insert into blastfromce values(?,?,?)");
							
							ps.setString(1, otherOrg);
							ps.setString(2, wbGene);
							ps.setString(3, content);
							
							ps.execute();
							System.out.println(wbGene);
							
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
	
						return null;
						}
			});
	
	
			}
		System.out.println("main done");
		}
	
	}
