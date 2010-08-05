package endrov.recording.widgets;

import java.util.ArrayList;
import java.util.Arrays;

import endrov.recording.widgets.RecWidgetOrder.OrderEntry;

/**
 * Recording dimension order settings
 * @author Johan Henriksson
 *
 */
public class RecSettingsDimensionsOrder
	{
	public final ArrayList<OrderEntry> entrylist=new ArrayList<OrderEntry>();
	
	public static final String ID_POSITION="position";
	public static final String ID_CHANNEL="channel";
	public static final String ID_SLICE="slice";
	
	public RecSettingsDimensionsOrder(OrderEntry... entry)
		{
		entrylist.addAll(Arrays.asList(entry));
		}
	
	public static RecSettingsDimensionsOrder createStandard()
		{
		return new RecSettingsDimensionsOrder(
				new OrderEntry(ID_POSITION,"Position"),
				new OrderEntry(ID_CHANNEL,"Channel"),
				new OrderEntry(ID_SLICE,"Slice"));
		}
	}