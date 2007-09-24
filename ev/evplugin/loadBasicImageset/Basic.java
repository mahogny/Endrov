package evplugin.loadBasicImageset;

//import evplugin.basicWindow.*;
//import evplugin.imageset.*;
import evplugin.metadata.*;

import java.io.File;
import java.awt.event.*;

import javax.swing.*;


public class Basic //implements BasicWindowExtension
	{
	public static void initPlugin() {}
	static
		{
	//	BasicWindow.addBasicWindowExtension(new Basic());
		
		MetadataBasic.extensions.add(new MetadataExtension()
			{

			public void buildOpen(JMenu menu)
				{
				final JMenuItem miLoadSequenceImageset=new JMenuItem("Load sequence imageset");
				final JMenuItem miLoadNamebasedImageset=new JMenuItem("Load namebased imageset");
				menu.add(miLoadSequenceImageset);
				menu.add(miLoadNamebasedImageset);
				
				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						if(e.getSource()==miLoadSequenceImageset)
							dialogSequenceLoadImageset();
						else if(e.getSource()==miLoadNamebasedImageset)
								dialogNamebasedLoadImageset();
						}
					
					/**
					 * Show dialog for opening a new sequence based imageset
					 */
					public void dialogSequenceLoadImageset()
						{
						JFileChooser chooser = new JFileChooser();
				    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    chooser.setCurrentDirectory(new File(Metadata.lastDataPath));
				    int returnVal = chooser.showOpenDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION)
				    	{
				    	String filename=chooser.getSelectedFile().getAbsolutePath();
				    	Metadata.lastDataPath=chooser.getSelectedFile().getParent();
				    	Metadata.addMetadata(new SequenceImageset(filename));
				    	}
						}
					
					
					/**
					 * Show dialog for opening a new sequence based imageset
					 */
					public void dialogNamebasedLoadImageset()
						{
						JFileChooser chooser = new JFileChooser();
				    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    chooser.setCurrentDirectory(new File(Metadata.lastDataPath));
				    int returnVal = chooser.showOpenDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION)
				    	{
				    	String filename=chooser.getSelectedFile().getAbsolutePath();
				    	Metadata.lastDataPath=chooser.getSelectedFile().getParent();
				    	Metadata.addMetadata(new SequenceImageset(filename));
				    	}
						}
					};
					
				miLoadNamebasedImageset.addActionListener(listener);
				miLoadSequenceImageset.addActionListener(listener);
				}
			
			public void buildSave(JMenu menu, final Metadata meta)
				{
				if(meta instanceof NamebasedImageset)
					{
					JMenuItem miSetup=new JMenuItem("Setup");
					menu.add(miSetup);
					miSetup.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{((NamebasedImageset)meta).setup();}
						});	
					}
				else if(meta instanceof SequenceImageset)
					{
					JMenuItem miSetup=new JMenuItem("Setup");
					menu.add(miSetup);
					miSetup.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{((SequenceImageset)meta).setup();}
						});	
					}
				}
			});
		
		}
	
	}