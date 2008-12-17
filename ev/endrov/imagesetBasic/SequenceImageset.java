package endrov.imagesetBasic;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import endrov.basicWindow.BasicWindow;
import endrov.data.DataMenuExtension;
import endrov.data.EvData;
import endrov.data.EvDataMenu;
import endrov.data.RecentReference;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.imagesetBasic.NamebasedImageset;
import endrov.imagesetBasic.SequenceImageset;
import endrov.util.EvDecimal;


//bug: new does not halt code.

/**
 * 
 * @author Johan Henriksson
 */
public class SequenceImageset extends Imageset
	{	
	public static void initPlugin() {}
	static
		{
	//	BasicWindow.addBasicWindowExtension(new Basic());
		
		EvDataMenu.extensions.add(new DataMenuExtension()
			{

			public void buildOpen(JMenu menu)
				{
				final JMenuItem miLoadSequenceImageset=new JMenuItem("Load sequence imageset");
				final JMenuItem miLoadNamebasedImageset=new JMenuItem("Load namebased imageset");
				addMetamenu(menu,miLoadSequenceImageset);
				addMetamenu(menu,miLoadNamebasedImageset);
				
				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						if(e.getSource()==miLoadSequenceImageset)
							dialogSequenceLoadImageset();
						else if(e.getSource()==miLoadNamebasedImageset)
							dialogNamebasedLoadImageset();
						}
					
					/**
					 * Show dialog for opening a new sequence based imageset
					 */
					public void dialogSequenceLoadImageset()
						{
						JFileChooser chooser = new JFileChooser();
				    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    chooser.setCurrentDirectory(new File(EvData.getLastDataPath()));
				    int returnVal = chooser.showOpenDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION)
				    	{
				    	String filename=chooser.getSelectedFile().getAbsolutePath();
				    	EvData.setLastDataPath(chooser.getSelectedFile().getParent());
				    	EvData.addMetadata(new SequenceImageset(filename));
				    	}
						}
					
					
					/**
					 * Show dialog for opening a new sequence based imageset
					 */
					public void dialogNamebasedLoadImageset()
						{
						JFileChooser chooser = new JFileChooser();
				    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    chooser.setCurrentDirectory(new File(EvData.getLastDataPath()));
				    int returnVal = chooser.showOpenDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION)
				    	{
				    	String filename=chooser.getSelectedFile().getAbsolutePath();
				    	EvData.setLastDataPath(chooser.getSelectedFile().getParent());
				    	EvData.addMetadata(new NamebasedImageset(filename));
				    	}
						}
					};
					
				miLoadNamebasedImageset.addActionListener(listener);
				miLoadSequenceImageset.addActionListener(listener);
				}
			
			public void buildSave(JMenu menu, final EvData meta)
				{
				if(meta.io instanceof NamebasedImageset)
					{
					JMenuItem miSetup=new JMenuItem("Setup");
					menu.add(miSetup);
					miSetup.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{((NamebasedImageset)meta).setup();}
						});	
					}
				else if(meta.io instanceof SequenceImageset)
					{
					JMenuItem miSetup=new JMenuItem("Setup");
					menu.add(miSetup);
					miSetup.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{((SequenceImageset)meta).setup();}
						});	
					}
				}
			});
		
		}
	
	
	
	
	
	
	
	/** Path to imageset */
	private String basedir;
	
	private String fileConvention="";
	private String channelList="";
	private String rebuildLog="";
	
	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 * @param basedir
	 */
	public SequenceImageset(String basedir)
		{
		this.basedir=basedir;
		this.imageset=(new File(basedir)).getName();
		setup();
		}

	public String toString()
		{
		return getMetadataName();
		}

	
	public File datadir(){return null;}

	/**
	 * Go through all files and put in database
	 */
	public void buildDatabase()
		{
		new SequenceDatabaseBuilder();
		BasicWindow.updateWindows();
		}
	
	
	public void saveMeta()
		{
		}
	
	public RecentReference getRecentEntry()
		{
		return null;
		}
	
	/**
	 * Imageset specific settings
	 */
	public void setup()
		{
		new FileConvention();
		}
	
	/**
	 * Get a channel or create it if it does not exist
	 * TODO RENAME, OVERRIDES A METHOD IN A STUPID WAY
	 */
	public Imageset.ChannelImages getChannel(String ch)
		{
		if(!channelImages.containsKey(ch))
			channelImages.put(ch, internalMakeChannel(meta.channelMeta.get(ch)));
		return channelImages.get(ch);
		}
	

	/**
	 * Dialog for selecting sequence of files
	 */
	public class FileConvention extends JFrame implements ActionListener
		{
		static final long serialVersionUID=0;
		
		private JButton bRebuild=new JButton("Rebuild database");
		private JButton bSyntax=new JButton("Syntax");
		
		private JTextField eSequence=new JTextField();
		private JTextField eChannels=new JTextField();
		private JTextArea eLog=new JTextArea();
		
		
		/**
		 * Embed control with a label
		 */
		private JComponent withLabel(String text, JComponent right)
			{
			JPanel p=new JPanel(new BorderLayout());
			p.add(new JLabel(text),BorderLayout.WEST);
			p.add(right,BorderLayout.CENTER);
			return p;
			}
		
		public FileConvention()
			{
			setTitle(EV.programName+" Sequence Import File Conventions: "+imageset);
			
			//GridBox might be better
			
			JPanel input=new JPanel(new GridLayout(2,1));
			input.add(withLabel("Sequence:",eSequence));
			input.add(withLabel("Channels:", eChannels));
			
			
			
			eSequence.setPreferredSize(new Dimension(430,20));
			eChannels.setPreferredSize(new Dimension(400,20));

			eSequence.setText(fileConvention);
			eChannels.setText(channelList);

			
			JPanel bp=new JPanel(new GridLayout(1,2));
			bp.add(bRebuild);
			bp.add(bSyntax);
			
			JPanel left=new JPanel(new BorderLayout());
			left.add(input,BorderLayout.NORTH);
			left.add(bp,BorderLayout.SOUTH);

			setLayout(new GridLayout(1,2));
			add(left);
			add(new JScrollPane(eLog,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
			eLog.setEditable(false);
			
			bRebuild.addActionListener(this);
			bSyntax.addActionListener(this);
			
			
			
			pack();
			setBounds(0, 100, 1000, 400);
			setVisible(true);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			}
		
		
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bSyntax)
				BrowserControl.displayURL(EV.website+"Plugin_ImagesetBasic");
			else if(e.getSource()==bRebuild)
				{
				fileConvention=eSequence.getText();
				channelList=eChannels.getText();
				buildDatabase();
				eLog.setText(rebuildLog);
				}
			}
		}
	

	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	/**
	 * Class for building database from sequences. Has to be a class because java has immutable primitives
	 */
	private class SequenceDatabaseBuilder
		{
		File[] fileList;
		Vector<String> channelVector=new Vector<String>();
		int currentFile=0;
		int frame=0;
		int stringpos=0;
		int numSlices=0;
		
		public SequenceDatabaseBuilder()
			{
			try
				{
				rebuildLog="";				
				
				File dir=new File(basedir);
				fileList=dir.listFiles();
				
				//Parse list of channels into vector
				StringTokenizer ctok=new StringTokenizer(channelList,",");
				while(ctok.hasMoreTokens())
					channelVector.add(ctok.nextToken());
				
				//Clear up old database
				for(ChannelImages ch:channelImages.values())
					{
					ch.imageLoader.clear();
					boolean exists=false;
					for(String channelName:channelVector)
						if(channelName.equals(ch.getMeta().name))
							exists=true;
					if(!exists)
						channelImages.remove(ch.getMeta().name);
					}
				
				//Go through list of files
				build(true);
				}
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(null,e.getMessage());
				e.printStackTrace();
				}
			}
	
		
		
		/** Main parser */
		private void build(boolean toplevel) throws Exception
			{
			if(stringpos==fileConvention.length())
				{
				if(!toplevel)
					throw new Exception("Missing )");
				}
			else
				{
				char firstChar=fileConvention.charAt(stringpos);
				if(firstChar=='r')
					{
					stringpos++; //r
					int numRepeat=parseInt();
					//EV.printDebug("r"+numRepeat);
					stringpos++; //(
					int savedStringPos=stringpos;
					for(int i=0;i<numRepeat;i++)
						{
						stringpos=savedStringPos;
						build(false);
						}
					stringpos++; //)
					build(toplevel);
					}
				else if(firstChar==',')
					{
					stringpos++;
					build(toplevel);
					}
				else if(firstChar==')')
					return;
				else if(firstChar=='n')
					{
					//Number of slices
					stringpos++;
					numSlices=parseInt();
					build(toplevel);
					}
				else if(firstChar=='s')
					{
					//Stack of images
					stringpos++; //s

					int channelNum=0;
					int skipSlices=1;
					int frameForward=0;

					while(stringpos<fileConvention.length())// && fileConvention.charAt(stringpos)!=',' && fileConvention.charAt(stringpos)!=')')
						{
						if(stringpos>=fileConvention.length())
							break;
						int paramchar=fileConvention.charAt(stringpos);
						if(paramchar=='c')
							{
							stringpos++;
							channelNum=parseInt();
							}
						else if(paramchar=='k')
							{
							stringpos++;
							skipSlices=parseInt();
							}
						else if(paramchar=='i')
							{
							stringpos++;
							frameForward=parseInt();
							}
						else
							break; //Cannot identify more parameters
						}
					
					String channelName=channelVector.get(channelNum);
					File f=nextFile();
					if(f==null)
						return;
					else
						{
						TreeMap<EvDecimal, EvImage> loaders=new TreeMap<EvDecimal, EvImage>();
						Channel ch=(Channel)getChannel(channelName);
						for(int i=0;i<numSlices;i+=skipSlices)
//							loaders.put(i, new EvImageJubio(f.getAbsolutePath(),i));
							loaders.put(new EvDecimal(i), ch.newImage(f.getAbsolutePath(),i)); //TODO bd set resolution
						
						
						ch.imageLoader.put(new EvDecimal(frame), loaders); //TODO bd need to set res at loading
						rebuildLog+=f.getName()+" Ch: "+channelName+ " Fr: "+frame+" #slcs: "+numSlices+" skip: "+skipSlices+"\n";
						}
					frame+=frameForward;
					
					build(toplevel);
					}
				else
					{
					fileConvention.charAt(stringpos);
					
					String firstPart=fileConvention.substring(0,stringpos);
					String lastPart=fileConvention.substring(stringpos+1);
					
					throw new Exception("Could not parse: "+firstPart+" >>>"+firstChar+"<<< "+lastPart);
					}
				}
			}

		

		/** Get the next int */
		private int parseInt() throws Exception
			{
			String part="";
			while("1234567890".indexOf(fileConvention.charAt(stringpos))>=0)
				{
				part=part+fileConvention.charAt(stringpos);
				stringpos++;
				}
			if(part=="")
				throw new Exception("Integer expected");
			return Integer.parseInt(part);
			}
		
		/** Get the next file to assign frame/slice */
		private File nextFile()
			{
			if(currentFile<fileList.length)
				{
				File f=fileList[currentFile];
				currentFile++;
				if(!f.getName().startsWith("."))
					return f;
				else
					return nextFile();
				}
			else
				return null;
			}
		
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
			return new EvImageExt("");
			}
		
		public EvImageExt newImage(String filename)
			{
			return new EvImageExt(filename);
			}
		public EvImageExt newImage(String filename, int slice)
			{
			return new EvImageExt(filename, slice);
			}
		
		private class EvImageExt extends EvImageJAI_OLD
			{
			public EvImageExt(String filename){super(filename);}
			public EvImageExt(String filename, int z){super(filename,z);}
			public int getBinning(){return getMeta().chBinning;}
			public double getDispX(){return getMeta().dispX;}
			public double getDispY(){return getMeta().dispY;}
			public double getResX(){return meta.resX;}
			public double getResY(){return meta.resY;}
			}
		
		}
	
	}
