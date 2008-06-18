package imserv;

import java.awt.GridLayout;

import javax.swing.JFrame;

public class ClientGUI extends JFrame
	{
	public static final long serialVersionUID=0;
	
	ImservDataPane pane=new ImservDataPane();
	
	public ClientGUI()
		{
		setLayout(new GridLayout(1,1));
		add(pane);
		setBounds(0, 0, 300,300);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		}
	
	public static void main(String[] arg)
		{
		new ClientGUI();
		}
	
	
	
	}
