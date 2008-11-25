package util2.brian;


import java.io.*;
import java.sql.ResultSet;
import java.util.TreeSet;

import bioserv.seqserv.io.*;

/**
 * Do reverse blasting
 */
public class Brian2
	{
	public static void main(String[] args)
		{
		try
			{
			System.out.println(BrianSQL.connectPostgres("//193.11.32.108/brian", "postgres", "wuermli"));
			
			for(String otherOrg:new String[]{"ppatens","creinhardtii"})
				{
				ResultSet rs=
				BrianSQL.runQuery(
						"select cegene from (" +
						"select cegene,organism from blastfromce where organism='"+otherOrg+"' " +
						"and blastout is not null and (cegene,organism) not in (select cegene,organism from reverseblast)" +
						") as foo1");
				TreeSet<String> geneTODO=new TreeSet<String>();
				while(rs.next())
					geneTODO.add(rs.getString(1));
				System.out.println("TODO: "+geneTODO.size());
				
				for(String wbGene:geneTODO)
					{
					System.out.println(wbGene);
					String finContent=BrianSQL.getBlastResult("blastfromce", otherOrg, wbGene);
					Blast2 b=Blast2.readModeXML(new StringReader(finContent));
					
					if(!b.entry.isEmpty())
						{	
						Blast2.Entry e=b.entry.iterator().next();
						String found=e.hseq;
					
						found=found.replaceAll("\\p{Punct}", "");
						System.out.println("Found: "+found);

						File outfile=File.createTempFile("foo", "");
						Blast2.invokeTblastnToFile("celegans", "",found,outfile,Blast2.MODE_XML);
						BrianSQL.insertBlastResult("reverseblast", otherOrg, wbGene, outfile);
						outfile.delete();
						}
					else
						BrianSQL.insertBlastResult("reverseblast", otherOrg, wbGene, "");
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
