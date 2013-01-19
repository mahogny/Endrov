/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;

import endrov.core.EndrovCore;
import endrov.core.EndrovUtil;
import endrov.core.log.EvLog;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.RecentReference;
import endrov.gui.EvSwingUtil;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.gui.window.EvBasicWindow.DialogReturnStatus;

/**
 * Extension to BasicWindow
 * 
 * @author Johan Henriksson
 */
public class EvDataMenu implements EvBasicWindowExtension
	{
	public static Vector<DataMenuExtension> extensions=new Vector<DataMenuExtension>();


	
	public void newBasicWindow(EvBasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new BasicHook());
		}
	private class BasicHook implements EvBasicWindowHook, ActionListener
		{
		private JMenu mData=new JMenu("Data");
		private JMenuItem miNew=new JMenuItem("New");
		private JMenu mRecent=new JMenu("Recent files");

		private JMenuItem miOpenFile=new JMenuItem("Load file");
		private JMenuItem miOpenFilePath=new JMenuItem("Load file by path");
		
		public void createMenus(EvBasicWindow w)
			{
			w.addMenubar(mData);
			JMenu mFile=w.menuFile;
			
			miNew.setIcon(BasicIcon.iconMenuNew);
			miOpenFile.setIcon(BasicIcon.iconMenuLoad);
			miOpenFilePath.setIcon(BasicIcon.iconMenuLoad);
			mRecent.setIcon(BasicIcon.iconMenuLoad);
			
			EvBasicWindow.addMenuItemSorted(mFile, miNew, "data_1new");
			EvBasicWindow.addMenuItemSorted(mFile, mRecent, "data_recent");
			EvBasicWindow.addMenuItemSorted(mFile, miOpenFile, "data_open_file");			
			EvBasicWindow.addMenuItemSorted(mFile, miOpenFilePath, "data_open_file_by_path");
			
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
				EvBasicWindow.updateWindows();
				}
			else if(e.getSource()==miOpenFile)
				{
				EvData data=GuiEvDataIO.loadFileDialog(null);
				EvData.registerOpenedData(data);
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
					JMenuItem miSetAuthor=new JMenuItem("Set author");
					obmenu.add(miSetAuthor);
					JMenuItem miGetMetainfo=new JMenuItem("Get metainfo");
					obmenu.add(miGetMetainfo);
					
					if(ob.isGeneratedData)
						{
						JMenuItem miSetEditable=new JMenuItem("Store permanently");
						obmenu.add(miSetEditable);
						miSetEditable.addActionListener(new ActionListener()
							{
							public void actionPerformed(ActionEvent e)
								{
								if(EvBasicWindow.showConfirmYesNoDialog("Do you really want to store this object permanently?"))
									{
									ob.isGeneratedData=false;
									EvBasicWindow.updateWindows();
									}
								}
							});
						
						}
					
					/*
					JMenu miMoveOb=buildMoveMenu(thisMeta, obId);
					obmenu.add(miMoveOb);
					
					JMenu miCopyOb=buildCopyMenu(thisMeta, obId);
					obmenu.add(miCopyOb);
					*/
					ob.buildMetamenu(obmenu, thisMeta);

					attachSubObjectMenus(obmenu, ob);

					//Menu item listener: object/Remove
					ActionListener obListenerRemove=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							if(EvBasicWindow.showConfirmYesNoDialog("Are you sure you want to delete "+ob.getMetaTypeDesc()+" "+obId+"?"))
								{
								thisMeta.metaObject.remove(obId);
								EvBasicWindow.updateWindows();
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
							String newId=(String)JOptionPane.showInputDialog(null, "Name:", EndrovCore.programName+" Rename object", 
									JOptionPane.QUESTION_MESSAGE, null, null, obId);
							//Maybe use weak reference?
							if(newId!=null)
								{
								EvObject ob=thisMeta.metaObject.remove(obId);
								if(ob!=null)
									thisMeta.metaObject.put(newId, ob);
								EvBasicWindow.updateWindows();
								}
							}
						};
					miRenameOb.addActionListener(obListenerRename);

					
					ActionListener obListenerSetAuthor=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							String newId=(String)JOptionPane.showInputDialog(null, "Name:", EndrovCore.programName+" Set Author", 
									JOptionPane.QUESTION_MESSAGE, null, null, thisMeta.author);
							//Maybe use weak reference?
							if(newId!=null)
								{
								thisMeta.author=newId;
								thisMeta.setMetadataModified();
								EvBasicWindow.updateWindows();
								}
							}
						};
					miSetAuthor.addActionListener(obListenerSetAuthor);
					

					ActionListener obListenerGetMetainfo=new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							String dateCreate=thisMeta.dateCreate==null ? "" : thisMeta.dateCreate + " " + (new Date(thisMeta.dateCreate.longValue()));
							String dateMod=thisMeta.dateLastModify==null ? "" : thisMeta.dateLastModify + " " + (new Date(thisMeta.dateLastModify.longValue()));
							JOptionPane.showMessageDialog(null, 
									"Author: "+thisMeta.author+"\n"+
									"Created: "+dateCreate+"\n"+
									"Modified: "+dateMod
									);
							}
						};
					miGetMetainfo.addActionListener(obListenerGetMetainfo);

					
					
					}
				}
			
			}
		
		/**
		 * Top level function to build menus
		 */
		public void buildMenu(EvBasicWindow w)
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
						EvBasicWindow.updateWindows();
						}
					});
			
			
			//Special entry: For all
			//if(!EvData.openedData.isEmpty())
				{
				//JMenu menuMetadata=new JMenu("For all data");
				//mData.add(menuMetadata);
				
				JMenuItem miUnloadAllData=new JMenuItem("Unload all data");
				JMenuItem miSaveAllData=new JMenuItem("Save all data");
				miSaveAllData.setIcon(BasicIcon.iconMenuSave);
				mData.add(miUnloadAllData);
				mData.add(miSaveAllData);

				mData.addSeparator();
				
				
				//// Menu item Listener: Unload
				ActionListener metaListenerUnload=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						boolean anyMod=false;
						for(EvData thisMeta:EvData.openedData)
							if(thisMeta.isMetadataModified())
								anyMod=true;
						
						//Give the user the option of saving if any file has been modified
						if(anyMod)
							{
							EvBasicWindow.DialogReturnStatus status=EvBasicWindow.showConfirmYesNoCancelDialog("Metadata has been modified. Save before closing all?");
							if(status==DialogReturnStatus.CANCEL)
								return;
							else if(status==DialogReturnStatus.YES)
								{
								for(EvData thisMeta:EvData.openedData)
									if(thisMeta.isMetadataModified())
										{
										try
											{
											thisMeta.saveData();
											thisMeta.setMetadataNotModified();
											}
										catch (IOException e1)
											{
											EvLog.printError("Save error", e1);
											return;
											}
										}
								}
							}

						//Close all files
						EvData.openedData.clear();
						EvBasicWindow.updateWindows();
						System.gc();
						}
					};
				miUnloadAllData.addActionListener(metaListenerUnload);

				
				//// Menu item Listener: Save
				ActionListener metaListenerSave=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						GuiEvDataIO.saveFile(EvData.openedData);
						}
					};
				miSaveAllData.addActionListener(metaListenerSave);
				
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
						EvBasicWindow.updateWindows();
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
							EndrovUtil.openExternalProgram(thisMeta.io.datadir());
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
							EvBasicWindow.DialogReturnStatus ret=EvBasicWindow.showConfirmYesNoCancelDialog("Metadata has been modified. Save before close?");
							if(ret==DialogReturnStatus.YES)
								{
								try
									{
									thisMeta.saveData();
									thisMeta.setMetadataNotModified();
									}
								catch (IOException e1)
									{
									EvLog.printError("Save error", e1);
									return;
									}
								}
							else if (ret==DialogReturnStatus.CANCEL)
								return;
							}
						EvData.openedData.remove(thisMeta);
						EvBasicWindow.updateWindows();
						System.gc();
						}
					};
				miUnload.addActionListener(metaListenerUnload);

				attachSubObjectMenus(menuMetadata, thisMeta);
				
				}
			}
		}
	}