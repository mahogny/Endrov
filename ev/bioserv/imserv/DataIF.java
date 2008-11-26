package bioserv.imserv;

import java.io.Serializable;
import java.rmi.Remote;

import endrov.util.EvDecimal;

/**
 * Server "data" object: interface
 * 
 * @author Johan Henriksson
 */
public interface DataIF extends Remote
	{
	static final long serialVersionUID=0;
	
//	public void print() throws Exception;
	
	public String getName() throws Exception;
	
	public byte[] getThumb() throws Exception;
	
	public CompressibleDataTransfer getImageList() throws Exception;
	
	public CompressibleDataTransfer getRMD() throws Exception;
	public void setRMD(CompressibleDataTransfer data) throws Exception;
	
	public ImageTransfer getImage(String channel, EvDecimal frame, EvDecimal z) throws Exception;
	public void putImage(String channel, EvDecimal frame, EvDecimal z, ImageTransfer data) throws Exception;
	//it is up to server to recalculate imagelist after put. easiest way, delete cache.
	
	public void setTag(String tag, String value, boolean enable) throws Exception;

	public Tag[] getTags() throws Exception;
	
	public static class ImageTransfer implements Serializable
		{
		static final long serialVersionUID=0;
		public String format;
		public byte[] data;
		}
	
	public static class CompressibleDataTransfer implements Serializable
		{
		static final long serialVersionUID=0;
		public static final int NONE=0;
		public static final int LZMA=1;
		public int compression;
		public byte[] data;
		}
	
	
	}
