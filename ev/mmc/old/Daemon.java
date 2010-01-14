/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package mmc.old;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.image.*;
import javax.swing.*;


public class Daemon 
	{
	private final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	private final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	private final Rectangle screenRect = new Rectangle(0, 0, WIDTH, HEIGHT);
	
	public static void main(String arg[])
		{
		new Daemon();
		}
	
	public Daemon()
		{
		try
			{
			ServerSocket s = new ServerSocket(1166);
			while (true)
				{
				Socket socket = s.accept();
				RobotThread t = new RobotThread(socket);
				Thread thread = new Thread(t);
				thread.start();
				}
			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
		}        
	
	public void sendImage(ObjectOutputStream out, Robot robot) throws IOException
		{
		BufferedImage i = robot.createScreenCapture(screenRect);
		Image image = i.getScaledInstance(WIDTH/Constants.SCALE_AMOUNT, HEIGHT/Constants.SCALE_AMOUNT, Image.SCALE_SMOOTH);
		out.writeObject(new ImageIcon(image));
		out.flush();
		System.gc();
		System.out.println(""+System.currentTimeMillis()+" sent image");
		}
	
	
	private class CaptureThread implements Runnable
		{
		private ObjectOutputStream out;
		private Robot robot;
		private volatile boolean keepRunning = true;
		
		public CaptureThread(Robot r, ObjectOutputStream o)
			{
			out = o;
			robot = r;
			}
		
		public void run()   
			{
			while (keepRunning)
				{
				try
					{
					sendImage(out, robot);
					}  
				catch (IOException ex)
					{
					//ex.printStackTrace();
					System.out.println("Closing down this server thread");
					keepRunning=false;
					}
				}        
			}
		
		public void stopRunning()
			{
			keepRunning = false; 
			}
		
		}
	
	private class RobotThread implements Runnable
		{
		private Robot robot;
		private BufferedReader in;
		private ObjectOutputStream out;
		private CaptureThread captureThread;
		
		RobotThread(Socket socket)
			{
			try
				{
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				robot = new Robot();
			
				sendImage(out, robot);
			
				captureThread = new CaptureThread(robot, out);
				Thread thread = new Thread(captureThread);
				thread.start();
				}
			catch (IOException ex)
				{
				ex.printStackTrace();
				}
			catch (AWTException ex)
				{   
				ex.printStackTrace();
				}
			}
		
		public void run()
			{
			while (true)
				{
				try
					{
					String e = (String)in.readLine();
					int eventType = Integer.parseInt(e.substring(0, e.indexOf("|")));
					int arg1 = Integer.parseInt(e.substring(e.indexOf("|")+1, e.lastIndexOf("|")));
					int arg2 = Integer.parseInt(e.substring(e.lastIndexOf("|")+1));
					if (eventType == Constants.CLOSE)
						{
						captureThread.stopRunning();
						in.close();
						out.close();
						return;
						}
					else if (eventType == Constants.KEY_PRESS)
						robot.keyPress(arg1);
					else if (eventType == Constants.KEY_RELEASE)
						robot.keyRelease(arg1);
					else if (eventType == Constants.MOUSE_MOVE)
						robot.mouseMove(arg1, arg2);
					else if (eventType == Constants.MOUSE_PRESS)
						robot.mousePress(arg1);
					else if (eventType == Constants.MOUSE_RELEASE)
						robot.mouseRelease(arg1);
					else if (eventType == Constants.MOUSE_WHEEL)
						robot.mouseWheel(arg1);
					}
				catch (IOException ex)
					{
					ex.printStackTrace();
					}
				}
			}
		
		
		
		}
	}
