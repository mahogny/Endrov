package OSTmaker;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import evplugin.data.EvData;
import evplugin.ev.CompleteBatch;
import evplugin.ev.EvSwingTools;
import evplugin.ev.Log;
import evplugin.ev.StdoutLog;
import evplugin.imagesetBioformats.BioformatsImageset;
import evplugin.imagesetOST.SaveOSTThread;


/**
 * GUI for OST maker
 * @author Johan Henriksson
 */
public class GUI extends JFrame implements ActionListener
	{
	static final long serialVersionUID=0;
	
	private JList guiList=new JList(new Vector<ToImport>());
	private JButton bGo=new JButton("Go");
	private JButton bAdd=new JButton("Add");
	private JButton bRemove=new JButton("Remove");
	private JButton bQuit=new JButton("Quit");
	private JPanel pComp=new JPanel();
	private Timer timer=new Timer(50,null); //hack
	
	private Vector<ToImport> importList=new Vector<ToImport>();
	private static HashMap<String, Integer> chancomp=new HashMap<String, Integer>();
	private JLabel labelStatus=new JLabel("");

	/**
	 * Get compression level or default 100
	 */
	private int getComp(String ch)
		{
		return chancomp.containsKey(ch) ? chancomp.get(ch) : 100;
		}
	
	
	/**
	 * One file to import
	 */
	private class ToImport
		{
		File file;
		BioformatsImageset im;
		public String toString()
			{
			return file.toString()+" ("+im.imageReader.getFormat()+")";
			}
		}
	
	/**
	 * Handle drag and drop of files to JList
	 */
	private class FSTransfer extends TransferHandler 
		{
		static final long serialVersionUID=0;
		
		public boolean importData(JComponent comp, Transferable t) 
			{
			if (!(comp instanceof JList) || !t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) 
				return false;
			try 
				{
				List data = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
				Iterator i = data.iterator();
				ReadingThread rt=new ReadingThread();
				while (i.hasNext()) 
					rt.files.add((File)i.next());
//					addFile((File)i.next());
				new Thread(rt).start();
				return true;
				}
			catch (UnsupportedFlavorException ufe) 
				{
				System.err.println("Ack! we should not be here.\nBad Flavor.");
				}
			catch (IOException ioe) 
				{
				System.out.println("Something failed during import:\n" + ioe);
				}
			return false;
			}
	
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) 
			{
			return true;
			}
		}

	
	/**
	 * Thread: read file information in background
	 */
	private class ReadingThread implements Runnable
		{
		Vector<File> files=new Vector<File>();
		public void run()
			{
			for(File f:files)
				{
				labelStatus.setText("Reading "+f);
				addFile(f);
				}
			labelStatus.setText("");
			}
		}
	
	private Thread importThread=null;
	private void setStopImportThread(){importThread=null;}

	/**
	 * Thread: Import in background
	 */
	private class ImportThread implements Runnable
		{
		public void run()
			{
			try
				{
				while(!importList.isEmpty())
					{
					ToImport toim=importList.get(0);
					labelStatus.setText("Importing "+toim.file.getName());
					
					String outfilename=toim.file.getName();
					if(outfilename.indexOf(".")!=-1)
						{
						outfilename=outfilename.substring(0,outfilename.lastIndexOf("."));
						outfilename=outfilename.replace('-', '_');
						File outfile=new File(toim.file.getParentFile(),outfilename);
						
						BioformatsImageset inim=toim.im;
						
						//Set compression
						for(String chname:inim.channelImages.keySet())
							if(chancomp.containsKey(chname))
								{
								System.out.println("comp "+chname+" "+chancomp.get(chname));
								inim.meta.channelMeta.get(chname).compression=chancomp.get(chname);
								}
						
						//the save system could now be replaced by writable OST imagesets.
						//problem though: lack a system to set compression rates, write locks are in, rather ugly in general
						System.out.println("Saving to: "+outfile);
						new CompleteBatch(new SaveOSTThread(inim, outfile.getAbsolutePath()));
						}
					else
						JOptionPane.showMessageDialog(null, "Cannot handle "+toim.file+": no file ending");
					
					importList.remove(0);
					updateFileList();
					}
				JOptionPane.showMessageDialog(null, "Finished importing data");
				}
			catch (Exception e)
				{
				System.out.println("Interrupted");
				}
			
			labelStatus.setText("");
			bGo.setText("Go");
			bGo.setEnabled(true);
			setStopImportThread();
			}
		}
	
	
	/**
	 * Create GUI for OST maker
	 */
	public GUI()
		{
		Log.listeners.add(new StdoutLog());
		timer.start();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		bGo.addActionListener(this);
		bAdd.addActionListener(this);
		bRemove.addActionListener(this);
		bQuit.addActionListener(this);
		
		
		JScrollPane scrollPane = new JScrollPane(guiList,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel bpanel=new JPanel(new GridLayout(1,4));
		bpanel.add(bGo);
		bpanel.add(bAdd);
		bpanel.add(bRemove);
		bpanel.add(bQuit);
		
		JPanel lpanel2=new JPanel(new GridLayout(2,1));
		lpanel2.add(bpanel);
		lpanel2.add(labelStatus);

		JPanel lpanel=new JPanel(new BorderLayout());
		lpanel.add(pComp,BorderLayout.CENTER);
		lpanel.add(lpanel2,BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(scrollPane,BorderLayout.CENTER);
		add(lpanel,BorderLayout.SOUTH);


		
		guiList.setDragEnabled(false);
    guiList.setTransferHandler(new FSTransfer());
		
		setSize(640, 400);
		setLocationRelativeTo(null);
		setVisible(true);
		}

	/**
	 * Add a file to the import list
	 */
	private /*synchronized*/ void addFile(File filename)
		{
  	try
			{
			ToImport toim=new ToImport();
    	toim.file=filename;
			toim.im=new BioformatsImageset(toim.file.getAbsolutePath());
			System.out.println(toim.im.getMetadataName());
    	importList.add(toim);
			}
		catch (Exception e1)
			{
			JOptionPane.showMessageDialog(this, "Failed to load file");
			e1.printStackTrace();
			}
  	updateFileList();
		}

	/**
	 * Update GUI list of files to import
	 */
	private void updateFileList()
		{
		guiList.setListData(importList);

		//Which channels?
		TreeSet<String> channels=new TreeSet<String>();
		for(ToImport toim:importList)
			for(String ch:toim.im.channelImages.keySet())
				channels.add(ch);
		
		for(ActionListener list:timer.getActionListeners())
			timer.removeActionListener(list);
		
		//Generate list of controls
		pComp.removeAll();
		pComp.setLayout(new GridLayout(channels.size(),1));
		for(final String ch:channels)
			{
			JPanel tp=new JPanel(new BorderLayout());
			pComp.add(tp);
			
			final SpinnerNumberModel model=new SpinnerNumberModel(getComp(ch),0,100,1);
			JSpinner compSpin=new JSpinner(model);
			timer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				chancomp.put(ch, (Integer)model.getValue());
				}
			});
			
			tp.add(new JLabel(ch+": "),BorderLayout.WEST);
			tp.add(EvSwingTools.withLabel("Compression", compSpin));
			}
		validate();
		}
	
	
	/**
	 * Handle actions
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bGo)
			{
			if(importThread==null)
				{
				importThread=new Thread(new ImportThread());
				importThread.start();
//				bGo.setText("Stop");
				bGo.setEnabled(false);
				}
			else
				{/*
				importThread.stop();
				setStopImportThread();
				bGo.setText("Go");
				bGo.setEnabled(true);
				labelStatus.setText("");*/
				}
			}
		else if(e.getSource()==bAdd)
			{
			JFileChooser chooser = new JFileChooser();
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    chooser.setCurrentDirectory(new File(EvData.getLastDataPath()));
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    	{
	    	EvData.setLastDataPath(chooser.getSelectedFile().getParent());
	    	addFile(chooser.getSelectedFile());
	    	}
			}
		else if(e.getSource()==bRemove)
			{
			importList.remove(guiList.getSelectedIndex());
			updateFileList();
			}
		else if(e.getSource()==bQuit)
			System.exit(0);
		}
	

	
	
	
	}
