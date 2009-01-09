package endrov.util;

import java.awt.Component;
import java.awt.GridLayout;
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
		String[] lines=label.split("\n");
		setLayout(new GridLayout(lines.length,1));
		for(String line:lines)
			{
			//To avoid squashing lines, store " "
			add(new JLabel(line));
			//add(new JLabel(s.equals("") ? " " : s));
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
