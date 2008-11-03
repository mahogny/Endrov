package bioserv.seqserv.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
		String queryid, subjectid;
		double identity;
		int alignmentLength;
		int mismatches;
		int gapOpenings;
		int qStart, qEnd;
		int sStart, sEnd;
		double evalue, bitscore;
		}
	
	public List<Entry> entry=new LinkedList<Entry>();
	
	//Assume output "tabular commented"
	public Blast2(File infile) throws IOException
		{
		BufferedReader input = new BufferedReader( new FileReader(infile) );
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
	
	
	public void invokeTblastn(String database, File input, File output)
		{
		//String cmd="blast2 -p tblastn -d "+database+" -i "+input.getPath()+" -m 9 -o "+output.getPath();
		
		
		
		}
	
	}
