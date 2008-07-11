package endrov.flow;

import java.awt.GridLayout;

import javax.swing.JFrame;

public class Test extends JFrame
	{
	FlowPanel fp=new FlowPanel();
	
	public Test()
		{
		setLayout(new GridLayout(1,1));
		add(fp);
		
		
		pack();
		setSize(300,200);
		setVisible(true);
		}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		Test t=new Test();
		
		}

	}
