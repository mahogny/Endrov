package endrov.imagesetStarrynite;


import java.io.*;
import java.util.*;

import endrov.data.*;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.imageset.*;
import endrov.imagesetBasic.BasicSliceIO;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.Tuple;



/**
 * Support for Starrynite/Acetree fileformat
 * 
 * @author Johan Henriksson
 */
public class EvIODataStarrynite implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedFileFormats.add(new EvDataSupport(){
			public Integer loadSupports(String fileS)
				{
				File file=new File(fileS);
				return file.isDirectory() & file.getName().endsWith(".starrynite") ? 100 : null; //Low priority; need to find a way to check extensions
				}
			public List<Tuple<String,String[]>> getLoadFormats()
				{
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>(); 
				formats.add(new Tuple<String,String[]>("Starrynite/Acetree",new String[]{".starrynite"}));
				return formats;
				}
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				EvData d=new EvData();
				d.io=new EvIODataStarrynite(d, new File(file));
				return d;
				}
			public Integer saveSupports(String file){return null;}
			public List<Tuple<String,String[]>> getSaveFormats(){return new LinkedList<Tuple<String,String[]>>();};
			public EvIOData getSaver(EvData d, String file) throws IOException{return null;}
		});
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	/** Path to imageset */
	public File basedir;

	
	
	/**
	 * Open a new recording
	 */
	public EvIODataStarrynite(EvData d, File basedir) throws Exception
		{
		this.basedir=basedir;
		if(!basedir.exists())
			throw new Exception("File does not exist");
		
		buildDatabase(d);
		}
	
	

	public File datadir()
		{
		return basedir.getParentFile();
		}

	
	/**
	 * Save data to disk
	 */
	public void saveData(EvData d, EvData.FileIOStatusCallback cb)
		{
		EvLog.printError("Saving is not supported with this plugin", null);
		}
	

	/**
	 * Scan recording for channels and build a file database
	 */
	//@SuppressWarnings("unchecked") 
	public void buildDatabase(EvData d)
		{
		d.metaObject.clear();
		Imageset im=new Imageset();
		d.metaObject.put("im", im);
		NucLineage lin=new NucLineage();
		im.metaObject.put("1", lin);
		
		
		
		
		try
			{
			readParameters();
			loadImages(im);
			readNuclei(lin);
			}
		catch (IOException e)
			{
			EvLog.printError(e);
			}
		}

	//Parameters
	private double xy_res, z_res; //[um/px]
	private EvDecimal time_interval; //[s], originally [min]
	
	
	/**
	 * Read parameters
	 */
	private void readParameters() throws IOException
		{
		boolean found=false;
		File fParamsDir=new File(basedir,"parameters");
		for(File f:fParamsDir.listFiles())
			{
			if(f.getName().endsWith("-parameters"))
				{
				found=true;
				String content=EvFileUtil.readFile(f);
				StringTokenizer stok=new StringTokenizer(content, "\n");
				while(stok.hasMoreTokens())
					{
					String line=stok.nextToken();
					if(!line.startsWith("#") && !line.equals(""))
						{
						StringTokenizer linetok=new StringTokenizer(line, " ");
						String key=linetok.nextToken();
						
						if(key.equals("xy_res"))
							xy_res=Double.parseDouble(linetok.nextToken());
						else if(key.equals("z_res"))
							z_res=Double.parseDouble(linetok.nextToken());
						else if(key.equals("time_interval"))
							time_interval=new EvDecimal(linetok.nextToken()).multiply(60);
						else
							System.out.println("Unhandled: "+key);
						
						}
					}
				}
			}
		if(!found)
			throw new IOException("No parameters file found");
			
		}
	
	private EvDecimal frame2time(int frame)
		{
		return time_interval.multiply(frame);
		}
	
	private void loadImages(Imageset imset)
		{
		File fTif=new File(basedir,"tif");
		if(fTif.exists())
			{
			
			EvChannel ch=new EvChannel();
			imset.metaObject.put("ch0", ch);
			
			for(File f:fTif.listFiles())
				{
				String name=f.getName();
				if(name.endsWith(".tif"))
					{
					name=name.substring(name.indexOf("-")+2);
					//Now have e.g. 006-p05.tif
					name=name.substring(0, name.length()-4);
					//Now have e.g. 006-p05
					String sFrame=name.substring(0,name.indexOf("-"));
					String sPlane=name.substring(name.indexOf("-")+2);
					
					EvStack s=ch.getCreateFrame(frame2time(Integer.parseInt(sFrame)));
					s.resY=s.resX=xy_res;
					s.resZ=new EvDecimal(z_res);
					//s.binning=1;
					
					EvImage evim=new EvImage();
					evim.io=new BasicSliceIO(f);
					s.putInt(Integer.parseInt(sPlane), evim);
					}
				}
		
			}
		
		}
	
	/**
	 * Read lineage
	 */
	private void readNuclei(NucLineage lin) throws IOException
		{
		File fNuclei=new File(basedir,"nuclei");
		

		HashMap<Integer, String> indexNuc; //index -> nucname
		HashMap<Integer, String> nextIndexNuc=new HashMap<Integer, String>();

		for(int curi=1;;curi++)
			{
			indexNuc=nextIndexNuc;
			nextIndexNuc=new HashMap<Integer, String>();
			File f=new File(fNuclei,"t"+EV.pad(curi, 3)+"-nuclei");
			if(!f.exists())
				break;
			System.out.println("curi "+curi);
			String content=EvFileUtil.readFile(f);
			StringTokenizer stok=new StringTokenizer(content, "\n");
			while(stok.hasMoreTokens())
				{
				String line=stok.nextToken();
				StringTokenizer linetok=new StringTokenizer(line, ",");
				
				String sIndex=linetok.nextToken();
				String sStatus=linetok.nextToken().substring(1);
				/*String sPred=*/linetok.nextToken().substring(1);				
				String sSucc1=linetok.nextToken().substring(1);				
				String sSucc2=linetok.nextToken().substring(1);				
				String sX=linetok.nextToken().substring(1);				
				String sY=linetok.nextToken().substring(1);				
				String sZ=linetok.nextToken().substring(1);				
				String sDiam=linetok.nextToken().substring(1);
				String sIdentity=linetok.nextToken().substring(1);
				/*String sAce=*/linetok.nextToken().substring(1);				
				
//				int iAce=Integer.parseInt(sAce);
				int iSucc1=Integer.parseInt(sSucc1);
				int iSucc2=Integer.parseInt(sSucc2);
				int iIndex=Integer.parseInt(sIndex);
//				int iPred=Integer.parseInt(sPred);


				//Get this nuc
				NucLineage.Nuc nuc;
				String thisName=indexNuc.get(iIndex);
				if(thisName==null)
					{
					thisName=lin.getUniqueNucName();
					nuc=lin.getCreateNuc(thisName);
					}
				else
					nuc=lin.nuc.get(thisName);

				//Naming
				if(thisName.startsWith(":") && !sIdentity.equals("nill"))
					{
					if(lin.nuc.containsKey(sIdentity))
						System.out.println("Name collision "+sIdentity);
					else
						{
						lin.renameNucleus(thisName, sIdentity);
						thisName=sIdentity;
						}
					}
				
				//Show how to continue next frame
				if(iSucc2!=-1)
					{
					//Split
					String name1=lin.getUniqueNucName();
					NucLineage.Nuc nuc1=lin.getCreateNuc(name1);
					String name2=lin.getUniqueNucName();
					NucLineage.Nuc nuc2=lin.getCreateNuc(name2);
					nuc.child.add(name1);
					nuc.child.add(name2);
					nuc1.parent=thisName;
					nuc2.parent=thisName;
					nextIndexNuc.put(iSucc1, name1);
					nextIndexNuc.put(iSucc2, name2);					
					}
				else if(iSucc1!=-1)
					//Just add more coordinates
					nextIndexNuc.put(iSucc1, thisName);
				
				//Position
				EvDecimal frame=frame2time(curi);
				NucLineage.NucPos pos=nuc.getCreatePos(frame);
				pos.x=Double.parseDouble(sX)*xy_res;
				pos.y=Double.parseDouble(sY)*xy_res;
				pos.z=Double.parseDouble(sZ)*z_res; // image/Image3D.java    z uses getZPixRes, xy does not
				pos.r=Double.parseDouble(sDiam)*xy_res/2; 
				
				//Cell death
				
				}
			}
		
		
		}

	
	
	
	
	public RecentReference getRecentEntry()
		{
		return new RecentReference(getMetadataName(), basedir.getPath());
		}

	public String getMetadataName()
		{
		String imageset=basedir.getName();
		return imageset;
		}
	
	}
