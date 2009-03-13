package endrov.frameTime;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import endrov.basicWindow.EvDropDownButton;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Frame control
 * @author Johan Henriksson
 *
 */
public class MyFrameControl extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	public EvDropDownButton buttonFrameTime=new EvDropDownButton("FT")
		{
		private static final long serialVersionUID = 1L;

		@Override
		public JPopupMenu createPopup()
			{
			JPopupMenu popup = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("A popup menu item");
			menuItem.addActionListener(this);
			popup.add(menuItem);
			menuItem = new JMenuItem("Another popup menu item");
			menuItem.addActionListener(this);
			popup.add(menuItem);
			
			return popup;
			}
		};
	
	public MyFrameControl()
		{
		setLayout(new GridLayout(1,1));
		add(buttonFrameTime);
		buttonFrameTime.addActionListener(this);
		}
	
	public void actionPerformed(ActionEvent e)
		{
		
		
		}
	
	

	/**
	 * Show time as minutes and seconds
	 */
	public static String frameControlMinutes(EvDecimal d)
		{
		Tuple<EvDecimal,EvDecimal> ms=d.divideRemainder(new EvDecimal(60));
		StringBuffer sb=new StringBuffer();
		//if(!ms.fst().equals(EvDecimal.ZERO))
		sb.append(ms.fst()+"m");
		//if(!ms.fst().equals(EvDecimal.ZERO))
		sb.append(ms.snd()+"s");
		return sb.toString();
		}

	/**
	 * Parse time, in seconds, from a string representation
	 */
	public static EvDecimal parseTime(String s)
		{
		EvDecimal accTime=EvDecimal.ZERO;
		Pattern pvalue=Pattern.compile("([0-9]+(?:[.][0-9]+)?[mhs]?)?([0-9]+(?:[.][0-9]+)?[mhs]?)?([0-9]+(?:[.][0-9]+)?[mhs]?)?");
		Matcher m=pvalue.matcher(s);
		if(!m.matches())
			return null;
		for(int i=1;i<=m.groupCount();i++)
			{
			/*
			System.out.println(m.group(i));
			int pos2=i==m.groupCount() ? s.length() : m.start(i+1);
			String spart=s.substring(m.start(i),pos2);
			System.out.println(spart+" "+spartlen+" "+pos2+" ");
			*/
			String spart=m.group(i);
			if(spart!=null)
				{
				int spartlen=spart.length();
				char lastChar=spart.charAt(spartlen-1);
				if(lastChar=='s')
					accTime=accTime.add(new EvDecimal(spart.substring(0,spartlen-1)));
				else if(lastChar=='m')
					accTime=accTime.add(new EvDecimal(spart.substring(0,spartlen-1)).multiply(new EvDecimal(60)));
				else if(lastChar=='h')
					accTime=accTime.add(new EvDecimal(spart.substring(0,spartlen-1)).multiply(new EvDecimal(3600)));
				else
					accTime=accTime.add(new EvDecimal(spart));
				}
			}
		return accTime;
		}

	

	public static void main(String[] args)
		{
		System.out.println(parseTime("5.2s"));
		System.out.println(parseTime("1m3s"));
		}
	
	}
