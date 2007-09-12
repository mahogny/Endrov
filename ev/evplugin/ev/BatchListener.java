package evplugin.ev;

public interface BatchListener
	{

	public void batchLog(String s);
	public void batchError(String s);
	
	public void batchDone();
	}
