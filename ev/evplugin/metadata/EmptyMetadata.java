package evplugin.metadata;

import evplugin.ev.*;

public class EmptyMetadata extends Metadata
	{
	public String getMetadataName()
		{
		return "(empty)";
		}

	public void saveMeta()
		{
		Log.printError("Error: trying to save Empty metadata",null);
		}

	}
