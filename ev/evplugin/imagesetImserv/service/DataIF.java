package evplugin.imagesetImserv.service;

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
	
	public ImageList getImageList() throws Exception;
	
	
	
	public static class ImageList
		{
		public static final int NONE=0;
		public static final int LZMA=1;
		int compression;
		byte[] data;
		}
	
	
	}
