package endrov.imageset;

import java.io.File;

import endrov.data.RecentReference;
import endrov.util.EvDecimal;


public class EmptyImageset extends Imageset
	{
	public EmptyImageset()
		{
		imageset="(Empty)";
		meta.resX=1;
		meta.resY=1;
		meta.resZ=1;
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
		protected EvImage internalMakeLoader(EvDecimal frame, EvDecimal z)
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
		
		
		public double transformWorldImageX(double c){return c;}
		public double transformWorldImageY(double c){return c;}
		public double transformImageWorldX(double c){return c;}
		public double transformImageWorldY(double c){return c;}
		public double scaleWorldImageX(double d){return d;}
		public double scaleWorldImageY(double d){return d;}
		public double scaleImageWorldX(double d){return d;}
		public double scaleImageWorldY(double d){return d;}
		
		public int getBinning()
			{
			return 1;
			}
		public double getDispX()
			{
			return 0;
			}
		public double getDispY()
			{
			return 0;
			}
		public double getResX()
			{
			return 1;
			}
		public double getResY()
			{
			return 1;
			}
		}
	
	public RecentReference getRecentEntry()
		{
		return null;
		}
	}
