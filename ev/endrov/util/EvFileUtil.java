package endrov.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;

/**
 * Utility functions for files
 * @author Johan Henriksson
 *
 */
public class EvFileUtil
	{
	/**
	 * Work-around for a bug/faulty design in getResource().getFile(), making space %20 etc
	 */
	public static File getFileFromURL(URL urlToDecode)
		{
	  try
			{
			return new File(URLDecoder.decode(urlToDecode.getFile(),"UTF-8"));
			}
		catch (UnsupportedEncodingException e)
			{
			e.printStackTrace();
			return null;
			}
		}

	/**
	 * Read file into string
	 */
	public static String readFile(File file) throws IOException
		{
		StringBuffer bf=new StringBuffer();
		BufferedReader br=new BufferedReader(new FileReader(file));
		String line;
		while((line=br.readLine())!=null)
			{
			bf.append(line);
			bf.append("\n");
			}
		//TODO: should read file exactly as is. do not use readline!
		return bf.toString();
		}
	
	/**
	 * Read file into string
	 */
	public static String readStream(InputStream is) throws IOException
		{
		StringBuffer bf=new StringBuffer();
		BufferedReader br=new BufferedReader(new InputStreamReader(is));
		String line;
		while((line=br.readLine())!=null)
			{
			bf.append(line);
			bf.append("\n");
			}
		//TODO: should read file exactly as is. do not use readline!
		return bf.toString();
		}
	
	/**
	 * Write string to file
	 */
	public static void writeFile(File file,String out) throws IOException
		{
		FileWriter fw=new FileWriter(file);
		fw.write(out);
		fw.close();
		}
	
	public static void touchRecursive(File f, long timestamp)
		{
		f.setLastModified(timestamp);
		File parent=f.getParentFile();
		if(parent!=null)
			touchRecursive(parent,timestamp);
		}
	
	/**
	 * Return file ending not including the dot, or null if there is none
	 */
	public static String fileEnding(File file)
		{
		String fn=file.getName();
		int doti=fn.lastIndexOf(".");
		if(doti!=-1)
			{
			String fileEnd=fn.substring(doti+1);
			return fileEnd;
			
			}
		else
			return null;
		}
	
	
	public static void deleteRecursive(File f) throws IOException
		{
		if(f.isDirectory())
			for(File c:f.listFiles())
				deleteRecursive(c);
		f.delete();
		}

	
	}
