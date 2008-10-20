package endrov.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
			bf.append(line);
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
	}
