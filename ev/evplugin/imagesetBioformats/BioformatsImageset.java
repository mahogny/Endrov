package evplugin.imagesetBioformats;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.imageset.*;
import evplugin.data.*;
import evplugin.script.Script;

import loci.formats.*;

/**
 * Support for proprietary formats through LOCI Bioformats
 * 
 * @author Johan Henriksson (binding to library only)
 */
public class BioformatsImageset extends Imageset
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
	
		Script.addCommand("dbio", new CmdDBIO());
		
		EvDataBasic.extensions.add(new DataMenuExtension()
			{
			public void buildOpen(JMenu menu)
				{
				final JMenuItem miLoadBioformats=new JMenuItem("Load Bioformats imageset");
				addMetamenu(menu,miLoadBioformats);
				
				ActionListener listener=new ActionListener()
					{
					/**
					 * Show dialog for opening a new native imageset
					 */
					public void actionPerformed(ActionEvent e)
						{
						if(e.getSource()==miLoadBioformats)
							dialogOpenBioformats();
						}

					
					};
				miLoadBioformats.addActionListener(listener);
				}
			public void buildSave(JMenu menu, EvData meta)
				{
				}
			});
		}
	
	/**
	 * Open a dialog to open a bioformats imageset
	 */
	public static void dialogOpenBioformats()
		{
		JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setCurrentDirectory(new File(EvData.getLastDataPath()));
    int returnVal = chooser.showOpenDialog(null); //null=window
    if(returnVal == JFileChooser.APPROVE_OPTION)
    	{
    	String filename=chooser.getSelectedFile().getAbsolutePath();
    	EvData.setLastDataPath(chooser.getSelectedFile().getParent());
    	

    	
    	//doesn't really show, but better than nothing
    	JFrame loadingWindow=new JFrame(EV.programName); 
    	loadingWindow.setLayout(new GridLayout(1,1));
    	loadingWindow.add(new JLabel("Loading imageset"));
    	loadingWindow.pack();
    	loadingWindow.setBounds(200, 200, 300, 50);
    	loadingWindow.setVisible(true);
    	loadingWindow.repaint();
    	
    	try
				{
				EvData.metadata.add(new BioformatsImageset(filename));
				}
			catch (Exception e2)
				{
				evplugin.ev.Log.printError("bioformats", e2);
				}
    	BasicWindow.updateWindows();
    	loadingWindow.dispose();

    	
    	}
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	
	/** Path to imageset */
	public String basedir;

	
	public IFormatReader imageReader=null;
	
	/**
	 * Open a new recording
	 */
	public BioformatsImageset(String basedir) throws Exception
		{
		this.basedir=basedir;
		this.imageset=(new File(basedir)).getName();
		if(!(new File(basedir)).exists())
			throw new Exception("File does not exist");

		imageReader=new ImageReader();
		imageReader.setId(basedir);
		
		buildDatabase();
		}
	
	

	public File datadir()
		{
		return new File("");
		}

	
	public void saveMeta()
		{
		}
	
	

	
	/**
	 * Scan recording for channels and build a file database
	 */
	public void buildDatabase()
		{
		int numx=imageReader.getSizeX();
		int numy=imageReader.getSizeY();
		int numz=imageReader.getSizeZ();
		int numt=imageReader.getSizeT();
		int numc=imageReader.getSizeC();
		


		System.out.println("# XYZ "+numx+" "+numy+" "+numz+ " T "+numt+" C "+numc);
		for(Object o:(Set)imageReader.getMetadata().entrySet())
			{
			Map.Entry e=(Map.Entry)o;
			System.out.println("> "+e.getKey()+" "+e.getValue());
			}
		//imageReader.getMetadataValue(e.getValue()));
		
//		VoxelSizeZ
	//  Y, X
		//
		meta=new ImagesetMeta();


		
		
		channelImages.clear();
		if(imageReader.isRGB())
			{
			/////////////// One fat RGB //////////////////////
			for(int channelnum=0;channelnum<numc;channelnum++)
				{
				String channelName="ch"+channelnum;
				ImagesetMeta.Channel mc=meta.getCreateChannelMeta(channelName);
				loadMeta(mc);
	
				//Fill up with image loaders
				Channel c=new Channel(meta.getCreateChannelMeta(channelName));
				channelImages.put(channelName,c);
				for(int framenum=0;framenum<numt;framenum++)
					{
					TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
					for(int slicenum=0;slicenum<numz;slicenum++)
						{
						int effC=0;
						//System.out.println(" "+slicenum+" "+channelnum+" "+framenum);
						loaderset.put(slicenum, c.newImage(imageReader,imageReader.getIndex(slicenum, effC, framenum), channelnum, ""));
						}
					c.imageLoader.put(framenum, loaderset);
					}
				}
			}
		else
			{
			/////////////// Individual gray-scale images //////////////////////
			for(int channelnum=0;channelnum<numc;channelnum++)
				{
				String channelName="ch"+channelnum;
				ImagesetMeta.Channel mc=meta.getCreateChannelMeta(channelName);
				loadMeta(mc);
	
				//Fill up with image loaders
				Channel c=new Channel(meta.getCreateChannelMeta(channelName));
				for(int framenum=0;framenum<numt;framenum++)
					{
					TreeMap<Integer,EvImage> loaderset=new TreeMap<Integer,EvImage>();
					for(int slicenum=0;slicenum<numz;slicenum++)
						{
						//System.out.println(" "+slicenum+" "+channelnum+" "+framenum);
						loaderset.put(slicenum, c.newImage(imageReader,imageReader.getIndex(slicenum, channelnum, framenum), null, ""));
						}
					c.imageLoader.put(framenum, loaderset);
					}
				}
			}
		}

	
	private void loadMeta(ImagesetMeta.Channel mc)
		{
		mc.chBinning=1;
		
		}

	
	
	
	
	/**
	 * Channel - contains methods for building frame database
	 */
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
			return new EvImageExt(null,0,0,"");
			}
		
		
		public EvImageExt newImage(IFormatReader imageReader, int id, Integer subid, String sourceName)
			{
			return new EvImageExt(imageReader,id,subid,sourceName);
			}
		
		private class EvImageExt extends EvImageBioformats
			{
			public EvImageExt(IFormatReader imageReader, int id, Integer subid, String sourceName){super(imageReader,id,subid,sourceName);}
	
			public int getBinning(){return getMeta().chBinning;}
			public double getDispX(){return getMeta().dispX;}
			public double getDispY(){return getMeta().dispY;}
			public double getResX(){return meta.resX;}
			public double getResY(){return meta.resY;}
			}
		
		
		}
	
	}

