package endrov.recording;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.*;

import javax.swing.*;

import endrov.hardware.PropertyType;
import endrov.util.EvSwingTools;

/**
 * Virtual serial device. 
 * @author Johan Henriksson
 *
 */
public abstract class VirtualSerial implements HWSerial
	{
	/**
	 * Automatic responses. Meant for overriding classes for testing.
	 */
	public Map<String,String> autoresponse=new HashMap<String, String>();

	/**
	 * Return clever response or null if none. Only used if there is no autoresponse
	 */
	public abstract String response(String s);
	
	/**
	 * Incoming queue
	 */
	private StringFIFO fifoIn=new StringFIFO();

	/***************************************************
	 * Line breaks
	 */
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
		
		
	/***************************************************
	 * Status window
	 */
	public class VirtualSerialWindow extends JFrame implements ActionListener, WindowListener
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
		public VirtualSerialWindow(String title)
			{
			tOut.setEditable(false);
			tIn.setEditable(false);
			tInput.addActionListener(this);
			
			JPanel pMid=new JPanel(new GridLayout(2,1));
			pMid.add(EvSwingTools.withLabelAbove("Output",new JScrollPane(tOut,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)));
			pMid.add(EvSwingTools.withLabelAbove("Input", new JScrollPane(tIn,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)));
			
			setLayout(new BorderLayout());
			add(pMid,BorderLayout.CENTER);
			add(cLineBreak,BorderLayout.NORTH);
			add(tInput,BorderLayout.SOUTH);
			
			setTitle("Virtual Serial: "+title);
			setSize(300, 400);
			setVisible(true);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			String s=tInput.getText();
			tInput.setText("");
			tOut.append(s+"\n"); //LF alone does not break line
			s=s+((LineBreak)cLineBreak.getSelectedItem()).real;
			fifoIn.addFifoIn(s);
			}

		public void windowActivated(WindowEvent e){}
		public void windowClosed(WindowEvent e){setWindowGone();}
		public void windowClosing(WindowEvent e){}
		public void windowDeactivated(WindowEvent e){}
		public void windowDeiconified(WindowEvent e){}
		public void windowIconified(WindowEvent e){}
		public void windowOpened(WindowEvent e){}
		}

	/** Current window */
	private VirtualSerialWindow window=null;

	/** Title of window */
	private String serialTitle;

	private void setWindowGone()
		{
		window=null;
		}

	/**
	 * Constructor
	 */
	public VirtualSerial(String title)
		{
		serialTitle=title;
		}
	
	public synchronized VirtualSerialWindow getWindow()
		{
		if(window==null)
			window=new VirtualSerialWindow(serialTitle);
		return window;
		}
	
	
	/////////////////////////////// Serial hardware functions ////////////////////////////////////

	public String nonblockingRead()
		{
		return fifoIn.nonblockingRead();
		}
	public String readUntilTerminal(String term)
		{
		return fifoIn.readUntilTerminal(term);
		}
	public void writePort(final String s)
		{
		SwingUtilities.invokeLater(new Runnable(){
		public void run()
			{
			VirtualSerialWindow w=getWindow();
			w.tIn.append(s);
			
			for(Map.Entry<String, String> me:autoresponse.entrySet())
				if(me.getKey().equals(s))
					{
					w.tOut.append(me.getValue());
					fifoIn.addFifoIn(me.getValue());
					}
			}
		});
		}
	
	
	///////////////////////////////// Generic hardware functions ////////////////////////////////////
	
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
	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}
	public void setPropertyValue(String prop, boolean value)
		{
		}
	public void setPropertyValue(String prop, String value)
		{
		}
	
	
	}
