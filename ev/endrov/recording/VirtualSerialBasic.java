package endrov.recording;

/**
 * Virtual serial device: No automatic replies or anything. Override VirtualSerial to create a proper testing environment
 * @author Johan Henriksson
 */
public class VirtualSerialBasic extends VirtualSerial
	{
	public VirtualSerialBasic(String title)
		{
		super(title);
		}
	public VirtualSerialBasic()
		{
		super("-");
		}
	public String response(String s)
		{
		return null;
		}
	}
