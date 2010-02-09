/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.adAlldab.oldOrTest;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

//to import: repeats , gene_IDs, gene_interpolated_map_positions, genetic_interactions, swissprot_mappings
//http://www.sanger.ac.uk/Software/formats/GFF/GFF_Spec.shtml

//elegans_pmap has attributes:
//Clone xxx
//Note Sequenced


//Clone C05A2; Note Sequenced; Accession number Z73968<tab>
//ctg313	sequenced	clone	59870	60200	.	.		Clone B0024; Note Sequenced; Accession number Z71178

//from spec: <seqname> <source> <feature> <start> <end> <score> <strand> <frame> [attributes] [comments]

///Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegans_pmapWS183.gff
//field 2 ex: yac, sequenced, cosmid, fosmid, plasmid
//field 3 ex: clone, contig

///Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegansWS183.gff
//incompatible
//reality: <chromosome> <seqname> <source> <feature> <start> <end> <score> <strand> <frame> [attributes] [comments]
//field 1: chromosome
//field 2: program it came from or something
//field 3: exon, intron, coding_exon, inverted_repeat, protein_match, nucleotide_match, RNAi_reagent, translated_nucleotide_match, EST_match, SAGE_tag, expressed_sequence_match, repeat_region, 
//field 4: start
//field 5: end
//field 6: .
//field 7: -
//field 8: .
//field 9: attribs
//attrib CDS "F31C3.2a"
//attrib Transcript "F31C3.2a"



/**
 * How to import: 
 * links.gff contains the lengths of the chromosomes as metadata. All start at 1. useless, ignore. 
 * 
 * 
 * 
 */


/**
 * For importing a genome annotation
 * 
 * @author Johan Henriksson
 */
public class ImportGffold3
	{
	public static class GffRecord
		{
		public String seqname, source, feature;
		public int start, end;
		public String score;
		//strand
		//frame
		Vector<String> attribs=new Vector<String>();
		//comments
		}
	
	public static interface GffRecordHandler
		{
		public void gffRecord(GffRecord r);
		}
	
	public static void importGFF(File infile, GffRecordHandler h) throws IOException
		{
		BufferedReader input = new BufferedReader(new FileReader(infile));
//		BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
		String line = null;
		while (( line = input.readLine()) != null)
			{
			String inp[]=p.split(line, 10);
			GffRecord gff=new GffRecord();
			gff.seqname=inp[0];
			gff.source=inp[1];
			gff.feature=inp[2];
			gff.start=Integer.parseInt(inp[3]);
			gff.end=Integer.parseInt(inp[4]);
			gff.score=inp[5];
			//6 ignore
			//7 ignore
			
			
			/*
			StringTokenizer st=new StringTokenizer(line,"\t");
			gff.seqname=st.nextToken();
			gff.source=st.nextToken();
			gff.feature=st.nextToken();
			gff.start=Integer.parseInt(st.nextToken());
			gff.end=Integer.parseInt(st.nextToken());
			gff.score=st.nextToken();
			st.nextToken();
			st.nextToken();
			String attribs=st.nextToken();
			*/
			StringTokenizer sta=new StringTokenizer(inp[8],";"); // "; "
			while(sta.hasMoreTokens())
				gff.attribs.add(sta.nextToken().trim());
			
			h.gffRecord(gff);
			}
		}
	private static Pattern p=Pattern.compile("\t");
	
	
	
		
	
	
	public static void main(String[] args)
		{
		try 
			{
//			File infile=new File("/Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegans_pmapWS183.gff");
			File infile=new File("/Volumes/TBU_xeon01_500GB02/userdata/biodb/celegans/incoming/gff2/elegansWS183.gff");
			importGFF(infile, new GffRecordHandler()
				{
				public void gffRecord(GffRecord gff)
					{
					System.out.print(" "+gff.seqname);
//					System.out.println("- "+gff.seqname+"\t"+gff.feature+"\t"+gff.start+"\t"+gff.end);
					}
				});
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	}
