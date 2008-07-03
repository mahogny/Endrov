package endrov.data;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.ev.EV;

/**
 * Extension to BasicWindow
 * 
 * @author Johan Henriksson
 */
public class EvDataMenu implements BasicWindowExtension
	{
	public static Vector<DataMenuExtension> extensions=new Vector<DataMenuExtension>();
	
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new BasicHook());
		}
	private class BasicHook implements BasicWindowHook, ActionListener
		{
		private JMenu mData=new JMenu("Data");
		private JMenuItem miNew=new JMenuItem("New XML");
		private JMenu mRecent=new JMenu("Recent Files");

		private JMenuItem miOpenFile=new JMenuItem("Load File");
		private JMenuItem miOpenFilePath=new JMenuItem("Load File by Path");

		WeakReference<BasicWindow> w;
		
		public void createMenus(BasicWindow w)
			{
			w.addMenubar(mData);
			this.w=new WeakReference<BasicWindow>(null);
			buildMenu(w);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==miNew)
				{
				EvData.addMetadata(new EvDataXML());
				BasicWindow.updateWindows();
				}
			else if(e.getSource()==miOpenFile)
				{
				new Thread(){
					public void run()
						{
						LoadProgressDialog loadDialog=new LoadProgressDialog(1);
						final EvData data=EvData.loadFileDialog(loadDialog);
						SwingUtilities.invokeLater(new Runnable(){
							public void run()
								{
								EvData.registerOpenedData(data);
								}
						});
						loadDialog.dispose();
						}
				}.start();
				}
			else if(e.getSource()==miOpenFilePath)
				loadByPath();
			}

		
		public void loadByPath()
			{
			String clipboardString=null;
			try
				{
				clipboardString=(String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				}
			catch(Exception e2)
				{
				System.out.println("Failed to get text from clipboard");
				}
			if(clipboardString==null)
				clipboardString="";
			String fileName=JOptionPane.showInputDialog("Path",clipboardString);
			if(fileName!=null)
				{
				final File thefile=new File(fileName);
				if(thefile.exists())
					{
					new Thread(){
					public void run()
						{
						LoadProgressDialog loadDialog=new LoadProgressDialog(1);
						final EvData data=EvData.loadFile(thefile,loadDialog);
						SwingUtilities.invokeLater(new Runnable(){
						public void run()
							{
							EvData.registerOpenedData(data);
							}
						});
						loadDialog.dispose();
						}
					}.start();
//					EvData.registerOpenedData(EvData.loadFile(thefile));
					}
				else
					JOptionPane.showMessageDialog(null, "Path does not exist");
				}
			}
		
		
		
		public void buildMenu(BasicWindow w)
			{
			BasicWindow.tearDownMenu(mRecent);
			BasicWindow.tearDownMenu(mData);
			
			BasicWindow.addMenuItemSorted(mData, miNew);
			BasicWindow.addMenuItemSorted(mData, miOpenFile);			
			BasicWindow.addMenuItemSorted(mData, miOpenFilePath);			
			BasicWindow.addMenuItemSorted(mData, mRecent);			
			
			miNew.addActionListener(this);
			miOpenFile.addActionListener(this);
			miOpenFilePath.addActionListener(this);
			for(DataMenuExtension e:extensions)
				e.buildOpen(mData);
			mData.addSeparator();
			
			//List recent entries
			for(final RecentReference rref:EvData.recentlyLoadedFiles)
				{
				JMenuItem mi=new JMenuItem(rref.descName+" ("+rref.url+")");
				mRecent.add(mi);
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{EvData.registerOpenedData(EvData.loadFile(new File(rref.url)));}
					});
				}
			
			
			
			//Special entry: For all
			if(!EvData.metadata.isEmpty())
				{
				JMenu menuMetadata=new JMenu("For All Data");
				mData.add(menuMetadata);
				mData.addSeparator();
				
				JMenuItem miUnload=new JMenuItem("Unload");
				JMenuItem miSave=new JMenuItem("Save");
				menuMetadata.add(miUnload);
				menuMetadata.add(miSave);
				
				//// Menu item Listener: Unload
				ActionListener metaListenerUnload=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						boolean anyMod=false;
						for(EvData thisMeta:EvData.metadata)
							if(thisMeta.isMetadataModified())
								anyMod=true;

						int option=!anyMod ? JOptionPane.NO_OPTION : JOptionPane.showConfirmDialog(null, 
								"Metadata has been modified. Save before closing all?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
						if (option == JOptionPane.CANCEL_OPTION)
							return;
						else
							{
							if (option==JOptionPane.YES_OPTION)
								for(EvData thisMeta:EvData.metadata)
									{
									thisMeta.saveMeta();
									thisMeta.setMetadataModified(false);//this might be wrong if save not supported
									}
							EvData.metadata.clear();
							BasicWindow.updateWindows();
							System.gc();
							}
						}
					};
				miUnload.addActionListener(metaListenerUnload);

				
				//// Menu item Listener: Save
				ActionListener metaListenerSave=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						for(EvData thisMeta:EvData.metadata)
							{
							thisMeta.saveMeta();
							thisMeta.setMetadataModified(false);//this might be wrong if save not supported
							}
						}
					};
				miSave.addActionListener(metaListenerSave);
				
				}
			
			
			
			//List all global Metadata
			for(final EvData thisMeta:EvData.metadata)
				{
				JMenu menuMetadata=new JMenu(thisMeta.getMetadataName());
				mData.add(menuMetadata);
				
				JMenuItem miUnload=new JMenuItem("Unload");
				JMenuItem miSave=new JMenuItem("Save");
				menuMetadata.add(miUnload);
				menuMetadata.add(miSave);
				
				for(DataMenuExtension e:extensions)
					e.buildSave(menuMetadata, thisMeta);
				menuMetadata.addSeparator();
				
				//// Menu item Listener: Unload
				ActionListener metaListenerUnload=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						if(thisMeta.isMetadataModified())
							{
							int option=JOptionPane.showConfirmDialog(null, 
									"Metadata has been modified. Save before close?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
							if (option==JOptionPane.YES_OPTION)
								{
								thisMeta.saveMeta();
								thisMeta.setMetadataModified(false);//this might be wrong if save not supported
								}
							else if (option == JOptionPane.CANCEL_OPTION)
								return;
							}
						EvData.metadata.remove(thisMeta);
						BasicWindow.updateWindows();
						System.gc();
						}
					};
				miUnload.addActionListener(metaListenerUnload);

				//// Menu item Listener: Save
				ActionListener metaListenerSave=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						thisMeta.saveMeta();
						thisMeta.setMetadataModified(false);//this might be wrong if save not supported
						}
					};
				miSave.addActionListener(metaListenerSave);
				
				
				for(final String obId:thisMeta.metaObject.keySet())
					{
					final EvObject ob=thisMeta.metaObject.get(obId); //might become problematic?
					JMenu obmenu=new JMenu(""+obId+": "+ob.getMetaTypeDesc());
					menuMetadata.add(obmenu);

					JMenuItem miRemoveOb=new JMenuItem("Delete");
					obmenu.add(miRemoveOb);
					JMenuItem miRenameOb=new JMenuItem("Rename");
					obmenu.add(miRenameOb);
					
					ob.buildMetamenu(obmenu);
					
					//Menu item listener: object/Remove
					ActionListener obListenerRemove=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							int option = JOptionPane.showConfirmDialog(null, 
									"Are you sure you want to delete "+ob.getMetaTypeDesc()+" "+obId+"?", "Delete?", JOptionPane.YES_NO_OPTION);
							if (option == JOptionPane.YES_OPTION)
								{
								thisMeta.metaObject.remove(obId);
								BasicWindow.updateWindows();
								thisMeta.setMetadataModified(true);
								}
							}
						};
					miRemoveOb.addActionListener(obListenerRemove);
					
					//Menu item listener: object/Rename
					ActionListener obListenerRename=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							String newId=(String)JOptionPane.showInputDialog(null, "Name:", EV.programName+" Rename object", 
									JOptionPane.QUESTION_MESSAGE, null, null, obId);
							//Maybe use weak reference?
							if(newId!=null)
								{
								EvObject ob=thisMeta.metaObject.remove(obId);
								if(ob!=null)
									thisMeta.metaObject.put(newId, ob);
								BasicWindow.updateWindows();
								}
							}
						};
					miRenameOb.addActionListener(obListenerRename);
					
					}
				}

			
			}
		}
	}