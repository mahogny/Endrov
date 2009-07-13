package endrov.recording.recmetMultidim;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Widget for recording settings: imageset meta
 * @author Johan Henriksson
 *
 */
public class RecWidgetRecDesc extends JPanel
	{
	private static final long serialVersionUID = 1L;
	
	
	JTextField tfAuthor=new JTextField();
	JTextField tfSample=new JTextField();
	JTextArea taComment=new JTextArea("\n\n");
	
	
	public RecWidgetRecDesc()
		{
		setBorder(BorderFactory.createTitledBorder("Description"));
		setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridy=0; add(new JLabel("Author"),c);
		c.gridy=1; add(new JLabel("Sample"),c);
		c.gridy=2; add(new JLabel("Comment"),c);


		
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridx=1;
		c.weightx=1;
		
		c.gridy=0; add(tfAuthor,c);
		c.gridy=1; add(tfSample,c);
		
		c.gridy=2;
		c.fill=GridBagConstraints.BOTH ;//| GridBagConstraints.VERTICAL;
		c.weighty=1;
		JScrollPane sp=new JScrollPane(taComment,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(sp,c);

		
		}
	}
