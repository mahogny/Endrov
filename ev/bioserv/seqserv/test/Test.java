package bioserv.seqserv.test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import bioserv.seqserv.io.Fasta;
import bioserv.seqserv.io.GFF;

public class Test
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		try
			{
			new GFF(new File("/home/tbudev3/foo.gff"),null);
			Fasta f=new Fasta(new File("/home/tbudev3/foo.fasta"));
			for(Map.Entry<String, String> e:f.seq.entrySet())
				System.out.println(e);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}

	}
