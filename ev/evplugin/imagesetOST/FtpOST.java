package evplugin.imagesetOST;

//JSR 203
//JAVA 7 will have a VFS, making parts of this code obsolete. do not make any bigger modifications as it will have to be redone anyway.

import com.jcraft.jsch.*;
import javax.swing.*;
import java.awt.*;

public class FtpOST
	{
	public Session session;
	public ChannelSftp c;
	
	public FtpOST(Session session) throws Exception
		{
		this.session=session;
		Channel channel=session.openChannel("sftp");
		channel.connect();
		c=(ChannelSftp)channel;
		}
	
	public FtpOST() throws Exception
		{
		JSch jsch=new JSch();
		
		String host=JOptionPane.showInputDialog("Enter username@hostname", System.getProperty("user.name")+"@localhost");
		String user=host.substring(0, host.indexOf('@'));
		host=host.substring(host.indexOf('@')+1);

		session=jsch.getSession(user, "localhost", 22);

		//Username and password will be given via UserInfo interface.
		UserInfo ui=new MyUserInfo();
		session.setUserInfo(ui);
		session.connect();

		Channel channel=session.openChannel("sftp");
		channel.connect();
		c=(ChannelSftp)channel;
		}
	
	

	public static void main(String[] arg)
		{
		try
			{
			FtpOST ftp=new FtpOST();

			String p1="testfile";
			String p2="testfile";
			SftpProgressMonitor monitor=new MyProgressMonitor();
			int mode=ChannelSftp.OVERWRITE;
			ftp.c.get(p1, p2, monitor, mode);

			}  
		catch(Exception e)
			{
			System.out.println(e);
			}
		System.exit(0);
		}

	private static class MyUserInfo implements UserInfo, UIKeyboardInteractive
		{
		public String getPassword(){ return passwd; }
	
		public boolean promptYesNo(String str)
			{
			Object[] options={ "yes", "no" };
			int foo=JOptionPane.showOptionDialog(null,str,"Warning",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,	null, options, options[0]);
			return foo==0;
			}
	
		String passwd;
		JTextField passwordField=(JTextField)new JPasswordField(20);
	
		public String getPassphrase(){ return null; }
		public boolean promptPassphrase(String message){ return true; }
		public boolean promptPassword(String message)
			{
			Object[] ob={passwordField};
			int result=JOptionPane.showConfirmDialog(null, ob, message,					JOptionPane.OK_CANCEL_OPTION);
			if(result==JOptionPane.OK_OPTION)
				{
				passwd=passwordField.getText();
				return true;
				}
			else return false; 
			}
		
		public void showMessage(String message)
			{
			JOptionPane.showMessageDialog(null, message);
			}
		
		public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo)
			{
			final GridBagConstraints gbc = new GridBagConstraints(0,0,1,1,1,1,	GridBagConstraints.NORTHWEST,	GridBagConstraints.NONE,	new Insets(0,0,0,0),0,0);
			Container panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			panel.add(new JLabel(instruction), gbc);
			gbc.gridy++;

			gbc.gridwidth = GridBagConstraints.RELATIVE;

			JTextField[] texts=new JTextField[prompt.length];
			for(int i=0; i<prompt.length; i++){
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0; 
			gbc.weightx = 1;
			panel.add(new JLabel(prompt[i]),gbc);

			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weighty = 1;
			if(echo[i])
				texts[i]=new JTextField(20);
			else
				texts[i]=new JPasswordField(20);
			panel.add(texts[i], gbc);
			gbc.gridy++;
			}

			if(JOptionPane.showConfirmDialog(null, panel, destination+": "+name, JOptionPane.OK_CANCEL_OPTION,	JOptionPane.QUESTION_MESSAGE)==JOptionPane.OK_OPTION)
				{
				String[] response=new String[prompt.length];
				for(int i=0; i<prompt.length; i++)
					response[i]=texts[i].getText();
				return response;
				}
			else
				return null;  // cancel
			}
		}
	
	
	
	 private static class MyProgressMonitor implements SftpProgressMonitor
		 {
		 ProgressMonitor monitor;
		 long count=0;
		 long max=0;
		 private long percent=-1;
		 
		 public void init(int op, String src, String dest, long max)
			 {
			 this.max=max;
			 monitor=new ProgressMonitor(null,
					 ((op==SftpProgressMonitor.PUT)?
							 "put" : "get")+": "+src,
							 "",  0, (int)max);
			 count=0;
			 percent=-1;
			 monitor.setProgress((int)this.count);
			 monitor.setMillisToDecideToPopup(1000);
			 }
		 
		 public boolean count(long count)
			 {
			 this.count+=count;
	
			 if(percent>=this.count*100/max){ return true; }
			 percent=this.count*100/max;
	
			 monitor.setNote("Completed "+this.count+"("+percent+"%) out of "+max+".");
			 monitor.setProgress((int)this.count);
	
			 return !(monitor.isCanceled());
			 }
		 public void end()
			 {
			 monitor.close();
			 }
		 }

	}
