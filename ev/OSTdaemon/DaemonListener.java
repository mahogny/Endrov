package OSTdaemon;

public interface DaemonListener
	{
	public void daemonLog(String s);

	public void daemonError(String s, Exception e);
	}
