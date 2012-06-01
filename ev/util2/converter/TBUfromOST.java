package util2.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.util.EvFileUtil;

public class TBUfromOST
	{

	
	
	public static void main(String[] args)
		{
		try
			{
			PrintWriter errors=new PrintWriter(new FileWriter("/x/errors.txt"));
			
			
			EvLog.addListener(new EvLogStdout());
			EV.loadPlugins();

			
			//TODO deal with recordings that failed conversion!
			

			doone(
					errors,
					new File("/Volumes/TBU_main06/ost3dgood"),
					new File("/x/convertedost3dgood")
			);

			doone(
					errors,
					new File("/Volumes/TBU_main06/ost3dfailed"),
					new File("/x/convertedost3dfailed")
			);

			doone(
					errors,
					new File("/Volumes/TBU_main06/ost4dgood"),
					new File("/x/convertedost4dgood")
			);

			doone(
					errors,
					new File("/Volumes/TBU_main06/ost4dfailed"),
					new File("/x/convertedost4dfailed")
			);

			doone(
					errors,
					new File("/Volumes/TBU_main06/ost4dgood.soso"),
					new File("/x/convertedost4dgood.soso")
			);

			doone(
					errors,
					new File("/Volumes/TBU_main06/ost4dgood.excluded"),
					new File("/x/convertedost4dgood.excluded")
			);
			

			errors.close();
			
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		System.exit(0);
		
		
		
		}
	
	public static void copyRecursive(File from, File to) throws IOException
		{
		if(from.isDirectory())
			{
			to.mkdirs();
			for(File sub:from.listFiles())
				copyRecursive(sub, new File(to,sub.getName()));
			}
		else 
			{
			EvFileUtil.copy(from, to);
			}
		
		}
	
	
	private static void doone(PrintWriter errors, 
			File indir, File outdir)
		{
		

		for(File ostfile:indir.listFiles())
			{

			if(ostfile.getName().endsWith(".ost"))
				{

				String fname=ostfile.getName();
				fname=fname.substring(0, fname.length()-4)+".ome.tiff";
				System.out.println(fname);

				File outfileOMETIFF=new File(outdir,fname);


				
				if(outfileOMETIFF.exists())
					System.out.println("Already done: "+outfileOMETIFF);
				else
					{
					System.out.println(outfileOMETIFF);
					
					try
						{
						//Convert extra data
						File outfileData=new File(outdir,fname+".data");
						outfileData.mkdirs();
						System.out.println(outfileData);

						
						File infileData=new File(ostfile,"data");
						
						
						//Copy all the files in the data directory
						for(File indata:infileData.listFiles())
							{
							File outdata=new File(outfileData,indata.getName());
							System.out.println("  "+indata+" -> "+outdata);
							//should the create date etc be preserved?
							copyRecursive(indata, outdata);
							}
						
						//Copy all the tag files
						for(File indata:ostfile.listFiles())
							if(indata.getName().endsWith(".txt"))
							{
							File outdata=new File(outfileData,indata.getName());
							System.out.println("  "+indata+" -> "+outdata);
							//should the create date etc be preserved?
							//EvFileUtil.copy(indata, outdata);
							
							copyRecursive(indata, outdata);
							}
						
						
						
						
						//Convert images
						EvData data=EvData.loadFile(ostfile);
						data.saveDataAs(outfileOMETIFF);
						}
					catch (Exception e)
						{
						e.printStackTrace();
						
						errors.println(e.getMessage());
						for(StackTraceElement el:e.getStackTrace())
							errors.println(el);
						
						errors.flush();
						}
					
					

					
					}
				
				
				
				
				

				}


			}
		
		}
	}
