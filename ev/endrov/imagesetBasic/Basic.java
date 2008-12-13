package endrov.imagesetBasic;

import endrov.data.*;

import java.io.File;
import java.awt.event.*;

import javax.swing.*;


public class Basic //implements BasicWindowExtension
	{
	public static void initPlugin() {}
	static
		{
	//	BasicWindow.addBasicWindowExtension(new Basic());
		
		EvDataMenu.extensions.add(new DataMenuExtension()
			{

			public void buildOpen(JMenu menu)
				{
				final JMenuItem miLoadSequenceImageset=new JMenuItem("Load sequence imageset");
				final JMenuItem miLoadNamebasedImageset=new JMenuItem("Load namebased imageset");
				addMetamenu(menu,miLoadSequenceImageset);
				addMetamenu(menu,miLoadNamebasedImageset);
				
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
				    chooser.setCurrentDirectory(new File(EvData.getLastDataPath()));
				    int returnVal = chooser.showOpenDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION)
				    	{
				    	String filename=chooser.getSelectedFile().getAbsolutePath();
				    	EvData.setLastDataPath(chooser.getSelectedFile().getParent());
				    	EvData.addMetadata(new SequenceImageset(filename));
				    	}
						}
					
					
					/**
					 * Show dialog for opening a new sequence based imageset
					 */
					public void dialogNamebasedLoadImageset()
						{
						JFileChooser chooser = new JFileChooser();
				    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    chooser.setCurrentDirectory(new File(EvData.getLastDataPath()));
				    int returnVal = chooser.showOpenDialog(null);
				    if(returnVal == JFileChooser.APPROVE_OPTION)
				    	{
				    	String filename=chooser.getSelectedFile().getAbsolutePath();
				    	EvData.setLastDataPath(chooser.getSelectedFile().getParent());
				    	EvData.addMetadata(new NamebasedImageset(filename));
				    	}
						}
					};
					
				miLoadNamebasedImageset.addActionListener(listener);
				miLoadSequenceImageset.addActionListener(listener);
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
							{((NamebasedImageset)meta).setup();}
						});	
					}
				else if(meta.io instanceof SequenceImageset)
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