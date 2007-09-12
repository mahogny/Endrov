package evplugin.metadata;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import evplugin.basicWindow.*;
//import evplugin.ev.EV;
//import evplugin.script.Script;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class MetadataBasic implements BasicWindowExtension
	{
	
	public static Vector<MetadataExtension> extensions=new Vector<MetadataExtension>();
	
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new BasicHook());
		}
	private class BasicHook implements BasicWindowHook, ActionListener
		{
		private JMenu mData=new JMenu("Data");
		private JMenuItem miNew=new JMenuItem("New XML");
		private JMenuItem miOpen=new JMenuItem("Read from XML");
		
		public void createMenus(BasicWindow w)
			{
			w.addMenubar(mData);
			buildMenu(w);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==miNew)
				{
				Metadata.addMetadata(new XmlMetadata());
				BasicWindow.updateWindows();
				}
			else if(e.getSource()==miOpen)
				{
				XmlMetadata.loadMeta();
				}
			}

		
	
		
		
		
		public void buildMenu(BasicWindow w)
			{
			BasicWindow.tearDownMenu(mData);
			
			mData.add(miNew);
			mData.add(miOpen);			
			miNew.addActionListener(this);
			miOpen.addActionListener(this);
			for(MetadataExtension e:extensions)
				e.buildOpen(mData);
			mData.addSeparator();
			
			//List all global Metadata
			for(final Metadata thisMeta:Metadata.metadata)
				{
				JMenu menuMetadata=new JMenu(thisMeta.getMetadataName());
				mData.add(menuMetadata);
				
				JMenuItem miUnload=new JMenuItem("Unload");
				JMenuItem miSave=new JMenuItem("Save");
				menuMetadata.add(miUnload);
				menuMetadata.add(miSave);
				
				for(MetadataExtension e:extensions)
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
						Metadata.metadata.remove(thisMeta);
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
				
				
				for(final int obId:thisMeta.metaObject.keySet())
					{
					final MetaObject ob=thisMeta.metaObject.get(obId); //might become problematic?
					JMenu obmenu=new JMenu(""+obId+": "+ob.getMetaTypeDesc());
					menuMetadata.add(obmenu);

					JMenuItem miRemoveOb=new JMenuItem("Remove");
					obmenu.add(miRemoveOb);
					
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
					}
				}
			}
		}
	}