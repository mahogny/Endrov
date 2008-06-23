package evplugin.imagesetImserv.service;

import java.io.Serializable;
import java.rmi.Remote;

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
	
	public ImageTransfer getImage(String channel, int frame, int z) throws Exception;
	
	
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
