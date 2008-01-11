package evplugin.imagesetBasic;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.imageset.*;

public class NamebasedImageset extends Imageset
	{	
	/** Path to imageset */
	private String basedir;
	
	private String fileConvention="";
	private String channelList="";
	private String rebuildLog="";
	
	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 * @param basedir
	 */
	public NamebasedImageset(String basedir)
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
		new NamebasedDatabaseBuilder();
		BasicWindow.updateWindows();
		}
	
	
	public void saveMeta()
		{
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
			channelImages.put(ch, internalMakeChannel(meta.channel.get(ch)));
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
		
		
		public FileConvention()
			{
			setTitle(EV.programName+" Name based Import File Conventions: "+imageset);
			
			//GridBox might be better
			
			JPanel input=new JPanel(new GridLayout(2,1));
			JPanel input1=new JPanel();
			JPanel input2=new JPanel();
			input.add(input1);
			input.add(input2);
			
			eSequence.setPreferredSize(new Dimension(430,20));
			eChannels.setPreferredSize(new Dimension(400,20));
			
			eSequence.setText(fileConvention);
			eChannels.setText(channelList);

			input1.add(new JLabel("Name:"));				input1.add(eSequence);
			input2.add(new JLabel("Channels:"));			input2.add(eChannels);
			
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
				BrowserControl.displayURL(EV.website+"DocsEVconvert");
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
	 * Class for building database. Has to be a class because java has immutable primitives
	 */
	private class NamebasedDatabaseBuilder
		{
		File[] fileList;
		int currentFile=0;
		
		public NamebasedDatabaseBuilder()
			{
			Vector<String> channelVector=new Vector<String>();
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
				File f;
				while((f=nextFile())!=null)
					{
					String filename=f.getName();
					int i=0;
					int j=0;
					int channelNum=0;
					int slice=0;
					int frame=0;
					while(i<fileConvention.length())
						{
						if(j==filename.length())
							break;
						if(fileConvention.charAt(i)=='%')
							{
							char type=fileConvention.charAt(i+1);
							i+=2;
							if(type=='%')
								j++;
							else
								{
								String params=parseInt(filename.substring(j));
								if(params.equals(""))
									{
									JOptionPane.showMessageDialog(null, "Not matching "+filename+" Missing parameter "+type+", filename pos"+j);
									return;
									}
								else
									{
									j+=params.length();
									int parami=Integer.parseInt(params);
									if(type=='C')
										channelNum=parami;
									else if(type=='F')
										frame=parami;
									else if(type=='Z')
										slice=parami;
									else if(type=='#')
										;
									else
										{
										JOptionPane.showMessageDialog(null, "Unknown parameter: "+type);
										return;
										}
									}
								}
							}
						else if(fileConvention.charAt(i)==filename.charAt(j))
							{
							i++;
							j++;
							}
						else
							{
							JOptionPane.showMessageDialog(null, "Not matching: "+filename+" rulepos "+i+" namepos "+j);
							return;
							}
						}
					
					//If everything was matched, continue
					if(j==filename.length())
						{
						String channelName=channelVector.get(channelNum);

						//Get a place to put EV. Create holders if needed
						ChannelImages ch=getChannel(channelName);
						TreeMap<Integer, EvImage> loaders=ch.imageLoader.get(frame);
						if(loaders==null)
							{
							loaders=new TreeMap<Integer, EvImage>();
							ch.imageLoader.put(frame, loaders);
							}
						
						//Plug EV
						loaders.put(slice, ((Channel)ch).newImage(f.getAbsolutePath()));
						rebuildLog+=filename+" Ch: "+channelName+ " Fr: "+frame+" Sl: "+slice+"\n";
						}
					else
						JOptionPane.showMessageDialog(null, "Not matching: "+filename+" Premature end of filename");
					}
				
				
				}
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(null,e.getMessage());
				e.printStackTrace();
				}
			}
	
		

		/** Get the next int */
		private String parseInt(String s)
			{
			String part="";
			int stringpos=0;
			while("1234567890".indexOf(s.charAt(stringpos))>=0)
				{
				part=part+s.charAt(stringpos);
				stringpos++;
				}
			return part;
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
		protected EvImage internalMakeLoader(int frame, int z)
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
		
		private class EvImageExt extends EvImageJAI
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
