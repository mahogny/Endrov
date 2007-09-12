package util;

import java.io.*;

/**
 * Simple incremental backup utility optimized for OST2 but generalizes. Requires parent folder dates>=child date and uses this to avoid
 * disk traversal. Only works on unix systems, uses posix shell commands for speed.
 * We don't give guarantees if someone else decides to use this!
 * @author Johan Henriksson
 */
public class Backup
	{
	/**
	 * Entry point
	 */
	public static void main(String arg[])
		{
		if(arg.length==2)
			{
			File from=new File(arg[0]);
			File to=new File(arg[1]);
			try
				{
				backup(from,to);
				}
			catch(IOException e)
				{
				e.printStackTrace();
				}
			System.out.println("Done");
			}
		else
			System.out.println("args: fromDir toDir");
		}

	public final static boolean SAFE=false;
	
	
	/**
	 * Corresponds to cp -r. Will use current time.
	 */
	public static void copyRecursive(File from, File to) throws IOException
		{
		String f=from.getPath();
		String t=to.getPath();
		System.out.println("Copy "+f+" to "+t);
		if(!SAFE)
			waitProcess(Runtime.getRuntime().exec(new String[]{"/bin/cp","-R", f,t})); //could add -p
		}
	
	/**
	 * Delete recursively. 
	 * One could for safety also consider just moving them to a "trashbin"
	 */
	public static void deleteRecursive(File f) throws IOException
		{
		String p=f.getPath();
		System.out.println("Delete "+p);
		if(!SAFE)
			waitProcess(Runtime.getRuntime().exec(new String[]{"/bin/rm","-Rf", p}));
		}
	
	/**
	 * Recursive function, check from-directory if it should copy to to-directory
	 */
	public static void backup(File from, File to) throws IOException
		{
		System.out.println("recurse: "+from.getAbsolutePath()+" => "+to.getAbsolutePath());
		File[] fromFiles=from.listFiles();
		File[] toFiles=to.listFiles();
		for(File source:fromFiles)//.!!
			{
			File target=new File(to,source.getName());
			if(target.exists())
				{
				//If source and target exist, traverse if source younger
				long sourcet=source.lastModified();
				long targett=target.lastModified();
				if(sourcet>targett)
					{
					if(source.isDirectory())
						{
						backup(source,target);
						if(!SAFE)
							target.setLastModified(sourcet);
						}
					else
						{
						//File and need update, just copy
						copyRecursive(source, target);
						}
					}
				else
					System.out.println("Not updated "+source.getPath());
				}
			else
				{
				//If target does not exist, just copy over
				copyRecursive(source, target);
				}
			}
		for(File target:toFiles)//.!
			{
			File source=new File(from,target.getName());
			if(!source.exists())
				{
				//If source no longer is there, delete
				deleteRecursive(target);
				//MAKE SURE IT WORKS
				}
			}
		}
	
	
	private static void waitProcess(Process p) throws IOException
		{
		BufferedReader stdInput = new BufferedReader(new 
	      InputStreamReader(p.getInputStream()));
	  while ((stdInput.readLine()) != null);
		}
	}
