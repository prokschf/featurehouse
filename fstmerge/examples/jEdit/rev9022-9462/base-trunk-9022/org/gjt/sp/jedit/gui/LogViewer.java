

package org.gjt.sp.jedit.gui;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.util.Log;



public class LogViewer extends JPanel implements DefaultFocusComponent,
	EBComponent
{
	
	public LogViewer()
	{
		super(new BorderLayout());

		JPanel caption = new JPanel();
		caption.setLayout(new BoxLayout(caption,BoxLayout.X_AXIS));
		caption.setBorder(new EmptyBorder(6,6,6,6));

		String settingsDirectory = jEdit.getSettingsDirectory();
		if(settingsDirectory != null)
		{
			String[] args = { MiscUtilities.constructPath(
				settingsDirectory, "activity.log") };
			JLabel label = new JLabel(jEdit.getProperty(
				"log-viewer.caption",args));
			caption.add(label);
		}

		caption.add(Box.createHorizontalGlue());

		tailIsOn = jEdit.getBooleanProperty("log-viewer.tail", false);
		tail = new JCheckBox(
			jEdit.getProperty("log-viewer.tail.label"),tailIsOn);
		tail.addActionListener(new ActionHandler());
		caption.add(tail);

		caption.add(Box.createHorizontalStrut(12));

		copy = new JButton(jEdit.getProperty("log-viewer.copy"));
		copy.addActionListener(new ActionHandler());
		caption.add(copy);

		ListModel model = Log.getLogListModel();
		model.addListDataListener(new ListHandler());
		list = new LogList(model);

		add(BorderLayout.NORTH,caption);
		JScrollPane scroller = new JScrollPane(list);
		Dimension dim = scroller.getPreferredSize();
		dim.width = Math.min(600,dim.width);
		scroller.setPreferredSize(dim);
		add(BorderLayout.CENTER,scroller);

		propertiesChanged();
	} 

	
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		scrollLaterIfRequired();
	} 

	
	public void handleMessage(EBMessage msg)
	{
		if(msg instanceof PropertiesChanged)
			propertiesChanged();
	} 

	
	public void addNotify()
	{
		super.addNotify();
		if(tailIsOn)
			scrollToTail();

		EditBus.addToBus(this);
	} 

	
	public void removeNotify()
	{
		super.removeNotify();

		EditBus.removeFromBus(this);
	} 

	
	public void focusOnDefaultComponent()
	{
		list.requestFocus();
	} 

	
	private final JList list;
	private final JButton copy;
	private final JCheckBox tail;
	private boolean tailIsOn;

	
	private void propertiesChanged()
	{
		list.setFont(jEdit.getFontProperty("view.font"));
		list.setFixedCellHeight(list.getFontMetrics(list.getFont())
			.getHeight());
	} 

	
	
	private void scrollToTail()
	{
		int index = list.getModel().getSize();
		if(index != 0)
			list.ensureIndexIsVisible(index - 1);
	} 

	
	private void scrollLaterIfRequired()
	{
		if (tailIsOn)
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					scrollToTail();
				}
			});
	} 

	

	
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Object src = e.getSource();
			if(src == tail)
			{
				tailIsOn = !tailIsOn;
				jEdit.setBooleanProperty("log-viewer.tail",tailIsOn);
				if(tailIsOn)
				{
					scrollToTail();
				}
			}
			else if(src == copy)
			{
				StringBuilder buf = new StringBuilder();
				Object[] selected = list.getSelectedValues();
				if(selected != null && selected.length != 0)
				{
					for(int i = 0; i < selected.length; i++)
					{
						buf.append(selected[i]);
						buf.append('\n');
					}
				}
				else
				{
					ListModel model = list.getModel();
					for(int i = 0; i < model.getSize(); i++)
					{
						buf.append(model.getElementAt(i));
						buf.append('\n');
					}
				}
				Registers.setRegister('$',buf.toString());
			}
		}
	} 

	
	class ListHandler implements ListDataListener
	{
		public void intervalAdded(ListDataEvent e)
		{
			contentsChanged(e);
		}

		public void intervalRemoved(ListDataEvent e)
		{
			contentsChanged(e);
		}

		public void contentsChanged(ListDataEvent e)
		{
			scrollLaterIfRequired();
		}
	} 

	
	class LogList extends JList
	{
		LogList(ListModel model)
		{
			super(model);
			setVisibleRowCount(24);
			getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			setAutoscrolls(true);
		}

		public void processMouseEvent(MouseEvent evt)
		{
			if(evt.getID() == MouseEvent.MOUSE_PRESSED)
			{
				startIndex = list.locationToIndex(
					evt.getPoint());
			}
			super.processMouseEvent(evt);
		}

		public void processMouseMotionEvent(MouseEvent evt)
		{
			if(evt.getID() == MouseEvent.MOUSE_DRAGGED)
			{
				int row = list.locationToIndex(evt.getPoint());
				if(row != -1)
				{
					if(startIndex == -1)
					{
						list.setSelectionInterval(row,row);
						startIndex = row;
					}
					else
						list.setSelectionInterval(startIndex,row);
					list.ensureIndexIsVisible(row);
					evt.consume();
				}
			}
			else
				super.processMouseMotionEvent(evt);
		}

		private int startIndex;
	} 
}