package burndaemon;

public interface DaemonListener
	{
	public void daemonLog(String s);
	public void daemonLogAttention(String s);
	public void daemonError(String s, Exception e);
	}
