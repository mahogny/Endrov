package mmc;

import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;


public class Client implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
	{
	private PrintWriter out;
	private ObjectInputStream in;
	private ClientFrame f;
	private ScreenThread screenThread;

	public static void main(String args[])
		{
		new Client();
		}

	public Client()
		{
		String serverName =	JOptionPane.showInputDialog(f, "Enter a JVNC Server name to connect to", "Server", JOptionPane.INFORMATION_MESSAGE);

		f = new ClientFrame();
		f.addWindowListener(new WindowAdapter()
			{
			public void windowClosed(WindowEvent e)
				{
				screenThread.stopRunning();
				sendAndUpdate(Constants.CLOSE, 0, 0);
				System.exit(0);
				}
			});
		f.addKeyListener(this);
		f.getLabel().addMouseListener(this);
		f.getLabel().addMouseMotionListener(this);
		f.getLabel().addMouseWheelListener(this);

		try
			{
			Socket s = new Socket(serverName, 1166);
			out = new PrintWriter(s.getOutputStream());
			in = new ObjectInputStream(s.getInputStream());

			ImageIcon i = (ImageIcon)in.readObject();
			f.setSize(i.getIconWidth()+40, i.getIconHeight()+40);
			f.updateScreen(i);		

			f.setVisible(true);

			screenThread = new ScreenThread();
			Thread thread = new Thread(screenThread);
			thread.start();
			}
		catch (UnknownHostException ex)
			{
			ex.printStackTrace();
			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
		catch (ClassNotFoundException ex)
			{
			ex.printStackTrace();
			}
		}

	private void sendAndUpdate(int type, int arg1, int arg2)
		{
		String s = "" + type + "|" + arg1 + "|" + arg2;
		out.println(s);
		out.flush();
		}

	public void mouseClicked(MouseEvent e)
		{
		}

	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void mousePressed(MouseEvent e)
		{
		int x=e.getX() * Constants.SCALE_AMOUNT;
		int y=e.getY() * Constants.SCALE_AMOUNT;
		System.out.println("MousePress "+ convertMouse(e) + " @ "+x+" " +y);
		sendAndUpdate(Constants.MOUSE_PRESS, convertMouse(e), 0);
		}

	public void mouseReleased(MouseEvent e)
		{
		int x=e.getX() * Constants.SCALE_AMOUNT;
		int y=e.getY() * Constants.SCALE_AMOUNT;
		System.out.println("MouseRelease "+ convertMouse(e) + " @ "+x+" " +y);
		sendAndUpdate(Constants.MOUSE_RELEASE, convertMouse(e), 0);
		}

	
	public void mouseDragged(MouseEvent e)
		{
		sendAndUpdate(Constants.MOUSE_MOVE, e.getX() * Constants.SCALE_AMOUNT, e.getY() * Constants.SCALE_AMOUNT);
		}

	
	public void mouseMoved(MouseEvent e)
		{
		//Moves are not interesting to log
		sendAndUpdate(Constants.MOUSE_MOVE, e.getX() * Constants.SCALE_AMOUNT, e.getY() * Constants.SCALE_AMOUNT);
		}

	public void mouseWheelMoved(MouseWheelEvent e)
		{
		//I don't know about this one
		sendAndUpdate(Constants.MOUSE_WHEEL, e.getScrollAmount(), 0);
		}

	public void keyPressed(KeyEvent e)
		{
		System.out.println("KeyPress, code "+e.getKeyCode()+" char "+e.getKeyChar());
		sendAndUpdate(Constants.KEY_PRESS, e.getKeyCode(), 0);
		}

	public void keyReleased(KeyEvent e)
		{
		System.out.println("KeyRelease, code "+e.getKeyCode()+" char "+e.getKeyChar());
		sendAndUpdate(Constants.KEY_RELEASE, e.getKeyCode(), 0);
		}

	public void keyTyped(KeyEvent e)
		{
		}

	private int convertMouse(MouseEvent e)
		{
		if (SwingUtilities.isLeftMouseButton(e))
			return InputEvent.BUTTON1_MASK;
		if (SwingUtilities.isMiddleMouseButton(e))
			return InputEvent.BUTTON2_MASK;
		else if (SwingUtilities.isRightMouseButton(e))
			return InputEvent.BUTTON3_MASK;
		else
			return 0;
		}

	
	
	private class ScreenThread implements Runnable
		{
		private volatile boolean keepRunning = true;
	
		public void run()
			{
			while (keepRunning)
				{
				try
					{			
					ImageIcon i = (ImageIcon)in.readObject(); //there is no compression!
					f.updateScreen(i);
					System.out.println(""+System.currentTimeMillis()+" get image");
					}
				catch (Exception ex)
					{
					ex.printStackTrace();
					}
				}
			}
	
		public void stopRunning()
			{
			keepRunning = false;
			}
		}
	}
