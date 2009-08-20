package endrov.nuc.ccm;

/**
 * 
 * @author Johan Henriksson
 *
 */
public class CCMutil
	{
	
	/*

	public static void writeLineageNeighDistances(CellContactMap lin) throws IOException
		{
		PrintWriter pw=new PrintWriter(new FileWriter(new File("/Volumes/TBU_main03/userdata/cellcontactmap/dist.csv")));
		for(Map.Entry<EvDecimal, NucVoronoi> entry:lin.fcontacts.entrySet())
			{
			Map<NucSel,NucLineage.NucInterp> inter=lin.lin.getInterpNuc(entry.getKey());
			Map<String,NucLineage.NucInterp> inters=new HashMap<String, NucLineage.NucInterp>();
			for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
				inters.put(e.getKey().snd(),e.getValue());

			boolean first=true;
			for(Tuple<String,String> pair:entry.getValue().getNeighPairSet())
				if(inters.containsKey(pair.fst()) && inters.containsKey(pair.snd()) && !pair.fst().startsWith(":") && !pair.snd().startsWith(":"))
					{
					if(!first)
						pw.print(',');
					Vector3d vA=inters.get(pair.fst()).pos.getPosCopy();
					Vector3d vB=inters.get(pair.snd()).pos.getPosCopy();
					vA.sub(vB);
					pw.print(vA.length());
					first=false;
					}
			pw.println();
			}
		pw.close();
		}
	*/

	
	
	/*
	 * 
	
	public static void doesChildrenSplit(CellContactMap theCE, File f, Set<String> nucNames) throws IOException
		{
		StringBuffer outSplitChild=new StringBuffer();
		for(String name:nucNames)
			{
			NucLineage.Nuc nuc=theCE.lin.nuc.get(name);
			if(nuc!=null && nuc.child.size()==2)
				{
				String cn1=nuc.child.first();
				String cn2=nuc.child.last();
				if(EvArrayUtil.all(getOverlaps(theCE, cn1, cn2)) || EvArrayUtil.all(getOverlaps(theCE, cn2, cn1)))
					;
				else
					outSplitChild.append(cn1+"\t"+cn2+"\n");
				}
			}
		EvFileUtil.writeFile(f, outSplitChild.toString());

		}
		*/

	
	

	/*
	//Find cells in common for AE and CE
	HashSet<String> ceaNames=new HashSet<String>(theCE.lin.nuc.keySet());
	ceaNames.retainAll(theA.lin.nuc.keySet());
	for(String s:theA.lin.nuc.keySet())
		if(theA.lin.nuc.get(s).pos.isEmpty())
			ceaNames.remove(s);

	//Which cells are not in common? log
	StringBuffer hasCellDiff=new StringBuffer();
	hasCellDiff.append("---- Cells in CE but not AE -----\n");
	for(String s:theCE.lin.nuc.keySet())
		if(!theCE.lin.nuc.get(s).pos.isEmpty())
			if(!ceaNames.contains(s))
				hasCellDiff.append(s+"\n");
	hasCellDiff.append("---- Cells in AE but not CE -----\n");
	for(String s:theA.lin.nuc.keySet())
		if(!theA.lin.nuc.get(s).pos.isEmpty()) //true if has a child
			if(!ceaNames.contains(s)) //true if not contains
				hasCellDiff.append(s+"\n");
	hasCellDiff.append("------\n");
	EvFileUtil.writeFile(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/hasCellDiff.txt"), hasCellDiff.toString());
	
	
	//Compare CE and A model
			CellContactMap theCE=orderedLin.get("celegans2008.2");
			CellContactMap theA=orderedLin.get("AnglerUnixCoords");
			StringBuffer outDiffList2=new StringBuffer();

			//Skip cells which are beyond a certain time
			//Taken from /Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/volstats.txt  manually
			EvDecimal cutoffFrame=new EvDecimal(8590);
			for(String name:theCE.lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=theCE.lin.nuc.get(name);
				if(nuc.pos.isEmpty() || nuc.pos.firstKey().greater(cutoffFrame))
					ceaNames.remove(name);
				}
			
			for(String name:ceaNames)
				for(String name2:ceaNames)
					if(!name.equals(name2))
						{
						boolean ceHasChild=!theCE.lin.nuc.get(name).child.isEmpty() && !theCE.lin.nuc.get(name2).child.isEmpty();
						boolean aHasChild=!theA.lin.nuc.get(name).child.isEmpty() && !theA.lin.nuc.get(name2).child.isEmpty();

						double c1=getOverlapPercent(theCE, name, name2);
						double c2=getOverlapPercent(theA, name, name2);

						if(c1+c2!=0)
							{
							NucLineage.Nuc nuc=theCE.lin.nuc.get(name);
							double dur=nuc.pos.isEmpty() ? 0 : nuc.getLastFrame().add(EvDecimal.ONE).subtract(nuc.getFirstFrame()).doubleValue();
							if(ceHasChild && aHasChild)
								outDiffList2.append(""+c1+"\t"+c2+"\t"+dur+"\t"+name+"\t"+name2+"\n");
							}

						}
	
	*/
	
	}
