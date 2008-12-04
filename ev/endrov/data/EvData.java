package endrov.data;

import java.util.*;
import java.io.*;

import javax.swing.JFileChooser;

import org.jdom.*;
import org.jdom.input.*;

import endrov.basicWindow.*;
import endrov.ev.*;
import endrov.util.EvDecimal;

/**
 * Container of data for EV
 * @author Johan Henriksson
 */
public abstract class EvData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	public static Vector<EvDataSupport> supportFileFormats=new Vector<EvDataSupport>();
	
	
	public static void initPlugin() {}
	static
		{		
		//OST XML-support
		supportFileFormats.add(new EvDataSupport(){
			public Integer supports(String fileS)
				{
				File file=new File(fileS);
				return file.isFile() && (file.getName().endsWith(".xml") ||
						file.getName().endsWith(".ostxml")) ? 10 : null;
				}
			public EvData load(String file) throws Exception
				{
				return new EvDataXML(file);
				}
		});
		
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

	
	
	public static int selectedMetadataId=-1;

	/**
	 * Get currently selected metadata or null
	 */
	public static EvData getSelectedMetadata()
		{
		if(selectedMetadataId>=0 && selectedMetadataId<metadata.size())
			return metadata.get(selectedMetadataId);
		else
			return null;
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

	
	public interface LoadFileCallback
		{
		public void loadFileStatus(double proc, String text);
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
	public static EvData loadFile(File file, LoadFileCallback cb)
		{
		return loadFile(file.getPath(),cb);
		}

	
	/**
	 * Load file by path, receive feedback on process
	 */
	public static EvData loadFile(String file, LoadFileCallback cb)
		{
		EvDataSupport thes=null;
		int lowest=0;
		for(EvDataSupport s:EvData.supportFileFormats)
			{
			Integer sup=s.supports(file);
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
					cb.loadFileStatus(0, "Loading "+file);
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
	 */
	public static EvData loadFileDialog(LoadFileCallback cb)
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
					if(s.supports(f.getPath())!=null)
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
				cb.loadFileStatus(0, "Loading "+filename.getName());
			return loadFile(filename);
			}
		return null;
		}

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	/** All meta objects */
	public TreeMap<String,EvObject> metaObject=new TreeMap<String,EvObject>();

	/** Flag if the metadata container itself has been modified */
	private boolean coreMetadataModified=false;

	
	public int selectedMetaobjectId=-1;

	/**
	 * Get currently selected metadata or null
	 */
	public EvObject getSelectedMetaobject()
		{
		return metaObject.get(selectedMetaobjectId);
		}

	/**
	 * Get all objects of a certain type
	 */
	@SuppressWarnings("unchecked") public <E> List<E> getObjects(Class<E> cl)
		{
		LinkedList<E> ll=new LinkedList<E>();
		for(EvObject ob2:metaObject.values())
			if(ob2.getClass() == cl)
				ll.add((E)ob2);
		return ll;
		}
	
	/**
	 * Get all ID and objects of a certain type
	 */
	@SuppressWarnings("unchecked") public <E> SortedMap<String, E> getIdObjects(Class<E> cl)
		{
		TreeMap<String, E> map=new TreeMap<String, E>();
		for(Map.Entry<String, EvObject> e:metaObject.entrySet())
			if(e.getValue().getClass()==cl)
				map.put(e.getKey(),(E)e.getValue());
		return map;
		}
	
	
	
	
	/**
	 * Set if metadata has been modified
	 */
	public void setMetadataModified(boolean flag)
		{
		if(flag)
			coreMetadataModified=true;
		else
			{
			coreMetadataModified=false;
			for(EvObject ob:metaObject.values())
				ob.metaObjectModified=false;
			}
		}
	
	/**
	 * Check if the metadata or any object has been modified
	 */
	public boolean isMetadataModified()
		{
		boolean modified=coreMetadataModified;
		for(EvObject ob:metaObject.values())
			modified|=ob.metaObjectModified;
		return modified;
		}
	
	/**
	 * Get a meta object by ID
	 */
	public EvObject getMetaObject(String i)
		{
		return metaObject.get(i);
		}
	
	/**
	 * Put a meta object into the collection
	 */
	public int addMetaObject(EvObject o)
		{
		int i=1;
		while(metaObject.get(Integer.toString(i))!=null)
			i++;
		metaObject.put(Integer.toString(i), o);
		return i;
		}
	
	/**
	 * Remove an object via the pointer
	 */
	public void removeMetaObjectByValue(EvObject ob)
		{
		String id=null;
		for(Map.Entry<String, EvObject> entry:metaObject.entrySet())
			if(entry.getValue()==ob)
				{
				id=entry.getKey();
				break;
				}
		if(id!=null)
			metaObject.remove(id);
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
  				id=""+-1;
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
	

	/******************************************************************************************************
	 *                               Abstract Instance                                                    *
	 *****************************************************************************************************/


	/**
	 * Get a name description of the metadata
	 */
	public abstract String getMetadataName();
	
	/**
	 * Save metadata
	 */
	public abstract void saveMeta();

	/**
	 * Get entry for Load Recent or null if not possible
	 */
	public abstract RecentReference getRecentEntry();
	}
