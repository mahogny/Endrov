package bioserv.netio;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import endrov.util.EvUtilBits;



//////
// Message format 
//
// msg := <call id>::short <msgsize>::int <msgtype>::byte <function>::string <arguments....>::untyped
//
// one message can be split into several packages, allowing the queue to be sent
// as round-robin. this is needed if several processes need to share the line.
// return and call are treated a bit differently, it should never be possible for a client
// to manually return as this opens up for too many security issues.
// msgsize is the size of what follows.
//
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

/*	private static final byte MSGTYPE_CALLEND=0;
	private static final byte MSGTYPE_CALLCONT=MSGBIT_CONT;
	private static final byte MSGTYPE_RETEND=MSGBIT_RET;
	private static final byte MSGTYPE_RETCONT=MSGBIT_RET+MSGBIT_CONT;*/
	
	
	private Socket socket;
	private OutputStream socketo;
	
	
	/*****************************************************************************
	 * One registered method
	 */
	public static class RegMethod
		{
		public Method m;
		public Class<?>[] c;
		
		public RegMethod(Method m)
			{
			this.m=m;
			c=m.getParameterTypes();
			}
		}
	
	
	/***********************************************************************
	 * Message pending to be sent
	 */
	private static class SendingMessage
		{
		SMessage msg;
		int offset=0;
		int msgid;
		
		//mark as return... best done in message. how to do it safely?
		
		
		public void putBytes(OutputStream os) throws IOException
			{
			byte msgtype=0;
			
			int totlen=msg.getBytesLength();
			
			int left=totlen-offset;
			if(left>packetSize)
				{
				left=packetSize;
				msgtype|=MSGBIT_CONT;
				}

			short msgid=0;
			
			//<call id>::short 
			os.write(EvUtilBits.shortToByteArray(msgid));
			//<msgsize>::int 
			os.write(EvUtilBits.intToByteArray(left));
			//<msgtype>::byte
			os.write(msgtype);
			//{<callbody>::[byte] | <returnbody>::[byte]}
			msg.write(os, offset, left);
			}
		}
	
	
	
	private HashMap<String, RegMethod> regfunc=new HashMap<String, RegMethod>();
	private HashMap<Integer, RegMethod> cb=new HashMap<Integer, RegMethod>();
	private int nextCB=0;
	
	private Object qlock=new Object();
	private LinkedList<SendingMessage> sendq=new LinkedList<SendingMessage>();
	private HashMap<Integer,SMessage> recvq=new HashMap<Integer, SMessage>(); //msgid -> msg

	public RMImanager(Socket s) throws IOException
		{
		socket=s;
		socketo=socket.getOutputStream();
		}

	
	public synchronized int registerCallback(RegMethod r)
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
		final RegMethod r;
		synchronized (cb)
			{
			r=cb.remove(id);
			}
		if(r!=null)
			new Thread(){
			public void run()
				{
				try
					{
					r.m.invoke(null, new Object[]{}); //TODO
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}
			}.run();
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
	
	
	public void recv(SMessage msg)
		{
		try
			{
			//if return, check type of callback
			//if func, check type of func
			msg.unpackAndInvoke(regfunc,cb,0);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		}
	
	public Thread rmithread=new Thread(){
	public void run()
		{
		for(;;)
			{
			SendingMessage sm;
			synchronized (qlock)
				{
				while(sendq.isEmpty())
					try
					{
					sendq.wait();
					}
				catch (InterruptedException e)
					{
					e.printStackTrace();
					}
				sm=sendq.removeFirst();
				}
			
			
			
			if(sm!=null)
				synchronized (qlock)
					{
					sendq.addLast(sm);
					}
			}
		}
	};
	
	public void send(SMessage msg)
		{
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
		try
			{
			recv(msg);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	}
