package endrov.recording.widgets;

import java.util.LinkedList;
import java.util.List;

import endrov.recording.StoredStagePosition;

public class RecSettingsPositions
	{
	public List<StoredStagePosition> positions=new LinkedList<StoredStagePosition>();
	public boolean useAutofocus;
	
	public RecSettingsPositions(LinkedList<StoredStagePosition> positions, boolean useAutofocus)
		{
		this.positions = new LinkedList<StoredStagePosition>(positions);
		this.useAutofocus=useAutofocus;
		}
	
	}