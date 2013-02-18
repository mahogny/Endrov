package endrov.gui.memoryUsageWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.gui.window.EvBasicWindow;

/**
 * Window displaying memory usage over time
 * 
 * @author Johan Henriksson
 */
public class MemoryUsageWindow extends EvBasicWindow implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private MemoryUsageGraphWidget memWidget=new MemoryUsageGraphWidget();
	private Timer timer=new Timer(1000, this);

	
	public MemoryUsageWindow()
		{
		//Put GUI together
		setLayout(new BorderLayout());
		add(memWidget,BorderLayout.CENTER);
		
		//Window overall things
		packEvWindow();
		setBoundsEvWindow(50,50,500,100);
		setLocationEvWindow(null);
		setVisibleEvWindow(true);
		
		timer.start();
		}
	
	
	public void dataChangedEvent()
		{
		}

	public void windowSavePersonalSettings(Element root)
		{
		}

	public void windowLoadPersonalSettings(Element e)
		{
		}

	public void windowEventUserLoadedFile(EvData data)
		{
		}

	public void windowFreeResources()
		{
		timer.stop();
		}

	@Override
	public String windowHelpTopic()
		{
		return "Memory usage window";
		}


	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==timer)
			memWidget.measureMemory();
		}

	}
