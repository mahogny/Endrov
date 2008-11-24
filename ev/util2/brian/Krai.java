package util2.brian;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import bioserv.seqserv.io.GFF;
import bioserv.seqserv.io.HMMER;
import bioserv.seqserv.io.GFF.Entry;


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
			System.out.println("Reading HMMER");
			HMMER h=new HMMER(new File("/home/tbudev3/bioinfo/krai/pfam.out"));
			System.out.println("#entries: "+h.entry.size());
			System.out.println("Reading GFF");
			GFF gff=new GFF(new File("/home/bioinfo/incdata/celegans/gff2"),new GFF.EntryFilter(){
				public boolean keep(Entry e)
					{
					e.attributes="";
					e.source="";
					return e.feature.equals(GFF.featureEXON) /*|| e.feature.equals(GFF.featureINTRON)*/;
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
			for(GFF.Entry ge:gff.entry)
				sortedGFF.put(ge.start, ge);
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

			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		}

	}
