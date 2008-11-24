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


import org.jdom.Document;
import org.jdom.Element;
import endrov.util.EvXmlUtil;

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
		
		public String qseq, hseq, midline;
		
		public String toString()
			{
			return queryid+"\t"+subjectid+"\t"+identity+"\t"+alignmentLength+"\t"+mismatches+"\t"+
			gapOpenings+"\t"+qStart+"\t"+qEnd+"\t"+sStart+"\t"+sEnd+"\t"+evalue+"\t"+bitscore;
			//return subjectid;
			}
		}
	
	public static final int MODE_XML=7;
	public static final int MODE_TABULAR=9;
	
	
	public List<Entry> entry=new LinkedList<Entry>();
	
	//Assume output "tabular commented"
	public static Blast2 readModeTabular(File infile) throws IOException
		{
		return Blast2.readModeTabular(new BufferedReader( new FileReader(infile) ));
		}
	public static Blast2 readModeTabular(BufferedReader input) throws IOException
		{
		Blast2 b=new Blast2();
		
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
				
				b.entry.add(e);
				}
			}
		return b;
		}
	
	
	//These parsers are not fast!
	public static Blast2 readModeXML(File infile) throws Exception
		{
		return Blast2.readModeXML(new BufferedReader( new FileReader(infile) ));
		}
	public static Blast2 readModeXML(BufferedReader input) throws Exception
		{
		Blast2 b=new Blast2();
		
		Document doc=EvXmlUtil.readXML(input);
		
		Element root=doc.getRootElement();
		Element hits=root.getChild("BlastOutput_iterations").getChild("Iteration").getChild("Iteration_hits");
		
		String queryDef=root.getChildText("BlastOutput_query-def");
		
		
		
		if(hits!=null)
			for(Element hite:EvXmlUtil.getChildrenE(hits,"Hit"))
				{
	//			int hitnum=Integer.parseInt(hite.getChildText("Hit_num"));
				
				String hitDef=hite.getChildText("Hit_def");
				
				for(Element hsp:EvXmlUtil.getChildrenE(hite.getChild("Hit_hsps"),"Hsp"))
					{
					Entry e=new Entry();
					e.queryid=queryDef;
					e.subjectid=hitDef;
					
					e.qseq=hsp.getChildText("Hsp_qseq");
					e.hseq=hsp.getChildText("Hsp_hseq");
					e.midline=hsp.getChildText("Hsp_midline");
					b.entry.add(e);
					}
				}
		return b;
		}
	
	
	/*
	public class Mode7parser extends DefaultHandler
		{
		int cmode=0;
		public void startElement(String namespaceURI, String localName,String qName, Attributes atts) 
			{
			if(qName.equals("Hsp_qseq"))
				System.out.println(qName);
			}
	
		public void characters(char[] ch, int start, int length) throws SAXException 
			{
			String s = new String(ch,start,length);
			switch(cmode)
				{
				case 1:
				break;
				}
			cmode=0;
			}
		}
	public static Blast2 firstHitMode7(File infile) throws Exception
		{
		Blast2 b=new Blast2();
		
		Mode7parser p=b.new Mode7parser();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		sp.parse(infile, p);
		return b;
		}
	*/
	
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
			
			

			return Blast2.readModeTabular(new BufferedReader(new InputStreamReader(p.getInputStream())));
//			return new Blast2(new BufferedReader(new InputStreamReader(p.getInputStream())));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		//WTF?
		return null;
		}
	
	
	public static void invokeTblastnToFile(String database, String seqname, String sequence,File outfile, int mode) throws IOException
		{
		//9 nice text
		//7 xml
		
		File tempFile=File.createTempFile("blast", "");
		ProcessBuilder pb=new ProcessBuilder("/usr/bin/blast2","-p","tblastn","-d",database,"-m",""+mode,"-o",tempFile.getPath());

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
			if(outfile.exists())
				outfile.delete();
			tempFile.renameTo(outfile);
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
