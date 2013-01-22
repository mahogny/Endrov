package endrov.recording.windowPlatePositions;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.gui.EvSwingUtil;
import endrov.hardware.EvHardware;
import endrov.recording.RecordingResource;
import endrov.recording.StoredStagePosition;
import endrov.recording.StoredStagePositionAxis;
import endrov.recording.device.HWStage;

/**
 * Checkbox list used to select desired hardware when creating positions
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */

public class RecWidgetAxisInclude extends JPanel
	{
	private static final long serialVersionUID = 1L;

	private HashSet<StoredStagePositionAxis> enabled = new HashSet<StoredStagePositionAxis>();

	public RecWidgetAxisInclude()
		{
		List<StoredStagePositionAxis> allaxis = new LinkedList<StoredStagePositionAxis>();
		for (HWStage stage : EvHardware.getDeviceMapCast(HWStage.class).values())
			for (int i = 0; i<stage.getNumAxis(); i++)
				allaxis.add(new StoredStagePositionAxis(stage, i, stage.getStagePos()[i]));

		JComponent[] comps = new JComponent[allaxis.size()*2];
		for (int i = 0; i<allaxis.size(); i++)
			{
			final JCheckBox b = new JCheckBox();
			final StoredStagePositionAxis info = allaxis.get(i);
			HWStage stage=info.getDevice();
			comps[i*2+0] = b;
			comps[i*2+1] = new JLabel(""+stage.getDescName()+" "+stage.getAxisName()[info.getAxis()]);

			b.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						if (b.isSelected())
							enabled.add(info);
						else
							enabled.remove(info);
						}
				});

			}
		
		setLayout(new GridLayout(1, 1));
		add(EvSwingUtil.layoutTableCompactWide(comps));
		}

	
	private StoredStagePositionAxis[] getEnabledAxis()
		{
		StoredStagePositionAxis[] axisCopy = new StoredStagePositionAxis[enabled.size()];
		axisCopy = enabled.toArray(axisCopy);
		return axisCopy;
		}

	
	
	public StoredStagePosition createCurrentPosition()
		{
		StoredStagePositionAxis[] axis=getEnabledAxis();
			
		StoredStagePositionAxis[] newInfo = new StoredStagePositionAxis[axis.length];
		for (int i = 0; i<getEnabledAxis().length; i++)
			{
			StoredStagePositionAxis a=axis[i];
			newInfo[i] = new StoredStagePositionAxis(
					a.getDevice(),
					a.getAxis(), 
					a.getDevice().getStagePos()[a.getAxis()]);
			}
		
		return new StoredStagePosition(newInfo,	RecordingResource.getUnusedPosName());
		}
	
	
	
	}
