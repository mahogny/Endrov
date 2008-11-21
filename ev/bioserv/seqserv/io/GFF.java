package bioserv.seqserv.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * GFF file handler
 * @author Johan Henriksson
 *
 */
public class GFF
	{
	public static final String featureINTRON="intron";
	public static final String featureEXON="exon";
	
	public static class Entry
		{
		
		
		public String seqname;
		public String source;
		public String feature;
		public Integer start, end;
		public String score;
		public String strand; // One of '+', '-' or '.'. '.' should be used when strand is not relevant, e.g. for dinucleotide repeats. Version 2 change: This field is left empty '.' for RNA and protein features.
		public String frame;  //One of '0', '1', '2' or '.'. '0' indicates that the specified region is in frame, i.e. that its first base corresponds to the first base of a codon. '1' indicates that there is one extra base, i.e. that the second base of the region corresponds to the first base of a codon, and '2' means that the third base of the region is the first base of a codon. If the strand is '-', then the first base of the region is value of <end>, because the corresponding coding region will run from <end> to <start> on the reverse strand. As with <strand>, if the frame is not relevant then set <frame> to '.'. It has been pointed out that "phase" might be a better descriptor than "frame" for this field. Version 2 change: This field is left empty '.' for RNA and protein features.

		public String attributes;
//		List<String> attribute=new LinkedList<String>();
		
		public String toString()
			{
			return seqname+"\t"+source+"\t"+feature+"\t"+start+"\t"+end+"\t"+score+"\t"+strand+"\t"+frame;
			}
		}

	public interface EntryFilter
		{
		boolean keep(Entry e);
		}
	
	
	public LinkedList<Entry> entry=new LinkedList<Entry>();
	
	
	public GFF(File file, EntryFilter f) throws IOException
		{
//		int keep=0,sofar=0;
		BufferedReader r=new BufferedReader(new FileReader(file));
		String line;
		while((line=r.readLine())!=null)
			{
			if(!line.startsWith("#"))
				try
					{
					
					int fromi=0;
					LinkedList<String> cols=new LinkedList<String>();
					for(;;)
						{
						int nexti=line.indexOf("\t",fromi);
						if(nexti==-1)
							{
							cols.add(line.substring(fromi));
							break;
							}
						else
							{
							String col=line.substring(fromi, nexti);
							cols.add(col);
							fromi=nexti+1;
							}
						}
					
					Entry e=new Entry();
					e.seqname=cols.get(0);
					e.source=cols.get(1);
					e.feature=cols.get(2);
					e.start=Integer.parseInt(cols.get(3));
					e.end=Integer.parseInt(cols.get(4));
					e.score=cols.get(5);
					e.strand=cols.get(6);
					e.frame=cols.get(7);
					e.attributes=cols.get(8);
	
//					sofar++;
					if(f==null || f.keep(e))
						{
						entry.add(e);
//						keep++;
//						if(keep%10000==0)
//							System.out.println(""+keep+"/"+sofar);
						}
	
					//System.out.println(e);
					}
				catch (NumberFormatException e)
					{
					System.out.println("err>"+line);
					for(int i=0;i<10;i++)
						System.out.println("follows>"+r.readLine());
	
					throw e;
					}
			}
		}
	
	
	}
