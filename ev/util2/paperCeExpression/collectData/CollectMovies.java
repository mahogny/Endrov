package util2.paperCeExpression.collectData;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.util.EvFileUtil;

public class CollectMovies
	{

	/**
	 * Assumes movies are already created - then puts them all in the right directory for publication
	 */
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later
		
		//Find recordings to compare
		Set<File> datas=PaperCeExpressionUtil.getAnnotated(); 

		
		File outdir=new File("/home/tbudev3/articleMovies"); 
		File indir=new File("/Volumes/TBU_main06/ost4dgood");

		
		try
			{
			outdir.mkdirs();
			EvFileUtil.deleteRecursive(outdir);
			outdir.mkdirs();
			
			for(File ostfile:indir.listFiles())
				{
				File fInfile=new File(new File(ostfile,"data"),"thumbnailMPEG4.avi");
				if(fInfile.exists())
					{
					System.out.println(fInfile);
					
					String gfpgene=PaperCeExpressionUtil.getGeneName(ostfile);
					
					System.out.println(gfpgene);
					
					String inset=datas.contains(ostfile) ? " *" : "";
					
					File fOutfile=new File(outdir,gfpgene+" ("+ostfile.getName()+")"+inset+".avi");
					System.out.println("----> "+fOutfile);
					
					if(!fOutfile.exists())
						EvFileUtil.copy(fInfile, fOutfile);
					
					}
				
				
				
				
				}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		System.exit(0);
		
		
		}
	
	}
