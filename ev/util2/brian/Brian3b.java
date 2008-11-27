package util2.brian;


import java.io.*;
import java.sql.*;
import java.util.*;

import endrov.util.EvParallel;

import bioserv.seqserv.io.*;

/**
 * Extract ranking or null
 */
public class Brian3b
	{
	
	public static void main(String[] args)
		{
		System.out.println(BrianSQL.connectPostgres("//193.11.32.108/brian", "postgres", "wuermli"));
	
		try
			{
			for(final String otherOrg:new String[]{"creinhardtii","ppatens"})
				{
				//Which records need be done?
				System.out.println("Finding work: "+otherOrg);
				ResultSet rs=
				BrianSQL.runQuery(
						"select cegene from (" +
						"select cegene,organism from reverseblast as t1 where organism='"+otherOrg+"' " +
						"and blastout is not null and (cegene,organism) not in (select cegene,organism from blastrank2 as t2 where t2.cegene=t1.cegene)" +
				") as foo1");
				LinkedList<String> geneTODO=new LinkedList<String>();
				while(rs.next())
					geneTODO.add(rs.getString(1));
				System.out.println("TODO: "+geneTODO.size());
	
				//CPU not limiting, use many threads
				//why is it not??
				//is it the XML validation????
				
				//Do records (in parallel)
				EvParallel.map_(500, geneTODO, 
						new EvParallel.FuncAB<String, Object>(){
						public Object func(String wbGene)
							{
							try
								{
								//Read BLAST output
								System.out.println(wbGene);
								Blast2 bforward=Blast2.readModeXML(new StringReader(BrianSQL.getBlastResult("blastfromce", otherOrg, wbGene)));
								Blast2 brev=Blast2.readModeXML(new StringReader(BrianSQL.getBlastResult("reverseblast", otherOrg, wbGene)));
	
								//What is the rank?
								int rank=0;
								BrianSQL.runUpdate("delete from blastrank2 where organism='"+otherOrg+"' and cegene='"+wbGene+"'");
								Integer foundRank=null;
								Double reveval=null;
								for(Blast2.Entry e:brev.entry)
									{
									//TODO: how to parse?
									StringTokenizer stok=new StringTokenizer(e.subjectid,"|");  //WBGene00007201|exos-4.1
									String thisWbGene=stok.nextToken();
									if(thisWbGene.equals(wbGene))
										{
										System.out.println("Found "+wbGene+" rank# "+rank);
										foundRank=rank;
										reveval=e.evalue;
										break;
										}
									rank++;
									}

								//E-value in forward
								double foreval=bforward.entry.get(0).evalue;
								
								
								BrianSQL.runUpdate("insert into blastrank2 values('"+otherOrg+"','"+wbGene+"',"+foreval+","+reveval+","+foundRank+")");
								}
							catch (Exception e)
								{
								e.printStackTrace();
								}
	
							return null;
							}
				});
				}
			}
		catch (SQLException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		System.out.println("main done");
		System.exit(0);
		}
	
	}
