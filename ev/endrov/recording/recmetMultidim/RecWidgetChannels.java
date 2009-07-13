package endrov.recording.recmetMultidim;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.basicWindow.SpinnerSimpleEvDecimal;
import endrov.basicWindow.SpinnerSimpleInteger;
import endrov.basicWindow.icon.BasicIcon;
import endrov.util.JImageButton;

/**
 * Widget for recording settings: Channel settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetChannels extends JPanel
	{
	private static final long serialVersionUID = 1L;

	
	/**
	 * * current settings, what to call channel?
	 * * auto exposure?
	 * * z-scan while recording? a la emilie
	 */
	

	private ArrayList<Row> entrylist=new ArrayList<Row>();
	
	private JComponent p=new JPanel();
	
	
	public static class ChannelEntry
		{
		
		}
	
	
	private JButton bAdd=new JImageButton(BasicIcon.iconAdd,"Add channel to list");
	
	private static class Row
		{
		JComboBox comboChannel=new JComboBox(new Object[]{"foo"});
		SpinnerSimpleEvDecimal spExposure=new SpinnerSimpleEvDecimal();
		JCheckBox chLightCompensate=new JCheckBox();
		JButton bUp=new JImageButton(BasicIcon.iconButtonUp,"Move toward first in order");
		JButton bDown=new JImageButton(BasicIcon.iconButtonDown,"Move toward end in order");
		
		JButton bRemove=new JImageButton(BasicIcon.iconRemove,"Remove channel from list");
		
		SpinnerSimpleInteger spZinc=new SpinnerSimpleInteger(1,1,1000,1);
		SpinnerSimpleInteger spZ0=new SpinnerSimpleInteger(0,0,1000,1);
		SpinnerSimpleInteger spTinc=new SpinnerSimpleInteger(1,1,1000,1);
		}
	
	
	
	
	
	public RecWidgetChannels()
		{
		setBorder(BorderFactory.createTitledBorder("Channnels"));
		setLayout(new BorderLayout());
		add(new JLabel("TODO"),BorderLayout.NORTH);
		add(p,BorderLayout.CENTER);
		addChannel();
		addChannel();
		addChannel();
		layoutChannels();
		}
	
	
	private void addChannel()
		{
		Row row=new Row();
		entrylist.add(row);
		}
	
	private void layoutChannels()
		{
		p.removeAll();
		p.setLayout(new GridBagLayout());

		//For each line and description
		for(int i=-1;i<entrylist.size();i++)
			{
			GridBagConstraints c=new GridBagConstraints();
			c.gridy=i+1;
			c.gridx=0;
			
			Row row=null;
			if(i!=-1) row=entrylist.get(i);
	
			
			if(i==-1) p.add(bAdd,c);
			else p.add(row.bRemove,c);
			c.gridx++;

			if(i==-1) ;
			else p.add(row.bUp,c);
			c.gridx++;

			if(i==-1) ;
			else p.add(row.bDown,c);
			c.gridx++;


			c.fill=GridBagConstraints.HORIZONTAL;
			c.weightx=1;
			if(i==-1) p.add(new JLabel("Channel"),c);
			else p.add(row.comboChannel,c);
			c.gridx++;
			
			if(i==-1) p.add(new JLabel("Exp [s]"),c);
			else p.add(row.spExposure,c);
			c.weightx=0;
			c.gridx++;
			
			if(i==-1) p.add(new JLabel("L.C"),c);
			else p.add(row.chLightCompensate,c);
			c.gridx++;
			
			if(i==-1)	p.add(new JLabel("Z++"),c);
			else p.add(row.spZinc,c);
			c.gridx++;
			
			if(i==-1)	p.add(new JLabel("Z0"),c);
			else p.add(row.spZ0,c);
			c.gridx++;
			
			if(i==-1)	p.add(new JLabel("t++"),c);
			else p.add(row.spTinc,c);
			c.gridx++;
			

			
			
			}
		
		
		}
	

	}
