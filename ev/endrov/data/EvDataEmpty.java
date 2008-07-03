package endrov.data;

import endrov.ev.*;

public class EvDataEmpty extends EvData
	{
	public String getMetadataName()
		{
		return "(empty)";
		}

	public void saveMeta()
		{
		Log.printError("Error: trying to save Empty metadata",null);
		}

	public RecentReference getRecentEntry()
		{
		return null;
		}
	}
