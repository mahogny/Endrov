package bioserv.seqserv.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import bioserv.seqserv.io.Blast2;
import bioserv.seqserv.io.Fasta;

//Naos blast in c.e: 3.6 sec
//2h for entire genome


public class BrianOld
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
			
			PrintWriter pw=new PrintWriter(new FileWriter("/home/tbudev3/briantemp.txt"));
			
			/*
			System.out.println(cegenes.seq.get("bbs-1"));
			Blast2 blast2=Blast2.invokeTblastn("celegans", "WBGene00000241|bbs-1",cegenes.seq.get("WBGene00000241|bbs-1"));
			System.exit(1);*/
			
			
			System.out.println("# of C.e genes: "+cegenes.seq.size());
			
			for(String key:cegenes.seq.keySet())
				{
				String ceseq=cegenes.seq.get(key);
				
				StringTokenizer ntok=new StringTokenizer(key);
				String shortName=ntok.nextToken();
				
				Blast2 blast=Blast2.invokeTblastn("ppatens", shortName,ceseq);

				
				System.out.println(key);
				
				System.out.println("blasthits: "+blast);

				if(!blast.entry.isEmpty())
					{
					/*
					int sStart=blast.entry.get(0).sStart;
					int sEnd=blast.entry.get(0).sEnd;*/
					
					
				
					
					
					
					pw.println(key);
					pw.println(blast);
					pw.flush();
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
