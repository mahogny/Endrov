package endrov.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * EV Object container
 * @author Johan Henriksson
 */
public class EvContainer
	{
	/** All meta objects */
	public TreeMap<String,EvObject> metaObject=new TreeMap<String,EvObject>();

	
	/** Flag if the metadata container itself has been modified */
	private boolean coreMetadataModified=false;
	
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
	
	


	}
