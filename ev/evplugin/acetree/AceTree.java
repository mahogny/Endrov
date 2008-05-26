package evplugin.acetree;

import java.util.*;
import java.io.*;

import evplugin.ev.*;
import evplugin.script.*;
import evplugin.nuc.*;

/**
 * Import for AceTree data.
 * At this point parameters are not imported; the ones needed to get a complete dataset are set arbitrarily.
 * 
 * @author Johan Henriksson
 */
public class AceTree
	{
	public static void initPlugin() {}
	static
		{
		Script.addCommand("loadace", new CmdLoadAce());
		}

	
	public static class ARec
		{
		int lastLine;
		int nextLine;
		int childLine;
		double x,y,z,r;
		String nucName;
		}

	
	
	public final List<Vector<ARec>> afile=new LinkedList<Vector<ARec>>();
	
	public double xy_res=0.09, z_res=1.00;
	public double frametime=1.0/60.0;

	
	/**
	 * Load acetree data
	 * @param nameBasedir Base directory
	 * @return If it succeeded
	 */
	public boolean load(String nameBasedir)
		{
		afile.clear();
		File basedir=new File(nameBasedir);
		if(!basedir.exists())
			return false;
		
		//Load Coordinates
		File nucdir=new File(basedir,"nuclei");
		for(int fnum=1;;fnum++)
			{
			File nf=new File(nucdir,"t"+pad(fnum,3)+"-nuclei");
			if(nf.exists())
				afile.add(readNucFile(nf));
			else
				break;
			}
		return true;
		}
	
	/**
	 * Get metadata
	 */
	public NucLineage getMeta()
		{
		NucLineage lin=new NucLineage();

		//Convert positions and find parents
		for(int frame=0;frame<afile.size();frame++)
			for(ARec line:afile.get(frame))
				if(!line.nucName.startsWith("Nuc") && !line.nucName.equals("nill")) //Filter for what is imported
					{
					NucLineage.Nuc nuc=lin.getNucCreate(line.nucName);
					NucLineage.NucPos pos=nuc.getPosCreate(frame);
					pos.x=line.x*xy_res;
					pos.y=line.y*xy_res;
					pos.z=line.z*z_res;
					pos.r=line.r*xy_res; //unit?
					
					if(line.lastLine!=-2)
						{
						ARec aparent=afile.get(frame-1).get(line.lastLine);
						if(!aparent.nucName.equals(line.nucName))
							nuc.parent=aparent.nucName;
						}
					}
		
		//Associate children with parents
		for(String nucName:lin.nuc.keySet())
			{
			NucLineage.Nuc nuc=lin.nuc.get(nucName);
			if(nuc.parent!=null)
				{
				NucLineage.Nuc parent=lin.getNucCreate(nuc.parent);
				parent.child.add(nucName);
				}
			}
		
		return lin;
		}

	
	
	/**
	 * Read one frame of coordinates
	 */
	private Vector<ARec> readNucFile(File f)
		{
		Vector<ARec> list=new Vector<ARec>();
		
		BufferedReader input = null;
    try
    	{
    	input = new BufferedReader( new FileReader(f) );
    	String line = null;
    	while (( line = input.readLine()) != null)
    		{
    		StringTokenizer tok=new StringTokenizer(line,",");
    		ARec rec=new ARec();
    		
    		tok.nextToken();
    		tok.nextToken();
    		rec.lastLine=Integer.parseInt(tok.nextToken().substring(1))-1;
    		rec.nextLine=Integer.parseInt(tok.nextToken().substring(1))-1;
    		rec.childLine=Integer.parseInt(tok.nextToken().substring(1))-1;
    		rec.x=Double.parseDouble(tok.nextToken().substring(1));
    		rec.y=Double.parseDouble(tok.nextToken().substring(1));
    		rec.z=Double.parseDouble(tok.nextToken().substring(1));
    		rec.r=Double.parseDouble(tok.nextToken().substring(1))/2.0;
    		rec.nucName=tok.nextToken().substring(1);
    		
    		list.add(rec);
    		}
      return list;
    	}
    catch(IOException e)
    	{
    	Log.printError(null, e);
    	return null;
    	}
		}
	
	
	/**
	 * Pad an integer up to # digits
	 */
	private static String pad(int i, int pad)
		{
		String s=""+i;
		while(s.length()<pad)
			s="0"+s;
		return s;
		}

	}
