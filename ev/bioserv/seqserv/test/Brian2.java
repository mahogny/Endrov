package bioserv.seqserv.test;


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
				for(File bfile:new File("/home/tbudev3/bioinfo/brian/blast",otherOrg).listFiles())
					{
					System.out.println(bfile);
					Blast2 b=Blast2.readMode7(bfile);
					//Blast2.firstHitMode7(bfile);

					
					
					}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("main done");
		}

	}
