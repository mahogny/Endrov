package endrov.recording.driver;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
		
	public class VirtualSerialWindow extends JFrame implements ActionListener
		{
		static final long serialVersionUID=0;
		
		private JComboBox cLineBreak=new JComboBox(new LineBreak[]{
				new LineBreak("CR/LF","\r\n"),
				new LineBreak("CR","\r"),
				new LineBreak("LF","\n"),
		});;
		private JTextArea tOut=new JTextArea();
		public JTextArea tIn=new JTextArea();
		public JTextField tInput=new JTextField();
		public VirtualSerialWindow()
			{
			tOut.setEditable(false);
			tIn.setEditable(false);
			tOut.append("Output:\n");
			tIn.append("Input:\n");
			tInput.addActionListener(this);
			
			JPanel pMid=new JPanel(new GridLayout(2,1));
			pMid.add(new JScrollPane(tOut,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
			pMid.add(new JScrollPane(tIn,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
			
			setLayout(new BorderLayout());
			add(pMid,BorderLayout.CENTER);
			add(cLineBreak,BorderLayout.NORTH);
			add(tInput,BorderLayout.SOUTH);
			
			setSize(300, 400);
			setVisible(true);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			String s=tInput.getText();
			tInput.setText("");
			tOut.append(s+"\n"); //LF alone does not break line
			s=s+((LineBreak)cLineBreak.getSelectedItem()).real;
			System.out.println("here");
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
		VirtualSerialWindow w=getWindow();
		return "123\r\n";
//		return "";//TODO
		}
	public void writePort(final String s)
		{
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
			{
			VirtualSerialWindow w=getWindow();
			w.tIn.append(s);
			}
		});
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
