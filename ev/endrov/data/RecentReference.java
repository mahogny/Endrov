package endrov.data;

/**
 * Entry in Load Recent
 * 
 * @author Johan Henriksson
 */
public class RecentReference
	{
	/** Description, just shown in menu */
	public String descName;
	/** URL, everything needed to load the file via EvDataSupport */
	public String url;
	
	public RecentReference(String descName, String url)
		{
		this.descName=descName;
		this.url=url;
		}
	}
