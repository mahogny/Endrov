package javawebstart.sub;

import java.net.URL;

import javax.swing.*;


public class Test
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		// TODO Auto-generated method stub

		
		URL url = Test.class.getResource("res.txt"); 

		URL url2 = Test.class.getResource("../bar.txt"); 

		boolean isInJar=url.toString().indexOf(".jar!")!=-1;
	
		
		if(isInJar)
			{
			String jarfileName=url.toString();
			jarfileName=jarfileName.substring(0, jarfileName.lastIndexOf('!'));
			
			/*
			String out="";
			try
				{
				JarFile jf=new JarFile(jarfileName);
				
				Enumeration enu=jf.entries();
				while(enu.hasMoreElements())
					{
					ZipEntry z=(ZipEntry)enu.nextElement();
					out+=z.getName();
					}
				
				}
			catch (IOException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
*/			
			
					JOptionPane.showMessageDialog(null, url.toString()+" is in jar: "+isInJar+ " "+jarfileName+"\n"+url2.toString());
			}

		
		

//		JButton button=new JButton(new ImageIcon(url));
		
//		button.setIcon(new ImageIcon(url));

		
		}

	}
