package endrov.recording.positionsWindow;

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

import endrov.hardware.EvHardware;
import endrov.recording.device.HWStage;
import endrov.util.EvSwingUtil;

/**
 * Checkbox list used to select desired hardware when creating positions
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */

public class CheckBoxList extends JPanel
	{
	private static final long serialVersionUID = 1L;

	private HashSet<AxisInfo> enabled = new HashSet<AxisInfo>();

	public CheckBoxList()
		{

		setLayout(new GridLayout(1, 1));

		List<AxisInfo> allaxis = new LinkedList<AxisInfo>();

		for (HWStage stage : EvHardware.getDeviceMapCast(HWStage.class).values())
			{
			for (int i = 0; i<stage.getNumAxis(); i++)
				{
				allaxis.add(new AxisInfo(stage, i, stage.getStagePos()[i]));

				}

			}

		JComponent[] comps = new JComponent[allaxis.size()*2];
		for (int i = 0; i<allaxis.size(); i++)
			{
			final JCheckBox b = new JCheckBox();
			final AxisInfo info = allaxis.get(i);
			comps[i*2+0] = b;
			comps[i*2+1] = new JLabel(""+info.getDevice().getDescName()+" "
					+info.getDevice().getAxisName()[info.getAxis()]);

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
		add(EvSwingUtil.layoutTableCompactWide(comps));
		}

	public AxisInfo[] getInfo()
		{
		AxisInfo[] axisCopy = new AxisInfo[enabled.size()];
		axisCopy = enabled.toArray(axisCopy);
		return axisCopy;

		}

	}
