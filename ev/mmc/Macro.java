package mmc;

import java.awt.event.*;

/**
 * Control application by sending virtual key presses and mouse movements
 * @author Johan Henriksson
 */
public abstract class Macro
	{

	
	//not sure if this is right
	public final static int MOUSE_LEFT=InputEvent.BUTTON1_MASK;
	public final static int MOUSE_RIGHT=InputEvent.BUTTON3_MASK;
	public final static int MOUSE_MID=InputEvent.BUTTON2_MASK;
	
	
	
	
	
	
	
	/** Click with given buttons at location */
	public abstract void mouseClick(int x, int y, int buttons);
	
	/**
	 * Type a string
	 */
	public void keyType(String s)
		{
		for(char c:s.toCharArray())
			{
			if(Character.isLetter(c))
				{
				boolean shift=Character.isUpperCase(c);
				c=Character.toLowerCase(c);
				int keycode=0;
				if(c=='a') keycode=KeyEvent.VK_A;
				if(c=='b') keycode=KeyEvent.VK_B;
				if(c=='c') keycode=KeyEvent.VK_C;
				if(c=='d') keycode=KeyEvent.VK_D;
				if(c=='e') keycode=KeyEvent.VK_E;
				if(c=='f') keycode=KeyEvent.VK_F;
				if(c=='g') keycode=KeyEvent.VK_G;
				if(c=='h') keycode=KeyEvent.VK_H;
				if(c=='i') keycode=KeyEvent.VK_I;
				if(c=='j') keycode=KeyEvent.VK_J;
				if(c=='k') keycode=KeyEvent.VK_K;
				if(c=='l') keycode=KeyEvent.VK_L;
				if(c=='m') keycode=KeyEvent.VK_M;
				if(c=='n') keycode=KeyEvent.VK_N;
				if(c=='o') keycode=KeyEvent.VK_O;
				if(c=='p') keycode=KeyEvent.VK_P;
				if(c=='q') keycode=KeyEvent.VK_Q;
				if(c=='r') keycode=KeyEvent.VK_R;
				if(c=='s') keycode=KeyEvent.VK_S;
				if(c=='t') keycode=KeyEvent.VK_T;
				if(c=='u') keycode=KeyEvent.VK_U;
				if(c=='v') keycode=KeyEvent.VK_V;
				if(c=='w') keycode=KeyEvent.VK_W;
				if(c=='x') keycode=KeyEvent.VK_X;
				if(c=='y') keycode=KeyEvent.VK_Y;
				if(c=='z') keycode=KeyEvent.VK_Z;
				keyType(keycode,shift);
				}
			else if(Character.isDigit(c))
				{
				int keycode=0;
				if(c=='1') keycode=KeyEvent.VK_1;
				if(c=='2') keycode=KeyEvent.VK_2;
				if(c=='3') keycode=KeyEvent.VK_3;
				if(c=='4') keycode=KeyEvent.VK_4;
				if(c=='5') keycode=KeyEvent.VK_5;
				if(c=='6') keycode=KeyEvent.VK_6;
				if(c=='7') keycode=KeyEvent.VK_7;
				if(c=='8') keycode=KeyEvent.VK_8;
				if(c=='9') keycode=KeyEvent.VK_9;
				if(c=='0') keycode=KeyEvent.VK_0;
				keyType(keycode,false);
				}
			else if(c=='!') keyType(KeyEvent.VK_EXCLAMATION_MARK,false);
			else if(c=='\"') keyType(KeyEvent.VK_QUOTEDBL,false);
			else if(c=='$') keyType(KeyEvent.VK_DOLLAR,false);
			else if(c=='&') keyType(KeyEvent.VK_AMPERSAND,false);
			else if(c=='/') keyType(KeyEvent.VK_SLASH,false);
			else if(c=='\\') keyType(KeyEvent.VK_BACK_SLASH,false);
			else if(c==':') keyType(KeyEvent.VK_COLON,false);
			else if(c==';') keyType(KeyEvent.VK_SEMICOLON,false);
			else if(c=='@') keyType(KeyEvent.VK_AT,false);
			else if(c=='_') keyType(KeyEvent.VK_UNDERSCORE,false);
			else if(c=='#');// keyType(KeyEvent.VK_QUOTEDBL,false);

			else if(c=='(') keyType(KeyEvent.VK_LEFT_PARENTHESIS,false);
			else if(c==')') keyType(KeyEvent.VK_RIGHT_PARENTHESIS,false);
			//else if(c=='{') keyType(KeyEvent.VK_BRACELEFT,false); //need alt mod
			//else if(c=='}') keyType(KeyEvent.VK_BRACERIGHT,false);
			else if(c=='[') keyType(KeyEvent.VK_OPEN_BRACKET,false); //need alt mod
			else if(c==']') keyType(KeyEvent.VK_CLOSE_BRACKET,false);

			else if(c=='=') keyType(KeyEvent.VK_EQUALS,false);
			else if(c=='<') keyType(KeyEvent.VK_LESS,false);
			else if(c=='>') keyType(KeyEvent.VK_GREATER,false);
			else if(c=='+') keyType(KeyEvent.VK_PLUS,false);
			else if(c=='-') keyType(KeyEvent.VK_MINUS,false);
			
			}
		}

	public void keyDown(){keyType(KeyEvent.VK_DOWN,false);}
	public void keyUp(){keyType(KeyEvent.VK_UP,false);}
	public void keyRight(){keyType(KeyEvent.VK_RIGHT,false);}
	public void keyLeft(){keyType(KeyEvent.VK_LEFT,false);}
	public void keyTab(){keyType(KeyEvent.VK_TAB,false);}
	public void keyHome(){keyType(KeyEvent.VK_HOME,false);}
	public void keyEnd(){keyType(KeyEvent.VK_END,false);}
	
	/**
	 * Type a key
	 */
	public abstract void keyType(int keycode, boolean shift);
	
	/**
	 * Wait for a given number of milliseconds
	 */
	public void delay(int msec)
		{
		try
			{
			Thread.sleep(msec);
			}
		catch (InterruptedException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	
	public abstract String getClipboard();
	
	
	}
