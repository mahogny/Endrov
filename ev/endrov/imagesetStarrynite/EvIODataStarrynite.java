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
 * @author Johan Henriksson (binding to library only)
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
	 *                               Image I/O class                                                      *
	 *****************************************************************************************************/
	

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

	//TODO move?
	double xy_res, z_res; //[um]? um/px?
	EvDecimal time_interval; //[s], originally [min]
	
	
	
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
					if(!line.startsWith("#") && !line.isEmpty())
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
					s.resY=s.resX=1.0/xy_res; //?
					s.resZ=new EvDecimal(z_res); //?
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
		
		for(int curi=1;;curi++)
			{
			File f=new File(fNuclei,"t"+EV.pad(curi, 3)+"-nuclei");
			if(!f.exists())
				break;

			//System.out.println("curi "+curi);
			
			String content=EvFileUtil.readFile(f);
			StringTokenizer stok=new StringTokenizer(content, "\n");
			while(stok.hasMoreTokens())
				{
				String line=stok.nextToken();
				StringTokenizer linetok=new StringTokenizer(line, ",");
				
				String sIndex=linetok.nextToken();
				String sStatus=linetok.nextToken();
				String sPred=linetok.nextToken();				
				String sSucc1=linetok.nextToken();				
				String sSucc2=linetok.nextToken();				
				String sX=linetok.nextToken();				
				String sY=linetok.nextToken();				
				String sZ=linetok.nextToken();				
				String sDiam=linetok.nextToken();				
				String sIdentity=linetok.nextToken();
				String sUnknown=linetok.nextToken();				

				//System.out.println(sIdentity);
				
				EvDecimal frame=frame2time(curi);
				
				NucLineage.Nuc nuc=lin.getCreateNuc(sIdentity);

				// image/Image3D.java    z uses getZPixRes, xy does not
				
				NucLineage.NucPos pos=nuc.getCreatePos(frame);
				pos.x=Double.parseDouble(sX)*xy_res;
				pos.y=Double.parseDouble(sY)*xy_res;
				pos.z=Double.parseDouble(sZ)*z_res;
				pos.r=Double.parseDouble(sDiam)*xy_res/2; //Think resolution comes in here
				}
			
			
			/**
			 * File format: Each file is a frame. Columns:
			 * 
			 * 2, 1, -1, 4, -1, X, Y, R, Z, NAME, ???score???
			 * 1, 1, -1, 1, -1, 163, 343, 6.1, 36, polar1, 103162,
			 * 
			 */
			
			/**
			 * 
			 * 1, 1, -1, 1, -1, 163, 343, 6.1, 36, polar1, 103162,
2, 1, -1, 4, -1, 380, 366, 16.1, 80, EMS, 2181015,
3, 1, -1, 2, -1, 387, 153, 16.6, 86, ABp, 2836266,
4, 1, -1, 3, -1, 189, 251, 17.2, 88, ABa, 2850348,
5, 1, -1, 5, -1, 562, 269, 18.1, 80, P2, 2168825,

			 * 
			 * 
			 */
			
			/**
			 * 
			 * 1, 1, 1, 5, -1, 163, 343, 6.1, 37, polar1, 105518,
2, 1, 3, 2, -1, 395, 163, 16.1, 91, ABp, 3072069,
3, 1, 4, 3, -1, 190, 226, 17.0, 91, ABa, 3056441,
4, 1, 2, 1, -1, 365, 385, 17.1, 83, EMS, 2455074,
5, 1, 5, 4, -1, 556, 254, 18.1, 83, P2, 2257600,

			 */
			
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
