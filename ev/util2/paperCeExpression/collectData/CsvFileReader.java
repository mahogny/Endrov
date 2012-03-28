package util2.paperCeExpression.collectData;

import java.io.*;
import java.util.ArrayList;


/**
 * 
 * @author Johan Henriksson
 *
 */
public class CsvFileReader
	{
	private BufferedReader in;
	private char sep;
	
	
	public CsvFileReader(File f, char sep) throws IOException
		{
		in=new BufferedReader(new FileReader(f));
		this.sep=sep;
		}
	
	/**
	 * Get next line or null
	 */
	public ArrayList<String> readLine() throws IOException
		{
		String line=in.readLine();
		//Skip comments
		while(line!=null && line.startsWith("#"))
			line=in.readLine();
		
		if(line==null)
			return null;
		line=line+sep;
		
		ArrayList<String> lineel=new ArrayList<String>();
		
		while(line.indexOf(sep)!=-1)
			{
			int i=line.indexOf(sep);
			String s=line.substring(0,i);
			lineel.add(s);
			line=line.substring(i+1);
			}
		return lineel;
		}
	}
