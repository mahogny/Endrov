/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import java.util.*;


/**
 * One hardware device/session
 * @author Johan Henriksson
 *
 */
public interface Device
	{
	/** Descriptive name of hardware */
	public String getDescName();
		
	
	////// For devices
	
	public SortedMap<String,PropertyType> getPropertyTypes();
	public SortedMap<String,String> getPropertyMap();
	public String getPropertyValue(String prop);
	public Boolean getPropertyValueBoolean(String prop);
	
	public void setPropertyValue(String prop, String value);
	public void setPropertyValue(String prop, boolean value);
	
	//Corresponds to MM config block
	/*
	public SortedMap<String,String> getInfoMap();
	public String getInfoValue(String prop);
	*/
	
	
	public boolean hasConfigureDialog();
	public void openConfigureDialog();
	}
