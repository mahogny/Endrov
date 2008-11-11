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
			
			
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		// TODO Auto-generated method stub

		}

	}
