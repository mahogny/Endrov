package util2.brian;


import java.io.*;
import java.sql.*;
import java.util.StringTokenizer;
import java.util.TreeSet;

import bioserv.seqserv.io.*;

/**
 * Forward blasting
 */
public class Brian
	{
	public static void main(String[] args)
		{
		System.out.println(BrianSQL.connectPostgres("//193.11.32.108/brian", "postgres", "wuermli"));
		
		try
			{
			Fasta cegenes=new Fasta(new File("/home/tbudev3/bioinfo/incdata/celegans/protein.faa"));
			System.out.println("read genes");
			
			System.out.println("# of C.e genes: "+cegenes.seq.size());
			
			for(String otherOrg:new String[]{"ppatens","creinhardtii"})
				{
				//Which genes are left to do?
				TreeSet<String> geneNotTODO=new TreeSet<String>(cegenes.seq.keySet());
				ResultSet rs=BrianSQL.runQuery("select cegene,organism from blastfromce where organism='"+otherOrg+"'");
				while(rs.next())
					geneNotTODO.add(rs.getString(1));
				System.out.println("Genes not todo: "+geneNotTODO.size());
				
				//Blast every gene
				for(String key:cegenes.seq.keySet())
					{
					StringTokenizer ntok=new StringTokenizer(key);
					ntok.nextToken(); //something
					ntok.nextToken(); //something
					String wbGene=ntok.nextToken(); //WBname

					if(!geneNotTODO.contains(wbGene))
						{
						String ceseq=cegenes.seq.get(key);

						File out=File.createTempFile("foo", "");
						//					File out=new File(new File("/home/tbudev3/bioinfo/brian/blast",otherOrg),shortName);
						System.out.println(wbGene);
						Blast2.invokeTblastnToFile(otherOrg, wbGene,ceseq,out,Blast2.MODE_XML);
						BrianSQL.insertBlastResult("blastfromce", otherOrg, wbGene, out);
						out.delete();
						}
					else
						System.out.println("Skipping "+wbGene);

					}
				}
			
			//For all genes in c.e
			//  blast in X
			//  store blast result in file
			//  take #1 hit, blast in ce, is the search gene the best hit?
			//   ### need to know the location of the c.e gene
			//   ### store the ranking in SQL?
			
			//repeat for Y,Z
			
			//use sql to find intersection etc based on score

			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("main done");
		}

	}
