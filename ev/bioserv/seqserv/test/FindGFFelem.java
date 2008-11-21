package bioserv.seqserv.test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import bioserv.seqserv.io.GFF;

/**
 * Check which different elements exist
 * @author Johan Henriksson
 */
public class FindGFFelem
	{
	public static void main(String[] arg)
		{
		try
			{
			new GFF(new File("/home/bioinfo/incdata/celegans/gff2"),new GFF.EntryFilter(){
			HashSet<String> found=new HashSet<String>();
			public boolean keep(GFF.Entry e)
				{
				if(!found.contains(e.feature))
					{
					found.add(e.feature);
					System.out.println(e.feature);
					//System.out.println(e);
					}
				return false;
				}
			});
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	}
