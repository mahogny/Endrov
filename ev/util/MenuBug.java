package util;
import javax.swing.*;

import java.awt.event.*;

public class MenuBug extends JFrame
	{
	public static final long serialVersionUID=0;
	private JMenuBar menubar=new JMenuBar();
	private JMenu menu1=new JMenu("foo");
	private JMenu menu2=new JMenu("bar");
	
		
	public MenuBug()
		{
		add(new JLabel("Hello"));
		
		setJMenuBar(menubar);
		menubar.add(menu1);
		menubar.add(menu2);
		
		JMenuItem mi1=new JMenuItem("foo");
		JMenuItem mi2=new JMenuItem("gc");
		menu1.add(mi1);
		menu2.add(mi2);
		
		mi1.addActionListener(new ActionListener()	
			{
			protected void finalize() throws Throwable
				{
				System.out.println("destroyed1");
				}
			public void actionPerformed(ActionEvent e)
				{
				menu1.removeAll();
				}
			});
		
		
		mi2.addActionListener(new ActionListener()	
			{
			protected void finalize() throws Throwable
				{
				System.out.println("destroyed2");
				}
			public void actionPerformed(ActionEvent e)
				{
				System.gc();
				}
			});
			
		pack();
		setVisible(true);
		}
	
	public static void main(String[] arg)
		{
		new MenuBug();
		}
	
	
	}
