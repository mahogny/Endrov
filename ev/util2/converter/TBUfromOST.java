package util2.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
			PrintWriter errors = new PrintWriter(new FileWriter("/home/tbudev3/errors.txt"));

			EvLog.addListener(new EvLogStdout());
			EV.loadPlugins();

			// TODO deal with recordings that failed conversion!

	
			/*
		  doone( errors, new File("/Volumes/TBU_main06/ost3dfailed"), new
				  File("/x/convertedost3dfailed") ); 

		  doone( errors, new
		 File("/Volumes/TBU_main06/ost4dfailed"), new
		 File("/x/convertedost4dfailed") ); 
			
			  doone( errors, new
			  File("/Volumes/TBU_main06/ost4dgood"), new
			  File("/x/convertedost4dgood") ); 
			
		  doone( errors, new File("/Volumes/TBU_main06/ost4dgood.excluded"), new
		  File("/x/convertedost4dgood.excluded") );

		  doone( errors, new
			  File("/Volumes/TBU_main06/ost4dgood.soso"), new
			  File("/x/convertedost4dgood.soso") );
						
			doone(errors, new File("/Volumes/TBU_main06/ost3dgood"), new File(
					"/x/convertedost3dgood"));
			*/
			
			
			
			doone( errors, new
				  File("/pimai/TBU_extra05/pimaiost3dgood"), new
				  File("/x/pimaiost3dgood") );

			
			doone( errors, new
				  File("/pimai/TBU_extra05/pimaiost4dfailed"), new
				  File("/x/pimaiost4dfailed") );

			doone( errors, new
				  File("/pimai/TBU_extra05/pimaiost4dgood"), new
				  File("/x/pimaiost4dgood") );

			doone( errors, new
				  File("/pimai/TBU_extra05/pimaiost4dpaper"), new
				  File("/x/c") );

			/* this one 1Tb
			doone( errors, new
				  File("/pimai/TBU_extra05/pimaiost/pimaiostdaemon"), new
				  File("/x/pimaiostdaemon") );
			*/
			

			errors.close();

			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.exit(0);

		}

	public static void copyRecursive(File from, File to) throws IOException
		{
		if (from.isDirectory())
			{
			to.mkdirs();
			for (File sub : from.listFiles())
				copyRecursive(sub, new File(to, sub.getName()));
			}
		else
			{
			EvFileUtil.copy(from, to);
			}

		}

	private static void fixOST(File ostfile)
		{

		for (File subfile : ostfile.listFiles())
			{
			if (subfile.getName().startsWith("blob"))
				{
				Set<String> hadname = new HashSet<String>();
				int planes = 0;
				File[] framefiles = subfile.listFiles();
				if (framefiles.length!=0)
					{
					// Count planes
					for (File ffile : framefiles)
						{
						if (ffile.isDirectory()&&!ffile.getName().startsWith("."))
							{
							for (File zfile : ffile.listFiles())
								{
								if (zfile.getName().endsWith(".jpg")
										||zfile.getName().endsWith(".jpg")
										||zfile.getName().startsWith("b"))
									planes++;
								hadname.add(zfile.getName());
								}
							break;
							}
						}
					}

				boolean modifiedFrames = false;

				// Now for every frame, copy the last slice as many times as needed
				for (File framefile : framefiles)
					if (framefile.isDirectory()&&!framefile.getName().startsWith("."))
						{

						// Find the last file that can be copied
						SortedMap<Integer, File> sliceFiles = new TreeMap<Integer, File>();
						File lastfile = null;
						int lastfileIndex = -1;
						for (File f : framefile.listFiles())
							if (f.getName().startsWith("b"))
								{
								String name = f.getName();

								name = name.substring(1, name.indexOf("."));
								int index = Integer.parseInt(name);
								if (lastfile==null||index>lastfileIndex)
									{
									lastfileIndex = index;
									lastfile = f;
									}

								sliceFiles.put(index, f);
								}
						// System.out.println("--- "+lastfile);

						// Check for expected files
						for (String n : hadname)
							{
							File ename = new File(framefile, n);
							if (!ename.exists())
								{

								String name = ename.getName();
								name = name.substring(1, name.indexOf("."));
								int index = Integer.parseInt(name);

								// Find the closest file above

								SortedMap<Integer, File> headFiles = sliceFiles.headMap(index);
								if (headFiles.isEmpty())
									System.out.println("eeeek, do manually "+ename);
								else
									{
									File copyfile = headFiles.get(headFiles.lastKey());
									System.out.println("todo: "+copyfile+" ----> "+ename);

									try
										{
										EvFileUtil.copy(copyfile, ename);
										modifiedFrames = true;
										}
									catch (IOException e)
										{
										e.printStackTrace();
										}

									}

								}
							}

						if (hadname.size()==1)
							{
							if (hadname.contains("b00000001.jpg"))
								{
								File fromfile=new File(framefile, "b00000001.jpg");
								File tofile  =new File(framefile, "b00000000.jpg");
								fromfile.renameTo(tofile);
								modifiedFrames = true;
								System.out.println("fix2: "+fromfile+" --> "+tofile);

								}
							else if (hadname.contains("b00000001.png"))
								{
								modifiedFrames = true;

								File fromfile=new File(framefile, "b00000001.png");
								File tofile  =new File(framefile, "b00000000.png");
								fromfile.renameTo(tofile);
								modifiedFrames = true;
								System.out.println("fix2: "+fromfile+" --> "+tofile);

								}

							}

						// Check which files exist
						// for(File zfi)

					

						}

				
				if (modifiedFrames)
					{
					File fimcache = new File(subfile, "imagecache.txt");
					fimcache.delete();
					}
				
				}
			}

		}

	private static void doone(PrintWriter errors, File indir, File outdir)
		{

		for (File ostfile : indir.listFiles())
			{

			if (ostfile.getName().endsWith(".ost"))
				{

				String fname = ostfile.getName();
				fname = fname.substring(0, fname.length()-4)+".ome.tiff";
				System.out.println(fname);

				File outfileOMETIFF = new File(outdir, fname);

				if (outfileOMETIFF.exists())
					System.out.println("Already done: "+outfileOMETIFF);
				else
					{
					System.out.println(outfileOMETIFF);

					try
						{
						fixOST(ostfile);
						}
					catch (Exception e1)
						{
						e1.printStackTrace();
						}

					try
						{
						// Convert extra data
						File outfileData = new File(outdir, fname+".data");
						outfileData.mkdirs();
						System.out.println(outfileData);

						// Copy all the files in the data directory
						File infileData = new File(ostfile, "data");
						if(infileData.exists())
							{
							for (File indata : infileData.listFiles())
								{
								File outdata = new File(outfileData, indata.getName());
								System.out.println("  "+indata+" -> "+outdata);
								// should the create date etc be preserved?
								copyRecursive(indata, outdata);
								}
							}
						
						// Copy all the tag files
						for (File indata : ostfile.listFiles())
							if (indata.getName().endsWith(".txt"))
								{
								File outdata = new File(outfileData, indata.getName());
								System.out.println("  "+indata+" -> "+outdata);
								// should the create date etc be preserved?
								// EvFileUtil.copy(indata, outdata);

								copyRecursive(indata, outdata);
								}
						
						// Convert images
						EvData data = EvData.loadFile(ostfile);
						data.saveDataAs(outfileOMETIFF);
						}
					catch (Exception e)
						{
						e.printStackTrace();

						errors.println(e.getMessage());
						for (StackTraceElement el : e.getStackTrace())
							errors.println(el);

						errors.flush();
						}

					}

				}

			}

		}
	}
