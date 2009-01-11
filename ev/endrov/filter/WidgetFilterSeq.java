package endrov.filter;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

import endrov.basicWindow.icon.BasicIcon;
import endrov.ev.SimpleObserver;
import endrov.filter.FilterImageExtension.*;

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
			filterseq.observerGUI.remove(this);
		filterseq=m;
		if(filterseq!=null)
			filterseq.observerGUI.addWeakListener(this);
		buildList();
		}
	public void observerEvent(Object o)
		{
		//Is the widget in this list? in that case, do nothing.
		if(!filterWidgets.contains(o))
			{
			System.out.println("buildlist "+o);
			buildList();
			}
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
//								BasicWindow.updateWindows();  //TODO updateWindows
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
	private Vector<JComponent> filterWidgets=new Vector<JComponent>();
	public void buildList()
		{
		inScroll.removeAll();		
		filterWidgets.clear();
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
				else
					filterWidgets.add(fc);
				
				JPanel titledPanel=new JPanel(new GridLayout(1,1));				
				titledPanel.setBorder(BorderFactory.createTitledBorder(f.getFilterName()));
				titledPanel.add(fc);
				
				//Buttons
				final JButton bUp=new JButton(BasicIcon.iconButtonUp);
				final JButton bDown=new JButton(BasicIcon.iconButtonDown);
				final JButton bDelete=BasicIcon.getButtonDelete();

				ActionListener listener=new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						if(e.getSource()==bUp)
							{
							filterseq.moveUp(currentPos);
	//						BasicWindow.updateWindows();   //TODO updateWindows
//							buildList();
							}
						else if(e.getSource()==bDown)
							{
							filterseq.moveDown(currentPos);
	//						BasicWindow.updateWindows();    //TODO updateWindows
		//					buildList();
							}
						else if(e.getSource()==bDelete)
							{
							filterseq.delete(currentPos);
		//					BasicWindow.updateWindows();   //TODO updateWindows
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
