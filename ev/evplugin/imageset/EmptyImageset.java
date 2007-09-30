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
	
	
	
	
	
	protected ChannelImages internalMakeChannel(ImagesetMeta.Channel ch)
		{
		return new Channel(ch);
		}
	public class Channel extends Imageset.ChannelImages
		{
		public Channel(ImagesetMeta.Channel channelName)
			{
			super(channelName);
			}
		protected EvImage internalMakeLoader(int frame, int z)
			{
			return new EvImageJAI("");
			}
		}
	}
