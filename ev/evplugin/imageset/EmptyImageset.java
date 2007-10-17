package evplugin.imageset;

import java.io.File;

import javax.vecmath.Vector2d;

import evplugin.jubio.EvImageJAI;


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
			return new EvImageEmpty("");
			}
		}
	
	private static class EvImageEmpty extends EvImageJAI
		{
		public EvImageEmpty(String name)
			{
			super(name);
			}
		
		public Vector2d transformWorldImage(Vector2d c)
			{
			return new Vector2d(c);
			}
		public Vector2d transformImageWorld(Vector2d c)
			{
			return new Vector2d(c);
			}
		public Vector2d scaleWorldImage(Vector2d d)
			{
			return new Vector2d(d);
			}
		public Vector2d scaleImageWorld(Vector2d d)
			{
			return new Vector2d(d);
			}
		}
	
	
	}
