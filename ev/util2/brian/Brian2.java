package util2.brian;


import java.io.*;

import bioserv.seqserv.io.*;


public class Brian2
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		try
			{
			for(String otherOrg:new String[]{"ppatens","creinhardtii"})
				{
				File outdir=new File("/home/tbudev3/bioinfo/brian/blast",otherOrg+".reverse");

				for(File bfile:new File("/home/tbudev3/bioinfo/brian/blast",otherOrg).listFiles())
					{
					File outfile=new File(outdir,bfile.getName());
					//Problem: sometimes target is not created because there was no source.
					//messy to re-run
					if(!outfile.exists())
						{
						System.out.println(bfile+"\t=>\t"+outfile);
						Blast2 b=Blast2.readModeXML(bfile);
						//Blast2.firstHitMode7(bfile);
						
						
						
						if(!b.entry.isEmpty())
							{						
							Blast2.Entry e=b.entry.iterator().next();
							String found=e.hseq;

							//found=found.replaceAll("-", "");
							found=found.replaceAll("\\p{Punct}", "");
							System.out.println(found);

							Blast2.invokeTblastnToFile("celegans", "",found,outfile,Blast2.MODE_XML);


							}
						}
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
