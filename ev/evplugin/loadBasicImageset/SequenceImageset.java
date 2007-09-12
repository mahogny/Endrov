package evplugin.loadBasicImageset;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.ev.*;
import evplugin.imageset.*;
import evplugin.jubio.ImageLoaderJubio;

public class SequenceImageset extends Imageset
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
	public SequenceImageset(String basedir)
		{
		this.basedir=basedir;
		this.imageset=(new File(basedir)).getName();
		setup();
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
	
	/**
	 * Imageset specific settings
	 */
	public void setup()
		{
		new FileConvention();
		}
	
	/**
	 * Get a channel or create it if it does not exist
	 */
	public Imageset.ChannelImages getChannel(String ch)
		{
		if(!channelImages.containsKey(ch))
			channelImages.put(ch, new Imageset.ChannelImages(meta.channel.get(ch)));
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
			setTitle(EV.programName+" Sequence Import File Conventions: "+imageset);
			
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

			input1.add(new JLabel("Sequence:"));			input1.add(eSequence);
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
						TreeMap<Integer, ImageLoader> loaders=new TreeMap<Integer, ImageLoader>();
						for(int i=0;i<numSlices;i+=skipSlices)
							loaders.put(i, new ImageLoaderJubio(f.getAbsolutePath(),i));
						ChannelImages ch=getChannel(channelName);
						ch.imageLoader.put(frame, loaders);
						rebuildLog+=f.getName()+" Ch: "+channelName+ " Fr: "+frame+" #slcs: "+numSlices+" skip: "+skipSlices+"\n";
						}
					frame+=frameForward;
					
					build(toplevel);
					}
				else
					throw new Exception("Could not parse: "+firstChar);
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

	}
