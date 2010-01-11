package endrov.keyBinding;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.*;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;

import org.jdom.*;

/**
 * Adjust Frame-Time mapping
 * @author Johan Henriksson
 */
public class WindowKeyBinding extends BasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;


	/**
	 * Make a new window at default location
	 */
	public WindowKeyBinding()
		{
		this(50,100,300,600);
		}

	private JPanel listPane=new JPanel();

	private JButton bNewScriptBinding=new JButton("New script binding");
	
	/**
	 * Make a new window at some specific location
	 */
	public WindowKeyBinding(int x, int y, int w, int h)
		{				
		
		JPanel wholePane=new JPanel(new BorderLayout());
		wholePane.add(listPane,BorderLayout.NORTH);
		JScrollPane spane=new JScrollPane(wholePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		bNewScriptBinding.addActionListener(this);
		
		setLayout(new BorderLayout());
		add(spane, BorderLayout.CENTER);
		add(bNewScriptBinding, BorderLayout.SOUTH);

		//Window overall things
		setTitleEvWindow("Key Bindings");
		packEvWindow();
		setBoundsEvWindow(x,y,w,h);
		setVisibleEvWindow(true);

		fillList();
		}

	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bNewScriptBinding)
			{
			ScriptBinding.list.add(new ScriptBinding());
			fillList();
			}
		}

	/**
	 * Get a constraint for the layout
	 */
	private GridBagConstraints gconstraint(int x, int y, int w, double weightx)
		{
		GridBagConstraints c=new GridBagConstraints();
		c.gridx=x;
		c.gridy=y;
		c.gridwidth=w;
		c.fill=GridBagConstraints.HORIZONTAL;// | GridBagConstraints.VERTICAL;
		c.weightx=weightx;
		return c;
		}

	
	/**
	 * Fill list with key bindings
	 */
	public void fillList()
		{
		//One might want to sort
		listPane.removeAll();
		listPane.setLayout(new GridBagLayout());
		
		int y=0;
		
		//Sort by plugin name
		TreeMap<String,TreeSet<KeyBinding>> keyGroups=new TreeMap<String, TreeSet<KeyBinding>>();
		for(KeyBinding b:KeyBinding.bindings.values())
			{
			TreeSet<KeyBinding> kg=keyGroups.get(b.pluginName);
			if(kg==null)
				keyGroups.put(b.pluginName, kg=new TreeSet<KeyBinding>());
			kg.add(b);
			}
		
		//Place all special key bindings
		for(Map.Entry<String, TreeSet<KeyBinding>> e:keyGroups.entrySet())
			{
			JPanel p=new JPanel();
			p.setBorder(BorderFactory.createTitledBorder(e.getKey()));
			listPane.add(p,gconstraint(0,y,3,1));
			y++;
			p.setLayout(new GridLayout(e.getValue().size(),2));
			
			for(final KeyBinding b:e.getValue())
				{
				p.add(new JLabel(b.description));
				JButton key=new JButton(b.getKeyDesc());
				p.add(key);
				key.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						new SetKey(b);
						}
					});
				}
			
			
			}
		
		//All script key bindings
		for(int i=0;i<ScriptBinding.list.size();i++)
			{
			final ScriptBinding b=ScriptBinding.list.get(i);
			JButton field=new JButton(b.script);
			if(b.script.equals(""))
				field.setText(" ");
			listPane.add(field,gconstraint(0,y,1,1));
			JButton key=new JButton(b.key.getKeyDesc());
			listPane.add(key,gconstraint(1,y,1,1));
			
			JButton bDelete=BasicIcon.getButtonDelete();
			listPane.add(bDelete,gconstraint(2,y,1,0));
			
			key.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					new SetKey(b.key);
					}
				});
			bDelete.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					ScriptBinding.list.remove(b);
					fillList();
					}
				});
			
			final int icopy=i;
			field.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					String newText=JOptionPane.showInputDialog(null, "Script code",b.script);
					if(newText!=null)
						{
						if(newText.equals(""))
							//Remove item
							ScriptBinding.list.remove(icopy);
						else
							//Set script
							b.script=newText;
						fillList();
						}
					}
				});
			y++;
			}
		
		 
		//Make sure all new components are visible
		setVisibleEvWindow(true);
		repaint();
		}
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		}
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		}

	public void loadedFile(EvData data){}

	
	/**
	 * Set new key for key binding
	 */
	private class SetKey extends JDialog implements KeyListener
		{
		static final long serialVersionUID=0;
		KeyBinding b;
		public SetKey(KeyBinding b)
			{
			this.b=b;
			add(new JLabel("Press new key"));
			setEnabled(true);
			addKeyListener(this);
			pack();
			setLocation(200, 200);
			setVisible(true);
			}
		public void keyPressed(KeyEvent e)
			{
			if(e.getKeyCode()==KeyEvent.VK_UNDEFINED)
				{
				b.types.clear();
				b.types.add(new KeyBinding.TypeChar(e.getKeyChar()));
				}
			else
				{
				b.types.clear();
				b.types.add(new KeyBinding.TypeKeycode(e.getKeyCode(),0));
				}
			fillList();
			dispose();
			}
		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
		}
	
	public void freeResources(){}

	
	}
