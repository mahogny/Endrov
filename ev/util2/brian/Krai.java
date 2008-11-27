package util2.brian;

import java.io.*;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import bioserv.seqserv.io.Fasta;
import bioserv.seqserv.io.GFF;
import bioserv.seqserv.io.HMMER;
import bioserv.seqserv.io.GFF.Entry;

/**
 * 
 
  CREATE TABLE krai1
(
  "name" text NOT NULL,
  "start" integer NOT NULL,
  "end" integer NOT NULL,
  "chromSeq" text NOT NULL,
  "matchSeq" text NOT NULL,
  evalue double precision,
  attr text ,
  CONSTRAINT krai1_pkey PRIMARY KEY (name, "start", "end")
)
WITH (OIDS=FALSE);
ALTER TABLE krai1 OWNER TO tbudev3;

  
  
 * 
 */


//GFF: directionality

public class Krai
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{

		try
			{
			System.out.println(BrianSQL.connectPostgres("//193.11.32.108/tbu", "postgres", "wuermli"));
			BrianSQL.runUpdate("delete from krai1");
			
			//Lengths of chromosomes and names
			System.out.println("Reading DNA");
			Fasta chrom=new Fasta(new File("/home/bioinfo/incdata/celegans/dna.fa"));
			TreeSet<String> chromNames=new TreeSet<String>(chrom.seq.keySet());
			final TreeMap<String, Integer> chromlength=new TreeMap<String, Integer>();
			for(String s:chrom.seq.keySet())
				chromlength.put(s, chrom.seq.get(s).length());
			
			System.out.println("Reading HMMER");
			HMMER h=new HMMER(new File("/home/tbudev3/bioinfo/krai/pfam.out"));
//			HMMER h=new HMMER(new File("/home/tbudev3/bioinfo/krai/pfamrevcom.out"));
			System.out.println("#entries: "+h.entry.size());
	
			
			final boolean reverse=false;
			
			System.out.println("Reading GFF");
			GFF gff=new GFF(new File("/home/bioinfo/incdata/celegans/gff2"),new GFF.EntryFilter(){
				public boolean keep(Entry e)
					{
					
//					System.out.println(e.seqname);
					if(!e.feature.equals(GFF.featureCDS))
						e.attributes="";
					e.source="";
					
					//Reverse direction
					if(reverse)
						{
						int cl=chromlength.get("CHROMOSOME_"+e.seqname);
						e.start=cl-1-e.start;
						e.end=cl-1-e.end;
						}
					
					return e.feature.equals(GFF.featureEXON) || e.feature.equals(GFF.featureCDS);
					}
			});
			System.out.println("#entries: "+gff.entry.size());
			
			//Here assuming no overlaps of exons. Then this can be done in O(nlogn + n) time
			System.out.println("Sorting HMMER");
			TreeMap<Integer, HMMER.Entry> sortedH=new TreeMap<Integer, HMMER.Entry>();
			for(HMMER.Entry he:h.entry)
				sortedH.put(he.start, he);
			System.out.println("Sorting GFF");
			TreeMap<Integer, GFF.Entry> sortedGFF=new TreeMap<Integer, GFF.Entry>();
			TreeMap<Integer, GFF.Entry> sortedCDS=new TreeMap<Integer, GFF.Entry>();
			for(GFF.Entry ge:gff.entry)
				if(ge.feature.equals(GFF.featureEXON))
					sortedGFF.put(ge.start, ge);
				else if(ge.feature.equals(GFF.featureCDS))
					sortedCDS.put(ge.start, ge);
			
			//POTENTIAL PROBLEM! chromosomes on top of each other.
			//need to sort into separate subgroups
			System.out.println("Filtering by exons");
			Iterator<GFF.Entry> itG=sortedGFF.values().iterator();
			GFF.Entry curG=itG.next();
			totalFor: for(HMMER.Entry curH:sortedH.values())
				{
				while(curG.end<curH.start)
					if(!itG.hasNext())
						break totalFor;
					else
						curG=itG.next();
				//Now curG.end>=curH.start
				if(!(curH.start>curG.end || curH.end<curG.start))
					{
					//System.out.println("Removing "+curH);
					h.entry.remove(curH);
					}
				}
			System.out.println("#entries after filter: "+h.entry.size());

			//Find related gene. By now there are not many.
			//BUT code it properly later on anyway. it IS slow!
			System.out.println("Naming entries");
			HashMap<HMMER.Entry, String> hName=new HashMap<HMMER.Entry, String>();
			for(HMMER.Entry curH:h.entry)
				{
				Integer closestDist=null;
				GFF.Entry closest=null;
				for(GFF.Entry curCDS:sortedCDS.values())
					if(("CHROMOSOME_"+curCDS.seqname).equals(curH.seqName))
						{
						int dist=curCDS.start-curH.end;
						if(dist>=0 && (closestDist==null || dist<closestDist))
							{
//							System.out.println("found");
							closestDist=dist;
							closest=curCDS;
							}
						}
//					else
//						System.out.println("-"+curCDS.seqname+"- -"+curH.seqName+"-");
				if(closest!=null)
					{
					System.out.println(closest.attributes);
					hName.put(curH,closest.attributes);
					}
				else
					hName.put(curH,null);
				}
			
		
			
			
			
			hmmerToSQL(h, hName);

			for(String cc:chromNames)
				{
				doCrom(cc.substring("CHROMOSOME_".length())); 
				}
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		}
	
	public static void doCrom(String chromName)
		{
		
		}
	
	public static void hmmerToSQL(HMMER h, Map<HMMER.Entry, String> hName) throws Exception
		{
		System.out.println("Uploading to sql");
		PreparedStatement ps=BrianSQL.conn.prepareStatement("insert into krai1 values(?,?,?,?,?,?,?)");
		for(HMMER.Entry e:h.entry)
			{
			ps.setString(1, e.seqName);
			ps.setInt(2, e.start);
			ps.setInt(3, e.end);
			ps.setString(4, e.chromSeq);
			ps.setString(5, e.matchSeq);
			ps.setDouble(6, e.Evalue);
			ps.setString(7, hName.get(e));
			ps.execute();
			}
		}

	}
