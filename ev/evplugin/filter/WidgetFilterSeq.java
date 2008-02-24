package evplugin.filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.ev.SimpleObserver;
import evplugin.filter.FilterImageExtension.*;

/**
 * Widget for editing a filter sequence
 * 
 * @author Johan Henriksson
 */
public class WidgetFilterSeq extends JPanel implements SimpleObserver.Listener
	{
	static final long serialVersionUID=0;
	
	private FilterSeq filterseq=null;	
	

	JPanel inScroll=new JPanel(new BorderLayout());
	public JScrollPane scroll=new JScrollPane(inScroll,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);	
	
	public WidgetFilterSeq()
		{
//buildMenu();
		setLayout(new GridLayout(1,1));
		add(scroll);
		buildList();
		}
	
	
	public void setFilterSeq(FilterSeq m)
		{
		if(filterseq!=null)
			filterseq.observer.remove(this);
		filterseq=m;
		if(filterseq!=null)
			filterseq.observer.addWeakListener(this);
		buildList();
		}
	public void observerEvent(Object o)
		{
	//	buildList();
		//TODO bugs. need do something more clever?
		
		}
	
	public FilterSeq getFilterSeq()
		{
		return filterseq;
		}
	
	/**
	 * Build menu for adding filters
	 */
	public void buildMenu(JMenu mAdd)
		{
		mAdd.removeAll();
		BindListener b=new BindListener()
			{
			public void bind(final Object fi, JMenuItem mi)
				{
				mi.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						if(filterseq!=null)
							{
							if(fi instanceof FilterInfo)
								{
								Filter firoi=((FilterInfo)fi).filterROI();
								filterseq.addFilter(firoi);
//								BasicWindow.updateWindows();  //TODO: delete
								}
							else if(fi!=filterseq)
								{
								FilterSeq fs=(FilterSeq)fi;
								filterseq.addFilter(fs);
//								BasicWindow.updateWindows();  //TODO: delete
								}
							buildList();
							}
						}
					});
				}
			};

		FilterImageExtension.fillFilters(mAdd, b);
		FilterImageExtension.fillFilterSeq(mAdd, b);
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
			for(int pos=0;pos<filterseq.getNumFilters();pos++)
				{
				final int currentPos=pos;
				Filter f=filterseq.getFilter(pos);
				
				JComponent fc=f.getFilterWidget();
				if(fc==null)
					fc=new JLabel("No parameters");
				
				
				JPanel titledPanel=new JPanel(new GridLayout(1,1));				
				titledPanel.setBorder(BorderFactory.createTitledBorder(f.getFilterName()));
				titledPanel.add(fc);
				
				//Buttons
				final JButton bUp=new JButton(BasicWindow.getIconUp());
				final JButton bDown=new JButton(BasicWindow.getIconDown());
				final JButton bDelete=new JButton(BasicWindow.getIconDelete());

				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						if(e.getSource()==bUp)
							{
							filterseq.moveUp(currentPos);
	//						BasicWindow.updateWindows();   //TODO: delete
//							buildList();
							}
						else if(e.getSource()==bDown)
							{
							filterseq.moveDown(currentPos);
	//						BasicWindow.updateWindows();    //TODO: delete
		//					buildList();
							}
						else if(e.getSource()==bDelete)
							{
							filterseq.delete(currentPos);
		//					BasicWindow.updateWindows();   //TODO: delete
	//						buildList();   //TODO: can be deleted if window listens to changes
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
