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

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.DataMenuExtension;
import endrov.data.EvData;
import endrov.data.EvDataMenu;
import endrov.data.EvIOData;
import endrov.data.RecentReference;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;

/**
 * Import a list of images by matching the names
 * @author Johan Henriksson
 *
 */
public class NamebasedImageset implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			public void buildData(JMenu menu)
				{
				
				}
			public void buildOpen(JMenu menu)
				{
				final JMenuItem miLoadNamebasedImageset=new JMenuItem("Load namebased imageset");
				miLoadNamebasedImageset.setIcon(BasicIcon.iconMenuLoad);
				BasicWindow.addMenuItemSorted(menu,miLoadNamebasedImageset,"data_open_namebased");
				
				
				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						JFileChooser chooser = new JFileChooser();
				    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    chooser.setCurrentDirectory(EvData.getLastDataPath());
				    int returnVal = chooser.showOpenDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION)
				    	{
				    	File filename=chooser.getSelectedFile();
				    	EvData.setLastDataPath(filename.getParentFile());
				    	
				    	EvData data=new EvData();
				    	data.io=new NamebasedImageset(data,filename);
				    	EvData.registerOpenedData(data);
				    	}
						}
					};
					
				miLoadNamebasedImageset.addActionListener(listener);
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
							{((NamebasedImageset)meta.io).setup();}
						});	
					}
				}
			});
		
		}
	
	
	/******************************************************************************************************
	 *                               Static: Loading                                                      *
	 *****************************************************************************************************/

	
	/** Path to imageset */
	private File basedir;
	private EvData data;
	
	private String fileConvention="";
	private String channelList="";
	private double resX=1;
	private double resY=1;
	private EvDecimal spacingZ=EvDecimal.ONE;
	
	
	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 */
	public NamebasedImageset(EvData data, File basedir)
		{
		this.data=data;
		this.basedir=basedir;
		setup();
		}

	public String toString()
		{
		return getMetadataName();
		}
	
	
	public File datadir()
		{
		return basedir;
		}

	/**
	 * Go through all files and put in database
	 */
	public void buildDatabase(EvData d)
		{
		NamebasedDatabaseBuilder b=new NamebasedDatabaseBuilder();
		b.run(d);
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
	 * Dialog for selecting sequence of files
	 */
	public class FileConvention extends JFrame implements ActionListener
		{
		static final long serialVersionUID=0;
		
		private JButton bRebuild=new JButton("Rebuild database");
		private JButton bSyntax=new JButton("Website");
		
		private JTextField eSequence=new JTextField("foo-%C-%F-%Z.jpg");
		private JTextField eChannels=new JTextField("chan1,chan2");
		private JTextArea eLog=new JTextArea();

		private JTextField eResX=new JTextField("1");
		private JTextField eResY=new JTextField("1");
		private JTextField eSpacingZ=new JTextField("1");

		
		
		public FileConvention()
			{
			setTitle(EV.programName+" Name based Import File Conventions");
			
			JPanel input=new JPanel(new GridLayout(6,1));
			input.add(new JLabel(basedir.toString()));
			input.add(EvSwingUtil.withLabel("Name:",eSequence));
			input.add(EvSwingUtil.withLabel("Channels:",eChannels));

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
				BrowserControl.displayURL(EV.website+"Plugin_ImagesetBasic");
			else if(e.getSource()==bRebuild)
				{
				fileConvention=eSequence.getText();
				channelList=eChannels.getText();
				resX=Double.parseDouble(eResX.getText());
				resY=Double.parseDouble(eResY.getText());
				spacingZ=new EvDecimal(eSpacingZ.getText());
				NamebasedDatabaseBuilder b=new NamebasedDatabaseBuilder();
				b.run(data);
				eLog.setText(b.rebuildLog);
				BasicWindow.updateWindows();
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
		private String rebuildLog="";
		
		
		public void run(EvData data)
			{
			Vector<String> channelVector=new Vector<String>();
			try
				{
				rebuildLog="";				
				
				File dir=basedir;
				fileList=dir.listFiles();

				//Parse list of channels into vector
				StringTokenizer ctok=new StringTokenizer(channelList,",");
				while(ctok.hasMoreTokens())
					channelVector.add(ctok.nextToken());

				//Get the imageset and clean it up
				Imageset im;
				Collection<Imageset> ims=data.getObjects(Imageset.class);
				if(!ims.isEmpty())
					im=ims.iterator().next();
				else
					{
					im=new Imageset();
					data.metaObject.put("im", im);
					}
				
				//Create channels, remove unneeded (for rebuild)
				for(String s:im.getChannels().keySet())
					{
					if(!channelVector.contains(s))
						im.metaObject.remove(s);
					}
				//im.channelImages.keySet().retainAll(channelVector);
				for(String cname:channelVector)
					im.getCreateChannel(cname);
				/*
				//Clear up old database
				List<String> channelsToRemove=new LinkedList<String>(); 
				for(Map.Entry<String, ChannelImages> entry:im.channelImages.entrySet())
					{
					entry.getValue().imageLoader.clear();
					boolean exists=false;
					for(String channelName:channelVector)
						if(channelName.equals(entry.getKey()))
							exists=true;
					if(!exists)
						channelsToRemove.add(entry.getKey());
					}
				for(String s:channelsToRemove)
					im.channelImages.remove(s);
				*/
				
				//Go through list of files
				File f;
				while((f=nextFile())!=null)
					buildAddFile(im,f,channelVector);
				}
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(null,e.getMessage());
				e.printStackTrace();
				}
			}
	
		private void buildAddFile(Imageset im, File f, Vector<String> channelVector) throws Exception
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
							rebuildLog+="Not matching "+filename+" Missing parameter "+type+", filename pos"+j+"\n";
//							JOptionPane.showMessageDialog(null, "Not matching "+filename+" Missing parameter "+type+", filename pos"+j);
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
								rebuildLog+="Unknown parameter: "+type+"\n";
//								JOptionPane.showMessageDialog(null, "Unknown parameter: "+type);
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
					rebuildLog+="Not matching: "+filename+" rulepos "+i+" namepos "+j+"\n";
//					JOptionPane.showMessageDialog(null, "Not matching: "+filename+" rulepos "+i+" namepos "+j);
					return;
					}
				}
			
			//If everything was matched, continue
			if(j==filename.length())
				{
				if(channelNum>=channelVector.size())
					throw new Exception("No channel for index "+channelNum+". Note that channels start counting from 0.\n"
							+"If your channels start from 1 then give the first channel 0 an arbitrary name, it will not be used.");
				String channelName=channelVector.get(channelNum);

				//Get a place to put EVimage. Create holders if needed
				EvChannel ch=im.getCreateChannel(channelName);
				EvStack stack=ch.imageLoader.get(new EvDecimal(frame));
				if(stack==null)
					{
					stack=new EvStack();
					ch.imageLoader.put(new EvDecimal(frame), stack);
					}
				
				//Plug EVimage
				EvImage evim=new EvImage();
				stack.dispX=0;
				stack.dispY=0;
				//stack.binning=1;
				stack.resX=resX; 
				stack.resY=resY;
				
				evim.io=new BasicSliceIO(f);
				
				EvDecimal realSlice=new EvDecimal(slice).multiply(spacingZ);
				
				stack.put(realSlice, evim); 
				String newLogEntry=filename+" Ch: "+channelName+ " Fr: "+frame+" Sl: "+slice+"\n";
				System.out.println(newLogEntry);
				rebuildLog+=newLogEntry;
				}
			else
				{
				rebuildLog+="Not matching: "+filename+" Premature end of filename\n";
//				JOptionPane.showMessageDialog(null, "Not matching: "+filename+" Premature end of filename");
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



	public String getMetadataName()
		{
		return basedir.getName();
		}

	public void saveData(EvData d, EvData.FileIOStatusCallback cb)
		{
		JOptionPane.showMessageDialog(null, "This image format does not support saving. Convert to e.g. OST instead");
		}
	
	
	
	}
