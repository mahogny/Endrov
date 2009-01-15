package endrov.data;

import java.util.*;

import org.jdom.Element;

import endrov.ev.EV;
import endrov.ev.Log;


/**
 * EV Object container
 * @author Johan Henriksson
 */
public class EvContainer
	{
	/** All meta objects */
	public TreeMap<String,EvObject> metaObject=new TreeMap<String,EvObject>();

	//TODO should make accessors
	//TODO parent pointer?
	//TODO putting an object should be checked to not create a cycle
	
	
	/** Flag if the metadata container itself has been modified */
	private boolean coreMetadataModified=false;
	
	/**
	 * This blobID is only valid *exactly after XML has been read*. This is because is really should
	 * be stored in the I/O-session, but more conveniently located here since 
	 */
	public String ostBlobID;
	
	
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
			for(EvContainer ob:metaObject.values())
				ob.coreMetadataModified=false;
			}
		}
	public void setMetadataModified()
		{
		setMetadataModified(true);
		}
	
	/**
	 * Check if the metadata or any object has been modified
	 */
	public boolean isMetadataModified()
		{
		boolean modified=coreMetadataModified;
		for(EvObject ob:metaObject.values())
			modified|=ob.isMetadataModified();
		return modified;
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
		//TODO getclass==, does this exclude subclasses?
		TreeMap<String, E> map=new TreeMap<String, E>();
		for(Map.Entry<String, EvObject> e:metaObject.entrySet())
			if(e.getValue().getClass()==cl)
				map.put(e.getKey(),(E)e.getValue());
		return map;
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
	
	/*
	public void saveSubObjectsXML(Element ostElement)
		{
		for(String id:metaObject.keySet())
			{
			EvObject o=metaObject.get(id);
			Element el=new Element("TEMPNAME");
			el.setAttribute("id",""+id);
			o.saveMetadata(el);
			ostElement.addContent(el);
			}
		}
*/
	
	
	/**
	 * Serialize object and all children
	 */
	public void recursiveSaveMetadata(Element root)
		{
		for(String id:metaObject.keySet())
			{
			EvObject o=metaObject.get(id);
			Element el=new Element("TEMPNAME");
			el.setAttribute("id",""+id);
			if(o.ostBlobID!=null)
				el.setAttribute("ostblobid",o.ostBlobID);
			o.saveMetadata(el);

			//subobjects
			if(!o.metaObject.isEmpty())
				{
				Element sube=new Element("ostsubobjects");
				o.recursiveSaveMetadata(sube);
				el.addContent(sube);
				}
			root.addContent(el);
			}
		}

	/**
	 * Load object and all children
	 */
	public void recursiveLoadMetadata(Element element)
		{
		//Extract objects
		for(Element child:EV.castIterableElement(element.getChildren()))
			{
			Class<? extends EvObject> ext=EvData.extensions.get(child.getName());
			EvObject o=null;
			if(ext==null)
				o=new CustomObject();
			else
				{
				try
					{
					o=ext.newInstance();
					}
				catch (InstantiationException e)
					{
					e.printStackTrace();
					}
				catch (IllegalAccessException e)
					{
					e.printStackTrace();
					}
				}
			String sid=child.getAttributeValue("id");
			String id;
			if(sid==null) 
				//TODO this should disappear once all OST is 3.2
				//This is only needed for imagesets without the EV extended attributes
				//should maybe grab a free one (detect)
				//id=""+-1;
				id="im"; //This is for the OST3 transition
			else
				id=sid;
			
			o.ostBlobID=child.getAttributeValue("ostblobid");
			
			
			Element subob=child.getChild("ostsubobjects");
			if(subob!=null)
				{
				o.recursiveLoadMetadata(subob);
				child.removeContent(subob);
				}
			o.loadMetadata(child);
			metaObject.put(id, o);
			Log.printLog("Found meta object of type "+child.getName());
			}
		}
	
	
	/**
	 * Extract EvObjects from an element.
	 * @deprecated
	 */
	/*
	private static Map<String,EvObject> extractSubObjectsFromXML(Element element)
		{
		Map<String,EvObject> obs=new HashMap<String, EvObject>();
	//Extract objects
		for(Element child:EV.castIterableElement(element.getChildren()))
			{
			Class<? extends EvObject> ext=EvData.extensions.get(child.getName());
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
					}
				catch (InstantiationException e)
					{
					e.printStackTrace();
					}
				catch (IllegalAccessException e)
					{
					e.printStackTrace();
					}
				o.loadMetadata(child);
				Log.printLog("Found meta object of type "+child.getName());
				}
			String sid=child.getAttributeValue("id");
			String id;
			if(sid==null) 
				//TODO this should disappear once all OST is 3.2
				//This is only needed for imagesets without the EV extended attributes
				//should maybe grab a free one (detect)
				//id=""+-1;
				id="im"; //This is for the OST3 transition
			else
				id=sid;
			obs.put(id, o);
			}
		return obs;
		}
*/
	
	}
