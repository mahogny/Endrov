package OSTdaemon;
//todo: need to harmonize settings file?

import java.awt.RenderingHints;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.nio.channels.*;
/*import com.sun.image.codec.jpeg.*; 
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
*/

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;

import org.jdom.*;

import endrov.util.EvImageUtils;
import endrov.util.EvXmlUtil;



//make max: duplicate whatever branch


public class OSTdaemon extends Thread
	{
	public String pathInput=".";
	public String pathConverted;
	public String pathOutput;
	
	public HashMap<String,Integer> compressionLevel=new HashMap<String, Integer>();
	public HashMap<String,String> outputFormat=new HashMap<String, String>();

	public int ostVersion=2;
	public boolean deleteInput=false;
	
	public HashSet<String> makeMaxChannel=new HashSet<String>();
	
	private boolean shutDown;
	private boolean isRunning;
	
	private boolean skipBlackSlices=false;
	private boolean skipWhiteSlices=false;

	/** Shut down whenever possible */
	public void shutDown() {shutDown=true;}
	/** Start whenever possible */
	public void go() {shutDown=false;}
	/** Check if daemon is running */	
	public boolean isRunning() {return isRunning;}
	
	private DaemonListener daemonListener;
	
	/** Parsed name of incoming file */
	private static class Incoming
		{
		public String filename;
		public String cmd="";
		public Map<String,String> param=new HashMap<String, String>();
		}
	
	/**
	 * Create daemon
	 * @param listener Listener for log events. Can be null.
	 */
	public OSTdaemon(DaemonListener listener)
		{
		daemonListener=listener;
		}
	
	/** Log ordinary message */
	public void log(String s)
		{
		if(daemonListener!=null)
			daemonListener.daemonLog(s);
		}
	/** Log error */
	public void error(String s, Exception e)
		{
		if(daemonListener!=null)
			daemonListener.daemonError(s,e);
		}
	
	/**
	 * Update configuration
	 */
	public void readConfig(String filename)
		{
		log("Reading config file "+filename);
		BufferedReader input = null;
    try
    	{
    	input = new BufferedReader( new FileReader(filename) );
    	String line = null;
    	while (( line = input.readLine()) != null)
    		{
    		StringTokenizer tok=new StringTokenizer(line);
    		if(!tok.hasMoreTokens())
    			continue;
    		String cmd=tok.nextToken();
    		if(cmd.equals("pathinput"))
    			{
    			String path=tok.nextToken();
    			File f=new File(path);
    			pathInput=f.getAbsolutePath();
    			log("Set pathInput = "+pathInput);
    			}
    		else if(cmd.equals("pathoutput"))
    			{
    			String path=tok.nextToken();
    			File f=new File(path);
    			pathOutput=f.getAbsolutePath();
    			log("Set pathOutput = "+pathOutput);
    			}
    		else if(cmd.equals("pathconverted"))
    			{
    			String path=tok.nextToken();
    			File f=new File(path);
    			pathConverted=f.getAbsolutePath();
    			log("Set pathConverted = "+pathConverted);
    			}
    		else if(cmd.equals("compression"))
    			{
    			String channel=tok.nextToken();
    			int level=Integer.parseInt(tok.nextToken());
    			compressionLevel.put(channel,level);
    			log("Set compression level for "+channel+" to "+level);
    			}
    		else if(cmd.equals("outputformat"))
    			{
    			String channel=tok.nextToken();
    			String format=tok.nextToken();
    			outputFormat.put(channel,format);
    			log("Set output format for "+channel+" to "+format);
    			}
    		else if(cmd.equals("addmax"))
    			{
    			String channel=tok.nextToken();
    			makeMaxChannel.add(channel);
    			log("Added maxSliceGeneration for "+channel);
    			}
    		else if(cmd.equals("removemax"))
    			{
    			String channel=tok.nextToken();
    			makeMaxChannel.remove(channel);
    			log("Removed maxSliceGeneration for "+channel);
    			}
    		else if(cmd.equals("ostversion"))
    			{
    			String versions=tok.nextToken();
    			ostVersion=Integer.parseInt(versions);
    			log("Set OST output version to "+ostVersion);
    			}
    		else if(cmd.equals("deleteinput"))
    			{
    			String deleteInputs=tok.nextToken();
    			deleteInput=Integer.parseInt(deleteInputs)==1;
    			log("Delete input file after conversion: "+deleteInput);
    			}
    		else if(cmd.equals("skipblackslices"))
    			{
    			String s=tok.nextToken();
    			skipBlackSlices=Integer.parseInt(s)==1;
    			log("Skip black slices: "+skipBlackSlices);
    			}
    		else if(cmd.equals("skipwhiteslices"))
    			{
    			String s=tok.nextToken();
    			skipWhiteSlices=Integer.parseInt(s)==1;
    			log("Skip white slices: "+skipWhiteSlices);
    			}
    		else if(cmd.equals("setmax"))
    			{
    			String log="maxSliceGeneration for these channels only: ";
    			makeMaxChannel.clear();
    			while(tok.hasMoreTokens())
    				{
    				String channel=tok.nextToken();
    				makeMaxChannel.add(channel);
    				log=log+channel+" ";
    				}
    			log(log);
    			}
    		else
    			log("Unknown parameter in config file: "+cmd);
    		}
    	}
    catch(Exception e)
    	{
    	error(null,e);
    	}
		}
	
	

	/** Get compression level for a channel in 0-100. Defaults to 100. */
	public int getCompressionLevel(String channel)
		{
		if(compressionLevel.containsKey(channel))
			return compressionLevel.get(channel);
		else
			return 100;
		}

	/** Get output format. */
	public String getOutputFormat(String channel)
		{
		if(outputFormat.containsKey(channel))
			return outputFormat.get(channel);
		else if(getCompressionLevel(channel)<100)
			return "jpg";
		else
			return "png";
		}
	


	/**
	 * Get the directory of the imageset
	 */
	public File getImagesetFile(String imageset)
		{
		return new File(pathOutput,imageset+".ost");
		}
	
	
	public File getChannelFile(String imageset, String channel)
		{
		return new File(getImagesetFile(imageset),"ch-"+channel);
		}
	
	/**
	 * Get the name of an image as stored to the output
	 */
	private File outputImageName(String argImageset, String argChannel, String ext, int argStack, int slice)
		{
		if(ostVersion==2)
			{
			File toDir = new File(getChannelFile(argImageset, argChannel),pad(argStack,8));
			toDir.mkdirs();
			File toFile = new File(toDir, pad(slice,8)+"."+ext); 
			return toFile;
			}
		else
			return null;
		}

	
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Relics //////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	

	/**
	 * OST1 input
	 * Copy metafile data which is of form imageset-channel.rmd
	 * It replaces any earlier RMD-data.
	 * Does not update RMD to OST2
	 * 
	 * actually still in use!
	 * 
	 */
	public void readMeta1(File from)
		{
		String filename=from.getName();
		String argImageset=filename.substring(0, filename.indexOf('-'));
		String argChannel=chopFileEnding(filename.substring(argImageset.length()+1));
		
		log("Reading metafile for "+argImageset+" / "+argChannel);
		
		File to=getChannelFile(argImageset, argChannel);
		if(to.exists() || to.mkdirs())
			{
			try
				{
				copyFile(from, new File(to,"rmd.txt"));
				if(makeMaxChannel.contains(argChannel))
					{
//					File tomax=new File(pathOutput+"/"+argImageset+"/"+argImageset+"-"+argChannel+"max");
					File tomax=getChannelFile(argImageset, argChannel+"max");
					tomax.mkdirs();
					copyFile(from, new File(tomax,"rmd.txt"));
					//copyMaxMeta(argImageset, argChannel); //not here
					}
				moveToConverted(from);
				}
			catch (Exception e)
				{
				error(null,e);
				}
			}
		else
			{
			log("Failed to make directory "+to.getAbsolutePath());
			}
		}
	
	
	public void copyMaxMeta(String argImageset, String argChannel) throws Exception
		{
		String maxchannel=argChannel+"max";
		
		File imagesetDir=getImagesetFile(argImageset);
		if(imagesetDir.exists())
			{
			File totalFile=new File(imagesetDir,"rmd.ostxml");
			Document rmd=EvXmlUtil.readXML(totalFile);
			Element root=rmd.getRootElement();
			Element imagesetEl=root.getChild("imageset");
			Element newImageset=new Element("imageset");

			//newImageset.removeContent could partially replace mess below
			
			//Filter out max channel
			for(Object e:imagesetEl.getChildren())
				{
				Element ee=(Element)e;
				if(ee.getName().equals("channel"))
					{
					String thisName=ee.getAttributeValue("name");
					if(thisName.equals(argChannel))
						{
						Element maxEl=(Element)ee.clone();
						maxEl.setAttribute("name",maxchannel);
						newImageset.addContent((Element)ee.clone());
						newImageset.addContent(maxEl);
						}
					else if(!thisName.equals(maxchannel))
						newImageset.addContent((Element)ee.clone());
					}
				else
					newImageset.addContent((Element)ee.clone());
				}
			root.removeChild("imageset");
			root.addContent(newImageset);
			EvXmlUtil.writeXmlData(rmd, totalFile);
			}
		}
	
	/**
	 * OST2 input
	 * Copy metafile data which is of form imageset-*.xml
	 * Data will be merged with the rmd-structure, likely you want to make an xml-file for every stack or picture
	 * the microscope generates.
	 */
	public void readMeta2(File from)
		{
		if(ostVersion==2)
			{
			String filename=from.getName();
			String argImageset=filename.substring(0, filename.indexOf('-'));
			File imagesetDir=getImagesetFile(argImageset);
			imagesetDir.mkdirs();
			File totalFile=new File(imagesetDir,"rmd.ostxml");

			try 
				{
				//Read current RMD
				Document total=null;
				if(totalFile.exists())
					total=EvXmlUtil.readXML(totalFile);
				else
					total=new Document(new Element("ost"));

				//Read new data
				Document newrmd=EvXmlUtil.readXML(from);

				//Merge
				EvXmlUtil.mergeXML(total.getRootElement(), newrmd.getRootElement());

				//Save new RMD
				EvXmlUtil.writeXmlData(total, totalFile);

				moveToConverted(from);
				}
			catch(Exception e)
				{
				error("Problem merging XML", e);
				}
			}
		else
			error("Unsupported version number",null);
		}

	
	/**
	 * OST2 input
	 * Copy metafile data which is of form imageset-new.xml
	 * Data will just be copied
	 */
	public void copyMeta2(File from)
		{
		String filename=from.getName();
		String argImageset=filename.substring(0, filename.indexOf('-'));
		File imagesetDir=getImagesetFile(argImageset);
		imagesetDir.mkdirs();
		File totalFile=new File(imagesetDir,"rmd.ostxml");
		try 
			{
			Document newrmd=EvXmlUtil.readXML(from);
			EvXmlUtil.writeXmlData(newrmd, totalFile);
			moveToConverted(from);
			for(String ch:makeMaxChannel)
				copyMaxMeta(argImageset,ch);
			}
		catch(Exception e)
			{
			error("Problem copying RMD ",e);
			}
		}

	
	

	/**
	 * Copy file into data/-directory
   * Filename is: imageset-data-FILENAME
	 */
	public void copyExtra(File from)
		{
		String filename=from.getName();
		String argImageset=filename.substring(0, filename.indexOf('-'));
		String argFilename=filename.substring(argImageset.length()+1+4+1);
		
		File toDir=new File(getImagesetFile(argImageset),"data");
		if(toDir.exists() || toDir.mkdirs())
			{
			File to=new File(toDir.getAbsolutePath(),argFilename);
			log("Copying file "+from.getAbsolutePath()+" to "+to.getAbsolutePath());
			try
				{
				copyFile(from, to);
				moveToConverted(from);
				}
			catch(Exception e)
				{
				error(null,e);
				}
			}
		else
			log("Error creating directory "+toDir.getAbsolutePath());
		}
	

	/**
	 * Read a stack using Bioformats
	 */
	public static List<BufferedImage> readStackBioformats(File filename) throws Exception
		{
		IFormatReader imageReader=new ImageReader();
		imageReader.setId(filename.getPath());
	
		int count=imageReader.getImageCount();
		int subid=0;
	
		LinkedList<BufferedImage> list=new LinkedList<BufferedImage>();
		for(int id=0;id<count;id++)
			{
			BufferedImage i=imageReader.openImage(id);
			int w=i.getWidth();
			int h=i.getHeight();
	
			BufferedImage im=new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
	
			float matrix[][]={{0,0,0}};
			if(i.getRaster().getNumBands()==1)
				matrix=new float[][]{{0/*,0*/}};
			else if(i.getRaster().getNumBands()==2)
				matrix=new float[][]{{0,0/*,0*/}};
			else if(i.getRaster().getNumBands()==3)
				matrix=new float[][]{{0,0,0/*,0*/}};
	
			matrix[0][subid]=1;
			RasterOp op=new BandCombineOp(matrix,new RenderingHints(null));
			op.filter(i.getRaster(), im.getRaster());
	
			list.add(im);
			}
		return list;
		}
	
	/**
	 * Read stack using JAI. cannot handle 16-bit tiff
	 */
	public static List<BufferedImage> readStackJAI(File from) throws IOException
		{
		ImageInputStream stream = ImageIO.createImageInputStream(from);
	  Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReaders(stream);
	  if (!readers.hasNext())
	  	throw new RuntimeException("no image reader found");
	  javax.imageio.ImageReader reader = readers.next();
	  reader.setInput(stream);            // don't omit this line!
	  int n = reader.getNumImages(true);  // don't use false!
	  System.out.println("numImages = " + n);
	  LinkedList<BufferedImage> imlist=new LinkedList<BufferedImage>();
	  for (int i = 0; i < n; i++) 
	  	{
	  	BufferedImage image = reader.read(i);
	  	imlist.add(image);
	  	}
	  stream.close(); 
	  return imlist;
		}

	/**
	 * ImageIO does not understand TIF. This helps.
	 */
	public BufferedImage readSliceHelper(File from)
		{
		try
			{
			if((from.getName().endsWith(".tif") || from.getName().endsWith(".tiff"))) //ImageIO failed on a .tif
				{
				javax.imageio.ImageReader imr=ImageIO.getImageReadersByFormatName("tiff").next();
				imr.setInput(new FileImageInputStream(from));
				Raster ir=imr.read(0).getRaster();
				/*
				SeekableStream s = new FileSeekableStream(from);
				TIFFDecodeParam param = null;
			  ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
			  Raster ir=dec.decodeAsRaster();
			  */
				
			  int type=ir.getSampleModel().getDataType();
			  if(type==0) type=BufferedImage.TYPE_BYTE_GRAY;//?
			  BufferedImage bim=new BufferedImage(ir.getWidth(),ir.getHeight(),type);
			  WritableRaster wr=bim.getRaster();
			  wr.setRect(ir);
			  return bim;
				}
			else
				return ImageIO.read(from);
			}
		catch (IOException e)
			{
			System.out.println(e.getMessage());
			return null;
			}
		
		}

	/**
	 * Convert an image slice: imageset-channel-frame-slice.*
	 */
	public void convertSlice(File from)
		{
		String filename=from.getName();
		String argImageset=filename.substring(0, filename.indexOf('-'));
		String rest=filename.substring(argImageset.length()+1);

		String argChannel=rest.substring(0, rest.indexOf('-'));
		String rest2=rest.substring(argChannel.length()+1);
		
		String argFrameS=rest2.substring(0, rest2.indexOf('-'));
		String rest3=rest2.substring(argFrameS.length()+1);

		int argFrame=Integer.parseInt(argFrameS);
		
		int argSlice=Integer.parseInt(chopFileEnding(rest3));
		
		log("Converting slice "+argImageset+" / "+argChannel+" / "+argFrame+" / "+argSlice);
		
		try
			{
			//Convert slice
			BufferedImage im=readSliceHelper(from);
			if(im==null)
				{
				error("Could not read "+from, null);
				return;
				}
			else
				{
				File toFile = outputImageName(argImageset, argChannel, getOutputFormat(argChannel), argFrame, argSlice);
				saveImage(im, toFile, getCompressionLevel(argChannel)/100.0f);
				
				//Make a maximum slice too
				if(makeMaxChannel.contains(argChannel))
					{
					log("Max not supported for slices yet");
					}
				}
			}
		catch (Exception e)
			{
			error(null,e);
			}
		
		moveToConverted(from);
		}
	

	
	
	
	/**
	 * Load slices from stack
	 */
	public BufferedImage calcMaxStack(List<BufferedImage> imlist)
		{
		//Filter out bad images
		List<BufferedImage> newlist=new LinkedList<BufferedImage>();
		for(BufferedImage im:imlist)
			if(!(skipBlackSlices && isBlackSlice(im)) && !(skipWhiteSlices && isWhiteSlice(im)))
				newlist.add(im);
		imlist=newlist;
			
		//
		Iterator<BufferedImage> it=imlist.iterator();
		BufferedImage fim=it.next();
		
		int height = fim.getHeight();
		int width = fim.getWidth();
		BufferedImage maxim = fim.getSubimage(0,0, width, height);
		
		while(it.hasNext())
			{
			BufferedImage im=it.next();
			Raster imr=im.getRaster();
			WritableRaster maxr=maxim.getRaster();
			for(int ay=0;ay<height;ay++)
				for(int ax=0;ax<width;ax++)
					{
					double s=imr.getSampleDouble(ax, ay, 0);
					maxr.setSample(ax, ay, 0, s);
					}
			}
		return maxim;
		}
	
	/**
	 * Convert an image stack: imageset-channel-frame.*
	 */
	public void convertStack(File from)
		{
		String filename=from.getName();
		String argImageset=filename.substring(0, filename.indexOf('-'));
		String rest=filename.substring(argImageset.length()+1);

		String argChannel=rest.substring(0, rest.indexOf('-'));
		String rest2=rest.substring(argChannel.length()+1);
		
		int argFrame=Integer.parseInt(chopFileEnding(rest2));
		
		log("Converting stack "+argImageset+" / "+argChannel+" / "+argFrame);
		
		try
			{
			///// new
			System.out.println("read stack");
			List<BufferedImage> slices=readStackBioformats(from);

			System.out.println("storing slices");
			//Convert slices
			int numz=slices.size();
			for(int i=0;i<numz;i++)
				{
				BufferedImage im=slices.get(i);
				if(!(skipBlackSlices && isBlackSlice(im)) && !(skipWhiteSlices && isWhiteSlice(im)))
					{				
					File toFile = outputImageName(argImageset, argChannel, getOutputFormat(argChannel), argFrame, i);
					saveImage(im, toFile, getCompressionLevel(argChannel)/100.0f);
					}
				else
					System.out.println("Skipping slice "+i);
				}

			System.out.println("make max");
			//Make a maximum slice too
			if(makeMaxChannel.contains(argChannel))
				{
				log("Making max channel");
				BufferedImage im=calcMaxStack(slices);
				File toFile = outputImageName(argImageset, argChannel+"max", getOutputFormat(argChannel+"max"), argFrame, 0);
				saveImage(im, toFile, getCompressionLevel(argChannel)/100.0f);
				}


			
			////////////// old ////////////////////
			
			//JAI or Jubio?... Jubio for now
			
			/*
			Jubio jubio=new Jubio(from.getAbsolutePath());
			
			//Convert slices
			int numz=jubio.getDepth();
			for(int i=0;i<numz;i++)
				{
				BufferedImage im=jubio.getBufferedImage(i);
				if(!(skipBlackSlices && isBlackSlice(im)) && !(skipWhiteSlices && isWhiteSlice(im)))
					{				
					File toFile = outputImageName(argImageset, argChannel, getOutputFormat(argChannel), argFrame, i);
					saveImage(im, toFile, getCompressionLevel(argChannel)/100.0f);
					}
				else
					System.out.println("Skipping slice "+i);
				}
			
			//Make a maximum slice too
			if(makeMaxChannel.contains(argChannel))
				{
				log("Making max channel");
				Jubio max=jubio.calcMax();
				BufferedImage im=max.getBufferedImage();
				File toFile = outputImageName(argImageset, argChannel+"max", getOutputFormat(argChannel+"max"), argFrame, 0);
				saveImage(im, toFile, getCompressionLevel(argChannel)/100.0f);
				}
			*/	
			
			}
		catch (Exception e)
			{
			error(null,e);
			}
		
		moveToConverted(from);
		}

	
	
	
	/**
	 * Save an image to disk
	 */
	public void saveImage(BufferedImage im, File toFile, float quality) throws Exception
		{
		String fileEnding=getFileEnding(toFile.getName());
		if(fileEnding.equals("jpg") || fileEnding.equals("jpeg"))
			{
			EvImageUtils.saveJPEG(im, toFile, quality);
/*	    FileOutputStream toStream = new FileOutputStream(toFile); 
	    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(toStream); 
	    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(im); 
	    param.setQuality(quality, false); 
	    encoder.setJPEGEncodeParam(param); 
	    encoder.encode(im); */
			}
		else
			{
			ImageIO.write(im, fileEnding, toFile);
			}
		}
	


	/**
	 * Check if file is an image
	 */
	private boolean isImageFile(String filename)
		{
		return filename.endsWith(".tif") || filename.endsWith(".tiff") ||
					filename.endsWith(".png") || filename.endsWith(".jpeg") ||
					filename.endsWith(".jpg") || filename.endsWith(".bmp") || filename.endsWith(".pict");
		}

	/**
	 * Check if image file is a stack. Assumes filename is for an image.
	 */
	private boolean isStack(String filename)
		{
		//Count # of -
		int numdash=0;
		while(filename.indexOf('-')>=0)
			{
			numdash++;
			filename=filename.substring(filename.indexOf('-')+1);
			}
		return numdash==2;
		}
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// New functions ///////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////
	

	/** Get file ending	 */
	private static String getFileEnding(String s)
		{
		return s.substring(s.lastIndexOf('.')+1);
		}

	/** Remove the file ending of a string */
	public static String chopFileEnding(String s)
		{
		int pos=s.lastIndexOf('.');
		if(pos>=0)
			return s.substring(0,pos);
		else
			return s;
		}

	
	
	/**
	 * Move a file to the converted-path or delete if this is the setting
	 */
	public void moveToConverted(File from)
		{
		if(deleteInput)
			{
			from.delete();
			log("Deleted converted "+from.getAbsolutePath());
			}
		else
			{
			File to=new File(pathConverted,from.getName());
			if(from.renameTo(to))
				log("Moved "+from.getAbsolutePath()+" to "+to.getAbsolutePath());
			else
				log("Failed to move "+from.getAbsolutePath()+" to "+to.getAbsolutePath());
			}
		}
	
	
	public static boolean isBlackSlice(BufferedImage im)
		{
		WritableRaster r=im.getRaster();
		double p=r.getSampleDouble(0, 0, 0);
		return p<0.01;
		}
	public static boolean isWhiteSlice(BufferedImage im)
		{
		WritableRaster r=im.getRaster();
		double p=r.getSampleDouble(0, 0, 0);
		return p>254.99;
		}
	
	
	
	
	
	
	/**
	 * Pause while "shut down"
	 */
	private void shutDownLoop()
		{
		if(shutDown)
			{
			log("Stopped");
			isRunning=false;
			while(shutDown)
				try {sleep(500);}
				catch (InterruptedException e){}
			isRunning=true;
			log("Started");
			}
		}
	
	
	

	/**
	 * Parse incoming arguments and invoke the right method
	 * @param file
	 */
	public void dealWithFile(File file)
		{
		Incoming inc=new Incoming();
		String inp=file.getName().substring(1);
		inc.filename=inp.substring(inp.indexOf("--")+2);
		inp=inp.substring(0,inp.indexOf("--"));
		StringTokenizer tok=new StringTokenizer(inp,"-");
		System.out.println(inc.filename);
		if(tok.hasMoreElements())
			inc.cmd=tok.nextToken().toLowerCase();
		while(tok.hasMoreElements())
			{
			String te=tok.nextToken();
			if(te.indexOf('_')>=0)
				{
				String attr=te.substring(0,te.indexOf('_'));
				String value=te.substring(te.indexOf('_')+1);
				System.out.println(attr+" - "+value);
				inc.param.put(attr, value);
				}
			else
				inc.param.put(te, null);
			}
		
		
		if(inc.cmd.equals("image"))
			{
			
			}
		else if(inc.cmd.equals("data"))
			{
			
			}
		else if(inc.cmd.equals("newxml"))
			{
			
			}
		else if(inc.cmd.equals("incxml"))
			{
			
			}
		}
	
	
	/**
	 * The main function of the thread
	 */
	public void run()
		{
		shutDown=true;
		for(;;)
			{
			shutDownLoop();

			try
				{
				//Scan directory
				File dir=new File(pathInput);
				if(!dir.exists())
					{
//					dir=new File(".");
					log("ERROR: input directory does not exist");
					}
				else
					for(File file:dir.listFiles())
						{
						shutDownLoop();
						if(file.exists())
							{
							String filename=file.getName();
							if(!file.getName().startsWith("."))
								{
								log(filename);
								if(filename.startsWith("-"))
									dealWithFile(file);
								else if(filename.equals("settings"))
									readConfig(file.getAbsolutePath());
								else if(filename.endsWith(".rmd"))
									readMeta1(file);
								else if(filename.endsWith("-new.xml"))
									copyMeta2(file);
								else if(filename.endsWith(".xml"))
									readMeta2(file);
								else if(filename.indexOf("-data-")>=0)
									copyExtra(file);
								else if(isImageFile(filename))
									{
									if(isStack(filename))
										convertStack(file);
									else
										convertSlice(file);						
									}
								else
									log("File does not match rules: "+filename);
								}
							}
						else
							System.out.println("Main loop: Could not find file: "+file.getAbsolutePath()+" (probably removed by user)");
						}
				}
			catch (RuntimeException e)
				{
				daemonListener.daemonError("Internal error", e);
				}

			//Wait a bit until next scan, otherwise the program will waste cpu
			try {sleep(500);}
			catch (InterruptedException e){}
			}
		}

	
	/**
	 * Pad an integer up to # digits
	 */
	public static String pad(int i, int pad)
		{
		String s=""+i;
		while(s.length()<pad)
			s="0"+s;
		return s;
		}
	
	/**
	 * Copy a file
	 */
	public static void copyFile(File in, File out) throws Exception
		{
		FileChannel sourceChannel = new	FileInputStream(in).getChannel();
		FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		sourceChannel.close();
		destinationChannel.close();
		}

	
	}