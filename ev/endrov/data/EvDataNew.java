package endrov.data;

public abstract class EvDataNew
	{
	
	//TODO remove these
	//saveMeta can point to io and get a concrete implementation
	/*
	public abstract String getMetadataName();
	public abstract void saveMeta();
	public abstract RecentReference getRecentEntry();
	*/
	
	//This is new
	
	private String metadataName;
	
	public String getMetadataName()
		{
		return metadataName;
		}
	
	public EvIOData io;
	
	//io must have RecentReference
	
	
	}
