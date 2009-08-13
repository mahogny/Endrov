///////////////////////////////////////////////////////////////////////////////
//FILE:          StateMetaPage.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Johan Henriksson
//               Derived from: Nenad Amodaj, nenad@amodaj.com, October 29, 2006
//
// COPYRIGHT:    University of California, San Francisco, 2006
//               + Johan Henriksson 2009
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package endrov.driverMicromanager.conf;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import endrov.productDatabase.WidgetChooseHardware;

import mmcorej.MMCoreJ;

/**
 * Wizard page to associate metadata for state devices.
 *
 */
public class StateMetaPage extends PagePanel {
   private static final long serialVersionUID = 1L;
   private boolean initialized_ = false;
   private WidgetChooseHardware labels_[] = new WidgetChooseHardware[]{new WidgetChooseHardware()};
   //private String deviceLabels_[][] = new String[0][0];
   ArrayList<Device> devices_ = new ArrayList<Device>();
   
   public class SelectionListener implements ListSelectionListener {
      JTable table;
  
      // It is necessary to keep the table since it is not possible
      // to determine the table from the event's source
      SelectionListener(JTable table) {
          this.table = table;
      }
      public void valueChanged(ListSelectionEvent e) {
         if (e.getValueIsAdjusting())
            return;
         
         ListSelectionModel lsm = (ListSelectionModel)e.getSource();
         ThisTableModel ltm = (ThisTableModel)metadataTable_.getModel();
         if (lsm.isSelectionEmpty()) {
            ltm.setData(model_, null);
         } else {
            String devName = (String)table.getValueAt(lsm.getMinSelectionIndex(), 0);
            ltm.setData(model_, devName);
         }
         ltm.fireTableStructureChanged();         
         metadataTable_.getColumnModel().getColumn(0).setWidth(40);
      }
   }
   
   class ThisTableModel extends AbstractTableModel {
      private static final long serialVersionUID = 1L;
      public final String[] COLUMN_NAMES = new String[] {
            "State",
            "Meta"
      };
      private Device curDevice_;

      public Device getCurrentDevice() {
         return curDevice_;
      }

      public void setData(MicroscopeModel model, String selDevice) {
         curDevice_ = model.findDevice(selDevice);
         labels_ = new WidgetChooseHardware[]{new WidgetChooseHardware()};
         if (curDevice_ == null) {
            return;
         }
         
         Property p = curDevice_.findProperty(MMCoreJ.getG_Keyword_Label());
         if (p == null)
            return;
         
         labels_ = new WidgetChooseHardware[curDevice_.getNumberOfStates()];
         for (int i= 0; i<labels_.length; i++)
            labels_[i] = new WidgetChooseHardware();
         	//TODO set value

         /*
         for (int i=0; i<curDevice_.getNumberOfSetupLabels(); i++) {
            Label lab = curDevice_.getSetupLabel(i);
            labels_[lab.state_] = lab.label_;
            //TODO set
         }*/
      }
      
      public int getRowCount() {
         return labels_.length;
      }
      public int getColumnCount() {
         return COLUMN_NAMES.length;
      }
      public String getColumnName(int columnIndex) {
         return COLUMN_NAMES[columnIndex];
      }
      public Object getValueAt(int rowIndex, int columnIndex) {
         if (columnIndex == 0)
            return Integer.toString(rowIndex);
         else
            return labels_[rowIndex];
      }
      
      public boolean isCellEditable(int nRow, int nCol) {
         if(nCol == 1)
            return true;
         else
            return false;
      }
      public void setValueAt(Object value, int row, int col) {
         if (col == 1) {
            try {
               labels_[row] = (WidgetChooseHardware) value;
               curDevice_.setSetupLabel(row, (String) value);
               fireTableCellUpdated(row, col);
            } catch (Exception e) {
               handleError(e.getMessage());
            }
         }
      }
   }

   class DevTableModel extends AbstractTableModel {
      private static final long serialVersionUID = 1L;
      public final String[] COLUMN_NAMES = new String[] {
            "devices"
      };
      
      public void setData(MicroscopeModel model) {
         if (!initialized_) {
            //storeLabels();
            initialized_ = true;
         }
         Device devs[] = model.getDevices();
         devices_.clear();
         for (int i=0; i<devs.length; i++) {
            if (devs[i].isStateDevice()) {
               devices_.add(devs[i]);
            }
         }
      }
      
      public int getRowCount() {
         return devices_.size();
      }
      public int getColumnCount() {
         return COLUMN_NAMES.length;
      }
      public String getColumnName(int columnIndex) {
         return COLUMN_NAMES[columnIndex];
      }
      public Object getValueAt(int rowIndex, int columnIndex) {
         return devices_.get(rowIndex).getName();
      }
   }

   private JTable devTable_;
   private JTable metadataTable_;
   /**
    * Create the panel
    */
   public StateMetaPage(Preferences prefs) {
      super();
      title_ = "Associate metadata for state devices";
      helpText_ = "State devices with discrete positions, such as filter changers or objective turrets, etc. can have " +
      		"additional metadata associated for each position e.g. filter manufacturer.\n\n" +
      "Select the device in the left-hand list and edit corresponding metadata in the right-hand list.\n\n";
      setHelpFileName("N/A.html");
      prefs_ = prefs;
      setLayout(null);

      final JScrollPane labelsScrollPane = new JScrollPane();
      labelsScrollPane.setBounds(186-80, 10, 377+80, 254);
      add(labelsScrollPane);

      metadataTable_ = new JTable();
      metadataTable_.setModel(new ThisTableModel());
      labelsScrollPane.setViewportView(metadataTable_);

      final JScrollPane devScrollPane = new JScrollPane();
      devScrollPane.setBounds(10, 10, 162-80, 255);
      add(devScrollPane);

      devTable_ = new JTable();
      DevTableModel m = new DevTableModel();
      devTable_.setModel(m);
      devTable_.getSelectionModel().addListSelectionListener(new SelectionListener(devTable_));
      devTable_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      devScrollPane.setViewportView(devTable_);


   }

   /*
   public void storeLabels() {
      // Store the initial list of labels for the reset button
      String labels[] = new String[0];
      Device devs[] = model_.getDevices();
      devices_.clear();
      for (int i=0; i<devs.length; i++) {
         if (devs[i].isStateDevice()) {
            devices_.add(devs[i]);
         }
      }
      deviceLabels_ = new String[devices_.size()][0];
      for (int j=0; j<devices_.size(); j++) {
         labels = new String[devices_.get(j).getNumberOfStates()];
         for (int i= 0; i<labels.length; i++)
            labels[i] = new String("State-" + i);
         
         for (int i=0; i<devices_.get(j).getNumberOfSetupLabels(); i++) {
            Label lab = devices_.get(j).getSetupLabel(i);
            labels[lab.state_] = lab.label_;
         }
         deviceLabels_[j] = new String[labels.length];
         for (int k=0; k<labels.length; k++)
            deviceLabels_[j][k]=labels[k];
      }
   }
*/
   public boolean enterPage(boolean next) {
      DevTableModel tm = (DevTableModel)devTable_.getModel();
      tm.setData(model_);
      try {
         model_.loadStateLabelsFromHardware(core_);
      } catch (Exception e) {
         handleError(e.getMessage());
         return false;
      }

      return true;
  }

   public boolean exitPage(boolean next) {
      // define labels in hardware and syhcronize device data with microscope model
      try {
         model_.applySetupLabelsToHardware(core_);
         model_.loadDeviceDataFromHardware(core_);
      } catch (Exception e) {
         handleError(e.getMessage());
         return false;
      }
      return true;
   }
   
   public void refresh() {
   }

   public void loadSettings() {
   }

   public void saveSettings() {
   }
}
