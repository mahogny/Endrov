/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.core.batch;

public interface BatchListener
	{

	public void batchLog(String s);
	public void batchError(String s);
	
	public void batchDone();
	}
