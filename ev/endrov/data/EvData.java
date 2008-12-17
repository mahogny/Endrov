package endrov.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import javax.swing.JFileChooser;

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
	
	public static TreeMap<String,EvObjectType> extensions=new TreeMap<String,EvObjectType>();
	public static Vector<EvData> metadata=new Vector<EvData>();

	public static void addMetadata(EvData m)
		{
		metadata.add(m);
		}

	
	


	/******************************************************************************************************
	 *                               Static: Loading                                                      *
	 *****************************************************************************************************/
	/** Remember last path used to load an imageset */
	private static String lastDataPath="/";
	public static String getLastDataPath()
		{
		if(lastDataPath==null)
			return "";
		else
			return lastDataPath;
		}
	public static void setLastDataPath(String s)
		{
		if(s!=null)
			lastDataPath=s;
		}
	
	
	public static Vector<RecentReference> recentlyLoadedFiles=new Vector<RecentReference>();

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
			BasicWindow.updateWindows();
			}
		}

	
	public interface FileIOStatusCallback
		{
		public void fileIOStatus(double proc, String text);
		}

	
	/**
	 * Load file by path
	 */
	public static EvData loadFile(String file)
		{
		return loadFile(file,null);
		}

	
	/**
	 * Load file by path
	 */
	public static EvData loadFile(File file)
		{
		return loadFile(file.getPath(),null);
		}
	
	/**
	 * Load file by path, receive feedback on process
	 */
	public static EvData loadFile(File file, FileIOStatusCallback cb)
		{
		return loadFile(file.getPath(),cb);
		}

	
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
				EvData data=thes.load(file);
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
	 * Save file by path, receive feedback on process
	 */
	public void saveFileAs(String file)
		{
		saveFileAs(file,null);
		}
	/**
	 * Save file by path, receive feedback on process
	 */
	public void saveFileAs(String file, FileIOStatusCallback cb)
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
			try
				{
				EvIOData io=thes.getSaver(this, file);
				if(io!=null)
					{
					this.io=io;
					if(cb!=null)
						cb.fileIOStatus(0, "Saving "+file);
					this.saveMeta();
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		}

	
	
	/**
	 * Load file by open dialog
	 */
	public static EvData loadFileDialog(FileIOStatusCallback cb)
		{
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
		fc.setCurrentDirectory(new File(EvData.getLastDataPath()));
		int ret=fc.showOpenDialog(null);
		if(ret==JFileChooser.APPROVE_OPTION)
			{
			EvData.setLastDataPath(fc.getSelectedFile().getParent());
			File filename=fc.getSelectedFile();
			if(cb!=null)
				cb.fileIOStatus(0, "Loading "+filename.getName());
			return loadFile(filename);
			}
		return null;
		}

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/




	/**
	 * Connection with disk for partial I/O etc
	 */
	public EvIOData io=null;


	public void saveMeta()
		{
		io.saveMeta(this);
		}
	
	
	

	
	
	
	/** Version of metadata */
	public String metadataVersion="0";

	
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
  		
  		//metadata 3->3.1
  		if(!metadataVersion.equals("3.1"))
	  		{
	  		System.out.println("Updating metadata to 3.1");
	  		Element eIm=element.getChild("imageset");
	  		EvDecimal timestep=new EvDecimal(eIm.getChild("timestep").getText());
	  		help331(element, timestep);
	  		}
  		
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
    	} 
    catch (Exception e) 
    	{
    	e.printStackTrace();
    	} 
		}

	
	/**
	 * Get all child-objects from XML
	 * TODO: What about object id?
	 */
	public static Vector<EvObject> getChildObXML(Element element)
		{
		Vector<EvObject> obs=new Vector<EvObject>();
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
		ostElement.setAttribute("version","3.1");
		Document doc = new Document(ostElement);
		for(String id:metaObject.keySet())
			{
			EvObject o=metaObject.get(id);
			Element el=new Element("TEMPNAME");
			el.setAttribute("id",""+id);
			o.saveMetadata(el);
			ostElement.addContent(el);
			}
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
