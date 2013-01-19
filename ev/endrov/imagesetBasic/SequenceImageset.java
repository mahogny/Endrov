/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetBasic;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import endrov.core.*;
import endrov.data.DataMenuExtension;
import endrov.data.EvData;
import endrov.data.EvDataMenu;
import endrov.data.EvIOData;
import endrov.data.RecentReference;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.BasicWindow;
import endrov.imageset.*;
import endrov.util.EvBrowserUtil;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;


//bug: new does not halt code.

/**
 * 
 * @author Johan Henriksson
 */
public class SequenceImageset implements EvIOData
	{	
	/** Path to imageset */
	private File basedir;
	private double resX=1;
	private double resY=1;
	private double resZ=1;
	
	private String fileConvention="";
	private String channelList="";
	private String rebuildLog="";
	
	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 * @param basedir
	 */
	public SequenceImageset(EvData data, File basedir)
		{
		this.basedir=basedir;
		setup(data);
		}

	public String toString()
		{
		return getMetadataName();
		}

	
	public File datadir()
		{
		return basedir.getParentFile();
		}

	/**
	 * Go through all files and put in database
	 */
	public void buildDatabase(EvData data)
		{
		new SequenceDatabaseBuilder(data);
		BasicWindow.updateWindows();
		}
	
	
	public void saveData(EvData data, EvData.FileIOStatusCallback cb)
		{
		JOptionPane.showMessageDialog(null, "This image format does not support saving. Convert to e.g. OST instead");
		}
	
	public RecentReference getRecentEntry()
		{
		return null;
		}
	
	/**
	 * Imageset specific settings
	 */
	public void setup(EvData data)
		{
		new FileConvention(data);
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
		private EvData data;

		private JTextField eResX=new JTextField("1");
		private JTextField eResY=new JTextField("1");
		private JTextField eSpacingZ=new JTextField("1");

		
		public FileConvention(EvData data)
			{
			setTitle(EndrovCore.programName+" Sequence Import File Conventions: "+basedir.getName());
			this.data=data;
			
			//GridBox might be better
			
			JPanel input=new JPanel(new GridLayout(6,1));
			input.add(EvSwingUtil.withLabel("Sequence:",eSequence));
			input.add(EvSwingUtil.withLabel("Channels:", eChannels));

			input.add(EvSwingUtil.withLabel("Resolution X [px/um]:",eResX));
			input.add(EvSwingUtil.withLabel("Resolution Y [px/um]:",eResY));
			input.add(EvSwingUtil.withLabel("Spacing Z [um/plane]:",eSpacingZ));

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
				EvBrowserUtil.displayURL(EndrovCore.websiteWikiPrefix+"Plugin_ImagesetBasic");
			else if(e.getSource()==bRebuild)
				{
				fileConvention=eSequence.getText();
				channelList=eChannels.getText();
				resX=Double.parseDouble(eResX.getText());
				resY=Double.parseDouble(eResY.getText());
				resZ=Double.parseDouble(eSpacingZ.getText());
				buildDatabase(data);
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
		
		public SequenceDatabaseBuilder(EvData data)
			{
			try
				{
				rebuildLog="";				
				
//				File dir=basedir;
				fileList=basedir.listFiles();
				
				//Parse list of channels into vector
				StringTokenizer ctok=new StringTokenizer(channelList,",");
				while(ctok.hasMoreTokens())
					channelVector.add(ctok.nextToken());
				
				Collection<Imageset> ims=data.getObjects(Imageset.class);
				Imageset im=null;
				if(!ims.isEmpty())
					im=ims.iterator().next();
				else
					{
					im=new Imageset();
					data.metaObject.put("im", im);
					}
					
				//Clear up old database
				for(String s:im.getChannels().keySet())
					{
					if(!channelVector.contains(s))
						im.metaObject.remove(s);
					}
				//im.channelImages.keySet().retainAll(channelVector);
				
				/*
				for(Map.Entry<String, ChannelImages> entry:im.channelImages.entrySet())
					{
					entry.getValue().imageLoader.clear();
					boolean exists=false;
					for(String channelName:channelVector)
						if(channelName.equals(entry.getKey()))
							exists=true;
					if(!exists)
						im.channelImages.remove(entry.getKey());
					}
				*/
				
				//Go through list of files
				build(im,true);
				}
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(null,e.getMessage());
				e.printStackTrace();
				}
			}
	
		
		
		/** Main parser */
		private void build(Imageset imset, boolean toplevel) throws Exception
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
						build(imset,false);
						}
					stringpos++; //)
					build(imset,toplevel);
					}
				else if(firstChar==',')
					{
					stringpos++;
					build(imset,toplevel);
					}
				else if(firstChar==')')
					return;
				else if(firstChar=='s')
					{
					//Stack of images
					stringpos++; //s

					int channelNum=0;
					int skipSlices=1;
					int skipFirstSlices=0;
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
						else if(paramchar=='f')
							{
							stringpos++;
							skipFirstSlices=parseInt();
							}
						else if(paramchar=='i')
							{
							stringpos++;
							frameForward=parseInt();
							}
						else if(firstChar=='n')
							{
							//Number of slices
							stringpos++;
							numSlices=parseInt();
							build(imset,toplevel);
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
						EvStack stack=new EvStack();
						stack.setRes(resX,resY,resZ);
						int outi=0;
						for(int i=skipFirstSlices;i<numSlices;i+=skipSlices, outi++)
//							loaders.put(i, new EvImageJubio(f.getAbsolutePath(),i));
							{
							EvImage evim=new EvImage();
							
							evim.io=new BasicSliceIO(f,i);
							//TODO is this the way to go? only works with TIFF stacks
							
							stack.putInt(outi, evim); 
							}
						
						EvChannel ch=imset.getCreateChannel(channelName);
						ch.putStack(new EvDecimal(frame), stack);
						rebuildLog+=f.getName()+" Ch: "+channelName+ " Fr: "+frame+" #slcs: "+numSlices+" skip: "+skipSlices+"\n";
						}
					frame+=frameForward;
					
					build(imset,toplevel);
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
				part+=fileConvention.charAt(stringpos);
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

	
	public String getMetadataName()
		{
		return basedir.getName();
		}



	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
	//	BasicWindow.addBasicWindowExtension(new Basic());
		
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			public void buildData(JMenu menu)
				{
				}
			public void buildOpen(JMenu menu)
				{
				JMenuItem miLoadSequenceImageset=new JMenuItem("Load sequence imageset");

				
				miLoadSequenceImageset.setIcon(BasicIcon.iconMenuLoad);
				BasicWindow.addMenuItemSorted(menu,miLoadSequenceImageset,"data_open_namebased");
				
				
				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						dialogSequenceLoadImageset();
						}
					
					/**
					 * Show dialog for opening a new sequence based imageset
					 */
					public void dialogSequenceLoadImageset()
						{
						JFileChooser chooser = new JFileChooser();
				    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    chooser.setCurrentDirectory(EvData.getLastDataPath());
				    int returnVal = chooser.showOpenDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION)
				    	{
				    	File filename=chooser.getSelectedFile();
				    	EvData.setLastDataPath(chooser.getSelectedFile().getParentFile());
				    	EvData data=new EvData();
				    	SequenceImageset io=new SequenceImageset(data,filename);
				    	data.io=io;
				    	EvData.registerOpenedData(data);
				    	}
						}
					
					
				
					};
					
				miLoadSequenceImageset.addActionListener(listener);
				}
			
			public void buildSave(JMenu menu, final EvData meta)
				{
				if(meta.io instanceof SequenceImageset)
					{
					JMenuItem miSetup=new JMenuItem("Setup");
					menu.add(miSetup);
					miSetup.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{((SequenceImageset)meta.io).setup(meta);}
						});	
					}
				}
			});
		
		}
	
	
	
	
	}
