package endrov.util;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.sun.javafx.newt.MouseEvent;
import com.sun.javafx.newt.MouseListener;



public class RepeatingKeyEventsTest extends JFrame implements KeyListener, MouseListener
	{
	private static final long serialVersionUID = 1L;

	public RepeatingKeyEventsTest()
		{
		add(new JLabel("foo"));
		setSize(300,300);
		addKeyListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		}
	
	public static void main(String[] args)
		{
		new RepeatingKeyEventsFixer().install();
		new RepeatingKeyEventsTest().setVisible(true);
		}

	public void keyPressed(KeyEvent e)
		{
		System.out.println("press");
		}

	public void keyReleased(KeyEvent e)
		{
		System.out.println("release");
		}

	public void keyTyped(KeyEvent e)
		{
		}

	public void mouseClicked(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mouseDragged(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mouseEntered(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mouseExited(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mouseMoved(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mousePressed(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mouseReleased(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	public void mouseWheelMoved(MouseEvent arg0)
		{
		// TODO Auto-generated method stub
		
		}

	}
