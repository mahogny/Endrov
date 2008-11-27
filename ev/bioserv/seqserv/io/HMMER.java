package bioserv.seqserv.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Parser for HMMER output
 * @author Johan Henriksson
 *
 */
public class HMMER
	{
	public class Entry	
		{
		public String seqName;
		public int start,end;
		public String chromSeq, matchSeq;
		public double score, Evalue;

		public String toString()
			{
			return seqName+": "+start+" - "+end+" score:"+score+" E:"+Evalue+" ("+chromSeq+")-("+matchSeq+")";
			}
		//System.out.println(""+);

		}
	
	public List<Entry> entry=new LinkedList<Entry>();
	
	
	public HMMER(File infile) throws IOException
		{
		this(new BufferedReader( new FileReader(infile) ));
		}
	public HMMER (BufferedReader input) throws IOException
		{
		String line = null;

		input.readLine(); //hmmpfam - search one or more sequences against HMM database
		input.readLine(); //HMMER 2.3.2 (Oct 2003)
		input.readLine(); //Copyright (C) 1992-2003 HHMI/Washington University School of Medicine
		input.readLine(); //Freely distributed under the GNU General Public License (GPL)
		input.readLine(); //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		input.readLine(); //HMM file:                 out.hmm
		input.readLine(); //Sequence file:            /home/bioinfo/incdata/celegans/dna.fa
		input.readLine(); //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

		
		
		while (( line = input.readLine()) != null)
			{
			String qseqLine=input.readLine();//		Query sequence: CHROMOSOME_X
			String seqName=qseqLine.substring(qseqLine.indexOf(":")+2);
			input.readLine(); //Accession:      [none]
			input.readLine(); //Description:    [none]
			input.readLine(); //
			input.readLine(); //Scores for sequence family classification (score includes all domains):
			input.readLine(); //Model    Description                                    Score    E-value  N 
			input.readLine(); //-------- -----------                                    -----    ------- ---
			input.readLine(); //input                                                  4475.1          0 3155
			input.readLine(); //
			input.readLine(); //Parsed for domains:
			input.readLine(); //Model    Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
			input.readLine(); //-------- ------- ----- -----    ----- -----      -----  -------
			input.readLine(); //input      1/3155  7379  7393 ..     1    15 []     0.8     0.37
			while (( line = input.readLine()) != null)
				if(line.length()==0)
					break;
			input.readLine(); //Alignments of top-scoring domains:

			while (( line = input.readLine()) != null)
				{
				if(line.startsWith("//"))
					break;
				String line1=line; //input: domain 1 of 3, from 1139 to 1153: score 0.1, E = 0.48
				String line2=input.readLine(); //*->atttgcataatccaa<-*
				String line3=input.readLine(); //   attt cataat   a   
				input.readLine(); //CHROMOSOME  1139    ATTTTCATAATTTTA    1153 
				input.readLine(); //

				line1=line1.replace(",", "");
				line1=line1.replace(":", "");
				Scanner sc=new Scanner(line1);
				sc.skip("input domain "); 
				sc.nextInt();
				sc.skip(" of ");          
				sc.nextInt();
				sc.skip(" from ");
				int seqStart=sc.nextInt();
				sc.skip(" to ");
				int seqEnd=sc.nextInt();
				sc.skip(" score ");
				double score=sc.nextDouble();
				sc.skip(" E = ");
				double Evalue=sc.nextDouble();
				
				int line2start=line2.indexOf(">")+1;
				int line2end=line2.indexOf("<");
				String chromSeq=line2.substring(line2start,line2end);
				String matchSeq=line3.substring(line2start,line2end);
				
				Entry e=new Entry();
				e.seqName=seqName;
				e.chromSeq=chromSeq;
				e.matchSeq=matchSeq;
				e.start=seqStart;
				e.end=seqEnd;
				e.Evalue=Evalue;
				e.score=score;
				entry.add(e);
		
				}			
			}
		

		}
	
	}

