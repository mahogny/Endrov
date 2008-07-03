package endrov.basicWindow;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
//http://java.sun.com/products/javamail/


/*
 * # Configuration file for javax.mail
# If a value for an item is not provided, then
# system defaults will be used. These items can
# also be set in code.

# Host whose mail services will be used
# (Default value : localhost)
mail.host=mail.blah.com

# Return address to appear on emails
# (Default value : username@host)
mail.from=webmaster@blah.net

# Other possible items include:
# mail.user=
# mail.store.protocol=
# mail.transport.protocol=
# mail.smtp.host=
# mail.smtp.user=
# mail.debug= 
 */

//requires an SMTP server, messy. should one set up a special service instead?

public class SendCommentDialog
	{
	
	
	
	public static void main( String... aArguments )
		{
		SendCommentDialog emailer = new SendCommentDialog();
		//the domains of these email addresses should be valid,
		//or the example will fail:
		emailer.sendEmail(
				"fromblah@blah.com", "johan.henriksson@ki.se",
				"Testing 1-2-3", "blah blah blah"
		);
		}
	
	/**
	 * Send a single email.
	 */
	public void sendEmail(
			String aFromEmailAddr, String aToEmailAddr,
			String aSubject, String aBody
	)
		{
		//Here, no Authenticator argument is used (it is null).
		//Authenticators are used to prompt the user for user
		//name and password.
		Session session = Session.getDefaultInstance( fMailServerConfig, null );
		MimeMessage message = new MimeMessage( session );
		try 
			{
			//the "from" address may be set in code, or set in the
			//config file under "mail.from" ; here, the latter style is used
			//message.setFrom( new InternetAddress(aFromEmailAddr) );
			message.addRecipient(
					Message.RecipientType.TO, new InternetAddress(aToEmailAddr)
			);
			message.setSubject( aSubject );
			message.setText( aBody );
			Transport.send( message );
			}
		catch (MessagingException ex)
			{
			System.err.println("Cannot send email. " + ex);
			}
		}
	
	/**
	 * Allows the config to be refreshed at runtime, instead of
	 * requiring a restart.
	 */
	public static void refreshConfig()
		{
		fMailServerConfig.clear();
		fetchConfig();
		}
	
	//PRIVATE //
	
	private static Properties fMailServerConfig = new Properties();
	
	static 
		{
		//fetchConfig();
		}
	
	/**
	 * Open a specific text file containing mail server
	 * parameters, and populate a corresponding Properties object.
	 */
	private static void fetchConfig()
		{
		InputStream input = null;
		try 	
			{
			//If possible, one should try to avoid hard-coding a path in this
			//manner; in a web application, one should place such a file in
			//WEB-INF, and access it using ServletContext.getResourceAsStream.
			//Another alternative is Class.getResourceAsStream.
			//This file contains the javax.mail config properties mentioned above.
			input = new FileInputStream( "C:\\Temp\\MyMailServer.txt" );
			fMailServerConfig.load( input );
			}
		catch ( IOException ex )
			{
			System.err.println("Cannot open and load mail server properties file.");
			}
		finally 
			{
			try 
				{
				if ( input != null ) input.close();
				}
			catch ( IOException ex )
				{
				System.err.println( "Cannot close mail server properties file." );
				}
			}
		}



	}
