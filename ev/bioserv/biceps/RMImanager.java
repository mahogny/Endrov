package bioserv.biceps;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import endrov.util.EvUtilBits;

//name: BIdirective Paranoid Message EXchange 
//BIPMEX RMI

//BICEPS
//bidirective command exchange, paranoid security


//////
// Message format 
//
// msg := <call id>::short <msgsize>::int <packetstatus>::byte ....part of content, #msgsize bytes....
// content := <function>::string <arguments....>::untyped
//
// one message can be split into several packages, allowing the queue to be sent
// as round-robin. this is needed if several processes need to share the line.
// return and call are treated a bit differently, it should never be possible for a client
// to manually return as this opens up for too many security issues.
// msgsize is the size of what follows.
//
// packetstatus:
// <bit 0>willcontinue
//
// the arguments has to be known on receiving end as meta information is not transmitted
// for bandwidth reasons. function is "" if returning value.
//

/**
 * RMI management. Registration of methods etc.
 * 
 * Every call has an ID that is used up until returned. It is important that there is one
 * instance of the ID space for each session or there is a potential for a DoS attack.
 * 
 * @author Johan Henriksson
 *
 */
public class RMImanager
	{
	private static final byte MSGBIT_CONT=1;

	public static final int packetSize=50000; 

	//The socket has the same send queue length as in C, more or less
	private Socket socket;
	private OutputStream socketo;
	private InputStream socketi;
	private RMImanager loopback;
	
	private HashMap<String, RegMethod> regfunc=new HashMap<String, RegMethod>();
	private HashMap<Short, RegMethod> callbacks=new HashMap<Short, RegMethod>();
	private LinkedList<SendingMessage> sendq=new LinkedList<SendingMessage>();
	private HashMap<Short,IncomingMessage> recvq=new HashMap<Short, IncomingMessage>(); //msgid -> msg

	private Object synchMsgID=new Object();
	private short nextMsgID=0;

	private RMImanager rmithis=this;
	
	
	
	/***********************************************************************
	 * Message pending to be sent
	 */
	private static class SendingMessage
		{
		final byte[] buf;
		final short msgid;
		int offset=0;
		
		public SendingMessage(short msgid, byte[] buf)
			{
			this.msgid=msgid;
			this.buf=buf;
			}
		
		public boolean putBytes(OutputStream os) throws IOException
			{
			byte msgtype=0;
			
			boolean done=true;
			int len=buf.length-offset;
			if(len>packetSize)
				{
				len=packetSize;
				msgtype|=MSGBIT_CONT;
				done=false;
				}
			
			//<call id>::short 
			os.write(EvUtilBits.shortToByteArray(msgid));
			//<msgsize>::int 
			os.write(EvUtilBits.intToByteArray(len));
			//<packetstatus>::byte
			os.write(msgtype);
			//{<callbody>::[byte] | <returnbody>::[byte]}
			os.write(buf, offset, len);
			os.flush();
			offset+=len;
			return done;
			}
		}
	
	/***********************************************************************
	 * Incoming message
	 */
	private static class IncomingMessage
		{
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		}
	
	/***********************************************************************
	 * Sending thread
	 */
	private Thread sendthread=new Thread(){
		public void run()
			{
			for(;;)
				{
				//Take one message off the queue
				SendingMessage sm;
				synchronized (sendq) //qlock
					{
					while(sendq.isEmpty())
						try{sendq.wait();}
						catch (InterruptedException e){e.printStackTrace();}
					sm=sendq.removeFirst();
					}
				if(sm!=null)
					{
					try
						{
						if(!sm.putBytes(getOutputStream()))
							addSendQueue(sm);
						System.out.println("sent bytes, now q#="+sendq.size());
						}
					catch (IOException e)
						{
						//No idea what to do in this case
						addSendQueue(sm);
						e.printStackTrace();
						}
					}
				}
			}
		};
	
	/***********************************************************************
	 * Receiving thread
	 */
	private Thread recvthread=new Thread(){
		public void run()
			{
			for(;;)
				try
					{
					System.out.println("ready to recv "+rmithis);
					//Receive one message
					//<call id>::short
					short callID=(short)readShort(getInputStream());
					
					System.out.println("callid "+callID);
					
					//<msgsize>::int
					int msgSize=readInt(getInputStream());
					//<packetstatus>::byte
					int packetStatus=getInputStream().read();
					//package body
					byte[] msg=new byte[msgSize];
					getInputStream().read(msg);

					System.out.println("recv "+msgSize+" "+packetStatus);
					//Collect body
					IncomingMessage inc=recvq.get(callID);
					if(inc==null)
						recvq.put(callID, inc=new IncomingMessage());
					inc.bos.write(msg);

					//When ready to parse, no MSGBIT_CONT, execute
					if(packetStatus==0)
						try
							{
							//TODO: does it stall?
							Message.unpackAndInvoke(inc.bos.toByteArray(), rmithis, regfunc, callbacks, callID);
							recvq.remove(callID);
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
			}
	};	
		
	

	
	
	

	/**
	 * Create RMI session from existing connection
	 */
	public RMImanager(Socket s) throws IOException
		{
		socket=s;
		socketo=socket.getOutputStream();
		socketi=socket.getInputStream();
		sendthread.start();
		recvthread.start();
		}
	
	/**
	 * Connect to another RMI manager. This is a loopback interface
	 */
/*	public RMImanager(RMImanager rmi) throws IOException
		{
		loopback=rmi;
		}
/*
	
	
	/**
	 * Register a method
	 */
	public void regFunc(String netname, Object mthis, Method m)
		{
		RegMethod a=new RegMethod(mthis,m);
		System.out.println("reg: "+netname);
		regfunc.put(netname, a);
		}
	
	/**
	 * Register all methods in a class, based on if they are annotated
	 */
	public void regClass(Class<?> c, Object mthis)
		{
		for(Method m:c.getDeclaredMethods())
			{
			NetFunc a=m.getAnnotation(NetFunc.class);
			/*for(Annotation aa:m.getAnnotations())
				System.out.println(aa);
			System.out.println("found func "+m+" "+a);*/
			if(a!=null)
				regFunc(a.name(), mthis, m);
			}
		}
	
	/**
	 * Connect to remote host
	 */
	public static RMImanager connect(String host, int port) throws IOException
		{
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
		
		//Allow anonymous handshake ie no certificate needed
		LinkedList<String> okCipher=new LinkedList<String>();
		for(String s:socket.getSupportedCipherSuites())
			if(s.contains("_anon_"))
				okCipher.add(s);
		socket.setEnabledCipherSuites(okCipher.toArray(new String[]{}));
				
		return new RMImanager(socket);
		}
	
	
	/**
	 * Queue a message for sending
	 */
	public int send(Message msg)
		{
		return send(msg,null);
		}

	/**
	 * Queue a message for sending
	 */
	public int send(Message msg, Short thisid)
		{
		synchronized(synchMsgID)
			{
			if(thisid==null)
				{
				thisid=nextMsgID;
				nextMsgID++;
				}
			//TODO: theoretically need to check if free

			//Register callback first
			if(msg.getCallback()!=null)
				callbacks.put(thisid, msg.getCallback());
			
			//Send message
			if(loopback!=null)
				{
				//TODO send message
//				loopback.recvq.put(smsg.msgid,msg);
				}
			else
				{
				SendingMessage smsg=new SendingMessage(thisid,msg.getBytes());
				addSendQueue(smsg);
				
				System.out.println("sent");
				}
			return thisid;
			}
		}
	
	private static class CallClass
		{
		public Object returnval;
		public Semaphore sem=new Semaphore(0);
		
		//@SuppressWarnings("unused")
		public void run(Integer o)
			{
//			System.out.println("cb "+o);
			returnval=o;
			sem.release();
			}
		}
	
	public Object call(Serializable arg[],String command) throws IOException
		{
		CallClass cc=new CallClass();
		Message msg=Message.withCallback(arg,command,cc);
		
		
		
		//Send message
		/*int thisid=*/send(msg,null);
		try
			{
			cc.sem.acquire();
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		
		return cc.returnval;
		}
	
	private static int readShort(InputStream socketi) throws IOException
		{return EvUtilBits.byteArrayToShort((byte)socketi.read(),(byte)socketi.read());}
	private static int readInt(InputStream socketi) throws IOException
		{return EvUtilBits.byteArrayToInt((byte)socketi.read(),(byte)socketi.read(),(byte)socketi.read(),(byte)socketi.read());}
	
	/** Add message last on send queue */
	private void addSendQueue(SendingMessage sm)
//		{synchronized (qlock){sendq.addLast(sm);}}
		{synchronized (sendq){sendq.addLast(sm); sendq.notify();}}
	
	private InputStream getInputStream(){return socketi;}
	private OutputStream getOutputStream(){return socketo;}
	}
