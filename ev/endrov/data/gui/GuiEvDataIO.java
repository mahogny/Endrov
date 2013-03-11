/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data.gui;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.JFileChooser;


import endrov.core.log.EvLog;
import endrov.data.EvData;
import endrov.data.EvIODataReaderWriterDeclaration;
import endrov.gui.window.EvBasicWindow;
import endrov.util.FuncAB;


/**
 * Graphical loading and saving of files
 * @author Johan Henriksson
 *
 */
public class GuiEvDataIO
	{
	
	
	/**
	 * Task for loading a list of files
	 */
	private static class DataLoader implements EvData.FileIOStatusCallback, EvDataProgressWindow.Task
		{
		private List<String> path;
		private int nowAt=0;
		private EvDataProgressWindow w;
		final FuncAB<EvData, Object> callback;
		
		public DataLoader(List<String> path, final FuncAB<EvData, Object> callback) 
			{ 
			this.callback=callback;
			this.path=path;
			}
		
		
		public void fileIOStatus(double proc, String text)
			{
			if(proc>=0 && proc<=1)
				w.setProgress((int)(100*nowAt+proc*100)/path.size());
			else
				EvLog.printError("fileIOstatus range should be 0-1", null);
			}

		public void run(EvDataProgressWindow w)
			{
			this.w=w;
			for(int i=0;i<path.size();i++)
				{
				w.setStatusText("Loading "+path.get(i));
				EvData data=EvData.loadFile(path.get(i),this);
				callback.func(data);
				
				nowAt++;
				fileIOStatus(0, "");
				}
			}

		public void cancel()
			{
			// TODO Auto-generated method stub
			}
		
		}
		

	/**
	 * Task to save files
	 */
	private static class DataSaver implements EvData.FileIOStatusCallback, EvDataProgressWindow.Task
		{
		private Collection<EvData> datas;
		private int nowAt=0;
		private EvDataProgressWindow w; 
		
		public DataSaver(Collection<EvData> path) 
			{ 
			this.datas=path;
			}
		
		public void fileIOStatus(double proc, String text)
			{
			if(proc>=0 && proc<=1)
				w.setProgress((int)(100*nowAt+proc*100)/datas.size());
			else
				EvLog.printError("fileIOstatus range should be 0-1", null);
			}

		public void run(EvDataProgressWindow w)
			{
			this.w=w;
			for(EvData data:datas)
				{
				try
					{
					w.setStatusText("Saving "+data.io.getMetadataName());
					data.saveData();
					}
				catch (Throwable e)
					{
					// TODO implement properly
					e.printStackTrace();
					}
				nowAt++;
				fileIOStatus(0, "");
				}
			}

		public void cancel()
			{
			// TODO implement properly
			}

		
		}

		
	/**
	 * Load file by open dialog
	 */
	public static String showLoadFileDialog(String customtitle)
		{
		JFileChooser fc=new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if(customtitle!=null)
			fc.setDialogTitle(customtitle);
		
		fc.setFileFilter(new javax.swing.filechooser.FileFilter()
			{
			public boolean accept(File f)
				{
				if(f.isDirectory())
					return true;
				for(EvIODataReaderWriterDeclaration s:EvData.supportedFileFormats)
					if(s.loadSupports(f.getPath())!=null)
						return true;
				return false;
				}
			public String getDescription()
				{
				return "Data Files and Imagesets";
				}
			});
		fc.setCurrentDirectory(EvBasicWindow.getLastDataPath());
		int ret=fc.showOpenDialog(null);
		if(ret==JFileChooser.APPROVE_OPTION)
			{
			EvBasicWindow.setLastDataPath(fc.getSelectedFile().getParentFile());
			File filename=fc.getSelectedFile();
			return filename.getAbsolutePath();
			}
		return null;
		}

	
	/**
	 * Load file, select with a dialog
	 * 
	 * @para     customTitle   Can be null
	 * @param    callback      See loadFiles  
	 */
	public static void showLoadFileDialog(String customTitle, final FuncAB<EvData, Object> callback)
		{
		final String file=showLoadFileDialog(customTitle);
		if(file!=null)
			loadFiles(Arrays.asList(file), callback);
		}
	

	/**
	 * Load files, with a status dialog
	 * 
	 * @param callback  Will be invoked for each file. Should return null. Guaranteed to be run inside the swing thread 
	 */
	public static void loadFiles(Collection<String> file, final FuncAB<EvData, Object> callback)
		{
		new EvDataProgressWindow("Loading", new DataLoader(new LinkedList<String>(file), callback));
		}

	/**
	 * Save one file
	 * TODO throw exception
	 */
	public static void saveFile(EvData file)
		{
		List<EvData> input=new LinkedList<EvData>();
		input.add(file);
		saveFile(input);
		}

	/**
	 * Save files
	 */
	public static void saveFile(List<EvData> file)
		{
		DataSaver task = new DataSaver(file);
		new EvDataProgressWindow("Saving",task);
		}
			
	
	/**
	 * Save file by dialog
	 */
	public static void saveFileDialog(EvData data)
		{
		JFileChooser fc=new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		fc.setFileFilter(new javax.swing.filechooser.FileFilter()
			{
			public boolean accept(File f)
				{
				if(f.isDirectory())
					return true;
				for(EvIODataReaderWriterDeclaration s:EvData.supportedFileFormats)
					if(s.loadSupports(f.getPath())!=null)
						return true;
				return false;
				}
			public String getDescription()
				{
				return "Data Files and Imagesets";
				}
			});
		fc.setCurrentDirectory(EvBasicWindow.getLastDataPath());
		int ret=fc.showSaveDialog(null);
		if(ret==JFileChooser.APPROVE_OPTION)
			{
			EvBasicWindow.setLastDataPath(fc.getSelectedFile().getParentFile());
			File filename=fc.getSelectedFile();
			if(filename.getName().indexOf(".")==-1)
				filename=new File(filename.getParent(),filename.getName()+".ost");
			
			try
				{
				data.setSaver(filename.getPath());
				saveFile(data);
				}
			catch (IOException e)
				{
				EvBasicWindow.showErrorDialog("Error on saving: "+e.getMessage());
				}
			}
		}
	
	}
