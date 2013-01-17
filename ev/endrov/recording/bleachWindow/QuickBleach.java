package endrov.recording.bleachWindow;

import java.util.LinkedList;
import java.util.List;
import endrov.basicWindow.BasicWindow;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.recording.RecordingResource;
import endrov.recording.device.HWImageScanner;
import endrov.roi.ROI;
import endrov.util.EvDecimal;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class QuickBleach 
	{
	private EvDecimal bleachTime;
	private ROI roi;
	private List<Listener> listeners=new LinkedList<Listener>();
	
	
	
	public void setBleachTime(EvDecimal bleachTime)
		{
		this.bleachTime = bleachTime;
		}

	public ROI getRoi()
		{
		return roi;
		}

	public void setRoi(ROI roi)
		{
		this.roi = roi;
		}
	
	
	/**
	 * Thread activity listener
	 */
	public interface Listener
		{
		public void acqStopped();
		}
	
	
	public void addListener(Listener l)
		{
		listeners.add(l);
		}

	public void removeListener(Listener l)
		{
		listeners.remove(l);
		}
	
	
	/**
	 * Thread to perform acquisition
	 */
	public class AcqThread extends Thread
		{
		private boolean toStop=true;
		
		public boolean isRunning()
			{
			return !toStop || isAlive();
			}
		
		public void tryStop()
			{
			toStop=true;
			}
		
		@Override
		public void run()
			{
			//Get current camera
			EvDevicePath campath=EvHardware.getCoreDevice().getCurrentDevicePathImageScanner();
			HWImageScanner scanner=EvHardware.getCoreDevice().getCurrentImageScanner();


			//Check that there are enough parameters
			if(scanner!=null)
				{
				synchronized (RecordingResource.acquisitionLock)
					{
//				Object lockCamera=RecordingResource.blockLiveCamera();

					try
						{
						//Acquire image before bleaching
						BasicWindow.updateWindows();

						//Bleach ROI
						double stageX=RecordingResource.getCurrentStageX();
						double stageY=RecordingResource.getCurrentStageY();
						String normalExposureTime=scanner.getPropertyValue("Exposure");
						scanner.setPropertyValue("Exposure", ""+bleachTime);
						int[] roiArray=RecordingResource.makeScanningROI(campath, scanner, roi, stageX, stageY);
						scanner.scan(null, null, roiArray);
						scanner.setPropertyValue("Exposure", normalExposureTime);
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}

		//			RecordingResource.unblockLiveCamera(lockCamera);
					
					BasicWindow.updateWindows();
					}

				}


		
			toStop=false;
			for(Listener l:listeners)
				l.acqStopped();
			}
		
		public void stopAcquisition()
			{
			toStop=true;
			}
		
		private void startAcquisition()
			{
			if(!isRunning())
				{
				toStop=false;
				start();
				}
			}
		
		}
	
	/**
	 * Get acquisition thread that links to this data
	 */
	public AcqThread startAcquisition()
		{
		AcqThread th=new AcqThread();
		th.startAcquisition();
		return th;
		}
	}
