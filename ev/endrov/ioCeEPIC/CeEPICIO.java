package endrov.ioCeEPIC;

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
import endrov.typeLineage.LineageExp;
import endrov.util.collection.Tuple;
import endrov.util.math.EvDecimal;

/**
 * Support for the EPIC datasets, epic.gs.washington.edu
 * 
 * @author Johan Henriksson
 *
 */
public class CeEPICIO implements EvIOData
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
		double xy_res=0.09;
		double z_res=1.00;
		int time_interval=1*60;
		
		BufferedReader br=new BufferedReader(new FileReader(f));
		
		//Read header
		if(!br.readLine().equals("cellTime,cell,time,none,global,local,blot,cross,z,x,y,size,gweight"))
			{
			br.close();
			throw new IOException("File format has changed");
			}
		
		Lineage lin=new Lineage();
		
		//Read all the cell positions/expressions
		String line;
		while((line=br.readLine())!=null)
			{
			StringTokenizer stok=new StringTokenizer(line,",");
			
			stok.nextToken(); //cellTime
			
			String cell=stok.nextToken();
			EvDecimal frame=new EvDecimal(time_interval*Integer.parseInt(stok.nextToken()));
			
			stok.nextToken(); //none
			stok.nextToken(); //global
			double local=Double.parseDouble(stok.nextToken()); //local
			stok.nextToken(); //blot
			stok.nextToken(); //cross
			double z=Double.parseDouble(stok.nextToken())*z_res;  //Can get size from the published file
			double x=Double.parseDouble(stok.nextToken())*xy_res;
			double y=Double.parseDouble(stok.nextToken())*xy_res;
			double size=Double.parseDouble(stok.nextToken())*xy_res/2;
			stok.nextToken(); //gweight
			
			Particle particle=lin.getCreateParticle(cell);
			
			ParticlePos pos=particle.getCreatePos(frame);
			pos.x=x;
			pos.y=y;
			pos.z=z;
			pos.r=size;
			
			LineageExp exp=particle.getCreateExp(f.getName());
			exp.level.put(frame,(double)local);
			}
		
		br.close();
		
		
		//Link all the cells
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
	public CeEPICIO(EvData d, File basedir) throws Exception
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
				if(file.getName().endsWith(".csv"))
					{
					try
						{
						//Read the first line and see if it makes sense
						BufferedReader r=new BufferedReader(new FileReader(file));
						String line=r.readLine();
						r.close();
						if(line.contains("cellTime"))
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
				formats.add(Tuple.make("EPIC", new String[]{"csv"}));
				return formats;
				}
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				EvData d=new EvData();
				d.io=new CeEPICIO(d, new File(file));
				return d;
				}
			public Integer saveSupports(String file){return null;}
			public List<Tuple<String,String[]>> getSaveFormats(){return new LinkedList<Tuple<String,String[]>>();};
			public EvIOData getSaver(EvData d, String file) throws IOException{return null;}
		});
		}
	
	
	}
