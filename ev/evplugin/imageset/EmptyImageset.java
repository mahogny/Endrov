package evplugin.imageset;

import java.io.File;

public class EmptyImageset extends Imageset
	{
	public EmptyImageset()
		{
		imageset="(Empty)";
		}
	public void buildDatabase(){}
	public void saveMeta(){}
	public File datadir(){return null;}
	public String toString()
		{
		return getMetadataName();
		}
	}
