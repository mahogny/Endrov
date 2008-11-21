package bioserv.seqserv.test;


import java.io.*;
import java.util.StringTokenizer;

import bioserv.seqserv.io.*;

//Naos blast in c.e: 3.6 sec
//2h for entire genome


public class Brian
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		try
			{
			//new GFF(new File("/home/tbudev3/bioinfo/incdata/celegans/gff2"));
//			Fasta cegenes=new Fasta(new File("/home/tbudev3/bioinfo/incdata/celegans/mart_gene100_0.faa"));
			Fasta cegenes=new Fasta(new File("/home/tbudev3/bioinfo/incdata/celegans/protein.faa"));
			System.out.println("read genes");
			
			System.out.println("# of C.e genes: "+cegenes.seq.size());
			
			for(String otherOrg:new String[]{"ppatens","creinhardtii"})
				for(String key:cegenes.seq.keySet())
					{
					String ceseq=cegenes.seq.get(key);
					
					StringTokenizer ntok=new StringTokenizer(key);
					ntok.nextToken(); //something
					ntok.nextToken(); //something
					String shortName=ntok.nextToken(); //WBname
					
					File out=new File(new File("/home/tbudev3/bioinfo/brian/blast",otherOrg),shortName);
					if(!out.exists())
						{
						System.out.println(out);
						Blast2.invokeTblastnToFile(otherOrg, shortName,ceseq,out,Blast2.MODE_XML);
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
		catch (IOException e)
			{
			e.printStackTrace();
			}
		System.out.println("main done");
		}

	}
