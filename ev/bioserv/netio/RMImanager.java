package bioserv.netio;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;


//////
// Message format 
//
// msg := <call id>::short <msgsize>::int <msgtype>::byte {<callbody>::[byte] | <returnbody>::[byte]}
//
// one message can be split into several packages, allowing the queue to be sent
// as round-robin. this is needed if several processes need to share the line.
// return and call are treated a bit differently, it should never be possible for a client
// to manually return as this opens up for too many security issues.
// msgsize is the size of what follows.
//
// msgtype=0   call, end of transmission
// msgtype=1   call, will continue
// msgtype=2   return, end of transmission
// msgtype=3   return, will continue
// <bit 0>willcontinue
// <bit 1>isreturn
//
// callbody := <function>::string <arguments....>::untyped
// 
// the arguments has to be known on receiving end as meta information is not transmitted
// for bandwidth reasons
//
// returnbody := <arguments....>::untyped
//
// returnbody as callbody

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
	private static final byte MSGBIT_RET=2;

	public static final int packetSize=50000; 

/*	private static final byte MSGTYPE_CALLEND=0;
	private static final byte MSGTYPE_CALLCONT=MSGBIT_CONT;
	private static final byte MSGTYPE_RETEND=MSGBIT_RET;
	private static final byte MSGTYPE_RETCONT=MSGBIT_RET+MSGBIT_CONT;*/
	
	
	private Socket socket;
	
	
	/*****************************************************************************
	 * One registered method
	 */
	private static class RegMethod
		{
		private Method m;
		private Class<?>[] c;
		
		public RegMethod(Method m)
			{
			this.m=m;
			c=m.getParameterTypes();
			}
		
		public void recv(Message msg)
			{
			try
				{
				m.invoke(null, msg.unpack(c));
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		}
	
	
	/***********************************************************************
	 * Message pending to be sent
	 */
	private static class SendingMessage
		{
		Message msg;
		int offset=0;
		int msgid;
		
		//mark as return... best done in message. how to do it safely?
		
		
		public void putBytes(OutputStream os) throws IOException
			{
			byte msgtype=0;
			
			int totlen=msg.getBytesLength();
			
			int left=totlen-offset;
			boolean sendLast;
			if(left>packetSize)
				{
				left=packetSize;
				sendLast=false;
				msgtype|=MSGBIT_CONT;
				}
			else
				sendLast=true;
			
			
			//<call id>::short 
			//<msgsize>::int 
			//<msgtype>::byte
			os.write(msgtype);
			//{<callbody>::[byte] | <returnbody>::[byte]}
			msg.write(os, offset, left);
			}
		}
	
	
	
	private HashMap<String, RegMethod> regfunc=new HashMap<String, RegMethod>();
	private HashMap<Integer, Runnable> cb=new HashMap<Integer, Runnable>();
	private int nextCB=0;
	
	private LinkedList<SendingMessage> sendq=new LinkedList<SendingMessage>();
	private HashMap<Integer,Message> recvq=new HashMap<Integer, Message>(); //msgid -> msg

	public RMImanager(Socket s)
		{
		socket=s;
		}

	
	public synchronized int registerCallback(Runnable r)
		{
		synchronized (cb)
			{
			//Can in theory fill up entire queue
			while(cb.containsKey(nextCB))
				nextCB++;
			cb.put(nextCB, r);
			return nextCB;
			}
		}
	
	
	public void invokeCallback(int id)
		{
		Runnable r;
		synchronized (cb)
			{
			r=cb.remove(id);
			}
		if(r!=null)
			new Thread(r).run();
		//r.run();
		//which semantic is wanted?
		}
	
	
	
	
	/**
	 * Register a method
	 */
	public void regFunc(String netname, Method m)
		{
		RegMethod a=new RegMethod(m);
		System.out.println("reg: "+netname);
		regfunc.put(netname, a);
		}
	
	/**
	 * Register all methods in a class, based on if they are annotated
	 */
	public void regClass(Class<?> c)
		{
		for(Method m:c.getDeclaredMethods())
			{
			NetFunc a=m.getAnnotation(NetFunc.class);
			for(Annotation aa:m.getAnnotations())
				System.out.println(aa);
					
			System.out.println("hm "+m+" "+a);
			if(a!=null)
				regFunc(a.name(), m);
			}
		}
	
	private Object synchMsgID=new Object();
	private int nextMsgID=0;
	
	
	public void send(Message msg)
		{
		RegMethod m=regfunc.get(msg.getCommand());
		
		int thisid=0;
		synchronized(synchMsgID)
			{
			thisid=nextMsgID;
			nextMsgID++;

			//TODO: need to check if free
			
			SendingMessage smsg=new SendingMessage();
			smsg.msg=msg;
			smsg.msgid=thisid;
			recvq.put(smsg.msgid,msg);
			sendq.add(smsg);
			}
		
		
		
		
		//Loopback
		if(m!=null)
			m.recv(msg);
		}
	
	}
