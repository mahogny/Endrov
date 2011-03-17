/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import org.jdesktop.swingworker.SwingWorker;

import endrov.ev.EvLog;


/**
 * Graphical loading and saving of files
 * @author Johan Henriksson
 *
 */
public class GuiEvDataIO
	{
	/**
	 * Loading task
	 */
	private static class DataLoader extends SwingWorker<List<EvData>, Void> implements EvData.FileIOStatusCallback
		{
		private List<String> path;
		private int nowAt=0;
		
		public DataLoader(List<String> path) 
			{ 
			this.path=path;
			}
		
		public List<EvData> doInBackground() 
			{
			List<EvData> output=new LinkedList<EvData>();
			for(int i=0;i<path.size();i++)
				{
				output.add(EvData.loadFile(path.get(i),this));
				nowAt++;
				fileIOStatus(0, "");
				}
			return output;
			}
		
		public void fileIOStatus(double proc, String text)
			{
			if(proc>=0 && proc<=1)
				setProgress((int)(100*nowAt+proc*100)/path.size());
			else
				EvLog.printError("fileIOstatus range should be 0-1", null);
			}
		
		}
		

	/**
	 * Saving task
	 */
	private static class DataSaver extends SwingWorker<Void, Void> implements EvData.FileIOStatusCallback
		{
		private Collection<EvData> datas;
		private int nowAt=0;
		
		public DataSaver(Collection<EvData> path) 
			{ 
			this.datas=path;
			}
		
		public Void doInBackground() 
			{
			for(EvData data:datas)
				{
				data.saveData();
				nowAt++;
				fileIOStatus(0, "");
				}
			return null;
			}
		
		public void fileIOStatus(double proc, String text)
			{
			if(proc>=0 && proc<=1)
				setProgress((int)(100*nowAt+proc*100)/datas.size());
			else
				EvLog.printError("fileIOstatus range should be 0-1", null);
			}
		
		}

		
	/**
	 * Progress bar
	 */
	private static class DataProgress extends JFrame
		{
		static final long serialVersionUID=0;
		private JProgressBar progressBar = new JProgressBar(0, 100);
		
		public DataProgress(String title, SwingWorker<?, ?> task)
			{
			super(title);
			setSize(200,50);
			setLocationRelativeTo(null);
			setLayout(new GridLayout(1,1));
			add(progressBar);
			setVisible(true);
		
			task.addPropertyChangeListener(new PropertyChangeListener() 
				{
				public void propertyChange(PropertyChangeEvent evt) 
					{
					if("progress".equals(evt.getPropertyName())) 
						progressBar.setValue((Integer)evt.getNewValue());
					}
				});
			}
		}
			
	
	
	/**
	 * Load file by open dialog
	 */
	public static String showLoadDialog(String customtitle)
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
				for(EvDataSupport s:EvData.supportedFileFormats)
					if(s.loadSupports(f.getPath())!=null)
						return true;
				return false;
				}
			public String getDescription()
				{
				return "Data Files and Imagesets";
				}
			});
		fc.setCurrentDirectory(EvData.getLastDataPath());
		int ret=fc.showOpenDialog(null);
		if(ret==JFileChooser.APPROVE_OPTION)
			{
			EvData.setLastDataPath(fc.getSelectedFile().getParentFile());
			File filename=fc.getSelectedFile();
			return filename.getAbsolutePath();
			}
		return null;
		}

	
	/**
	 * Load file, select with a dialog
	 */
	public static EvData loadFileDialog()
		{
		return loadFileDialog(null);
		}
		
	/**
	 * Load file, select with a dialog
	 */
	public static EvData loadFileDialog(String customTitle)
		{
		String file=showLoadDialog(customTitle);
		if(file!=null)
			return loadFile(file);
		else
			return null;
		}
	
	/**
	 * Load one file
	 */
	public static EvData loadFile(String file)
		{
		List<String> input=new LinkedList<String>();
		input.add(file);
		return loadFile(input).get(0);
		}

	/**
	 * Load files
	 */
	public static List<EvData> loadFile(List<String> file)
		{
		DataLoader task = new DataLoader(file);
		DataProgress progress=new DataProgress("Loading",task);
		try
			{
			task.execute();
			return task.get();
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		catch (ExecutionException e)
			{
			e.printStackTrace();
			}
		finally
			{
			progress.dispose();
			}
		return null;
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
	 * TODO throw exception
	 */
	public static void saveFile(List<EvData> file)
		{
		DataSaver task = new DataSaver(file);
		DataProgress progress=new DataProgress("Saving",task);
		try
			{
			task.execute();
			task.get();
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		catch (ExecutionException e)
			{
			e.printStackTrace();
			}
		finally
			{
			progress.dispose();
			}
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
				for(EvDataSupport s:EvData.supportedFileFormats)
					if(s.loadSupports(f.getPath())!=null)
						return true;
				return false;
				}
			public String getDescription()
				{
				return "Data Files and Imagesets";
				}
			});
		fc.setCurrentDirectory(EvData.getLastDataPath());
		int ret=fc.showSaveDialog(null);
		if(ret==JFileChooser.APPROVE_OPTION)
			{
			EvData.setLastDataPath(fc.getSelectedFile().getParentFile());
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
				e.printStackTrace();
				}
			}
		}
	
	}
