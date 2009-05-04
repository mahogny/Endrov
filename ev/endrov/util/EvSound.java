package endrov.util;

import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
import javax.swing.*;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * Quick routines for playing sound. Wraps around other libraries.
 * @author Johan Henriksson
 *
 */
public class EvSound
	{
	Clip clip=null;
	Player p=null;
	
	public EvSound(Class<?> cl, String name)
		{
		URL url=cl.getResource(name);
		if(name.endsWith(".mp3"))
			{
			//TODO only works once. slow?
      try
				{
				p = new Player(new BufferedInputStream(cl.getResourceAsStream(name)));
				}
			catch (JavaLayerException e)
				{
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Problem loading "+url);
				System.exit(0);
				}
			}
		else
			{
			try 
				{
				// Open an audio input stream.
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
				// Get a sound clip resource.
				clip = AudioSystem.getClip();
				// Open audio clip and load samples from the audio input stream.
				clip.open(audioIn);
				} 
			catch (UnsupportedAudioFileException e) 
				{
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Problem loading "+url);
				System.exit(0);
				} 
			catch (IOException e) 
				{
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Problem loading "+url);
				System.exit(0);
				} 
			catch (LineUnavailableException e) 
				{
				//
				e.printStackTrace();
				}
			}
		}
	
	public static Clip loadSound(URL url)
		{
		try 
			{
			// Open an audio input stream.
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			// Get a sound clip resource.
			Clip clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
			//    clip.start();
			return clip;
			} 
		catch (UnsupportedAudioFileException e) 
			{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Problem loading "+url);
			System.exit(0);
			} 
		catch (IOException e) 
			{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Problem loading "+url);
			System.exit(0);
			} 
		catch (LineUnavailableException e) 
			{
			//
			e.printStackTrace();
			}
		return null;
		}
	
	/**
	 * Play sound
	 */
	public void start()
		{
		if(p!=null)
			try
				{
				p.play();
				}
			catch (JavaLayerException e)
				{
				e.printStackTrace();
				}
		else if(clip!=null)
			clip.start();
		}
	
	/**
	 * Play sound clip. Ignore if it is null.
	 */
	/*
	public static void play(Clip clip)
		{
		if(clip!=null)
			clip.start();
		}
	
	
	public static void main(String[] arg)
		{
		Clip snd=EvSound.loadSound(EvSound.class.getResource("bang.wav"));
		play(snd);
		}
		*/
	
	}
