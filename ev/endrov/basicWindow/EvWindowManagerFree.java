package endrov.basicWindow;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JFrame;

import endrov.basicWindow.icon.BasicIcon;
import endrov.ev.EV;

/**
 * Ev Window Manager: Free-floating windows
 * @author Johan Henriksson
 */
public class EvWindowManagerFree extends JFrame implements WindowListener, EvWindowManager
	{
	static final long serialVersionUID=0; 
	//this is not needed in later versions of java. just for OSX compatibility
	private static WeakHashMap<Window, Void> java15windowList=new WeakHashMap<Window, Void>();
	public static Collection<Window> getWindows()
		{
		return java15windowList.keySet();
		}

	
	
	
	private WeakReference<BasicWindow> bw;
	private boolean shouldHaveBeenDisposed=false;
	
	
	private BasicWindow getBasicWindow()
		{
		return bw.get();
		}
	
	
	public EvWindowManagerFree(BasicWindow bw)
		{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.bw=new WeakReference<BasicWindow>(bw);
		addWindowListener(this);
		add(bw);

		
    //int titleBarHeight = getInsets().top; //can be used to set the right icon
    //20x20 seems good on windows? or more?
		//16x16 on gnome, but in alt+tab larger. can supply larger image
		if(!EV.isMac())
			setIconImage(BasicIcon.programIcon.getImage());
		
		java15windowList.put(this,null);
		}
	
	public void setTitle(String title)
		{
		super.setTitle(EV.programName+" "+title+" ["+getBasicWindow().windowInstance+"]");
		}
	
	
	public void windowClosing(WindowEvent e) 
		{
	
		}
	public void windowActivated(WindowEvent arg0)	{}
	public void windowDeactivated(WindowEvent arg0)	{}
	public void windowDeiconified(WindowEvent arg0)	{}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0)
		{
		//Remove listeners manually just to be sure GC works smoothly
		//for(WindowListener list:getWindowListeners())
		//	removeWindowListener(list);
		String title=getTitle();
		shouldHaveBeenDisposed=true;
		
		getBasicWindow().freeResourcesBasic();
		//Closing has already invoked dispose()
		System.out.println("window closed: "+title);
		}

	
	public void setResizable(boolean b)
		{
		super.setResizable(b);
		}
	
	
	protected void finalize() throws Throwable
		{
		String title=getTitle();
		System.out.println("Finalize "+title);
		}

	
	public static class Manager implements BasicWindow.EvWindowManagerMaker
		{
		public EvWindowManager createWindow(BasicWindow bw)
			{
			EvWindowManager w=new EvWindowManagerFree(bw);
			return w;
			}
		
		/**
		 * Get a list of all windows
		 */
		public List<BasicWindow> getAllWindows()
			{
			LinkedList<BasicWindow> list=new LinkedList<BasicWindow>();
			for(Window w:getWindows())
				if(w instanceof EvWindowManagerFree)
					{
					EvWindowManagerFree ww=(EvWindowManagerFree)w;
					BasicWindow bw=ww.getBasicWindow();
					if(!ww.shouldHaveBeenDisposed)
						list.add(bw);
					}
			return list;
			}
		
		}

	}
