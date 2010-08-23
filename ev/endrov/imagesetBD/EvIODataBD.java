/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetBD;

import java.io.*;
import java.util.*;

import endrov.data.*;
import endrov.ev.EV;
import endrov.imageset.*;
import endrov.imagesetOST.EvIODataOST;
import endrov.util.EvDecimal;
import endrov.util.Tuple;



/**
 * Support for BD Pathway files
 * 
 * @author Johan Henriksson 
 */
public class EvIODataBD implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	
	
	/******************************************************************************************************
	 *                               Image I/O class                                                      *
	 *****************************************************************************************************/
	
	/** Path to imageset */
	public File basedir;

	
	
	/**
	 * Open a new recording
	 */
	public EvIODataBD(EvData d, File basedir) throws Exception
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
	 * This plugin saves metadata into FILENAME.ostxml. This function constructs the name
	 * 
	 * TODO: call it bfxml instead?
	 */
	private File getMetaFile()
		{
		return new File(basedir.getParent(),basedir.getName()+".ostxml");
		}
	
	/**
	 * Save data to disk
	 */
	public void saveData(EvData d, EvData.FileIOStatusCallback cb)
		{
		try
			{
			EvIODataOST.saveMeta(d, getMetaFile());
			d.setMetadataNotModified();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	

	
	/**
	 * Scan recording for channels and build a file database
	 */
	//@SuppressWarnings("unchecked") 
	public void buildDatabase(EvData d)
		{
		//Load metadata from added OSTXML-file. This has to be done first or all image loaders are screwed
		File metaFile=getMetaFile();
		if(metaFile.exists())
			d.loadXmlMetadata(metaFile);

		
		//Read Experiment.exp
		IniFile fExperiment=null;
		try
			{
			fExperiment=new IniFile(new File(basedir,"Experiment.exp"));
			}
		catch (IOException e)
			{
			//Should never fail
			e.printStackTrace();
			}
		
		Map<String,Integer> dyeNumbers=new HashMap<String, Integer>();
		for(String key:fExperiment.section.get("Dyes").prop.keySet())
			dyeNumbers.put(fExperiment.section.get("Dyes").prop.get(key), Integer.parseInt(key));
		
		//Find all wells
		for(File f:basedir.listFiles())
			{
			if(f.getName().startsWith("Well "))
				{
				String wellName=f.getName().substring(5);
				
				//Make sure there is an imageset for the well
				EvContainer con=d.getChild(wellName);
				Imageset imset;
				if(con==null || !(con instanceof EvGroupObject))
					{
					imset=new Imageset();
					d.metaObject.put(wellName, imset);
					}
				else
					imset=(Imageset)con;

				
				for(String dyeName:dyeNumbers.keySet())
					{
					EvChannel ch=imset.getCreateChannel(dyeName);
					
					EvStack stack=ch.getCreateFrame(new EvDecimal(0));
					EvImage evim=new EvImage();
					
					final File fname=new File(f,dyeName+" - n"+EV.pad(0, 6)+".tif");
					evim.io=new EvIOImage()
						{
						public EvPixels loadJavaImage()
							{
							return EvCommonImageIO.loadPixels(fname, 0);
							}
						};
					stack.putInt(0, evim);
					
					
					stack.resX=1;
					stack.resY=1;
					stack.resZ=EvDecimal.ONE;
					
					}
				
				
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

	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedFileFormats.add(new EvDataSupport(){
			public Integer loadSupports(String fileS)
				{
				File file=new File(fileS);
				if(file.isDirectory())
					{
					if(new File(file,"Experiment.exp").exists())
						return 100;
					}
				return null;
				}
			public List<Tuple<String,String[]>> getLoadFormats()
				{
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>();
				return formats;
				}
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				EvData d=new EvData();
				d.io=new EvIODataBD(d, new File(file));
				return d;
				}
			public Integer saveSupports(String file){return null;}
			public List<Tuple<String,String[]>> getSaveFormats(){return new LinkedList<Tuple<String,String[]>>();};
			public EvIOData getSaver(EvData d, String file) throws IOException{return null;}
		});
		}
	
	}
