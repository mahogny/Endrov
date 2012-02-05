package util2.paperCeExpression.collectData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;

public class CollectGeneList
	{

	
	public static Map<String,String> readMap(File f) throws IOException
		{
		CsvFileReader csvOrf2gene=new CsvFileReader(f, '\t');
		Map<String,String> map=new TreeMap<String, String>();
		ArrayList<String> l;
		while((l=csvOrf2gene.readLine())!=null)
			map.put(l.get(0), l.get(1));
		return map;
		}
	
	public static Set<String> readSet(File f) throws IOException
		{
		CsvFileReader csvOrf2gene=new CsvFileReader(f, '\t');
		Set<String> map=new TreeSet<String>();
		ArrayList<String> l;
		while((l=csvOrf2gene.readLine())!=null)
			map.add(l.get(0));
		return map;
		}
	
	
	/**
	 * Collect a list of all genes
	 */
	public static void main(String[] args)
		{
		

		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
		new PaperCeExpressionUtil(); //Get password right away so it doesn't stop later





		Map<String,String> gene2onerec=new TreeMap<String, String>();



		Set<String> needorfname=new TreeSet<String>();
		needorfname.add("ceh-48");
		needorfname.add("ceh-57");
		needorfname.add("ceh-81");
		needorfname.add("ceh-83");
		needorfname.add("ceh-85");
		needorfname.add("ceh-87");
		needorfname.add("ceh-88");
		needorfname.add("ceh-89");
		needorfname.add("ceh-93");
		needorfname.add("ceh-99");
		needorfname.add("ceh-100");
		needorfname.add("eyg-1");
		needorfname.add("zfh-2");
		needorfname.add("duxl-1");
		needorfname.add("lim-4");



		/*
			if(JOptionPane.showConfirmDialog(null, "Empty cache?", "", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				PaperCeExpressionUtil.removeAllTags();
		 */

		TreeSet<String> genenames=new TreeSet<String>(new AlphanumComparator());
		TreeSet<String> annotated=new TreeSet<String>();
		TreeMap<String,String> gene2orf=new TreeMap<String, String>();

		for(File ostfile:PaperCeExpressionUtil.getAllOST())
			{
			PaperCeExpressionUtil.tagsFor(ostfile);

			String gene=PaperCeExpressionUtil.getGeneName(ostfile);
			if(PaperCeExpressionUtil.orf2gene.containsKey(gene))
				gene=PaperCeExpressionUtil.orf2gene.get(gene);
			//System.out.println(gene);

			genenames.add(gene);
			if(PaperCeExpressionUtil.isAnnotated(ostfile))
				annotated.add(gene);

			gene2orf.put(gene, PaperCeExpressionUtil.getORF(ostfile));

			gene2onerec.put(gene,ostfile.getName());
			}



		System.out.println("##################");


		for(String gene:genenames)
			{
			String orf=gene2orf.get(gene);
			if(PaperCeExpressionUtil.homeoboxes.contains(orf))
				{

				System.out.print(gene);
				if(!annotated.contains(gene))
					System.out.print(" *");
				if(needorfname.contains(gene))
					System.out.print(" ("+orf+")");
				System.out.println();
				}
			}

		System.out.println("-----------");

		for(String gene:genenames)
			{
			String orf=gene2orf.get(gene);
			if(!PaperCeExpressionUtil.homeoboxes.contains(orf))
				{

				System.out.print(gene);
				if(!annotated.contains(gene))
					System.out.print(" *");
				if(needorfname.contains(gene))
					System.out.print(" ("+orf+")");
				System.out.println();
				}
			}


		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

		for(String gene:genenames)
			{
			String orf=gene2orf.get(gene);
			System.out.println(gene+"\t"+orf+"\t"+gene2onerec.get(gene));
			}



		System.exit(0);


		}
	
	}
