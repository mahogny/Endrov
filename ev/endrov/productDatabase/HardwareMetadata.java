/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.productDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * One entry in the database
 * @author Johan Henriksson
 *
 */
public class HardwareMetadata
	{
	/**
	 * Properties, Name -> Value
	 */
	public HashMap<String, String> property=new HashMap<String, String>();
	
	public String getManufacturer()
		{
		return property.get("Manufacturer");
		}
	
	public String getModelName()
		{
		return property.get("ModelName");
		}
	
	public String getModelNumber()
		{
		return property.get("ModelNumber");
		}
	
	/**
	 * Wavelength -> fraction transmitted
	 */
	public Map<Double,Double> getTransmittance()
		{
		return new TreeMap<Double, Double>();
		//TODO
		}
	
	@Override
	public String toString()
		{
		return property.toString();
		}
	
	}
