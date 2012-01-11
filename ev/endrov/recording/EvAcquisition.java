package endrov.recording;

import java.util.LinkedList;
import java.util.List;

import endrov.data.EvContainer;
import endrov.data.EvObject;


public abstract class EvAcquisition extends EvObject
	{
	public EvContainer container;
	public String containerStoreName;

	/**
	 * Thread activity listener
	 */
	public interface AcquisitionListener
		{
		public void acquisitionEventStopped();
		public void acquisitionEventStatus(String s);
		}

	public interface AcquisitionThread
		{
		public void stopAcquisition();
		}
	
	public abstract AcquisitionThread startAcquisition();
	
	public void setStoreLocation(EvContainer con, String name)
		{
		container=con;
		containerStoreName=name;
		}
	
	

	protected List<EvAcquisition.AcquisitionListener> listeners=new LinkedList<EvAcquisition.AcquisitionListener>();
	public void addListener(EvAcquisition.AcquisitionListener l)
		{
		listeners.add(l);
		}
	public void removeListener(EvAcquisition.AcquisitionListener l)
		{
		listeners.remove(l);
		}

	}
