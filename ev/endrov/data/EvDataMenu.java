package endrov.data;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.ev.EV;
import endrov.util.EvSwingUtil;

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
		private JMenuItem miNew=new JMenuItem("New");
		private JMenu mRecent=new JMenu("Recent Files");

		private JMenuItem miOpenFile=new JMenuItem("Load File");
		private JMenuItem miOpenFilePath=new JMenuItem("Load File by Path");

		WeakReference<BasicWindow> w;
		
		public void createMenus(BasicWindow w)
			{
			w.addMenubar(mData);
			this.w=new WeakReference<BasicWindow>(null);
			JMenu mFile=w.menuFile;
			
			miNew.setIcon(BasicIcon.iconMenuNew);
			miOpenFile.setIcon(BasicIcon.iconMenuLoad);
			miOpenFilePath.setIcon(BasicIcon.iconMenuLoad);
			mRecent.setIcon(BasicIcon.iconMenuLoad);
			
			BasicWindow.addMenuItemSorted(mFile, miNew, "data_1new");
			BasicWindow.addMenuItemSorted(mFile, mRecent, "data_recent");
			BasicWindow.addMenuItemSorted(mFile, miOpenFile, "data_open_file");			
			BasicWindow.addMenuItemSorted(mFile, miOpenFilePath, "data_open_file_by_path");
			
			for(DataMenuExtension e:extensions)
				e.buildOpen(mFile);

			miNew.addActionListener(this);
			miOpenFile.addActionListener(this);
			miOpenFilePath.addActionListener(this);
			
			buildMenu(w);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==miNew)
				{
				EvData.registerOpenedData(new EvData());
				BasicWindow.updateWindows();
				}
			else if(e.getSource()==miOpenFile)
				{
				EvData data=GuiEvDataIO.loadFileDialog();
				EvData.registerOpenedData(data);
/*				new Thread(){
					public void run()
						{
						}
				}.start();*/
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
					EvData data=GuiEvDataIO.loadFile(thefile.getAbsolutePath());
					EvData.registerOpenedData(data);
					}
				else
					JOptionPane.showMessageDialog(null, "Path does not exist");
				}
			}
		
		
		/**
		 * Build Move Object menu
		 */
		private JMenu buildMoveMenu(EvContainer moveObRoot, String moveObName)
			{
			JMenu miMoveOb=new JMenu("Move");

			//All metadata
			for(final EvData thisMeta:EvData.openedData)
				{
				JMenu menuMetadata=new JMenu(thisMeta.getMetadataName());
				miMoveOb.add(menuMetadata);
				//All objects
				recurseBuildMove(menuMetadata,moveObRoot, moveObName, thisMeta);
				}
			
			return miMoveOb;
			}
		/**
		 * Build Move Object menu, helper
		 */
		private void recurseBuildMove(JMenu menu, final EvContainer moveObRoot, final String moveObName, final EvContainer root)
			{
			JMenuItem miHere=new JMenuItem(">>Here<<");
			menu.add(miHere);
			
			
			miHere.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					EvObject toMove=moveObRoot.metaObject.get(moveObName);
					if(toMove!=null)
						{
						moveObRoot.metaObject.remove(moveObName);
						root.metaObject.put(moveObName,toMove); //Danger. what about overlapping name?
						BasicWindow.updateWindows();
						}
					}
				});


			//Subobjects
			EvObject objectToMove=moveObRoot.getMetaObject(moveObName);
			for(Map.Entry<String, EvObject> me:root.metaObject.entrySet())
				{
				final String obId=me.getKey();
				final EvObject ob=me.getValue();
				if(ob!=objectToMove)
					{
					JMenu miSub=new JMenu(obId);
					menu.add(miSub);
					recurseBuildMove(miSub, moveObRoot, moveObName, ob);
					}
				}
			}
		

		/**
		 * Build menus for all subobjects in data menu
		 */
		private void attachSubObjectMenus(JMenu menuMetadata, final EvContainer thisMeta)
			{
			if(!thisMeta.metaObject.isEmpty())
				{
				menuMetadata.addSeparator();
				
				for(Map.Entry<String, EvObject> me:thisMeta.metaObject.entrySet())
					{
					final String obId=me.getKey();
					final EvObject ob=me.getValue();
					JMenu obmenu=new JMenu(""+obId+": "+ob.getMetaTypeDesc());
					menuMetadata.add(obmenu);
	
					JMenuItem miRemoveOb=new JMenuItem("Delete");
					obmenu.add(miRemoveOb);
					JMenuItem miRenameOb=new JMenuItem("Rename");
					obmenu.add(miRenameOb);
					JMenu miMoveOb=buildMoveMenu(thisMeta, obId);
					obmenu.add(miMoveOb);
					
					ob.buildMetamenu(obmenu);

					attachSubObjectMenus(obmenu, ob);

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
								thisMeta.setMetadataModified();
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
		
		/**
		 * Top level function to build menus
		 */
		public void buildMenu(BasicWindow w)
			{
			EvSwingUtil.tearDownMenu(mData);
			EvSwingUtil.tearDownMenu(mRecent);
			
			for(DataMenuExtension e:extensions)
				e.buildData(mData);

			
			//List recent entries
			for(final RecentReference rref:EvData.recentlyLoadedFiles)
				{
				JMenuItem mi=new JMenuItem(rref.descName+" ("+rref.url+")");
				mRecent.add(mi);
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{EvData.registerOpenedData(EvData.loadFile(rref.url));}
					});
				}
			JMenuItem miClearRecent=new JMenuItem("Clear");
			mRecent.addSeparator();
			mRecent.add(miClearRecent);
			miClearRecent.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{
						EvData.recentlyLoadedFiles.clear();
						BasicWindow.updateWindows();
						}
					});
			
			
			//Special entry: For all
			if(!EvData.openedData.isEmpty())
				{
				JMenu menuMetadata=new JMenu("For All Data");
				mData.add(menuMetadata);
				mData.addSeparator();
				
				JMenuItem miUnload=new JMenuItem("Unload");
				JMenuItem miSave=new JMenuItem("Save");
				miSave.setIcon(BasicIcon.iconMenuSave);
				menuMetadata.add(miUnload);
				menuMetadata.add(miSave);
				
				//// Menu item Listener: Unload
				ActionListener metaListenerUnload=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						boolean anyMod=false;
						for(EvData thisMeta:EvData.openedData)
							if(thisMeta.isMetadataModified())
								anyMod=true;
						
						
						int option=!anyMod ? JOptionPane.NO_OPTION : JOptionPane.showConfirmDialog(null, 
								"Metadata has been modified. Save before closing all?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
						if (option == JOptionPane.CANCEL_OPTION)
							return;
						else
							{
							if (option==JOptionPane.YES_OPTION)
								for(EvData thisMeta:EvData.openedData)
									{
									thisMeta.saveData();
									thisMeta.setMetadataNotModified(); //this might be wrong if save not supported
									}
							EvData.openedData.clear();
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
						GuiEvDataIO.saveFile(EvData.openedData);
						}
					};
				miSave.addActionListener(metaListenerSave);
				
				}
			
			
			
			//List all global Metadata
			for(final EvData thisMeta:EvData.openedData)
				{
				JMenu menuMetadata=new JMenu(thisMeta.getMetadataName());
				mData.add(menuMetadata);
				
				JMenuItem miUnload=new JMenuItem("Unload");
				menuMetadata.add(miUnload);
				
				//Only add save option if there is a current way of saving it
				if(thisMeta.io!=null)
					{
					JMenuItem miSave=new JMenuItem("Save");
					miSave.setIcon(BasicIcon.iconMenuSave);
					menuMetadata.add(miSave);
					ActionListener metaListenerSave=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							GuiEvDataIO.saveFile(thisMeta);
							//thisMeta.saveData();
							thisMeta.setMetadataNotModified();
							}
						};
					miSave.addActionListener(metaListenerSave);
					}
				
				
				JMenuItem miSaveAs=new JMenuItem("Save as");
				miSaveAs.setIcon(BasicIcon.iconMenuSaveAs);
				menuMetadata.add(miSaveAs);
				ActionListener metaListenerSaveAs=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						GuiEvDataIO.saveFileDialog(thisMeta);
						//thisMeta.saveFileDialog(null);
						thisMeta.setMetadataNotModified();//this might be wrong if save not supported
						BasicWindow.updateWindows();
						}
					};
				miSaveAs.addActionListener(metaListenerSaveAs);
				
				for(DataMenuExtension e:extensions)
					e.buildSave(menuMetadata, thisMeta);
				
				//Optional "Open data directory" menu item
				if(thisMeta.io!=null && thisMeta.io.datadir()!=null)
					{
					final JMenuItem miOpenDatadir=new JMenuItem("Open data dir");
					ActionListener listener=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							EV.openExternal(thisMeta.io.datadir());
							}
						};
					miOpenDatadir.addActionListener(listener);
					menuMetadata.add(miOpenDatadir);					
					}

				
				
				
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
								thisMeta.saveData();
								//thisMeta.setMetadataNotModified();//this might be wrong if save not supported
								}
							else if (option == JOptionPane.CANCEL_OPTION)
								return;
							}
						EvData.openedData.remove(thisMeta);
						BasicWindow.updateWindows();
						System.gc();
						}
					};
				miUnload.addActionListener(metaListenerUnload);

				attachSubObjectMenus(menuMetadata, thisMeta);
				
				}

			
			}
		}
	}