/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ioImageCollections;

import javax.swing.*;

import org.apache.commons.io.FilenameUtils;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.in.TiffReader;

import endrov.core.*;
import endrov.core.log.EvLog;
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
import endrov.util.io.EvFileUtil;
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

	boolean filesAsStacks=false;
	

	
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

		
		private JCheckBox cbFilesAsStacks=new JCheckBox("Interpret files as stacks");
		
		
		public FileConvention()
			{
			setTitle(EndrovCore.programName+" Name based Import File Conventions");
			
			JPanel input=new JPanel(new GridLayout(8,1));
			input.add(new JLabel(basedir.toString()));
			input.add(EvSwingUtil.withLabel("Name:",eSequence));
			input.add(new JLabel("Name is case-sensitive!"));
			input.add(EvSwingUtil.withLabel("Channels:",eChannels));

			input.add(EvSwingUtil.withLabel("Resolution X [px/um]:",eResX));
			input.add(EvSwingUtil.withLabel("Resolution Y [px/um]:",eResY));
			input.add(EvSwingUtil.withLabel("Spacing Z [um/plane]:",eSpacingZ));
			
			input.add(cbFilesAsStacks);
			
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
				filesAsStacks=cbFilesAsStacks.isSelected();
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
		String dataset;
		}
		
	/**
	 * Class for building database. Has to be a class because java has immutable primitives
	 */
	private class NamebasedDatabaseBuilder
		{
		//File[] fileList;
		List<File> fileList=new ArrayList<File>();
		int currentFile=0;
		private StringBuffer rebuildLog=new StringBuffer();
		
		private int countFilesAdded=0;
		
		private Integer minZ=null;
		
		
		private void getAllFiles(List<File> files, File dir)
			{
			for(File f:dir.listFiles())
				if(!f.getName().startsWith("."))
					{
					if(f.isDirectory())
						{
						getAllFiles(files, f);
						}
					else
						{
						files.add(f);
						}
					}
			}
		
		public void run(EvData data)
			{
			minZ=null;
			Vector<String> channelVector=new Vector<String>();
			try
				{
				File dir=basedir;
				fileList.clear();
				getAllFiles(fileList, dir);

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
			//String filename=f.getName();
			
			System.out.println(File.pathSeparatorChar);
			String filename=getRelativePath(f.getPath(), basedir.getPath(), File.separator);
			
			
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
					else if(type=='D')
						{
						StringBuilder sb=new StringBuilder();
						
						while(j<filename.length())
							{
							char c=filename.charAt(j);
							if(c!='.')//Character.isLetter(c) || Character.isDigit(c))
								{
								if(c=='/')
									c='@';
								sb.append(c);
								j++;
								}
							else
								break;
							}
						info.dataset=sb.toString();
						if(info.dataset.length()==0)
							{
							rebuildLog.append("Not matching "+filename+" Missing parameter "+type+", filename pos"+j+"\n");
							return null;
							}
						}
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

			//Get the right well / imageset
			String nameImageset=info.well;
			
			if(info.dataset!=null)
				nameImageset=info.dataset;  //TODO handle dataset and well simulatenously
			
			
			if(nameImageset==null)
				nameImageset="im";
			Imageset im=(Imageset)con.getChild(nameImageset);
			if(im==null)
				con.putChild(nameImageset,im=new Imageset());

			
			//Get channel
			EvChannel ch=im.getCreateChannel(channelName);
			System.out.println(ch);

			//Get stack
			EvStack stack=ch.getStack(new EvDecimal(info.frame));
			if(stack==null)
				{
				stack=new EvStack();
				ch.putStack(new EvDecimal(info.frame), stack);
				}
			
			int numPlanesInFile=1;
			
			if(filesAsStacks)
				{
				numPlanesInFile=countImagePlanes(info.f);
				System.out.println("********* "+numPlanesInFile);
				}
			
			
			
			//Create planes
			for(int i=0;i<numPlanesInFile;i++)
				{
				EvImagePlane evim=new EvImagePlane();
				stack.setRes(resX,resY,resZ);
				evim.io=new EvSingleImageFileReader(info.f, i);
				
				int slice=i+info.slice-minZ;
				stack.putPlane(slice, evim);
				
				String filename=info.f.getName();
				String newLogEntry=filename+" Ch: "+channelName+ " Fr: "+info.frame+" Sl: "+slice+"\n";
				System.out.println(newLogEntry);
				rebuildLog.append(newLogEntry);
				countFilesAdded++;
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
			if(currentFile<fileList.size())
				{
				File f=fileList.get(currentFile);
				currentFile++;
				return f;
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
		

	
	
	
	
	
	
	
	
	
	
	

  /**
   * Get the relative path from one file to another, specifying the directory separator. 
   * If one of the provided resources does not exist, it is assumed to be a file unless it ends with '/' or
   * '\'.
   * 
   * @param targetPath targetPath is calculated to this file
   * @param basePath basePath is calculated from this file
   * @param pathSeparator directory separator. The platform default is not assumed so that we can test Unix behaviour when running on Windows (for example)
   * @return
   */
	public static String getRelativePath(String targetPath, String basePath,
			String pathSeparator)
		{
		// Normalize the paths
		String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
		String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);

		// Undo the changes to the separators made by normalization
		if (pathSeparator.equals("/"))
			{
			normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
			normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

			}
		else if (pathSeparator.equals("\\"))
			{
			normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
			normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);
			}
		else
			{
			throw new IllegalArgumentException("Unrecognised dir separator '"
					+pathSeparator+"'");
			}

		String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
		String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

		// First get all the common elements. Store them as a string,
		// and also count how many of them there are.
		StringBuffer common = new StringBuffer();

		int commonIndex = 0;
		while (commonIndex<target.length&&commonIndex<base.length
				&&target[commonIndex].equals(base[commonIndex]))
			{
			common.append(target[commonIndex]+pathSeparator);
			commonIndex++;
			}

		if (commonIndex==0)
			{
			// No single common path element. This most
			// likely indicates differing drive letters, like C: and D:.
			// These paths cannot be relativized.
			throw new PathResolutionException("No common path element found for '"
					+normalizedTargetPath+"' and '"+normalizedBasePath+"'");
			}

		// The number of directories we have to backtrack depends on whether the
		// base is a file or a dir
		// For example, the relative path from
		//
		// /foo/bar/baz/gg/ff to /foo/bar/baz
		//
		// ".." if ff is a file
		// "../.." if ff is a directory
		//
		// The following is a heuristic to figure out if the base refers to a file
		// or dir. It's not perfect, because
		// the resource referred to by this path may not actually exist, but it's
		// the best I can do
		boolean baseIsFile = true;

		File baseResource = new File(normalizedBasePath);

		if (baseResource.exists())
			{
			baseIsFile = baseResource.isFile();

			}
		else if (basePath.endsWith(pathSeparator))
			{
			baseIsFile = false;
			}

		StringBuffer relative = new StringBuffer();

		if (base.length!=commonIndex)
			{
			int numDirsUp = baseIsFile ? base.length-commonIndex-1 : base.length-commonIndex;

			for (int i = 0; i<numDirsUp; i++)
				relative.append(".."+pathSeparator);
			}
		relative.append(normalizedTargetPath.substring(common.length()));
		return relative.toString();
		}

  static class PathResolutionException extends RuntimeException 
  	{
		private static final long serialVersionUID = 1L;

		PathResolutionException(String msg) 
			{
      super(msg);
			}
  	}
	
	
	
	
	
	
	
	
	/**
	 * Count number of planes in file
	 * 
	 * @throws IOException 
	 */
	public static int countImagePlanes(File file) throws IOException
		{
		try
			{
			String fend=EvFileUtil.fileEnding(file);
			fend=fend.toLowerCase();
			//Use JAI if possible, it can be assumed to be very fast
			
			
			//Rely on Bioformats in the worst case. Use the most stupid reader available, or bio-formats might attempt
			//detecting a special format
			IFormatReader reader;
			if(fend!=null && (fend.equals("tiff") || fend.equals("tif")))
				{
				TiffReader tr=new TiffReader();
				tr.setGroupFiles(false);
				reader=tr;
				}
			else
				{
				reader=new ImageReader();
				}
			//reader.setAllowOpenFiles(false);  //for scifio

			reader.setId(file.getAbsolutePath());
			
			int count=reader.getImageCount();

//			String[] dimTypes=reader.getChannelDimTypes();
	//		int[] dimLengths=reader.getChannelDimLengths();
			
			
			reader.close();
			
			return count;
			}
		catch (FormatException e)
			{
			EvLog.printError("Bioformats failed to read image "+file, null);
			throw new IOException(e.getMessage());
			}
		}

	
	
	
	}
