package endrov.basicWindow;

import java.util.Collections;
import java.util.LinkedList;

import endrov.data.EvContainer;
import endrov.data.EvObject;

/**
 * Object combo for one type of object only
 * @author Johan Henriksson
 */
public class EvComboObjectOne<E extends EvObject> extends EvComboObject
	{
	static final long serialVersionUID=0;
	private EvContainer example;

	/**
	 * Need to take an instance of the object as well due to erasure rules
	 */
	public EvComboObjectOne(E exampleObject, boolean allowNoSelection, boolean allowCreation)
		{
		super(allowCreation ? 
				new LinkedList<EvObject>(Collections.singleton(exampleObject)) :
				new LinkedList<EvObject>(), true, allowNoSelection);
		this.example=exampleObject;
		updateList();
		}
	
	public boolean includeObject(EvContainer cont)
		{
		return example!=null && cont.getClass()==example.getClass();
		}

	/**
	 * Get currently selected object or null
	 */
	@SuppressWarnings("unchecked")
	public E getSelectedObject()
		{
		return (E)super.getSelectedObject();
		}

	/**
	 * Get currently selected object, or an empty one if none is selected
	 */
	@SuppressWarnings("unchecked")
	public E getSelectedObjectNotNull()
		{
		try
			{
			E e=(E)super.getSelectedObject();
			if(e==null)
				return (E)example.getClass().newInstance();
			else
				return e;
			}
		catch (InstantiationException e)
			{
			e.printStackTrace();
			}
		catch (IllegalAccessException e)
			{
			e.printStackTrace();
			}
		System.out.println("total wtf!");
		return null; //Should never happen
		}

	
	}
