package bioserv.seqserv.test;

import java.io.File;
import java.io.IOException;

import bioserv.seqserv.io.Fasta;
import bioserv.seqserv.io.GFF;

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
			Fasta cegenes=new Fasta(new File("/home/tbudev3/bioinfo/incdata/celegans/mart_gene100_0.faa"));
			System.out.println("read genes");
			
			
			for(String cegeneName:cegenes.seq.keySet())
				{
				String ceseq=cegenes.seq.get(cegeneName);
				
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
		
		// TODO Auto-generated method stub

		}

	}
