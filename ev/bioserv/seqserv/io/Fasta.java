/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.seqserv.io;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * FASTA file handler
 * 
 * @author Johan Henriksson
 */
public class Fasta
	{
	public Map<String, String> seq=new HashMap<String, String>(); //Name -> Seq
	
	public Fasta(File infile) throws IOException
		{
		BufferedReader input = new BufferedReader( new FileReader(infile) );
		String line = null;
		StringBuffer out=null;//new StringBuffer();
		String cur=null;
		while (( line = input.readLine()) != null)
			{
			if(line.startsWith(">"))
				{
				if(out!=null)
					seq.put(cur,out.toString());
				out=new StringBuffer();
				cur=line.substring(1);
				}
			else
				out.append(line);
			}
		seq.put(cur,out.toString());
		}

	/**
	 * Get the first (only(?)) sequence in the set
	 */
	public String onlySeq()
		{
		return seq.values().iterator().next();
		}
	
	public String onlyName()
		{
		return seq.keySet().iterator().next();
		}
	
	public void writeFile(File file) throws IOException
		{
		PrintWriter pw=new PrintWriter(new FileWriter(file));
		for(Map.Entry<String, String> e:seq.entrySet())
			{
			pw.print(">");
			pw.print(e.getKey());
			pw.println();
			pw.println(e.getValue());
			}
		pw.close();
		}
	
	}
