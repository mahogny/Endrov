package evplugin.filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import evplugin.filter.FilterImageExtension.*;

/**
 * Widget for editing a filter sequence
 * 
 * @author Johan Henriksson
 */
public class WidgetFilterSeq extends JPanel
	{
	static final long serialVersionUID=0;
	
	private FilterSeq filterseq=null;	
	
	private static ImageIcon iconButtonUp    =new ImageIcon(WidgetFilterSeq.class.getResource("buttonUp.png"));
	private static ImageIcon iconButtonDown  =new ImageIcon(WidgetFilterSeq.class.getResource("buttonDown.png"));
	private static ImageIcon iconButtonDelete=new ImageIcon(WidgetFilterSeq.class.getResource("buttonDelete.png"));
	
	JPanel inScroll=new JPanel(new BorderLayout());
	public JScrollPane scroll=new JScrollPane(inScroll,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);	
	
	public WidgetFilterSeq()
		{
		buildMenu();
		setLayout(new GridLayout(1,1));
		add(scroll);
		buildList();
		}
	
	
	public void setFilterSeq(FilterSeq m)
		{
		filterseq=m;
		buildList();
		}
	
	public FilterSeq getFilterSeq()
		{
		return filterseq;
		}
	
	/**
	 * Build menu for adding filters
	 */
	public JMenu buildMenu()
		{
		JMenu mAdd=new JMenu("Add");
		FilterImageExtension.fillFilters(mAdd, new BindListener()
			{
			public void bind(final FilterInfo fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						FilterROI firoi=fi.filterROI();
						filterseq.sequence.add(firoi);
						buildList();
						
						}
					});
				}
			});
		return mAdd;
		}
	
	/**
	 * Build GUI list of all filters
	 */
	public void buildList()
		{
		inScroll.removeAll();		
		if(filterseq!=null)
			{
			JPanel inScroll2=new JPanel(new GridBagLayout());
			inScroll.add(inScroll2,BorderLayout.NORTH);
			for(int pos=0;pos<filterseq.sequence.size();pos++)
				{
				final int currentPos=pos;
				Filter f=filterseq.sequence.get(pos);
				
				JComponent fc=f.getFilterWidget();
				if(fc==null)
					fc=new JLabel("No parameters");
				
				
				JPanel titledPanel=new JPanel(new GridLayout(1,1));				
				titledPanel.setBorder(BorderFactory.createTitledBorder(f.getFilterName()));
				titledPanel.add(fc);
				
				//Buttons
				final JButton bUp=new JButton(iconButtonUp);
				final JButton bDown=new JButton(iconButtonDown);
				final JButton bDelete=new JButton(iconButtonDelete);

				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						if(e.getSource()==bUp)
							{
							if(currentPos>0)
								{
								Filter from=filterseq.sequence.get(currentPos);
								filterseq.sequence.remove(currentPos);
								filterseq.sequence.add(currentPos-1, from);
								buildList();
								}
							}
						else if(e.getSource()==bDown)
							{
							if(currentPos<filterseq.sequence.size()-1)
								{
								Filter from=filterseq.sequence.get(currentPos);
								filterseq.sequence.remove(currentPos);
								filterseq.sequence.add(currentPos+1, from);
								buildList();
								}
							}
						else if(e.getSource()==bDelete)
							{
							filterseq.sequence.remove(currentPos);
							buildList();
							}
						}
					};				
				bUp.addActionListener(listener);
				bDown.addActionListener(listener);
				bDelete.addActionListener(listener);

				//Put everything together for this filter
				JPanel bp=new JPanel(new GridLayout(2,2));
				bp.add(bUp);
				bp.add(bDelete);
				bp.add(bDown);
				bp.add(new JLabel(""));
				JPanel pc=new JPanel(new BorderLayout());
				pc.add(titledPanel,BorderLayout.CENTER);
				pc.add(bp,BorderLayout.EAST);
				
				//Add filter to list
				Insets ins=new Insets(0, 0, 0, 0);
				inScroll2.add(pc, new GridBagConstraints(0,pos,1,1,1,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,ins,0,0));
				}
			
			//need to update view here?
			inScroll.setVisible(false);
			inScroll.setVisible(true);
			}
		}
	
	
	
	}
