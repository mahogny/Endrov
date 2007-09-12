package evplugin.imageset;

import org.jdom.*;

import java.awt.GridLayout;
import java.io.File;
import java.util.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;


import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.metadata.*;
import evplugin.script.*;

public class ImagesetMeta extends MetaObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	
	private static final String metaType="imageset";
	
	public static void initPlugin() {}
	static
		{
		Script.addCommand("ost", new CmdOST());
		
		MetadataBasic.extensions.add(new MetadataExtension()
			{
			public void buildOpen(JMenu menu)
				{
				final JMenuItem miLoadVWBImageset=new JMenuItem("Load OST imageset");
				menu.add(miLoadVWBImageset);
				final JMenuItem miLoadVWBImagesetPath=new JMenuItem("Load OST imageset by path");
				menu.add(miLoadVWBImagesetPath);
				
				ActionListener listener=new ActionListener()
					{
					/**
					 * Show dialog for opening a new native imageset
					 */
					public void actionPerformed(ActionEvent e)
						{
						if(e.getSource()==miLoadVWBImageset)
							{
							JFileChooser chooser = new JFileChooser();
					    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					    chooser.setCurrentDirectory(new File(Imageset.lastImagesetPath));
					    int returnVal = chooser.showOpenDialog(null); //null=window
					    if(returnVal == JFileChooser.APPROVE_OPTION)
					    	{
					    	String filename=chooser.getSelectedFile().getAbsolutePath();
					    	Imageset.lastImagesetPath=chooser.getSelectedFile().getParent();
					    	load(filename);
					    	}
							}
						else if(e.getSource()==miLoadVWBImagesetPath)
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
								load(fileName);
							}
						}


					public void load(String filename)
						{
			    	//doesn't really show, but better than nothing
			    	JFrame loadingWindow=new JFrame(EV.programName); 
			    	loadingWindow.setLayout(new GridLayout(1,1));
			    	loadingWindow.add(new JLabel("Loading imageset"));
			    	loadingWindow.pack();
			    	loadingWindow.setBounds(200, 200, 300, 50);
			    	loadingWindow.setVisible(true);
			    	loadingWindow.repaint();
			    	

			
			 //   	Metadata.metadata.add(new EmptyImageset());
			    	Metadata.metadata.add(new OstImageset(filename));
			    	BasicWindow.updateWindows();
			    	loadingWindow.dispose();
						}
					
					};
				miLoadVWBImageset.addActionListener(listener);
				miLoadVWBImagesetPath.addActionListener(listener);
				}
			public void buildSave(JMenu menu, Metadata meta)
				{
				}
			});
		
		Metadata.extensions.put(metaType,new MetaObjectExtension()
			{
			public MetaObject extractObjects(Element e)
				{
				ImagesetMeta meta=new ImagesetMeta();
				
				for(Object oi:e.getChildren())
					{
					Element i=(Element)oi;
					
					if(i.getName().equals("timestep"))
						meta.metaTimestep=Double.parseDouble(i.getValue());
					else if(i.getName().equals("resX"))
						meta.resX=Double.parseDouble(i.getValue());
					else if(i.getName().equals("resY"))
						meta.resY=Double.parseDouble(i.getValue());
					else if(i.getName().equals("resZ"))
						meta.resZ=Double.parseDouble(i.getValue());
					else if(i.getName().equals("NA"))
						meta.metaNA=Double.parseDouble(i.getValue());
					else if(i.getName().equals("objective"))
						meta.metaObjective=Double.parseDouble(i.getValue());
					else if(i.getName().equals("optivar"))
						meta.metaOptivar=Double.parseDouble(i.getValue());
					else if(i.getName().equals("campix"))
						meta.metaCampix=Double.parseDouble(i.getValue());
					else if(i.getName().equals("slicespacing"))
						meta.metaSlicespacing=Double.parseDouble(i.getValue());
					else if(i.getName().equals("sample"))
						meta.metaSample=i.getValue();
					else if(i.getName().equals("description"))
						meta.metaDescript=i.getValue();
					else if(i.getName().equals("channel"))
						{
						ImagesetMeta.Channel ch=extractChannel(meta, i);
						meta.channel.put(ch.name, ch);
						}
					else if(i.getName().equals("frame"))
						extractFrame(meta.metaFrame, i);
					else
						meta.metaOther.put(i.getName(), i.getValue());
					}
				
				return meta;
				}
			
			/**
			 * Extract channel XML data
			 */
			public ImagesetMeta.Channel extractChannel(ImagesetMeta data, Element e)
				{
				ImagesetMeta.Channel ch=new ImagesetMeta.Channel();
				ch.name=e.getAttributeValue("name");
				
				for(Object oi:e.getChildren())
					{
					Element i=(Element)oi;
					
					if(i.getName().equals("dispX"))
						ch.dispX=Double.parseDouble(i.getValue());
					else if(i.getName().equals("dispY"))
						ch.dispY=Double.parseDouble(i.getValue());
					else if(i.getName().equals("binning"))
						ch.chBinning=Integer.parseInt(i.getValue());
					else if(i.getName().equals("frame"))
						extractFrame(ch.metaFrame, i);
					else
						ch.metaOther.put(i.getName(), i.getValue());
					}
				
				return ch;
				}
			
			/**
			 * Get frame metadata
			 */
			public void extractFrame(HashMap<Integer,HashMap<String,String>> metaFrame, Element e)
				{
				int fid=Integer.parseInt(e.getAttributeValue("frame"));
				for(Object oi:e.getChildren())
					{
					Element i=(Element)oi;
					HashMap<String,String> frame=metaFrame.get(fid);
					if(frame==null)
						{
						frame=new HashMap<String,String>();
						metaFrame.put(fid, frame);
						}
					frame.put(i.getName(), i.getValue());
					}
				
				}
			
			});
		}

	
	/******************************************************************************************************
	 *                               Channel                                                              *
	 *****************************************************************************************************/

	/**
	 * Channel specific meta data
	 */
	public static class Channel
		{
		public String name;
		
		/** Binning, a scale factor from the microscope */
		public int chBinning=1;
		
		/** Displacement */
		public double dispX=0, dispY=0;
		
		/** Other */
		public HashMap<String,String> metaOther=new HashMap<String,String>();
		
		/** frame data */
		public HashMap<Integer,HashMap<String,String>> metaFrame=new HashMap<Integer,HashMap<String,String>>();
		
		
		
		
		/** Get (other) meta data in form of a string (default="") */
		public String getMetaValueString(String s)
			{
			String t=metaOther.get(s);
			if(t==null)	return "";
			else return t;
			}

		/** Get (other) meta data in form of a double (default=0) */
		public double getMetaValueDouble(String s)
			{
			String t=getMetaValueString(s);
			if(t.equals("")) return 0;
			else return Double.parseDouble(t);
			}
		
		/**
		 * Get a common frame. Creates structure if it does not exist.
		 */
		public HashMap<String,String> getMetaFrame(int fid)
			{
			HashMap<String,String> frame=metaFrame.get(fid);
			if(frame==null)
				{
				frame=new HashMap<String,String>();
				metaFrame.put(fid, frame);
				}
			return frame;
			}
		}

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	/** Common resolution [px/um] */
	public double resX, resY, resZ;
	
	/** Number of seconds each frame */
	public double metaTimestep=1;
	
	public double metaNA=0;
	public double metaObjective=1;
	public double metaOptivar=1;
	public double metaCampix=1;
	public double metaSlicespacing=1;
	public String metaSample="";
	public String metaDescript="";
	
	/** Other */
	public HashMap<String,String> metaOther=new HashMap<String,String>();
	
	/** Frame data */
	public HashMap<Integer,HashMap<String,String>> metaFrame=new HashMap<Integer,HashMap<String,String>>();
	
	/** Channel specific data */
	public HashMap<String,Channel> channel=new HashMap<String,Channel>();

	public String getMetaTypeDesc()
		{
		return metaType;
		}

	
	/** Get (other) meta data in form of a string (default="") */
	public String getMetaValueString(String s)
		{
		String t=metaOther.get(s);
		if(t==null)	return "";
		else return t;
		}

	/** Get (other) meta data in form of a double (default=0) */
	public double getMetaValueDouble(String s)
		{
		String t=getMetaValueString(s);
		if(t.equals("")) return 0;
		else return Double.parseDouble(t);
		}
	
	/**
	 * Get a common frame. Creates structure if it does not exist.
	 */
	public HashMap<String,String> getMetaFrame(int fid)
		{
		HashMap<String,String> frame=metaFrame.get(fid);
		if(frame==null)
			{
			frame=new HashMap<String,String>();
			metaFrame.put(fid, frame);
			}
		return frame;
		}
	
	
	/**
	 * Get a channel. Creates structure if it does not exist.
	 */
	public Channel getChannel(String ch)
		{
		Channel c=channel.get(ch);
		if(c==null)
			{
			c=new Channel();
			c.name=ch;
			channel.put(ch,c);
			}
		return c;
		}
	
	/**
	 * Save down data
	 */
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		
		//Common
		e.addContent(new Element("resX").addContent(""+resX));
		e.addContent(new Element("resY").addContent(""+resY));
		e.addContent(new Element("resZ").addContent(""+resZ));
		e.addContent(new Element("timestep").addContent(""+metaTimestep));
		e.addContent(new Element("NA").addContent(""+metaNA));
		e.addContent(new Element("objective").addContent(""+metaObjective));
		e.addContent(new Element("optivar").addContent(""+metaOptivar));
		e.addContent(new Element("campix").addContent(""+metaCampix));
		e.addContent(new Element("slicespacing").addContent(""+metaSlicespacing));
		e.addContent(new Element("sample").addContent(""+metaSample));
		e.addContent(new Element("description").addContent(""+metaDescript));
		for(String key:metaOther.keySet())
			e.addContent(new Element(key).addContent(""+metaOther.get(key)));
		saveFrameMetadata(metaFrame, e);
		
		//Channels
		for(Channel ch:channel.values())
			{
			Element elOstChannel=new Element("channel");
			elOstChannel.setAttribute("name", ch.name);
			e.addContent(elOstChannel);
			
			elOstChannel.addContent(new Element("binning").addContent(""+ch.chBinning));
			elOstChannel.addContent(new Element("dispX").addContent(""+ch.dispX));
			elOstChannel.addContent(new Element("dispY").addContent(""+ch.dispY));
			for(String key:ch.metaOther.keySet())
				elOstChannel.addContent(new Element(key).addContent(""+ch.metaOther.get(key)));
			saveFrameMetadata(ch.metaFrame, elOstChannel);
			}
		
		}
	
	/**
	 * Save down frame data
	 */
	private static void saveFrameMetadata(HashMap<Integer,HashMap<String,String>> fd, Element e)
		{
		for(int fid:fd.keySet())
			{
			Element frameEl=new Element("frame");
			frameEl.setAttribute("frame", ""+fid);
			
			HashMap<String,String> frame=fd.get(fid);
			for(String field:frame.keySet())
				{
				String value=frame.get(field);
				Element fieldEl=new Element(field);
				fieldEl.addContent(value);
				frameEl.addContent(fieldEl);
				}
			
			e.addContent(frameEl);
			}
		}
	
	}
