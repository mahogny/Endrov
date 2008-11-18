package bioserv.seqserv.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * BLAST2 output reader.
 * Assuming format tabular commented
 * @author Johan Henriksson
 *
 */
public class Blast2
	{
	public static class Entry
		{
		public String queryid; //Name of sequence searched for
		public String subjectid; //Name of sequence in which there is a hit
		public double identity;
		public int alignmentLength;
		public int mismatches;
		public int gapOpenings;
		public int qStart, qEnd;
		public int sStart, sEnd;
		public double evalue, bitscore;
		
		public String toString()
			{
			return queryid+"\t"+subjectid+"\t"+identity+"\t"+alignmentLength+"\t"+mismatches+"\t"+
			gapOpenings+"\t"+qStart+"\t"+qEnd+"\t"+sStart+"\t"+sEnd+"\t"+evalue+"\t"+bitscore;
			//return subjectid;
			}
		}
	
	public List<Entry> entry=new LinkedList<Entry>();
	
	//Assume output "tabular commented"
	public Blast2(File infile) throws IOException
		{
		this(new BufferedReader( new FileReader(infile) ));
		}
	
	public Blast2(BufferedReader input) throws IOException
		{
		String line = null;
		while (( line = input.readLine()) != null)
			{
			if(line.startsWith("#"))
				;
			else
				{
				//# Fields: Query id, Subject id, % identity, alignment length, mismatches, gap openings, q. start, q. end, s. start, s. end, e-value, bit score
				StringTokenizer tok=new StringTokenizer(line,"\t");
				Entry e=new Entry();
				e.queryid=tok.nextToken();
				e.subjectid=tok.nextToken();
				e.identity=Double.parseDouble(tok.nextToken());
				e.alignmentLength=Integer.parseInt(tok.nextToken());
				e.mismatches=Integer.parseInt(tok.nextToken());
				e.gapOpenings=Integer.parseInt(tok.nextToken());
				e.qStart=Integer.parseInt(tok.nextToken());
				e.qEnd=Integer.parseInt(tok.nextToken());
				e.sStart=Integer.parseInt(tok.nextToken());
				e.sEnd=Integer.parseInt(tok.nextToken());
				e.evalue=Double.parseDouble(tok.nextToken());
				e.bitscore=Double.parseDouble(tok.nextToken());
				
				entry.add(e);
				}
			}
		}
	
	
	//work with files or stdin?
	//how to cache data? lazy evaluation?
	//flows? separate from all this
	//SQL integration?
	
	
	public static Blast2 invokeTblastn(String database, String seqname, String sequence)
		{
		//String cmd="blast2 -p tblastn -d "+database+" -i "+input.getPath()+" -m 9 -o "+output.getPath();
		ProcessBuilder pb=new ProcessBuilder("/usr/bin/blast2","-p","tblastn","-d",database,"-m","9");

		/*
		StringBuffer sb=new StringBuffer();
		for(String s:pb.command())
			sb.append(s+" ");
		System.out.println(sb);*/
		
		try
			{
			
			Process p=pb.start();
			PrintWriter out=new PrintWriter(p.getOutputStream());
			out.println(">"+seqname);
			out.println(sequence);
			out.flush();
			out.close();
			
			

			return new Blast2(new BufferedReader(new InputStreamReader(p.getInputStream())));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		//WTF?
		return null;
		}
	
	
	public static void invokeTblastnToFile(String database, String seqname, String sequence,File outfile)
		{
		ProcessBuilder pb=new ProcessBuilder("/usr/bin/blast2","-p","tblastn","-d",database,"-m","9","-o",outfile.getPath());

		/*
		StringBuffer sb=new StringBuffer();
		for(String s:pb.command())
			sb.append(s+" ");
		System.out.println(sb);*/
		
		try
			{
			
			Process p=pb.start();
			PrintWriter out=new PrintWriter(p.getOutputStream());
			out.println(">"+seqname);
			out.println(sequence);
			out.flush();
			out.close();
			
			p.waitFor();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	/*		
	final BufferedReader din = new BufferedReader(new InputStreamReader(p.getInputStream()));
	String line;
	while ( (line = din.readLine()) != null ) out.write("out: "+line);
	return null;
	*/
	
	public String toString()
		{
		StringBuffer sb=new StringBuffer();
		sb.append("{");
		for(Entry e:entry)
			sb.append(e+"\n");
		sb.append("}");
		return sb.toString();
		}
	
	}
