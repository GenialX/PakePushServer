package me.pake.push.util;
/*
 * SMTP Email
 */

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.*;

import me.pake.push.conf.EmailConf;

import com.sun.mail.smtp.*;

/**
 * To construct and send an RFC822 (singlepart) message.
 *
 * @author GenialX
 * @email imgenialx@gmail.com
 */

public final class SMTPSend {


    public static void send(String _email, String _subject, String _content) {
	String to 			= _email, 
		   subject 		= _subject, 
		   from 		= EmailConf.FROM, 
		   cc 			= null, 
		   bcc 			= null, 
		   url 			= null; // connect
	String mailhost 	= EmailConf.HOST; // server
	String ServerVersion= "0.0.1";
	String MailServer	= "PakeMailServer";
	String file 		= null;
	String text 		= _content;
	String protocol 	= null, 
		   host 		= EmailConf.HOST,
		   user 		= EmailConf.USER, 
		   password 	= EmailConf.PASS;
	String record 		= null;	// name of folder in which to record mail
	boolean debug 		= EmailConf.DEBUG;
	boolean verbose 	= false;
	boolean auth 		= EmailConf.AUTH;
	String prot 		= "smtp";
	
	try {
		
	    /*
	     * Initialize the JavaMail Session.
	     */
	    Properties props = System.getProperties();
	    if(mailhost != null) {
	    	props.put("mail." + prot + ".host", mailhost);
	    }
	    if(auth) {
	    	props.put("mail." + prot + ".auth", "true");
	    }

	    // Get a Session object
	    Session session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);
	    
	    /*
	     * Construct the message and send it.
	     */
	    Message msg = new MimeMessage(session);
	    if (from != null)
		msg.setFrom(new InternetAddress(from));
	    else
		msg.setFrom();

	    msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
	    if (cc != null)
		msg.setRecipients(Message.RecipientType.CC,
					InternetAddress.parse(cc, false));
	    if (bcc != null)
		msg.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse(bcc, false));

	    msg.setSubject(subject);

	    if (file != null) {
		// Attach the specified file.
		// We need a multipart message to hold the attachment.
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(text);
		MimeBodyPart mbp2 = new MimeBodyPart();
		mbp2.attachFile(file);
		MimeMultipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);
		mp.addBodyPart(mbp2);
		msg.setContent(mp);
	    } else {
		// If the desired charset is known, you can use
		// setText(text, charset)
		msg.setText(text);
	    }
	    
	    msg.setHeader(MailServer, ServerVersion);
	    msg.setSentDate(new Date());
	    msg.setHeader("Content-Type", "text/html; charset=utf-8");

	    // send the thing off
	    /*
	     * The simple way to send a message is this:
	     *
	     *	Transport.send(msg);
	     *
	     * But we're going to use some SMTP-specific features for
	     * demonstration purposes so we need to manage the Transport
	     * object explicitly.
	     */
	    SMTPTransport t = (SMTPTransport)session.getTransport(prot);
	    try {
	    	if(auth) {
	    		t.connect(mailhost, user, password);
	    	} else {
	    		t.connect();
	    	}
	    	t.sendMessage(msg, msg.getAllRecipients());
	    } finally {
			if(verbose) {
				System.out.println("Response: " + t.getLastServerResponse());
			}
			t.close();
	    }

	    System.out.println("\nMail was sent successfully.");

	    /*
	     * Save a copy of the message, if requested.
	     */
	    if (record != null) {
			// Get a Store object
			Store store = null;
			if (url != null) {
			    URLName urln = new URLName(url);
			    store = session.getStore(urln);
			    store.connect();
			} else {
			    if (protocol != null){
			    	store = session.getStore(protocol);
			    } else {
			    	store = session.getStore();
			    }
				
			    // Connect
			    if(host != null || user != null || password != null) {
			    	store.connect(host, user, password);
			    } else {
			    	store.connect();
			    }
			}
	
			// Get record Folder.  Create if it does not exist.
			Folder folder = store.getFolder(record);
			if (folder == null) {
			    System.err.println("Can't get record folder.");
			    System.exit(1);
			}
			if (!folder.exists())
			    folder.create(Folder.HOLDS_MESSAGES);
	
			Message[] msgs = new Message[1];
			msgs[0] = msg;
			folder.appendMessages(msgs);
	
			System.out.println("Mail was recorded successfully.");
	    }

	} catch (Exception e) {
	    /*
	     * Handle SMTP-specific exceptions.
	     */
	    if (e instanceof SendFailedException) {
		MessagingException sfe = (MessagingException)e;
		if (sfe instanceof SMTPSendFailedException) {
		    SMTPSendFailedException ssfe =
				    (SMTPSendFailedException)sfe;
		    System.out.println("SMTP SEND FAILED:");
		    if (verbose)
			System.out.println(ssfe.toString());
		    System.out.println("  Command: " + ssfe.getCommand());
		    System.out.println("  RetCode: " + ssfe.getReturnCode());
		    System.out.println("  Response: " + ssfe.getMessage());
		} else {
		    if (verbose)
			System.out.println("Send failed: " + sfe.toString());
		}
		Exception ne;
		while ((ne = sfe.getNextException()) != null &&
			ne instanceof MessagingException) {
		    sfe = (MessagingException)ne;
		    if (sfe instanceof SMTPAddressFailedException) {
			SMTPAddressFailedException ssfe =
					(SMTPAddressFailedException)sfe;
			System.out.println("ADDRESS FAILED:");
			if (verbose)
			    System.out.println(ssfe.toString());
			System.out.println("  Address: " + ssfe.getAddress());
			System.out.println("  Command: " + ssfe.getCommand());
			System.out.println("  RetCode: " + ssfe.getReturnCode());
			System.out.println("  Response: " + ssfe.getMessage());
		    } else if (sfe instanceof SMTPAddressSucceededException) {
			System.out.println("ADDRESS SUCCEEDED:");
			SMTPAddressSucceededException ssfe =
					(SMTPAddressSucceededException)sfe;
			if (verbose)
			    System.out.println(ssfe.toString());
			System.out.println("  Address: " + ssfe.getAddress());
			System.out.println("  Command: " + ssfe.getCommand());
			System.out.println("  RetCode: " + ssfe.getReturnCode());
			System.out.println("  Response: " + ssfe.getMessage());
		    }
		}
	    } else {
		System.out.println("Got Exception: " + e);
		e.printStackTrace();
		if (verbose)
		    e.printStackTrace();
	    }
	}
    }
}
