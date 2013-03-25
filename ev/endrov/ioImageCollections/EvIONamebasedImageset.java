/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ioImageCollections;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import endrov.core.*;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvIOData;
import endrov.data.EvPath;
import endrov.data.RecentReference;
import endrov.data.gui.DataMenuExtension;
import endrov.data.gui.EvDataGUI;
import endrov.data.gui.EvDataMenu;
import endrov.gui.EvSwingUtil;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.typeImageset.*;
import endrov.util.EvBrowserUtil;
import endrov.util.math.EvDecimal;

/**
 * Import a list of images by matching the names
 * @author Johan Henriksson
 *
 */
public class EvIONamebasedImageset implements EvIOData
	{
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
	private double resZ=1;
	
	
	/**
	 * Create a new recording. Basedir points to imageset- ie without the channel name
	 */
	public EvIONamebasedImageset(EvData data, File basedir)
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
		EvBasicWindow.updateWindows();
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
		
		private JTextField eSequence=new JTextField("foo-%W-%C-%F-%Z.jpg");
		private JTextField eChannels=new JTextField("chan1,chan2");
		private JTextArea eLog=new JTextArea();

		private JTextField eResX=new JTextField("1");
		private JTextField eResY=new JTextField("1");
		private JTextField eSpacingZ=new JTextField("1");

		
		
		public FileConvention()
			{
			setTitle(EndrovCore.programName+" Name based Import File Conventions");
			
			JPanel input=new JPanel(new GridLayout(7,1));
			input.add(new JLabel(basedir.toString()));
			input.add(EvSwingUtil.withLabel("Name:",eSequence));
			input.add(new JLabel("Name is case-sensitive!"));
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
				EvBrowserUtil.displayURL(EndrovCore.websiteWikiPrefix+"Importing_collections_of_images");
			else if(e.getSource()==bRebuild)
				{
				fileConvention=eSequence.getText();
				channelList=eChannels.getText();
				resX=Double.parseDouble(eResX.getText());
				resY=Double.parseDouble(eResY.getText());
				resZ=Double.parseDouble(eSpacingZ.getText());
				NamebasedDatabaseBuilder b=new NamebasedDatabaseBuilder();
				b.run(data);
				eLog.setText(b.rebuildLog.toString());
				EvBasicWindow.updateWindows();
				}
			}
		}
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private static class FileInfo
		{
		File f;
		int channelNum=0;
		int slice=0;
		int frame=0;
		String well=null;
		}
		
	/**
	 * Class for building database. Has to be a class because java has immutable primitives
	 */
	private class NamebasedDatabaseBuilder
		{
		File[] fileList;
		int currentFile=0;
		private StringBuffer rebuildLog=new StringBuffer();
		
		private int countFilesAdded=0;
		
		private Integer minZ=null;
		
		public void run(EvData data)
			{
			minZ=null;
			Vector<String> channelVector=new Vector<String>();
			try
				{
				File dir=basedir;
				fileList=dir.listFiles();

				//Parse list of channels into vector
				StringTokenizer ctok=new StringTokenizer(channelList,",");
				while(ctok.hasMoreTokens())
					channelVector.add(ctok.nextToken());

				/*
				Imageset im;
				Collection<Imageset> ims=data.getObjects(Imageset.class);
				if(!ims.isEmpty())
					im=ims.iterator().next();
				else
					{
					im=new Imageset();
					data.metaObject.put("im", im);
					}

				//Remove all channels
				for(String s:im.getChannels().keySet())
					im.metaObject.remove(s);
*/
				
				//Get the imageset and clean it up
				for(EvPath p:data.getIdObjectsRecursive(Imageset.class).keySet())
					p.getParent().getObject().removeMetaObjectByValue(p.getObject());
				

				//Go through list of files, just to see what there is
				List<FileInfo> files=new LinkedList<FileInfo>();
				File f;
				currentFile=0;
				minZ=null;
				while((f=nextFile())!=null)
					{
					FileInfo info=parse(f);
					if(info!=null)
						files.add(info);
					}
				
				//Add all the files
				for(FileInfo info:files)
					buildAddFile(data,info,channelVector);
				}
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(null,"Error rebuilding: "+e.getMessage());
				e.printStackTrace();
				}
			
			rebuildLog.append("Total images identified: "+countFilesAdded);
			}
		
		
		/**
		 * Parse out information from filename. Return info if parse successful
		 */
		private FileInfo parse(File f) throws Exception
			{
			FileInfo info=new FileInfo();
			info.f=f;
			String filename=f.getName();
			int i=0;
			int j=0;
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
					else if(type=='W')
						{
						StringBuilder sb=new StringBuilder();
						
						while(j<filename.length())
							{
							char c=filename.charAt(j);
							if(Character.isLetter(c) || Character.isDigit(c))
								{
								sb.append(c);
								j++;
								}
							else
								break;
							}
						info.well=sb.toString();
						if(info.well.length()==0)
							{
							rebuildLog.append("Not matching "+filename+" Missing parameter "+type+", filename pos"+j+"\n");
							return null;
							}
						}
					else
						{
						String params=parseInt(filename.substring(j));
						if(params.equals(""))
							{
							rebuildLog.append("Not matching "+filename+" Missing parameter "+type+", filename pos"+j+"\n");
							return null;
							}
						else
							{
							j+=params.length();
							int parami=Integer.parseInt(params);
							if(type=='C')
								info.channelNum=parami;
							else if(type=='F')
								info.frame=parami;
							else if(type=='Z')
								info.slice=parami;
							else if(type=='#')
								;
							else
								{
								rebuildLog.append("Unknown parameter: "+type+"\n");
								return null;
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
					rebuildLog.append("Not matching: "+filename+" rulepos "+i+" namepos "+j+"\n");
					return null;
					}
				}
			
			//If everything was matched, continue
			if(j==filename.length())
				{
				//Keep track of smallest Z seen
				if(minZ==null || info.slice<minZ)
					minZ=info.slice;
				
				return info;
				}
			else
				{
				rebuildLog.append("Not matching: "+filename+" Premature end of filename\n");
				return null;
				}
			}
		
		/**
		 * Add file to channels
		 */
		private void buildAddFile(EvContainer con, FileInfo info, List<String> channelVector) throws Exception
			{
			if(info.channelNum>=channelVector.size())
				throw new Exception("For "+info.f+", no channel for index "+info.channelNum+". Note that channels start counting from 0.\n"
						+"If your channels start from 1 then give the first channel 0 an arbitrary name, it will not be used.");

			
			String channelName=channelVector.get(info.channelNum);

			//Get the right well
			String nameImageset=info.well;
			if(nameImageset==null)
				nameImageset="im";
			Imageset im=(Imageset)con.getChild(nameImageset);
			if(im==null)
				con.putChild(nameImageset,im=new Imageset());

			
			//Get a place to put EVimage. Create holders if needed
			EvChannel ch=im.getCreateChannel(channelName);
			System.out.println(ch);
			EvStack stack=ch.getStack(new EvDecimal(info.frame));
			if(stack==null)
				{
				stack=new EvStack();
				ch.putStack(new EvDecimal(info.frame), stack);
				}
			
			//Plug EVimage
			EvImagePlane evim=new EvImagePlane();
			stack.setRes(resX,resY,resZ);
			evim.io=new EvSingleImageFileReader(info.f);
			
			stack.putPlane(info.slice-minZ, evim);
			
			String filename=info.f.getName();
			String newLogEntry=filename+" Ch: "+channelName+ " Fr: "+info.frame+" Sl: "+info.slice+"\n";
			System.out.println(newLogEntry);
			rebuildLog.append(newLogEntry);
			countFilesAdded++;
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
	

	public void close() throws IOException
		{
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvDataMenu.addExtensions(new DataMenuExtension()
			{
			public void buildData(JMenu menu)
				{
				
				}
			public void buildOpen(JMenu menu)
				{
				final JMenuItem miLoadNamebasedImageset=new JMenuItem("Load namebased imageset");
				miLoadNamebasedImageset.setIcon(BasicIcon.iconMenuLoad);
				EvBasicWindow.addMenuItemSorted(menu,miLoadNamebasedImageset,"data_open_namebased");
				
				
				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						File filename=EvBasicWindow.openDialogChooseDir();
						if(filename!=null)
				    	{
				    	EvData data=new EvData();
				    	data.io=new EvIONamebasedImageset(data,filename);
				    	EvDataGUI.registerOpenedData(data);
				    	}
						}
					};
					
				miLoadNamebasedImageset.addActionListener(listener);
				}
			
			public void buildSave(JMenu menu, final EvData meta)
				{
				if(meta.io instanceof EvIONamebasedImageset)
					{
					JMenuItem miSetup=new JMenuItem("Setup");
					menu.add(miSetup);
					miSetup.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{((EvIONamebasedImageset)meta.io).setup();}
						});	
					}
				}
			});
		
		}
		
	
	}
