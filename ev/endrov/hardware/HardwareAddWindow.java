package endrov.hardware;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;

public class HardwareAddWindow extends BasicWindow
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
		
		
		
	
	JButton bAdd=new JButton("Add");
	
	
	public HardwareAddWindow()
		{
		this(new Rectangle(400,300));
		}
	
	/*
	public static class AddHW
		{
		HardwareProvider p;
		String hw;
		public String toString()
			{
			return ""+p.getName()+":"+hw;
			}
		}*/
	
	
	public HardwareAddWindow(Rectangle bounds)
		{
		//Vector<AddHW> hwlist=new Vector<AddHW>();
		
		
		//JList hwList=new JList(hwlist);

		

		
		setLayout(new BorderLayout());
		//add(hwList,BorderLayout.CENTER);
		add(bAdd,BorderLayout.SOUTH);
		
		//Window overall things
		setTitleEvWindow("Hardware Configuration");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		// TODO Auto-generated method stub
		
		}

	public void loadedFile(EvData data){}

	public void windowPersonalSettings(Element e)
		{
		// TODO Auto-generated method stub
		
		} 
	
	public void freeResources(){}
	/*
	public static void main(String[] arg)
		{
		new HardwareConfigWindow();
		}
*/	
	
	}
