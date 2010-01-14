/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package OSTdaemon;

public interface DaemonListener
	{
	public void daemonLog(String s);

	public void daemonError(String s, Exception e);
	}
