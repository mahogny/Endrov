package endrov.util;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Like a JLabel but splits over several lines
 * @author Johan Henriksson
 *
 */
public class JMultilineLabel extends JPanel
	{
	private static final long serialVersionUID = 1L;

	public JMultilineLabel()
		{
		this("");
		}
	public JMultilineLabel(String label)
		{
		setText(label);
		}
	
	public void setText(String label)
		{
		removeAll();
		StringTokenizer t = new StringTokenizer(label, "\n");
    int numLines = t.countTokens();
		setLayout(new GridLayout(numLines,1));
		while(t.hasMoreTokens())
			{
			//To avoid squashing lines, store " "
			String s=t.nextToken();
			add(new JLabel(s.equals("") ? " " : s));
			}
		setOpaque(isOpaque());
		}
	
	public void setOpaque(boolean v)
		{
		super.setOpaque(v);
		for(Component c:getComponents())
			((JLabel)c).setOpaque(v);
		}
	
	
	}
