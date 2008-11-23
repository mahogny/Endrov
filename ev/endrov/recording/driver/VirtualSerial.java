package endrov.recording.driver;

import java.util.*;

import javax.swing.*;

import endrov.hardware.PropertyType;
import endrov.recording.HWSerial;

/**
 * Virtual serial device. 
 * @author Johan Henriksson
 *
 */
public class VirtualSerial implements HWSerial
	{
	private String fifoIn="";
	private Object lock=new Object();
	
	
	private static class LineBreak
		{
		String show, real;
		public LineBreak(String show, String real)
			{
			this.show=show;
			this.real=real;
			}
		public String toString(){return show;}
		}
		
	public class VirtualSerialWindow extends JFrame
		{
		static final long serialVersionUID=0;
		
		private JComboBox cLineBreak=new JComboBox(new LineBreak[]{
				new LineBreak("CR/LF","\r\n"),
				new LineBreak("CR","\r"),
				new LineBreak("LF","\n"),
		});;
		private JTextArea tOut=new JTextArea();
		private JTextArea tIn=new JTextArea();
		public VirtualSerialWindow()
			{
			tOut.setEditable(false);
			tIn.setEditable(false);
			
			add(cLineBreak);
			add(tOut);
			add(tIn);
			
			setSize(300, 100);
			setVisible(true);
			}
		}
	
	private VirtualSerialWindow window=null;
	
	public VirtualSerialWindow getWindow()
		{
		if(window==null)
			window=new VirtualSerialWindow();
		return window;
		//window.toFront();
		}
	

	public String nonblockingRead()
		{
		synchronized (lock)
			{
			String s=fifoIn;
			fifoIn="";
			return s;
			}
		}
	public String readUntilTerminal(String term)
		{
		return "123\r\n";
//		return "";//TODO
		}
	public void writePort(String s)
		{
		
		}
	
	
	public String getDescName()
		{
		return "Virtual serial port";
		}
	public SortedMap<String, String> getPropertyMap()
		{
		//what about speed settings etc?
		TreeMap<String, String> m=new TreeMap<String, String>();
		return m;
		}
	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		TreeMap<String, PropertyType> m=new TreeMap<String, PropertyType>();
		return m;
		}
	public String getPropertyValue(String prop)
		{
		return null;
		}
	public boolean getPropertyValueBoolean(String prop)
		{
		return false;
		}
	public void setPropertyValue(String prop, boolean value)
		{
		}
	public void setPropertyValue(String prop, String value)
		{
		}
	
	
	}
