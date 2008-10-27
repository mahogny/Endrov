package bioserv.seqserv.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * GFF file handler
 * @author Johan Henriksson
 *
 */
public class GFF
	{
	public static class Entry
		{
		String seqname;
		String source;
		String feature;
		Integer start, end;
		String score;
		String strand; // One of '+', '-' or '.'. '.' should be used when strand is not relevant, e.g. for dinucleotide repeats. Version 2 change: This field is left empty '.' for RNA and protein features.
		String frame;  //One of '0', '1', '2' or '.'. '0' indicates that the specified region is in frame, i.e. that its first base corresponds to the first base of a codon. '1' indicates that there is one extra base, i.e. that the second base of the region corresponds to the first base of a codon, and '2' means that the third base of the region is the first base of a codon. If the strand is '-', then the first base of the region is value of <end>, because the corresponding coding region will run from <end> to <start> on the reverse strand. As with <strand>, if the frame is not relevant then set <frame> to '.'. It has been pointed out that "phase" might be a better descriptor than "frame" for this field. Version 2 change: This field is left empty '.' for RNA and protein features.

		String attributes;
//		List<String> attribute=new LinkedList<String>();
		}

	public LinkedList<Entry> entry=new LinkedList<Entry>();
	
	
	public GFF(File file) throws IOException
		{
		BufferedReader r=new BufferedReader(new FileReader(file));
		String line;
		while((line=r.readLine())!=null)
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
			entry.add(e);
			e.seqname=cols.get(0);
			e.source=cols.get(1);
			e.feature=cols.get(2);
			e.start=Integer.parseInt(cols.get(3));
			e.end=Integer.parseInt(cols.get(4));
			e.score=cols.get(5);
			e.strand=cols.get(6);
			e.frame=cols.get(7);
			e.attributes=cols.get(8);
			}
		}
	
	
	}
