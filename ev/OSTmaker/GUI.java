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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import evplugin.data.EvData;
import evplugin.ev.CompleteBatch;
import evplugin.ev.EvSwingTools;
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
	
	private Vector<ToImport> importList=new Vector<ToImport>();
	private static HashMap<String, Integer> chancomp=new HashMap<String, Integer>();
	

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
				while (i.hasNext()) 
					addFile((File)i.next());
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
	 * Create GUI for OST maker
	 */
	public GUI()
		{
		bGo.addActionListener(this);
		bAdd.addActionListener(this);
		bRemove.addActionListener(this);
		bQuit.addActionListener(this);
		
		
		
		JPanel bpanel=new JPanel(new GridLayout(1,4));
		bpanel.add(bGo);
		bpanel.add(bAdd);
		bpanel.add(bRemove);
		bpanel.add(bQuit);
		
		JPanel lpanel=new JPanel(new BorderLayout());
		lpanel.add(pComp,BorderLayout.CENTER);
		lpanel.add(bpanel,BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(guiList,BorderLayout.CENTER);
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
	private void addFile(File filename)
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
		
		//Generate list of controls
		pComp.removeAll();
		pComp.setLayout(new GridLayout(channels.size(),1));
		for(final String ch:channels)
			{
			JPanel tp=new JPanel(new BorderLayout());
			pComp.add(tp);
			
			final SpinnerNumberModel model=new SpinnerNumberModel(getComp(ch),0,100,1);
			JSpinner compSpin=new JSpinner(model);
			compSpin.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent arg0)
					{
					chancomp.put(ch, (Integer)model.getValue());
					}
			});
			
			tp.add(new JLabel(ch+": "),BorderLayout.WEST);
			tp.add(EvSwingTools.withLabel("Compression", compSpin));
			}
		validate();
		}
	
	
	public void startConversion()
		{
		Vector<ToImport> newImportList=importList;
		importList=new Vector<ToImport>();
		for(ToImport toim:newImportList)
			{
			String outfilename=toim.file.getName();
			if(outfilename.indexOf(".")!=-1)
				{
				outfilename=outfilename.substring(0,outfilename.lastIndexOf(".")-1);
				File outfile=new File(toim.file.getParentFile(),outfilename);
				
				BioformatsImageset inim=toim.im;
				
				//Set compression
				for(String chname:inim.channelImages.keySet())
					if(chancomp.containsKey(chname))
						inim.meta.channelMeta.get(chname).compression=chancomp.get(chname);
				
				//the save system could now be replaced by writable OST imagesets.
				//problem though: lack a system to set compression rates, write locks are in, rather ugly in general
				System.out.println("Saving to: "+outfile);
				new CompleteBatch(new SaveOSTThread(inim, outfile.getAbsolutePath()));
				importList.remove(toim);
				}
			else
				JOptionPane.showMessageDialog(null, "Cannot handle "+toim.file+": no file ending");
			}
		updateFileList();
		JOptionPane.showMessageDialog(null, "Finished importing data");
		}
	
	
	/**
	 * Handle actions
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bGo)
			{
			startConversion();
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
