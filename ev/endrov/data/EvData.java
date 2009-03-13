package endrov.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.SwingUtilities;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import endrov.basicWindow.BasicWindow;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.PersonalConfig;
import endrov.util.EvDecimal;

/**
 * Root of container tree, handler of types
 * @author Johan Henriksson
 */
public class EvData extends EvContainer
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	public static Vector<EvDataSupport> supportFileFormats=new Vector<EvDataSupport>();
	
	
	public static void initPlugin() {}
	static
		{		
	
		//Store recent entries in personal config
		EV.personalConfigLoaders.put("recentlyLoaded",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				RecentReference rref=new RecentReference(e.getAttributeValue("desc"),e.getAttributeValue("url"));
				recentlyLoadedFiles.add(rref);
				BasicWindow.updateWindows(); //Semi-ugly. Done many times.
				}
			public void savePersonalConfig(Element root)
				{
				try
					{
					for(RecentReference rref:EvData.recentlyLoadedFiles)
						{
						Element e=new Element("recentlyLoaded");
						e.setAttribute("desc",rref.descName);
						e.setAttribute("url",rref.url);
						root.addContent(e);
						}
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}
			});
		
		BasicWindow.addBasicWindowExtension(new EvDataMenu());
//		BasicWindow.updateWindows();
		//maybe update on new extension?
		//priorities on update? windows should really go last. then the updateWindows call here is solved.
		}
	
//	public static TreeMap<String,EvObjectType> extensions=new TreeMap<String,EvObjectType>();
	public static TreeMap<String,Class<? extends EvObject>> extensions=new TreeMap<String,Class<? extends EvObject>>();
	public static Vector<EvData> metadata=new Vector<EvData>();

	/**
	 * TODO better name
	 */
	public static void addMetadata(EvData m)
		{
		metadata.add(m);
		}

	
	


	/******************************************************************************************************
	 *                               Static: Loading                                                      *
	 *****************************************************************************************************/
	/** Remember last path used to load an imageset */
	private static File lastDataPath=EV.getHomeDir();
	public static File getLastDataPath()
		{
		if(lastDataPath==null)
			return EV.getHomeDir();
		else
			return lastDataPath;
		}
	public static void setLastDataPath(File s)
		{
		if(s!=null)
			lastDataPath=s;
		}
	
	
	public static Vector<RecentReference> recentlyLoadedFiles=new Vector<RecentReference>();

	/**
	 * Unregister loaded data
	 */
	public void unregisterData()
		{
		EvData.metadata.remove(this);
		BasicWindow.updateWindows();
		}
	
	/** 
	 * Register data file in GUI 
	 */
	public static void registerOpenedData(EvData data)
		{
		if(data!=null)
			{
			EvData.addMetadata(data);
			RecentReference rref=data.getRecentEntry();
			if(rref!=null)
				{
				boolean isAdded=false;
				for(RecentReference rref2:recentlyLoadedFiles)
					if(rref2.url.equals(rref.url))
						isAdded=true;
				if(!isAdded)
					{
					recentlyLoadedFiles.add(0,rref);
					while(recentlyLoadedFiles.size()>10)
						recentlyLoadedFiles.remove(recentlyLoadedFiles.size()-1);
					}
				}
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){BasicWindow.updateWindows();}
			});
			}
		}

	
	public interface FileIOStatusCallback
		{
		/**
		 * Tell current status. Fraction is within 0-1
		 */
		public void fileIOStatus(double frac, String text);
		}

	public static FileIOStatusCallback deafFileIOCB=new FileIOStatusCallback(){
		public void fileIOStatus(double frac, String text){}
	};
	
	/******************************************************************************************************
	 *                               Loading                                                              *
	 *****************************************************************************************************/

	/** Load file by path/URI */
	public static EvData loadFile(String file){return loadFile(file,deafFileIOCB);}
	
	/** Load file by path */
	public static EvData loadFile(File file){return loadFile(file.getPath(),deafFileIOCB);}
	
	/** Load file by path, receive feedback on process */
	public static EvData loadFile(File file, FileIOStatusCallback cb){return loadFile(file.getPath(),cb);}

	/**
	 * Load file by path, receive feedback on process
	 */
	public static EvData loadFile(String file, FileIOStatusCallback cb)
		{
		EvDataSupport thes=null;
		int lowest=0;
		for(EvDataSupport s:EvData.supportFileFormats)
			{
			Integer sup=s.loadSupports(file);
			if(sup!=null && (thes==null || lowest>sup))
				{
				thes=s;
				lowest=sup;
				}
			}
		if(thes!=null)
			{
			try
				{
				EvData data=thes.load(file, cb);
				if(cb!=null)
					cb.fileIOStatus(0, "Loading "+file);
				if(data!=null)
					return data;
				}
			catch (Exception e)
				{
				e.printStackTrace();
				return null;
				}
			}
		return null;
		}

	
	
	
	
	/**
	 * Load file by open dialog
	 * @deprecated
	 */
	/*
	public static EvData loadFileDialog(FileIOStatusCallback cb)
		{
		//TODO cb will not work properly due to swing. the only way around this is to 
		//TODO separate loading dialog and loading IO. it goes into a thread, swingutils will send updates.
		
		JFileChooser fc=new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		fc.setFileFilter(new javax.swing.filechooser.FileFilter()
			{
			public boolean accept(File f)
				{
				if(f.isDirectory())
					return true;
				for(EvDataSupport s:EvData.supportFileFormats)
					if(s.loadSupports(f.getPath())!=null)
						return true;
				return false;
				}
			public String getDescription()
				{
				return "Data Files and Imagesets";
				}
			});
		fc.setCurrentDirectory(EvData.getLastDataPath());
		int ret=fc.showOpenDialog(null);
		if(ret==JFileChooser.APPROVE_OPTION)
			{
			EvData.setLastDataPath(fc.getSelectedFile().getParentFile());
			File filename=fc.getSelectedFile();
			if(cb!=null)
				cb.fileIOStatus(0, "Loading "+filename.getName());
			return loadFile(filename);
			}
		return null;
		}
*/

	

	/******************************************************************************************************
	 *                               Saving                                                               *
	 *****************************************************************************************************/

	/** Save by file or URI */
	public void saveDataAs(String file) throws IOException {saveDataAs(file,deafFileIOCB);}

	/** Save by path */
	public void saveDataAs(File file) throws IOException {saveDataAs(file.getPath(),deafFileIOCB);}

	/**
	 * Point I/O to a new file, prepare for saving. This does not affect currently loaded data 
	 */
	public void setSaver(String file) throws IOException
		{
		EvDataSupport thes=null;
		int lowest=0;
		for(EvDataSupport s:EvData.supportFileFormats)
			{
			Integer sup=s.saveSupports(file);
			if(sup!=null && (thes==null || lowest>sup))
				{
				thes=s;
				lowest=sup;
				}
			}
		if(thes!=null)
			{
			EvIOData io=thes.getSaver(this, file);
			if(io!=null)
				this.io=io;
			else
				throw new IOException("Plugin does not support saving this file");
			}
		else
			throw new IOException("No suitable plugin to save file");
		}
	
	/**
	 * Save file by path, receive feedback on process. Return if ok.
	 */
	public void saveDataAs(String file, FileIOStatusCallback cb) throws IOException
		{
		setSaver(file);
		saveData(cb);
		/*
		EvDataSupport thes=null;
		int lowest=0;
		for(EvDataSupport s:EvData.supportFileFormats)
			{
			Integer sup=s.saveSupports(file);
			if(sup!=null && (thes==null || lowest>sup))
				{
				thes=s;
				lowest=sup;
				}
			}
		if(thes!=null)
			{
			try
				{
				EvIOData io=thes.getSaver(this, file);
				if(io!=null)
					{
					this.io=io;
					saveData(cb);
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		else
			throw new IOException("No suitable plugin to save file");
			*/
		}


	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/




	/**
	 * Connection with disk for partial I/O etc
	 */
	public EvIOData io=null;

	/** Version of metadata */
	public String metadataVersion="0";


	public void saveData()
		{
		saveData(deafFileIOCB);
		}
	
	public void saveData(EvData.FileIOStatusCallback cb)
		{
		io.saveData(this, cb);
		setMetadataModified(false);
		}
	
	

	
	/******************************************************************************************************
	 *                               Instance: XML tools                                                  *
	 *****************************************************************************************************/

	
	/**
	 * Load metadata from XML-file
	 */
	public void loadXmlMetadata(File filename)
		{
		try
			{
			FileInputStream fileInputStream = new FileInputStream(filename);
			loadXmlMetadata(fileInputStream);
			}
		catch (FileNotFoundException e)
			{
			e.printStackTrace();
			}
		}
	private static void help331(Element e, EvDecimal timestep)
		{
		String aF=e.getAttributeValue("f");
		if(aF!=null)
			e.setAttribute("f",new EvDecimal(aF).multiply(timestep).toString());
		String aFrame=e.getAttributeValue("frame");
		if(aFrame!=null)
			e.setAttribute("frame",new EvDecimal(aFrame).multiply(timestep).toString());
		for(Element child:EV.castIterableElement(e.getChildren()))
			help331(child, timestep);
		}
	public void loadXmlMetadata(InputStream is)
		{
		metaObject.clear();
    Document document = null;
    try 
    	{
  		SAXBuilder saxBuilder = new SAXBuilder();
  		document = saxBuilder.build(is);
  		Element element = document.getRootElement();

  		if(element.getAttribute("version")!=null)
  			metadataVersion=element.getAttributeValue("version");
  		
  		//metadata 3->3.2
  		if(!metadataVersion.equals("3.2"))
	  		{
	  		System.out.println("Updating metadata to 3.2");
	  		Element eIm=element.getChild("imageset");
	  		EvDecimal timestep=EvDecimal.ONE;
	  		if(eIm!=null)
	  			{
		  		Element timestepe=eIm.getChild("timestep");
		  		if(timestepe!=null)
		  			timestep=new EvDecimal(timestepe.getText());
	  			}
	  		help331(element, timestep);
	  		}
  		
  		recursiveLoadMetadata(element);
  		/*
  		//Extract objects
  		for(Element child:EV.castIterableElement(element.getChildren()))
  			{
  			EvObjectType ext=extensions.get(child.getName());
  			EvObject o;
  			if(ext==null)
  				{
  				o=new CustomObject(child);
  				Log.printLog("Found unknown meta object of type "+child.getName());
  				}
  			else
  				{
  				o=ext.extractObjects(child);
  				Log.printLog("Found meta object of type "+child.getName());
					}
  			String sid=child.getAttributeValue("id");
  			String id;
  			if(sid==null) 
  				//This is only needed for imagesets without the EV extended attributes
  				//should maybe grab a free one (detect)
  				//id=""+-1;
  				id="im"; //This is for the OST3 transition
  			else
  				id=sid;
 				metaObject.put(id, o);
  			}
  			*/
    	} 
    catch (Exception e) 
    	{
    	e.printStackTrace();
    	} 
		}

	
	/**
	 * Get all child-objects from XML
	 * TODO What about object id?
	 * TODO can extractsubobject replace this function?
	 * @deprecated
	 */
	public static Vector<EvObject> getChildObXML(Element element)
		{
		Vector<EvObject> obs=new Vector<EvObject>();
		for(Element child:EV.castIterableElement(element.getChildren()))
			{
			Class<? extends EvObject> ext=extensions.get(child.getName());
			EvObject o=null;
			if(ext==null)
				{
				o=new CustomObject();
				o.loadMetadata(child);
				Log.printLog("Found unknown meta object of type "+child.getName());
				}
			else
				{
				try
					{
					o=ext.newInstance();
					o.loadMetadata(child);
					Log.printLog("Found meta object of type "+child.getName());
					}
				catch (InstantiationException e)
					{
					e.printStackTrace();
					}
				catch (IllegalAccessException e)
					{
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
				}
			obs.add(o);
			}
		return obs;
		}
	
	/**
	 * Put all meta objects into an XML document
	 */
	public Document saveXmlMetadata() 
		{
		Element ostElement=new Element("ost");
		ostElement.setAttribute("version","3.2");
		Document doc = new Document(ostElement);
//		save
		recursiveSaveMetadata(ostElement);
//		saveSubObjectsXML(ostElement);
		return doc;
		}

	
	
	/**
	 * Get the name of this metadata to be displayed in menus
	 */
	public String getMetadataName()
		{
		if(io==null)
			return "<unnamed>";
		else
			return io.getMetadataName();
		}
	
	public String toString()
		{
		return getMetadataName();
		}

	
	/**
	 * Get entry for Load Recent or null if not possible
	 */
	public RecentReference getRecentEntry()
		{
		if(io==null)
			return null;
		else
			return io.getRecentEntry();
		}

	
	}
