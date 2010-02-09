/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.imserv;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
	
	public String getName() throws Exception;
	
	
	public CompressibleDataTransfer getImageCache(String blobid) throws Exception;
	public CompressibleDataTransfer getMetadata() throws Exception;
	public void setMetadata(CompressibleDataTransfer data) throws Exception;
	
	public byte[] getThumb() throws Exception; //Always PNG?
	public ImageTransfer getImage(String blobid, String channel, EvDecimal frame, EvDecimal z) throws Exception;
	public void putImage(String blobid, String channel, EvDecimal frame, EvDecimal z, ImageTransfer data) throws Exception;
	//it is up to server to recalculate imagelist after put. easiest way, delete cache. less stateful.
	
	public void setTag(String tag, String value, boolean enable) throws Exception;
	public Tag[] getTags() throws Exception;
	
	/**
	 * Transfer of an image. The image is compressed, no point in additional compression. But need to specify MIME.
	 */
	public static class ImageTransfer implements Serializable
		{
		static final long serialVersionUID=0;
		public String format;
		public byte[] data;
		}
	
	/**
	 * Transfer of a binary object, with optional compression
	 */
	public static class CompressibleDataTransfer implements Serializable
		{
		static final long serialVersionUID=0;
		public static final int NONE=0;
		public static final int LZMA=1;
		public static final int ZIP=2;
		public int compression;
		public byte[] data;
		
		/**
		 * Get data stream, uncompressed
		 */
		public InputStream getInputStream()
			{
			return new ByteArrayInputStream(data);
			}
		}
	
	
	}
