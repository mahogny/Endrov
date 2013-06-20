package endrov.ioSimiBioCell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import endrov.data.EvData;
import endrov.data.EvIODataReaderWriterDeclaration;
import endrov.data.EvIOData;
import endrov.data.RecentReference;
import endrov.data.EvData.FileIOStatusCallback;
import endrov.typeLineage.Lineage;
import endrov.typeLineage.Lineage.Particle;
import endrov.typeLineage.Lineage.ParticlePos;
import endrov.util.collection.Tuple;
import endrov.util.math.EvDecimal;

/**
 * Support for simi biocell lineages
 * 
 * @author Johan Henriksson
 *
 */
public class SimiBiocellIO implements EvIOData
	{
	private File file;
	
	public void buildDatabase(EvData d)
		{		
		}

	public File datadir()
		{
		return null;
		}

	public String getMetadataName()
		{
		return file.getName();
		}

	public RecentReference getRecentEntry()
		{
		return new RecentReference(file.getName(), file.getAbsolutePath());
		}

	public void saveData(EvData d, FileIOStatusCallback cb)
		{
		//Not implemented
		}
	
	
	/**
	 * Read the lineage from a file
	 */
	public Lineage readFile(File f) throws IOException
		{
		double xy_res=1;
		double z_res=50;
		/*
		int time_interval=1*60;
		*/
		
		BufferedReader br=new BufferedReader(new FileReader(f));
		
		//Read header
		for(int i=0;i<7;i++)
			br.readLine();
		if(!br.readLine().equals("---"))
			{
			br.close();
			throw new IOException("File format header does not match");
			}
		
		Lineage lin=new Lineage();
		
		//Read all the cell positions/expressions
		String line;
		while((line=br.readLine())!=null)
			{
			StringTokenizer stok=new StringTokenizer(line," ");
			stok.nextElement();
			stok.nextElement();
			stok.nextElement();
			stok.nextElement();
			String name=stok.nextToken();
	
			
			Lineage.Particle nuc=lin.getCreateParticle(name);
			
			line=br.readLine();
			line=br.readLine();
			
			
			line=br.readLine();
			int numCoord=Integer.parseInt(line);
			
			for(int i=0;i<numCoord;i++)
				{
				line=br.readLine();
				
				stok=new StringTokenizer(line," ");
				int frame=Integer.parseInt(stok.nextToken());
				double x=Integer.parseInt(stok.nextToken());
				double y=Integer.parseInt(stok.nextToken());
				double z=Integer.parseInt(stok.nextToken());
		
				
				ParticlePos p=nuc.getCreatePos(new EvDecimal(frame));
				p.x=x*xy_res;
				p.y=y*xy_res;
				p.z=z*z_res;
				p.r=10;
				}
			
			
			line=br.readLine();
			if(!line.equals("---"))
				{
				br.close();
				throw new IOException("File format nucleus does not match, found "+line);
				}
			
			}
		br.close();
		
		
		//Link all the cells. Note: this code could be shared with EPIC reader
		for(String pname:lin.particle.keySet())
			{
			//Figure out parent name
			String parentName=null;
			String suggestParentName=pname.substring(0,pname.length()-1);
			if(/*pname.startsWith("MS") &&*/ lin.particle.keySet().contains(suggestParentName))
				parentName=suggestParentName;

			if(pname.equals("AB")  || pname.equals("P1"))
				parentName="P0";
			if(pname.equals("EMS") || pname.equals("P2"))
				parentName="P1";
			if(pname.equals("E")   || pname.equals("MS"))
				parentName="EMS";
			if(pname.equals("P3")  || pname.equals("C"))
				parentName="P2";
			if(pname.equals("P4")  || pname.equals("D"))
				parentName="P3";
			
			
			//Link parent-child
			if(parentName!=null)
				{
				Particle childp=lin.particle.get(pname);
				Particle parentp=lin.particle.get(parentName);
				if(parentp!=null)
					{
					childp.parents.add(parentName);
					parentp.child.add(pname);
					}
				}
			}
		
		
		
		return lin;
		}
	
	

	/**
	 * Open a new recording
	 */
	public SimiBiocellIO(EvData d, File basedir) throws Exception
		{
		this.file=basedir;
		if(!basedir.exists())
			throw new Exception("File does not exist");
		
		Lineage lin=readFile(file);
		d.metaObject.put("lin",lin);
		}
	

	public void close() throws IOException
		{
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedFileFormats.add(new EvIODataReaderWriterDeclaration(){
			public Integer loadSupports(String fileS)
				{
				File file=new File(fileS);
				if(file.getName().endsWith(".sbd"))
					{
					try
						{
						//Read the first line and see if it makes sense
						BufferedReader r=new BufferedReader(new FileReader(file));
						String line=r.readLine();
						r.close();
						if(line.contains("SIMI"))
							return 50;
						}
					catch (IOException e)
						{
						}
					}
				return null;
				}
			public List<Tuple<String,String[]>> getLoadFormats()
				{
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>();
				formats.add(Tuple.make("Simi BioCell", new String[]{"sbd"}));
				return formats;
				}
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				EvData d=new EvData();
				d.io=new SimiBiocellIO(d, new File(file));
				return d;
				}
			public Integer saveSupports(String file){return null;}
			public List<Tuple<String,String[]>> getSaveFormats(){return new LinkedList<Tuple<String,String[]>>();};
			public EvIOData getSaver(EvData d, String file) throws IOException{return null;}
		});
		}
	
	
	}
