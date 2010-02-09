/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

/**
 * Entry in the "Load Recent"-menu
 * 
 * @author Johan Henriksson
 */
public class RecentReference
	{
	/** 
	 * Description, just shown in menu 
	 */
	public String descName;
	
	/** 
	 * URL, everything needed to load the file via EvDataSupport
	 */
	public String url;
	
	public RecentReference(String descName, String url)
		{
		this.descName=descName;
		this.url=url;
		}
	}
