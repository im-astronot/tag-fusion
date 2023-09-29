package filemanager.main;

// Swing
import com.formdev.flatlaf.FlatLightLaf;
import filemanager.tablecell.filetable.TableActionCellEditor;
import filemanager.tablecell.filetable.TableActionCellRender;
import filemanager.table.displaydata.DisplayTagInfo_FileInfoPane;
import filemanager.table.displaydata.DisplayTagInfo_TaggedFilesPane;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


//JNotify
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.contentobjects.jnotify.*;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;

// SQLite
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import filemanager.tablecell.filetable.FileTableActionEvent;
import filemanager.tablecell.tagtable.Tag_TableActionCellEditor;
import filemanager.tablecell.tagtable.Tag_TableActionCellRender;
import filemanager.tablecell.tagtable.Tag_TableActionEvent;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;


public final class FM_MainFrame extends javax.swing.JFrame {

    public FM_MainFrame() throws IOException {
        initComponents();
       

        // Application Icon
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/filemanager/icon/TagFusion.png")));
        
        // Create Database Function
        createDatabase();
        
        // Insert data into Color Tag Table
        insertDataInColorTagTable();
        
        // Maximize the Window
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Custom TableCellRenderer for fileTable and TagTable
        TableCellRenderer cellRender;
        cellRender = new FM_MainFrame.TextAreaCellRenderer();
        fileTable.setDefaultRenderer(Object.class, cellRender);
        TagTable.setDefaultRenderer(Object.class, cellRender);
        
        
        // To solve issue of double click for fileTable or TagTable
        fileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // set to multi row selection
        TagTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // set to multi row selection
        
        // Implementation of CheckBox in JList
        jList1.setCellRenderer(new CheckBoxListRenderer());
        jList1.setSelectionModel(new DefaultListSelectionModel() {
        @Override
        public void setSelectionInterval(int index0, int index1) {
            if(super.isSelectedIndex(index0)) {
                super.removeSelectionInterval(index0, index1);
            }
            else{
                super.addSelectionInterval(index0, index1);
            }
        }
        });
        
        // Set fileLabel Name as null
        fileNameLabel.setText("");
        
        // Function to display tagged files in tagged pane's TagTable
        taggedFiles_view();
        customtaggedFiles_view();
        
        // Function to list all tag names in jcomboboxes in the Settings pane
        updateComboBox();
        updateRedButtonName();
        updateBlueButtonName();
        updateGreenButtonName();
        updateAzureButtonName();
        updateOrangeButtonName();
        updateYellowButtonName();
        
        Tag1ComboBox.setSelectedIndex(-1);
        Tag2ComboBox.setSelectedIndex(-1);
        Tag3ComboBox.setSelectedIndex(-1);
        Tag4ComboBox.setSelectedIndex(-1);
        
        TabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (TabbedPane.getSelectedIndex() == 2) {
                    taggedFiles_view();
                }
            }
        });
        
        // Displays the tagged files when clicked on the Tagged Files Panel
        TabbedPane.addChangeListener((ChangeEvent e) -> {
            if (TabbedPane.getSelectedIndex() == 2) {
                taggedFiles_view();
            }
        });
        
        //Auto Complete Decorator for jComboBoxes
        AutoCompleteDecorator.decorate(RedComboBox);
        AutoCompleteDecorator.decorate(BlueComboBox);
        AutoCompleteDecorator.decorate(GreenComboBox);
        AutoCompleteDecorator.decorate(AzureComboBox);
        AutoCompleteDecorator.decorate(OrangeComboBox);
        AutoCompleteDecorator.decorate(YellowComboBox);
        AutoCompleteDecorator.decorate(RenameTagComboBox);
        AutoCompleteDecorator.decorate(DeleteTagComboBox);
        AutoCompleteDecorator.decorate(Tag1ComboBox);
        AutoCompleteDecorator.decorate(Tag2ComboBox);
        AutoCompleteDecorator.decorate(Tag3ComboBox);
        AutoCompleteDecorator.decorate(Tag4ComboBox);

        // fileJtree and fileTable Color Tag Action Events
        FileTableActionEvent event;
        event = new FileTableActionEvent() {
            @Override
            public void redTag(int row) {
                try {
                    // Establish Connection
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db");
                         Statement statement = con.createStatement()) {

                        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String filePath = model.getValueAt(row, 2).toString();
                        String fileType = model.getValueAt(row, 3).toString();
                        String tagColor = RedTagButton.getText();

                        String query = "SELECT COUNT(*) AS num_custom_tags FROM tagged_files_info WHERE file_name = '" + fileName + "' AND (customtag1 IS NOT NULL OR customtag2 IS NOT NULL OR customtag3 IS NOT NULL)";
                        ResultSet rs = statement.executeQuery(query);
                        rs.next();
                        int numCustomTags = rs.getInt("num_custom_tags");

                        if (numCustomTags == 0) {
                            statement.executeUpdate("INSERT INTO tagged_files_info(file_name, file_path, file_type, color_tag) VALUES ('" + fileName + "', '" + filePath + "','" + fileType + "','" + tagColor + "')");
                        } else {  
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + RedTagButton.getText() + "' WHERE file_name = '" + fileName + "'");
                        }
                        statement.close();
                        con.close();
                    }
                    customtaggedFiles_view();
                    refreshTagComboBoxComponent();
                } 
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File is already Tagged");  
                }

            }

            @Override
            public void blueTag(int row) {
                try {
                    // Establish Connection
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db");
                         Statement statement = con.createStatement()) {

                        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String filePath = model.getValueAt(row, 2).toString();
                        String fileType = model.getValueAt(row, 3).toString();
                        String tagColor = BlueTagButton.getText();

                        String query = "SELECT COUNT(*) AS num_custom_tags FROM tagged_files_info WHERE file_name = '" + fileName + "' AND (customtag1 IS NOT NULL OR customtag2 IS NOT NULL OR customtag3 IS NOT NULL)";
                        ResultSet rs = statement.executeQuery(query);
                        rs.next();
                        int numCustomTags = rs.getInt("num_custom_tags");

                        if (numCustomTags == 0) {
                            statement.executeUpdate("INSERT INTO tagged_files_info(file_name, file_path, file_type, color_tag) VALUES ('" + fileName + "', '" + filePath + "','" + fileType + "','" + tagColor + "')");
                        } else {  
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + BlueTagButton.getText() + "' WHERE file_name = '" + fileName + "'");
                        }
                        statement.close();
                        con.close();
                    }
                    customtaggedFiles_view();
                    refreshTagComboBoxComponent();
                } 
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File is already Tagged");  
                }
            }

            @Override
            public void greenTag(int row) {
                try {
                    // Establish Connection
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db");
                         Statement statement = con.createStatement()) {

                        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String filePath = model.getValueAt(row, 2).toString();
                        String fileType = model.getValueAt(row, 3).toString();
                        String tagColor = GreenTagButton.getText();

                        String query = "SELECT COUNT(*) AS num_custom_tags FROM tagged_files_info WHERE file_name = '" + fileName + "' AND (customtag1 IS NOT NULL OR customtag2 IS NOT NULL OR customtag3 IS NOT NULL)";
                        ResultSet rs = statement.executeQuery(query);
                        rs.next();
                        int numCustomTags = rs.getInt("num_custom_tags");

                        if (numCustomTags == 0) {
                            statement.executeUpdate("INSERT INTO tagged_files_info(file_name, file_path, file_type, color_tag) VALUES ('" + fileName + "', '" + filePath + "','" + fileType + "','" + tagColor + "')");
                        } else {  
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + GreenTagButton.getText() + "' WHERE file_name = '" + fileName + "'");
                        }
                        statement.close();
                        con.close();
                    }

                    customtaggedFiles_view();
                    refreshTagComboBoxComponent();
                } 
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File is already Tagged");  
                }
            }

            @Override
            public void aruzeTag(int row) {
                try {
                    // Establish Connection
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db");
                         Statement statement = con.createStatement()) {

                        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String filePath = model.getValueAt(row, 2).toString();
                        String fileType = model.getValueAt(row, 3).toString();
                        String tagColor = AzureTagButton.getText();

                        
                        String query = "SELECT COUNT(*) AS num_custom_tags FROM tagged_files_info WHERE file_name = '" + fileName + "' AND (customtag1 IS NOT NULL OR customtag2 IS NOT NULL OR customtag3 IS NOT NULL)";
                        ResultSet rs = statement.executeQuery(query);
                        rs.next();
                        int numCustomTags = rs.getInt("num_custom_tags");

                        if (numCustomTags == 0) {
                            statement.executeUpdate("INSERT INTO tagged_files_info(file_name, file_path, file_type, color_tag) VALUES ('" + fileName + "', '" + filePath + "','" + fileType + "','" + tagColor + "')");
                        } else {  
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + AzureTagButton.getText() + "' WHERE file_name = '" + fileName + "'");
                        }
                        statement.close();
                        con.close();
                    }

                    customtaggedFiles_view();
                    refreshTagComboBoxComponent();
                } 
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File is already Tagged");  
                }
            }

            @Override
            public void orangeTag(int row) {
                try {
                    // Establish Connection
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db");
                         Statement statement = con.createStatement()) {

                        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String filePath = model.getValueAt(row, 2).toString();
                        String fileType = model.getValueAt(row, 3).toString();
                        String tagColor = OrangeTagButton.getText();

                        
                        String query = "SELECT COUNT(*) AS num_custom_tags FROM tagged_files_info WHERE file_name = '" + fileName + "' AND (customtag1 IS NOT NULL OR customtag2 IS NOT NULL OR customtag3 IS NOT NULL)";
                        ResultSet rs = statement.executeQuery(query);
                        rs.next();
                        int numCustomTags = rs.getInt("num_custom_tags");

                        if (numCustomTags == 0) {
                            statement.executeUpdate("INSERT INTO tagged_files_info(file_name, file_path, file_type, color_tag) VALUES ('" + fileName + "', '" + filePath + "','" + fileType + "','" + tagColor + "')");
                        } else {  
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + OrangeTagButton.getText() + "' WHERE file_name = '" + fileName + "'");
                        }
                        statement.close();
                        con.close();
                    }
                    customtaggedFiles_view();
                    refreshTagComboBoxComponent();
                } 
                catch (SQLException  se) {
                    JOptionPane.showMessageDialog(null, "The File is already Tagged");  
                }
            }

            @Override
            public void yellowTag(int row) {
                try {
                    // Establish Connection
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db");
                         Statement statement = con.createStatement()) {

                        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String filePath = model.getValueAt(row, 2).toString();
                        String fileType = model.getValueAt(row, 3).toString();
                        String tagColor = YellowTagButton.getText();

                        
                        String query = "SELECT COUNT(*) AS num_custom_tags FROM tagged_files_info WHERE file_name = '" + fileName + "' AND (customtag1 IS NOT NULL OR customtag2 IS NOT NULL OR customtag3 IS NOT NULL)";
                        ResultSet rs = statement.executeQuery(query);
                        rs.next();
                        int numCustomTags = rs.getInt("num_custom_tags");

                        if (numCustomTags == 0) {
                            statement.executeUpdate("INSERT INTO tagged_files_info(file_name, file_path, file_type, color_tag) VALUES ('" + fileName + "', '" + filePath + "','" + fileType + "','" + tagColor + "')");
                        } else {  
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + YellowTagButton.getText() + "' WHERE file_name = '" + fileName + "'");
                        }
                        statement.close();
                        con.close();
                    }
                    customtaggedFiles_view();
                    refreshTagComboBoxComponent();
                } 
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File is already Tagged");  
                }
            }
            
            @Override
            public void deleteTag(int row) {
                DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                String fileName = model.getValueAt(row, 1).toString();

                try {
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); 
                    Statement statement = con.createStatement()) {

                        // To heck if the color tag exists in the tagged_files_info table
                        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        int count = 0;
                        if (resultSet.next()) {
                            count = resultSet.getInt(1);
                        }
                        if (count != 0) {
                            int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the first tag for " + fileName + "?", "Confirmation", JOptionPane.YES_NO_OPTION);
                            if (option == JOptionPane.YES_OPTION) {

                                try {
                                    String query = "SELECT COUNT(*) AS num_custom_tags FROM tagged_files_info WHERE file_name = '" + fileName + "' AND (customtag1 IS NOT NULL OR customtag2 IS NOT NULL OR customtag3 IS NOT NULL)";
                                    ResultSet rs = statement.executeQuery(query);
                                    rs.next();
                                    int numCustomTags = rs.getInt("num_custom_tags");

                                    if (numCustomTags == 0) {
                                        statement.executeUpdate("DELETE FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                                    } else {  
                                        statement.executeUpdate("UPDATE tagged_files_info SET color_tag = NULL WHERE file_name = '" + fileName + "'");
                                    }
                                    model.setValueAt("", row, 4);
                                    customtaggedFiles_view();
                                    refreshTagComboBoxComponent();
                                    
                                    statement.close();
                                    con.close();
                                } 
                                catch (SQLException se) {
                                    JOptionPane.showMessageDialog(null, se);
                                }
                            }
                            else {
                                JOptionPane.showMessageDialog(null, "Please select a file to delete tags for the selected file?", "Error", JOptionPane.ERROR_MESSAGE);
                            }    
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "The selected file(s) has no tag(s).", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (HeadlessException | SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                }    
            }
        };

        fileTable.getColumnModel().getColumn(5).setCellRenderer(new TableActionCellRender());
        fileTable.getColumnModel().getColumn(5).setCellEditor(new TableActionCellEditor(event));
        
        // Update tagged files from TagTable
        Tag_TableActionEvent tag_event;
        tag_event = new Tag_TableActionEvent() {
            @Override
            public void redTag(int row) {
                try {

                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                        DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String colorTag = RedTagButton.getText();

                        ResultSet rs = statement.executeQuery("SELECT color_tag FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        if (rs.next()) {
                            String currentColorTag = rs.getString("color_tag");
                            if (currentColorTag != null && currentColorTag.equals(colorTag)) {
                                JOptionPane.showMessageDialog(null, "The file is already tagged with the same tag name. Choose other button to update the tag if required.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to update the tag of selected file?", "Test", JOptionPane.YES_OPTION, JOptionPane.NO_OPTION);

                        if (result == JOptionPane.YES_OPTION){
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + colorTag + "' WHERE file_name = '" + fileName + "'");   
                        }
                        
                        statement.close();
                        con.close();
                        
                        model.setRowCount(0);
                        taggedFiles_view();
                    }
                    
                }
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File has been untagged");
                }
            }

            @Override
            public void blueTag(int row) {
                try {

                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                        DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String colorTag = BlueTagButton.getText();

                        ResultSet rs = statement.executeQuery("SELECT color_tag FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        if (rs.next()) {
                            String currentColorTag = rs.getString("color_tag");
                            if (currentColorTag != null && currentColorTag.equals(colorTag)) {
                                JOptionPane.showMessageDialog(null, "The file is already tagged with the same tag name. Choose other button to update the tag if required.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to update the tag of selected file?", "Test", JOptionPane.YES_OPTION, JOptionPane.NO_OPTION);

                        if (result == JOptionPane.YES_OPTION){
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + colorTag + "' WHERE file_name = '" + fileName + "'");   
                        }
                        statement.close();
                        con.close();
                        
                        model.setRowCount(0);
                        taggedFiles_view();
                    }   
                }
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File has been Untagged");
                }
            }

            @Override
            public void greenTag(int row) {
                try {
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                        DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String colorTag = GreenTagButton.getText();

                        ResultSet rs = statement.executeQuery("SELECT color_tag FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        if (rs.next()) {
                            String currentColorTag = rs.getString("color_tag");
                            if (currentColorTag != null && currentColorTag.equals(colorTag)) {
                                JOptionPane.showMessageDialog(null, "The file is already tagged with the same tag name. Choose other button to update the tag if required.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to update the tag of selected file?", "Test", JOptionPane.YES_OPTION, JOptionPane.NO_OPTION);
                        if (result == JOptionPane.YES_OPTION){
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + colorTag + "' WHERE file_name = '" + fileName + "'");   
                        }
                        statement.close();
                        con.close();
                        
                        model.setRowCount(0);
                        taggedFiles_view();
                    }   
                }
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File has been Untagged");
                }
            }

            @Override
            public void aruzeTag(int row) {
                try {

                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                        DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String colorTag = AzureTagButton.getText();

                        ResultSet rs = statement.executeQuery("SELECT color_tag FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        if (rs.next()) {
                            String currentColorTag = rs.getString("color_tag");
                            if (currentColorTag != null && currentColorTag.equals(colorTag)) {
                                JOptionPane.showMessageDialog(null, "The file is already tagged with the same tag name. Choose other button to update the tag if required.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to update the tag of selected file?", "Test", JOptionPane.YES_OPTION, JOptionPane.NO_OPTION);

                        if (result == JOptionPane.YES_OPTION){
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + colorTag + "' WHERE file_name = '" + fileName + "'");   
                        }
                        statement.close();
                        con.close();
                        
                        model.setRowCount(0);
                        taggedFiles_view();
                    }   
                }
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File has been Untagged");
                }
            }

            @Override
            public void orangeTag(int row) {
                try {
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                        DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String colorTag = OrangeTagButton.getText();

                        ResultSet rs = statement.executeQuery("SELECT color_tag FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        if (rs.next()) {
                            String currentColorTag = rs.getString("color_tag");
                            if (currentColorTag != null && currentColorTag.equals(colorTag)) {
                                JOptionPane.showMessageDialog(null, "The file is already tagged with the same tag name. Choose other button to update the tag if required.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to update the tag of selected file?", "Test", JOptionPane.YES_OPTION, JOptionPane.NO_OPTION);
                        if (result == JOptionPane.YES_OPTION){
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + colorTag + "' WHERE file_name = '" + fileName + "'");   
                        }
                        statement.close();
                        con.close();
                        
                        model.setRowCount(0);
                        taggedFiles_view();
                    }   
                }
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File has been Untagged");
                }
            }

            @Override
            public void yellowTag(int row) {
                try {

                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                        DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                        String fileName = model.getValueAt(row, 1).toString();
                        String colorTag = YellowTagButton.getText();

                        ResultSet rs = statement.executeQuery("SELECT color_tag FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        if (rs.next()) {
                            String currentColorTag = rs.getString("color_tag");
                            if (currentColorTag != null && currentColorTag.equals(colorTag)) {
                                JOptionPane.showMessageDialog(null, "The file is already tagged with the same color.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to update the tag of selected file?", "Test", JOptionPane.YES_OPTION, JOptionPane.NO_OPTION);
                        if (result == JOptionPane.YES_OPTION){
                            statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + colorTag + "' WHERE file_name = '" + fileName + "'");   
                        }
                        statement.close();
                        con.close();
                        
                        model.setRowCount(0);
                        taggedFiles_view();
                    }   
                }
                catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "The File has been Untagged");
                }
            }

            @Override
            public void deleteTag(int row) {
                // Need to be implemented
                DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                String fileName = model.getValueAt(row, 1).toString();

                try {
                   
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); 
                    Statement statement = con.createStatement()) {

                        // To heck if the color tag exists in the tagged_files_info table
                        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        int count = 0;
                        if (resultSet.next()) {
                            count = resultSet.getInt(1);
                        }

                        if (count != 0) {
                            int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the first tag for " + fileName + "?", "Confirmation", JOptionPane.YES_NO_OPTION);
                            if (option == JOptionPane.YES_OPTION) {
                                try {
                                    String query = "SELECT COUNT(*) AS num_custom_tags FROM tagged_files_info WHERE file_name = '" + fileName + "' AND (customtag1 IS NOT NULL OR customtag2 IS NOT NULL OR customtag3 IS NOT NULL)";
                                    ResultSet rs = statement.executeQuery(query);
                                    rs.next();
                                    int numCustomTags = rs.getInt("num_custom_tags");

                                    if (numCustomTags == 0) {
                                        statement.executeUpdate("DELETE FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                                        
                                        model.setRowCount(0);
                                        taggedFiles_view();
                                    } else {  
                                        statement.executeUpdate("UPDATE tagged_files_info SET color_tag = NULL WHERE file_name = '" + fileName + "'");
                                        model.setRowCount(0);
                                        taggedFiles_view();
                                    }

                                    //customtaggedFiles_view();
                                    refreshTagComboBoxComponent();

                                } 
                                catch (SQLException se) {
                                    JOptionPane.showMessageDialog(null, se);
                                }

                            }
                            else {
                                JOptionPane.showMessageDialog(null, "Please select a file to delete tags for the selected file?", "Error", JOptionPane.ERROR_MESSAGE);
                            }    
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "The selected file(s) has no tag(s).", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        statement.close();
                        con.close();
                    }
                } catch (HeadlessException  | SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                }                  
            }
        };
        
        TagTable.getColumnModel().getColumn(5).setCellRenderer(new Tag_TableActionCellRender());
        TagTable.getColumnModel().getColumn(5).setCellEditor(new Tag_TableActionCellEditor(tag_event));

        
        // JTree to JTable connection
        fileTree.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                TabbedPane.setSelectedIndex(0);
                
                DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                model.setRowCount(0);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    File file = fileTree.getSelectedFile();
                    if (file.isFile()) {
                        addRow(file);
                    } else if (file.isDirectory()) {
                        showDirectory(file);
                    }
                }
            }
        });
        
        fileTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                JLabel lb = new JLabel((Icon) value);  
                lb.setOpaque(true);
                lb.setPreferredSize(new Dimension(70, 70));
                
                if(isSelected) {
                    lb.setBackground(table.getSelectionBackground());
                }
                else {
                    lb.setBackground(table.getBackground());
                }
                
                if (value instanceof Icon) {               
                    return lb;
                    
                } else {
                    return com;
                }
                
            }
        });
        
        TagTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            JLabel lb = new JLabel((Icon) value);
            lb.setOpaque(true);
            lb.setPreferredSize(new Dimension(70, 70));
            
            if(isSelected) {
                lb.setBackground(table.getSelectionBackground());
            } else {
                lb.setBackground(table.getBackground());
            }
            
            if (value instanceof Icon) {               
                return lb;
            } else {
                return com;
            }
        }
    });
       
        // Create a popup menu for the file table
        JPopupMenu filePopupMenu = new JPopupMenu();
        JMenuItem openMenuItem = new JMenuItem("Open File");
        JMenuItem deleteAllTagsMenuItem = new JMenuItem("Remove All Tags");
        filePopupMenu.add(openMenuItem);
        filePopupMenu.add(new JSeparator()); // add separator line
        // Add a title item for tag actions
        JMenuItem titleMenuItem = new JMenuItem("Tag Actions");

        titleMenuItem.setEnabled(false);
        Font titleFont = new Font("Arial", Font.BOLD, 12);

        titleMenuItem.setFont(titleFont);
        filePopupMenu.add(titleMenuItem);
        filePopupMenu.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        filePopupMenu.add(deleteAllTagsMenuItem);

        // Add mouse listener to the file table
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = fileTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < fileTable.getRowCount()) {
                        fileTable.setRowSelectionInterval(row, row);
                        filePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // Add action listeners for the menu items
        openMenuItem.addActionListener((ActionEvent e) -> {
            int row = fileTable.getSelectedRow();
            if (row >= 0) {
                String filePath = (String) fileTable.getValueAt(row, 2);
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a file to open.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        openMenuItem.addActionListener((ActionEvent e) -> {
            int row = fileTable.getSelectedRow();
            if (row >= 0) {
                String filePath = (String) fileTable.getValueAt(row, 2);
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a file to open.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteAllTagsMenuItem.addActionListener((ActionEvent e) -> {
            int row = fileTable.getSelectedRow();
            if (row >=0 ) {
                //int row = fileTable.getSelectedRow();
                DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                String fileName = model.getValueAt(row, 1).toString();
                
                try {
                   
                    try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                        
                        // To check if the color tag exists in the tagged_files_info table
                        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        int count = 0;
                        if (resultSet.next()) {
                            count = resultSet.getInt(1);
                        }
                        
                        if (count != 0) {
                            if (row >= 0) {
                                //String fileName = (String) fileTable.getValueAt(row, 1);
                                int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all tags for " + fileName + "?", "Confirmation", JOptionPane.YES_NO_OPTION);
                                if (option == JOptionPane.YES_OPTION) {
                                    try {
                                        statement.executeUpdate("DELETE FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                                        fileTable.setValueAt("", row, 4);
                                        
                                        customtaggedFiles_view();
                                        refreshTagComboBoxComponent();
                                    }
                                    catch (SQLException se) {
                                        JOptionPane.showMessageDialog(null, "Error deleting file from database: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                            else {
                                JOptionPane.showMessageDialog(null, "Please select a file to delete tags for.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "The selected file(s) has no tag(s).", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        statement.close();
                        con.close();
                    }
                }
                catch (HeadlessException  | SQLException se) {
                    JOptionPane.showMessageDialog(null, se);
                }
            }
            else {
                JOptionPane.showMessageDialog(null, "Please select a file to delete tags for.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        // Create a popup menu for the tag table
        JPopupMenu tagPopupMenu = new JPopupMenu();
        JMenuItem tagOpenMenuItem = new JMenuItem("Open File");
        JMenuItem tagDeleteAllTagsMenuItem = new JMenuItem("Remove All Tags");

        tagPopupMenu.add(tagOpenMenuItem);
        tagPopupMenu.add(new JSeparator()); // add separator line

        // Add a title item for tag actions
        JMenuItem tagtitleMenuItem = new JMenuItem("Tag Actions");
        tagtitleMenuItem.setEnabled(false);
        Font tagtitleFont = new Font("Arial", Font.BOLD, 12);
        titleMenuItem.setFont(tagtitleFont);
        tagPopupMenu.add(tagtitleMenuItem);

        tagPopupMenu.add(tagDeleteAllTagsMenuItem);

        tagPopupMenu.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Add mouse listener to the tag table
        TagTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = TagTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < TagTable.getRowCount()) {
                        TagTable.setRowSelectionInterval(row, row);
                        tagPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        tagOpenMenuItem.addActionListener((ActionEvent e) -> {
            int row = TagTable.getSelectedRow();
            if (row >= 0) {
                String filePath = (String) TagTable.getValueAt(row, 2);
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        tagOpenMenuItem.addActionListener((ActionEvent e) -> {
            int row = TagTable.getSelectedRow();
            if (row >= 0) {
                String filePath = (String) TagTable.getValueAt(row, 2);
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a file to open.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        tagDeleteAllTagsMenuItem.addActionListener((ActionEvent e) -> {
            
            int row = TagTable.getSelectedRow();
            if (row > -1) {
                DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                String fileName = (String) TagTable.getValueAt(row, 1);
                int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all tags for " + fileName + "?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    try {
                        try ( 
                                // Establish Connection
                                Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); 
                                Statement statement = con.createStatement()) {
                            
                            statement.executeUpdate("DELETE FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                            statement.close();
                        con.close();
                        }
                        
                        model.setRowCount(0);
                        taggedFiles_view();
                        // Refresh the file table with updated data
                        customtaggedFiles_view();
                    }
                    
                    catch (SQLException  se) {
                        JOptionPane.showMessageDialog(null, "Error deleting file from database: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a file to delete tags for.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

    } // end of public FM_MainFrame()
    // END OF CONSTRUCTOR

    // JTree to JTable Connection
    public void addRow(File file) {
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon) icon;
            Image image = imageIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
        }
        
        String fileName = file.getName();
        String filePath = file.getAbsolutePath();
        String fileFormat = file.isFile() ? fileName.substring(fileName.lastIndexOf("."), fileName.length()) : "Directory";
        model.addRow(new Object[]{icon, fileName, filePath, fileFormat});
        
        customtaggedFiles_view();
    }
    
    public void showDirectory(File file) {
        for (File f : file.listFiles()) {
            addRow(f);
        }
    }
    
    // Word Wrap JTable
    public class TextAreaCellRenderer extends JTextArea implements TableCellRenderer  {

        private final List<List<Integer>> rowAndCellHeights = new ArrayList<>();

        public TextAreaCellRenderer() {
            setWrapStyleWord(true);
            setLineWrap(true);
            setOpaque(true);
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
        }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(Objects.toString(value, ""));
        adjustRowHeight(table, row, column);
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        return this;
    }

    private void adjustRowHeight(JTable table, int row, int column) {

        setBounds(table.getCellRect(row, column, false));
        int preferredHeight = getPreferredSize().height;
        while (rowAndCellHeights.size() <= row) {
            rowAndCellHeights.add(new ArrayList<>(column));
        }
        List<Integer> list = rowAndCellHeights.get(row);
        while (list.size() <= column) {
            list.add(0);
        }
        list.set(column, preferredHeight);
        int max = list.stream().max((x, y) -> Integer.compare(x, y)).get();
        int newHeight = Math.max(60, max); // Ensure the new height is at least 60
        if (table.getRowHeight(row) != newHeight) {
            table.setRowHeight(row, newHeight);
        }
    }
}
    
    
    // Adding jCheckBox to JList
    public class CheckBoxListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        JCheckBox checkbox = new JCheckBox(value.toString());
        checkbox.setSelected(list.isSelectedIndex(index));
        checkbox.setFocusPainted(false);
        checkbox.setEnabled(list.isEnabled());
        Font font = new Font("Segoe UI", Font.PLAIN, 13);
        checkbox.setFont(font);
        
        if (isSelected) {
            checkbox.setBackground(list.getSelectionBackground());
            checkbox.setForeground(list.getSelectionForeground());
        } else {
            checkbox.setBackground(list.getBackground());
            checkbox.setForeground(list.getForeground());
        }
        
        return checkbox;
        }
    }
 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        RedTagButton = new filemanager.component.Button_Shadowed();
        BlueTagButton = new filemanager.component.Button_Shadowed();
        GreenTagButton = new filemanager.component.Button_Shadowed();
        AzureTagButton = new filemanager.component.Button_Shadowed();
        OrangeTagButton = new filemanager.component.Button_Shadowed();
        YellowTagButton = new filemanager.component.Button_Shadowed();
        SettingsButton = new filemanager.component.Button_Shadowed();
        TagsLabel = new javax.swing.JLabel();
        displayTaggedFilesButton1 = new filemanager.component.Button_Shadowed();
        TabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        fileTable = new filemanager.table.swing.Table();
        UpdateExistingTagstoSelectedFilesButton = new filemanager.component.Button_Shadowed();
        AddTagsToSelectedFilesButton = new filemanager.component.Button_Shadowed();
        Tag4ComboBox = new javax.swing.JComboBox<>();
        Tag3ComboBox = new javax.swing.JComboBox<>();
        Tag2ComboBox = new javax.swing.JComboBox<>();
        Tag1ComboBox = new javax.swing.JComboBox<>();
        DeleteTagsToSelectedFiles = new filemanager.component.Button_Shadowed();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel10 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        fileNameLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        displayTaggedFilesButton = new filemanager.component.Button_Shadowed();
        jScrollPane1 = new javax.swing.JScrollPane();
        TagTable = new filemanager.table.swing.Table();
        SearchTags = new filemanager.component.Button_Shadowed();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        AddTagTextField = new filemanager.component.TextField();
        AddTagButton = new filemanager.component.Button_Shadowed();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        RenameTagComboBox = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        RenameTagTextField = new filemanager.component.TextField();
        RenameTagButton = new filemanager.component.Button_Shadowed();
        jPanel8 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        DeleteTagComboBox = new javax.swing.JComboBox<>();
        DeleteTagButton = new filemanager.component.Button_Shadowed();
        jPanel9 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        RedComboBox = new javax.swing.JComboBox<>();
        SetRedTagButton = new filemanager.component.Button_Shadowed();
        BlueComboBox = new javax.swing.JComboBox<>();
        SetBlueTagButton = new filemanager.component.Button_Shadowed();
        GreenComboBox = new javax.swing.JComboBox<>();
        SetGreenTagButton = new filemanager.component.Button_Shadowed();
        AzureComboBox = new javax.swing.JComboBox<>();
        SetAzureTagButton = new filemanager.component.Button_Shadowed();
        OrangeComboBox = new javax.swing.JComboBox<>();
        SetOrangeTagButton = new filemanager.component.Button_Shadowed();
        YellowComboBox = new javax.swing.JComboBox<>();
        SetYellowTagButton = new filemanager.component.Button_Shadowed();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        fileTree = new jtree.FileTree();
        FilesLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TagFusion");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setLocationByPlatform(true);

        RedTagButton.setBackground(new java.awt.Color(255, 77, 77));
        RedTagButton.setForeground(new java.awt.Color(0, 0, 0));
        RedTagButton.setText("Red");
        RedTagButton.setFont(RedTagButton.getFont().deriveFont(RedTagButton.getFont().getSize()+3f));
        RedTagButton.setRippleColor(new java.awt.Color(255, 204, 204));
        RedTagButton.setShadowColor(new java.awt.Color(153, 0, 0));
        RedTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RedTagButtonActionPerformed(evt);
            }
        });

        BlueTagButton.setBackground(new java.awt.Color(0, 153, 255));
        BlueTagButton.setForeground(new java.awt.Color(0, 0, 0));
        BlueTagButton.setText("Blue");
        BlueTagButton.setFont(BlueTagButton.getFont().deriveFont(BlueTagButton.getFont().getSize()+3f));
        BlueTagButton.setRippleColor(new java.awt.Color(102, 204, 255));
        BlueTagButton.setShadowColor(new java.awt.Color(0, 51, 102));
        BlueTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BlueTagButtonActionPerformed(evt);
            }
        });

        GreenTagButton.setBackground(new java.awt.Color(63, 213, 63));
        GreenTagButton.setForeground(new java.awt.Color(0, 0, 0));
        GreenTagButton.setText("Green");
        GreenTagButton.setFont(GreenTagButton.getFont().deriveFont(GreenTagButton.getFont().getSize()+3f));
        GreenTagButton.setRippleColor(new java.awt.Color(153, 255, 153));
        GreenTagButton.setShadowColor(new java.awt.Color(0, 102, 51));
        GreenTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GreenTagButtonActionPerformed(evt);
            }
        });

        AzureTagButton.setBackground(new java.awt.Color(0, 243, 243));
        AzureTagButton.setForeground(new java.awt.Color(0, 0, 0));
        AzureTagButton.setText("Azure");
        AzureTagButton.setFont(AzureTagButton.getFont().deriveFont(AzureTagButton.getFont().getSize()+3f));
        AzureTagButton.setRippleColor(new java.awt.Color(46, 183, 252));
        AzureTagButton.setShadowColor(new java.awt.Color(0, 120, 120));
        AzureTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AzureTagButtonActionPerformed(evt);
            }
        });

        OrangeTagButton.setBackground(new java.awt.Color(255, 152, 23));
        OrangeTagButton.setForeground(new java.awt.Color(0, 0, 0));
        OrangeTagButton.setText("Orange");
        OrangeTagButton.setFont(OrangeTagButton.getFont().deriveFont(OrangeTagButton.getFont().getSize()+3f));
        OrangeTagButton.setRippleColor(new java.awt.Color(255, 217, 133));
        OrangeTagButton.setShadowColor(new java.awt.Color(204, 102, 0));
        OrangeTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OrangeTagButtonActionPerformed(evt);
            }
        });

        YellowTagButton.setBackground(new java.awt.Color(243, 234, 35));
        YellowTagButton.setForeground(new java.awt.Color(0, 0, 0));
        YellowTagButton.setText("Yellow");
        YellowTagButton.setFont(YellowTagButton.getFont().deriveFont(YellowTagButton.getFont().getSize()+3f));
        YellowTagButton.setRippleColor(new java.awt.Color(202, 187, 0));
        YellowTagButton.setShadowColor(new java.awt.Color(153, 153, 0));
        YellowTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YellowTagButtonActionPerformed(evt);
            }
        });

        SettingsButton.setBackground(new java.awt.Color(204, 204, 204));
        SettingsButton.setForeground(new java.awt.Color(0, 0, 0));
        SettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/filemanager/icon/setting.png"))); // NOI18N
        SettingsButton.setText("Settings");
        SettingsButton.setFont(SettingsButton.getFont().deriveFont(SettingsButton.getFont().getSize()+3f));
        SettingsButton.setIconTextGap(5);
        SettingsButton.setRippleColor(new java.awt.Color(153, 153, 153));
        SettingsButton.setShadowColor(new java.awt.Color(51, 51, 51));
        SettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SettingsButtonActionPerformed(evt);
            }
        });

        TagsLabel.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        TagsLabel.setText("Tags");

        displayTaggedFilesButton1.setText("All Tagged Files");
        displayTaggedFilesButton1.setFont(displayTaggedFilesButton1.getFont().deriveFont(displayTaggedFilesButton1.getFont().getSize()+3f));
        displayTaggedFilesButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayTaggedFilesButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(BlueTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(GreenTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AzureTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(OrangeTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(YellowTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SettingsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(TagsLabel))
                    .addComponent(RedTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(displayTaggedFilesButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(TagsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(RedTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BlueTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(GreenTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AzureTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(OrangeTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(YellowTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayTaggedFilesButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(SettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        TabbedPane.setFont(TabbedPane.getFont().deriveFont(TabbedPane.getFont().getSize()+1f));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        fileTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Icon", "File Name", "File Path", "File Type", "Tags", "Color Tag Action"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        fileTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        fileTable.setFocusCycleRoot(true);
        fileTable.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        fileTable.setInheritsPopupMenu(true);
        fileTable.setRowHeight(55);
        fileTable.setSelectionBackground(new java.awt.Color(211, 211, 211));
        fileTable.setSelectionForeground(new java.awt.Color(5, 5, 182));
        fileTable.setShowGrid(true);
        fileTable.getTableHeader().setReorderingAllowed(false);
        fileTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fileTableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(fileTable);
        if (fileTable.getColumnModel().getColumnCount() > 0) {
            fileTable.getColumnModel().getColumn(0).setMinWidth(70);
            fileTable.getColumnModel().getColumn(0).setPreferredWidth(70);
            fileTable.getColumnModel().getColumn(0).setMaxWidth(100);
            fileTable.getColumnModel().getColumn(1).setMinWidth(230);
            fileTable.getColumnModel().getColumn(1).setPreferredWidth(230);
            fileTable.getColumnModel().getColumn(1).setMaxWidth(280);
            fileTable.getColumnModel().getColumn(2).setPreferredWidth(650);
            fileTable.getColumnModel().getColumn(2).setMaxWidth(650);
            fileTable.getColumnModel().getColumn(3).setMinWidth(90);
            fileTable.getColumnModel().getColumn(3).setPreferredWidth(90);
            fileTable.getColumnModel().getColumn(3).setMaxWidth(120);
            fileTable.getColumnModel().getColumn(4).setMinWidth(200);
            fileTable.getColumnModel().getColumn(4).setPreferredWidth(200);
            fileTable.getColumnModel().getColumn(4).setMaxWidth(200);
            fileTable.getColumnModel().getColumn(5).setMinWidth(230);
            fileTable.getColumnModel().getColumn(5).setPreferredWidth(230);
            fileTable.getColumnModel().getColumn(5).setMaxWidth(230);
        }

        UpdateExistingTagstoSelectedFilesButton.setText("Update Tags");
        UpdateExistingTagstoSelectedFilesButton.setFont(UpdateExistingTagstoSelectedFilesButton.getFont().deriveFont(UpdateExistingTagstoSelectedFilesButton.getFont().getSize()+2f));
        UpdateExistingTagstoSelectedFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateExistingTagstoSelectedFilesButtonActionPerformed(evt);
            }
        });

        AddTagsToSelectedFilesButton.setText("Add New Tags");
        AddTagsToSelectedFilesButton.setFont(AddTagsToSelectedFilesButton.getFont().deriveFont(AddTagsToSelectedFilesButton.getFont().getSize()+2f));
        AddTagsToSelectedFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddTagsToSelectedFilesButtonActionPerformed(evt);
            }
        });

        Tag4ComboBox.setEditable(true);
        Tag4ComboBox.setFont(Tag4ComboBox.getFont().deriveFont(Tag4ComboBox.getFont().getSize()+2f));
        Tag4ComboBox.setMaximumRowCount(12);
        Tag4ComboBox.setAutoscrolls(true);
        Tag4ComboBox.setName(""); // NOI18N

        Tag3ComboBox.setEditable(true);
        Tag3ComboBox.setFont(Tag3ComboBox.getFont().deriveFont(Tag3ComboBox.getFont().getSize()+2f));
        Tag3ComboBox.setMaximumRowCount(12);
        Tag3ComboBox.setAutoscrolls(true);
        Tag3ComboBox.setName(""); // NOI18N

        Tag2ComboBox.setEditable(true);
        Tag2ComboBox.setFont(Tag2ComboBox.getFont().deriveFont(Tag2ComboBox.getFont().getSize()+2f));
        Tag2ComboBox.setMaximumRowCount(12);
        Tag2ComboBox.setAutoscrolls(true);
        Tag2ComboBox.setName(""); // NOI18N

        Tag1ComboBox.setEditable(true);
        Tag1ComboBox.setFont(Tag1ComboBox.getFont().deriveFont(Tag1ComboBox.getFont().getSize()+2f));
        Tag1ComboBox.setMaximumRowCount(12);
        Tag1ComboBox.setAutoscrolls(true);
        Tag1ComboBox.setName(""); // NOI18N

        DeleteTagsToSelectedFiles.setText("Remove All Tags");
        DeleteTagsToSelectedFiles.setFont(DeleteTagsToSelectedFiles.getFont().deriveFont(DeleteTagsToSelectedFiles.getFont().getSize()+2f));
        DeleteTagsToSelectedFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteTagsToSelectedFilesActionPerformed(evt);
            }
        });

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()+2f));
        jLabel1.setText("Tag 2");

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getSize()+2f));
        jLabel2.setText("Tag 1");

        jLabel9.setFont(jLabel9.getFont().deriveFont(jLabel9.getFont().getSize()+2f));
        jLabel9.setText("Tag 3");

        jLabel10.setFont(jLabel10.getFont().deriveFont(jLabel10.getFont().getSize()+2f));
        jLabel10.setText("Tag 4");

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel10.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel10.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        jPanel10.setDoubleBuffered(false);
        jPanel10.setFocusCycleRoot(true);
        jPanel10.setFocusTraversalPolicyProvider(true);
        jPanel10.setFont(jPanel10.getFont().deriveFont(jPanel10.getFont().getSize()+2f));
        jPanel10.setInheritsPopupMenu(true);

        jLabel12.setFont(jLabel12.getFont().deriveFont(jLabel12.getFont().getSize()+2f));
        jLabel12.setText("File Selected:");

        fileNameLabel.setFont(fileNameLabel.getFont().deriveFont(fileNameLabel.getFont().getSize()+2f));
        fileNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        fileNameLabel.setText("jLabel13");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(fileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Tag1ComboBox, 0, 153, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Tag2ComboBox, 0, 153, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Tag3ComboBox, 0, 153, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(Tag4ComboBox, 0, 149, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AddTagsToSelectedFilesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UpdateExistingTagstoSelectedFilesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                .addGap(9, 9, 9)
                                .addComponent(DeleteTagsToSelectedFiles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Tag4ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Tag3ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Tag2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Tag1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AddTagsToSelectedFilesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(UpdateExistingTagstoSelectedFilesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(DeleteTagsToSelectedFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE))
        );

        TabbedPane.addTab("File Info", jPanel1);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        displayTaggedFilesButton.setText("Display All Tagged Files");
        displayTaggedFilesButton.setFont(displayTaggedFilesButton.getFont().deriveFont(displayTaggedFilesButton.getFont().getSize()+1f));
        displayTaggedFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayTaggedFilesButtonActionPerformed(evt);
            }
        });

        TagTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Icon", "File Name", "File Path", "File Type", "Tags", "Color Tag Action"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TagTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        TagTable.setFocusCycleRoot(true);
        TagTable.setFont(TagTable.getFont().deriveFont(TagTable.getFont().getSize()+1f));
        TagTable.setInheritsPopupMenu(true);
        TagTable.setRowHeight(55);
        TagTable.setSelectionBackground(new java.awt.Color(211, 211, 211));
        TagTable.setSelectionForeground(new java.awt.Color(5, 5, 182));
        TagTable.setShowGrid(true);
        jScrollPane1.setViewportView(TagTable);
        if (TagTable.getColumnModel().getColumnCount() > 0) {
            TagTable.getColumnModel().getColumn(0).setMinWidth(70);
            TagTable.getColumnModel().getColumn(0).setPreferredWidth(70);
            TagTable.getColumnModel().getColumn(0).setMaxWidth(100);
            TagTable.getColumnModel().getColumn(1).setMinWidth(230);
            TagTable.getColumnModel().getColumn(1).setPreferredWidth(230);
            TagTable.getColumnModel().getColumn(1).setMaxWidth(280);
            TagTable.getColumnModel().getColumn(2).setPreferredWidth(650);
            TagTable.getColumnModel().getColumn(2).setMaxWidth(650);
            TagTable.getColumnModel().getColumn(3).setMinWidth(90);
            TagTable.getColumnModel().getColumn(3).setPreferredWidth(90);
            TagTable.getColumnModel().getColumn(3).setMaxWidth(120);
            TagTable.getColumnModel().getColumn(4).setMinWidth(200);
            TagTable.getColumnModel().getColumn(4).setPreferredWidth(200);
            TagTable.getColumnModel().getColumn(4).setMaxWidth(200);
            TagTable.getColumnModel().getColumn(5).setMinWidth(230);
            TagTable.getColumnModel().getColumn(5).setPreferredWidth(230);
            TagTable.getColumnModel().getColumn(5).setMaxWidth(230);
        }

        SearchTags.setText("Search Tagged Files");
        SearchTags.setFont(SearchTags.getFont().deriveFont(SearchTags.getFont().getSize()+1f));
        SearchTags.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchTagsActionPerformed(evt);
            }
        });

        jList1.setFont(jList1.getFont().deriveFont(jList1.getFont().getSize()+3f));
        jList1.setInheritsPopupMenu(true);
        jList1.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        jList1.setValueIsAdjusting(true);
        jList1.setVisibleRowCount(6);
        jScrollPane3.setViewportView(jList1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(displayTaggedFilesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1037, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SearchTags, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(SearchTags, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                        .addGap(22, 22, 22))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(displayTaggedFilesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 817, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        TabbedPane.addTab("Tagged Files", jPanel3);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() | java.awt.Font.BOLD, jLabel4.getFont().getSize()+2));
        jLabel4.setText("Create a Tag");

        AddTagTextField.setFont(AddTagTextField.getFont().deriveFont(AddTagTextField.getFont().getSize()+2f));

        AddTagButton.setText("Create Tag");
        AddTagButton.setFont(AddTagButton.getFont().deriveFont(AddTagButton.getFont().getSize()+2f));
        AddTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddTagButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(AddTagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AddTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4))
                .addGap(12, 12, 12))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AddTagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AddTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel5.setFont(jLabel5.getFont().deriveFont(jLabel5.getFont().getStyle() | java.awt.Font.BOLD, jLabel5.getFont().getSize()+2));
        jLabel5.setText("Edit or Rename Existing Tag");

        jLabel7.setFont(jLabel7.getFont().deriveFont(jLabel7.getFont().getStyle() | java.awt.Font.BOLD, jLabel7.getFont().getSize()+2));
        jLabel7.setText("From");

        RenameTagComboBox.setEditable(true);
        RenameTagComboBox.setFont(RenameTagComboBox.getFont().deriveFont(RenameTagComboBox.getFont().getSize()+2f));
        RenameTagComboBox.setMaximumRowCount(12);
        RenameTagComboBox.setAutoscrolls(true);

        jLabel8.setFont(jLabel8.getFont().deriveFont(jLabel8.getFont().getStyle() | java.awt.Font.BOLD, jLabel8.getFont().getSize()+2));
        jLabel8.setText("To");

        RenameTagTextField.setFont(RenameTagTextField.getFont().deriveFont(RenameTagTextField.getFont().getSize()+2f));

        RenameTagButton.setText("Rename Tag");
        RenameTagButton.setFont(RenameTagButton.getFont().deriveFont(RenameTagButton.getFont().getSize()+2f));
        RenameTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RenameTagButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(RenameTagComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(RenameTagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(RenameTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addGap(12, 12, 12))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RenameTagComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RenameTagTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RenameTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addGap(12, 12, 12))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getStyle() | java.awt.Font.BOLD, jLabel6.getFont().getSize()+2));
        jLabel6.setText("Delete Existing Tag");

        DeleteTagComboBox.setEditable(true);
        DeleteTagComboBox.setFont(DeleteTagComboBox.getFont().deriveFont(DeleteTagComboBox.getFont().getSize()+2f));
        DeleteTagComboBox.setMaximumRowCount(12);
        DeleteTagComboBox.setAutoscrolls(true);

        DeleteTagButton.setText("Delete Tag");
        DeleteTagButton.setFont(DeleteTagButton.getFont().deriveFont(DeleteTagButton.getFont().getSize()+2f));
        DeleteTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteTagButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(DeleteTagComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DeleteTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DeleteTagComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DeleteTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getStyle() | java.awt.Font.BOLD, jLabel3.getFont().getSize()+2));
        jLabel3.setText("Assign Tags to Color Buttons");

        RedComboBox.setEditable(true);
        RedComboBox.setFont(RedComboBox.getFont().deriveFont(RedComboBox.getFont().getSize()+2f));
        RedComboBox.setMaximumRowCount(12);
        RedComboBox.setAutoscrolls(true);

        SetRedTagButton.setBackground(new java.awt.Color(255, 77, 77));
        SetRedTagButton.setForeground(new java.awt.Color(0, 0, 0));
        SetRedTagButton.setText("Set Tag");
        SetRedTagButton.setFont(SetRedTagButton.getFont().deriveFont(SetRedTagButton.getFont().getSize()+3f));
        SetRedTagButton.setRippleColor(new java.awt.Color(255, 204, 204));
        SetRedTagButton.setShadowColor(new java.awt.Color(153, 0, 0));
        SetRedTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetRedTagButtonActionPerformed(evt);
            }
        });

        BlueComboBox.setEditable(true);
        BlueComboBox.setFont(BlueComboBox.getFont().deriveFont(BlueComboBox.getFont().getSize()+2f));
        BlueComboBox.setMaximumRowCount(12);
        BlueComboBox.setAutoscrolls(true);

        SetBlueTagButton.setBackground(new java.awt.Color(0, 153, 255));
        SetBlueTagButton.setForeground(new java.awt.Color(0, 0, 0));
        SetBlueTagButton.setText("Set Tag");
        SetBlueTagButton.setFont(SetBlueTagButton.getFont().deriveFont(SetBlueTagButton.getFont().getSize()+3f));
        SetBlueTagButton.setRippleColor(new java.awt.Color(102, 204, 255));
        SetBlueTagButton.setShadowColor(new java.awt.Color(0, 51, 102));
        SetBlueTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetBlueTagButtonActionPerformed(evt);
            }
        });

        GreenComboBox.setEditable(true);
        GreenComboBox.setFont(GreenComboBox.getFont().deriveFont(GreenComboBox.getFont().getSize()+2f));
        GreenComboBox.setMaximumRowCount(12);
        GreenComboBox.setAutoscrolls(true);

        SetGreenTagButton.setBackground(new java.awt.Color(63, 213, 63));
        SetGreenTagButton.setForeground(new java.awt.Color(0, 0, 0));
        SetGreenTagButton.setText("Set Tag");
        SetGreenTagButton.setFont(SetGreenTagButton.getFont().deriveFont(SetGreenTagButton.getFont().getSize()+3f));
        SetGreenTagButton.setRippleColor(new java.awt.Color(153, 255, 153));
        SetGreenTagButton.setShadowColor(new java.awt.Color(0, 102, 51));
        SetGreenTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetGreenTagButtonActionPerformed(evt);
            }
        });

        AzureComboBox.setEditable(true);
        AzureComboBox.setFont(AzureComboBox.getFont().deriveFont(AzureComboBox.getFont().getSize()+2f));
        AzureComboBox.setMaximumRowCount(12);
        AzureComboBox.setAutoscrolls(true);

        SetAzureTagButton.setBackground(new java.awt.Color(0, 243, 243));
        SetAzureTagButton.setForeground(new java.awt.Color(0, 0, 0));
        SetAzureTagButton.setText("Set Tag");
        SetAzureTagButton.setFont(SetAzureTagButton.getFont().deriveFont(SetAzureTagButton.getFont().getSize()+3f));
        SetAzureTagButton.setRippleColor(new java.awt.Color(46, 183, 252));
        SetAzureTagButton.setShadowColor(new java.awt.Color(0, 120, 120));
        SetAzureTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetAzureTagButtonActionPerformed(evt);
            }
        });

        OrangeComboBox.setEditable(true);
        OrangeComboBox.setFont(OrangeComboBox.getFont().deriveFont(OrangeComboBox.getFont().getSize()+2f));
        OrangeComboBox.setMaximumRowCount(12);
        OrangeComboBox.setAutoscrolls(true);

        SetOrangeTagButton.setBackground(new java.awt.Color(255, 152, 23));
        SetOrangeTagButton.setForeground(new java.awt.Color(0, 0, 0));
        SetOrangeTagButton.setText("Set Tag");
        SetOrangeTagButton.setFont(SetOrangeTagButton.getFont().deriveFont(SetOrangeTagButton.getFont().getSize()+3f));
        SetOrangeTagButton.setRippleColor(new java.awt.Color(255, 217, 133));
        SetOrangeTagButton.setShadowColor(new java.awt.Color(204, 102, 0));
        SetOrangeTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetOrangeTagButtonActionPerformed(evt);
            }
        });

        YellowComboBox.setEditable(true);
        YellowComboBox.setFont(YellowComboBox.getFont().deriveFont(YellowComboBox.getFont().getSize()+2f));
        YellowComboBox.setMaximumRowCount(12);
        YellowComboBox.setAutoscrolls(true);

        SetYellowTagButton.setBackground(new java.awt.Color(243, 234, 35));
        SetYellowTagButton.setForeground(new java.awt.Color(0, 0, 0));
        SetYellowTagButton.setText("Set Tag");
        SetYellowTagButton.setFont(SetYellowTagButton.getFont().deriveFont(SetYellowTagButton.getFont().getSize()+3f));
        SetYellowTagButton.setRippleColor(new java.awt.Color(202, 187, 0));
        SetYellowTagButton.setShadowColor(new java.awt.Color(153, 153, 0));
        SetYellowTagButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetYellowTagButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(RedComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(BlueComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(GreenComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(AzureComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(OrangeComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(YellowComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(SetBlueTagButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SetGreenTagButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SetAzureTagButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SetOrangeTagButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SetYellowTagButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SetRedTagButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(12, 12, 12))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RedComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SetRedTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BlueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SetBlueTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(GreenComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SetGreenTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AzureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SetAzureTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OrangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SetOrangeTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(YellowComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SetYellowTagButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(744, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(266, Short.MAX_VALUE))
        );

        TabbedPane.addTab("Settings", jPanel4);

        fileTree.setForeground(new java.awt.Color(0, 0, 0));
        fileTree.setAutoscrolls(true);
        fileTree.setCurrentFile(null);
        fileTree.setExpandsSelectedPaths(false);
        fileTree.setFocusable(false);
        fileTree.setFont(fileTree.getFont().deriveFont(fileTree.getFont().getSize()+2f));
        fileTree.setInheritsPopupMenu(true);
        fileTree.setScrollsOnExpand(false);
        jScrollPane2.setViewportView(fileTree);

        FilesLabel.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        FilesLabel.setText("Files");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(FilesLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FilesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(TabbedPane)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(TabbedPane, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void SettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SettingsButtonActionPerformed
        // TODO add your handling code here:
        TabbedPane.setSelectedIndex(2);
    }//GEN-LAST:event_SettingsButtonActionPerformed

    private void RedTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RedTagButtonActionPerformed
        // TODO add your handling code here:
        TabbedPane.setSelectedIndex(1);
        
        // Display Tagged Files with Color Red in JTable
        DefaultTableModel model = (DefaultTableModel)TagTable.getModel();
        model.setRowCount(0);
        redtaggedFiles_view();
    }//GEN-LAST:event_RedTagButtonActionPerformed

    private void BlueTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BlueTagButtonActionPerformed
        // TODO add your handling code here:
        TabbedPane.setSelectedIndex(1);
        
        // Display Tagged Files with Color Blue in JTable
        DefaultTableModel model = (DefaultTableModel)TagTable.getModel();
        model.setRowCount(0);
        bluetaggedFiles_view();
    }//GEN-LAST:event_BlueTagButtonActionPerformed

    private void GreenTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GreenTagButtonActionPerformed
        // TODO add your handling code here:
        TabbedPane.setSelectedIndex(1);
        
        // Display Tagged Files with Color Green in JTable
        DefaultTableModel model = (DefaultTableModel)TagTable.getModel();
        model.setRowCount(0);
        greentaggedFiles_view();
    }//GEN-LAST:event_GreenTagButtonActionPerformed

    private void AzureTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AzureTagButtonActionPerformed
        // TODO add your handling code here:
        TabbedPane.setSelectedIndex(1);
        
        // Display Tagged Files with Color Azure in JTable
        DefaultTableModel model = (DefaultTableModel)TagTable.getModel();
        model.setRowCount(0);
        azuretaggedFiles_view();
    }//GEN-LAST:event_AzureTagButtonActionPerformed

    private void OrangeTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OrangeTagButtonActionPerformed
        // TODO add your handling code here:
        TabbedPane.setSelectedIndex(1);
        
        // Display Tagged Files with Color Orange in JTable
        DefaultTableModel model = (DefaultTableModel)TagTable.getModel();
        model.setRowCount(0);
        orangetaggedFiles_view();
    }//GEN-LAST:event_OrangeTagButtonActionPerformed

    private void YellowTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YellowTagButtonActionPerformed
        // TODO add your handling code here:
        TabbedPane.setSelectedIndex(1);
        
        // Display Tagged Files with Color Yellow in JTable
        DefaultTableModel model = (DefaultTableModel)TagTable.getModel();
        model.setRowCount(0);
        yellowtaggedFiles_view();
    }//GEN-LAST:event_YellowTagButtonActionPerformed

    private void displayTaggedFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayTaggedFilesButtonActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel)TagTable.getModel();
        model.setRowCount(0);
        taggedFiles_view();
    }//GEN-LAST:event_displayTaggedFilesButtonActionPerformed

    private void AddTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddTagButtonActionPerformed
        // TODO add your handling code here:
        if (AddTagTextField.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "The Field is Empty");
        } else {
            try {
                try ( // Establish Connection
                        Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                    statement.executeUpdate("INSERT INTO customtags(tags) VALUES('" + AddTagTextField.getText() + "')");  
                    statement.close();
                    con.close();
                }
            }
            catch (SQLException  se) {
                JOptionPane.showMessageDialog(null, se);
            }
        }
        AddTagTextField.setText("");

        updateComboBox();
        updateRedButtonName();
        updateBlueButtonName();
        updateGreenButtonName();
        updateAzureButtonName();
        updateOrangeButtonName();
        updateYellowButtonName();
    }//GEN-LAST:event_AddTagButtonActionPerformed

    private void SetYellowTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetYellowTagButtonActionPerformed
        // TODO add your handling code here:
        try {
            try ( // Establish Connection
                Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                statement.executeUpdate("UPDATE colortags SET color_tag_name = '" + YellowComboBox.getSelectedItem() + "' WHERE color = 'YELLOW'");
                statement.close();
                con.close();
                updateYellowButtonName();
                }
            }
        catch (SQLException  se) {
                JOptionPane.showMessageDialog(null, se);
        }
    }//GEN-LAST:event_SetYellowTagButtonActionPerformed

    private void SetOrangeTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetOrangeTagButtonActionPerformed
        // TODO add your handling code here:
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                statement.executeUpdate("UPDATE colortags SET color_tag_name = '" + OrangeComboBox.getSelectedItem() + "' WHERE color = 'ORANGE'");
                statement.close();
                con.close();
                updateOrangeButtonName();
            }
            }
        catch (SQLException  se) {
                JOptionPane.showMessageDialog(null, se);
            } 
    }//GEN-LAST:event_SetOrangeTagButtonActionPerformed

    private void SetAzureTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetAzureTagButtonActionPerformed
        // TODO add your handling code here:
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                statement.executeUpdate("UPDATE colortags SET color_tag_name = '" + AzureComboBox.getSelectedItem() + "' WHERE color = 'AZURE'");
                statement.close();
                con.close();
                updateAzureButtonName();                
            }
            }
        catch (SQLException  se) {
                JOptionPane.showMessageDialog(null, se);
            } 
    }//GEN-LAST:event_SetAzureTagButtonActionPerformed

    private void SetGreenTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetGreenTagButtonActionPerformed
        // TODO add your handling code here:
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                statement.executeUpdate("UPDATE colortags SET color_tag_name = '" + GreenComboBox.getSelectedItem() + "' WHERE color = 'GREEN'");
                statement.close();
                con.close();
                updateGreenButtonName();                
            }
            }
        catch (SQLException  se) {
                JOptionPane.showMessageDialog(null, se);
            } 
    }//GEN-LAST:event_SetGreenTagButtonActionPerformed

    private void SetBlueTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetBlueTagButtonActionPerformed
        // TODO add your handling code here:
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                statement.executeUpdate("UPDATE colortags SET color_tag_name = '" + BlueComboBox.getSelectedItem() + "' WHERE color = 'BLUE'");
                statement.close();
                con.close();
                updateBlueButtonName();

            }
            }
        catch (SQLException  se) {
                JOptionPane.showMessageDialog(null, se);
            } 
    }//GEN-LAST:event_SetBlueTagButtonActionPerformed

    private void SetRedTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetRedTagButtonActionPerformed
        // TODO add your handling code here:
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                statement.executeUpdate("UPDATE colortags SET color_tag_name = '" + RedComboBox.getSelectedItem() + "' WHERE color = 'RED'");
                statement.close();
                con.close();
                updateRedButtonName();                
            }
            }
        catch (SQLException  se) {
                JOptionPane.showMessageDialog(null, se);
            } 
    }//GEN-LAST:event_SetRedTagButtonActionPerformed

    private void DeleteTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteTagButtonActionPerformed
        // TODO add your handling code here:
        try {
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tagged_files_info WHERE color_tag = '" + DeleteTagComboBox.getSelectedItem() + "'");
                int count = 0;
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
                
                resultSet = statement.executeQuery("SELECT COUNT(*) FROM colortags WHERE color_tag_name = '" + DeleteTagComboBox.getSelectedItem() + "'");
                int count2 = 0;
                if (resultSet.next()) {
                    count2 = resultSet.getInt(1);
                }
               
                if (count == 0 && count2 == 0) {
                    statement.executeUpdate("DELETE FROM customtags WHERE tags = '" + DeleteTagComboBox.getSelectedItem() + "'");
                    JOptionPane.showMessageDialog(null, "Tag deleted successfully.");
                    
                    updateComboBox();
                    updateRedButtonName();
                    updateBlueButtonName();
                    updateGreenButtonName();
                    updateAzureButtonName();
                    updateOrangeButtonName();
                    updateYellowButtonName();
                }
                else {
                    JOptionPane.showMessageDialog(null, "Tag could not be deleted because files are tagged with the selected name, please update the files to another tag.");
                }
                statement.close();
                con.close();
            }
        } 
        catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }//GEN-LAST:event_DeleteTagButtonActionPerformed

    private void RenameTagButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RenameTagButtonActionPerformed
        // TODO add your handling code here:
        if (RenameTagTextField.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "The Field is Empty");
        } 
        else {
            try {
                try ( // Establish Connection
                        Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                    String previousName = RenameTagComboBox.getSelectedItem().toString();
                    
                    statement.executeUpdate("UPDATE customtags SET tags = '" + RenameTagTextField.getText() + "' WHERE tags = '" + RenameTagComboBox.getSelectedItem() + "'" );
                    statement.executeUpdate("UPDATE colortags SET color_tag_name = '" + RenameTagTextField.getText() + "' WHERE color_tag_name = '" + previousName + "'");
                    statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + RenameTagTextField.getText() + "' WHERE color_tag = '" + previousName + "'");
                    statement.executeUpdate("UPDATE tagged_files_info SET customtag1 = '" + RenameTagTextField.getText() + "' WHERE customtag1 = '" + previousName + "'");
                    statement.executeUpdate("UPDATE tagged_files_info SET customtag2 = '" + RenameTagTextField.getText() + "' WHERE customtag2 = '" + previousName + "'");
                    statement.executeUpdate("UPDATE tagged_files_info SET customtag3 = '" + RenameTagTextField.getText() + "' WHERE customtag3 = '" + previousName + "'");
                    
                    updateComboBox();
                    updateRedButtonName();
                    updateBlueButtonName();
                    updateGreenButtonName();
                    updateAzureButtonName();
                    updateOrangeButtonName();
                    updateYellowButtonName();
                    
                statement.close();
                con.close();
                    
                }
                
                RenameTagTextField.setText("");
            }
            catch (SQLException  se) {
                JOptionPane.showMessageDialog(null, se);
            }
        }
    }//GEN-LAST:event_RenameTagButtonActionPerformed

    private void AddTagsToSelectedFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddTagsToSelectedFilesButtonActionPerformed
        // TODO add your handling code here:
        
        try {
            // Class
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db")) {
                DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                int[] rows = fileTable.getSelectedRows();

                String sql = "INSERT INTO tagged_files_info (file_name, file_path, file_type, color_tag, customtag1, customtag2, customtag3) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {

                    for (int i = 0; i < rows.length; i++) {
                        String fileName = model.getValueAt(rows[i], 1).toString();
                        String filePath = model.getValueAt(rows[i], 2).toString();
                        String fileType = model.getValueAt(rows[i], 3).toString();

                        // Duplicate check
                        Set<String> tags = new HashSet<>();
                        String colorTag = Tag1ComboBox.getSelectedItem() != null ? Tag1ComboBox.getSelectedItem().toString() : "";
                        String customTag1 = Tag2ComboBox.getSelectedItem() != null ? Tag2ComboBox.getSelectedItem().toString() : "";
                        String customTag2 = Tag3ComboBox.getSelectedItem() != null ? Tag3ComboBox.getSelectedItem().toString() : "";
                        String customTag3 = Tag4ComboBox.getSelectedItem() != null ? Tag4ComboBox.getSelectedItem().toString() : "";

                        if (!colorTag.isEmpty()) {
                            if (tags.contains(colorTag)) {
                                throw new Exception("Duplicate tag values found, Select a different one!");
                            }
                            tags.add(colorTag);
                        }
                        if (!customTag1.isEmpty()) {
                            if (tags.contains(customTag1)) {
                                throw new Exception("Duplicate tag values found, Select a different one!");
                            }
                            tags.add(customTag1);
                        }
                        if (!customTag2.isEmpty()) {
                            if (tags.contains(customTag2)) {
                                throw new Exception("Duplicate tag values found, Select a different one!");
                            }
                            tags.add(customTag2);
                        }
                        if (!customTag3.isEmpty()) {
                            if (tags.contains(customTag3)) {
                                throw new Exception("Duplicate tag values found, Select a different one!");
                            }
                            tags.add(customTag3);
                        }

                        // If all tags are empty, the optional pane shows  error
                        if (tags.isEmpty()) {
                            throw new Exception("No tags selected!");
                        }

                        pstmt.setString(1, fileName);
                        pstmt.setString(2, filePath);
                        pstmt.setString(3, fileType);
                        pstmt.setString(4, colorTag);
                        pstmt.setString(5, customTag1);
                        pstmt.setString(6, customTag2);
                        pstmt.setString(7, customTag3);
                        pstmt.addBatch();
                    }

                    int[] rowsInserted = pstmt.executeBatch();
                    if (rowsInserted.length > 0) {
                        customtaggedFiles_view();
                    }
                    
                
                }
                taggedFiles_view();
                con.close();
            }
        } catch (SQLException  se) {
            JOptionPane.showMessageDialog(null, "The selected file(s) are already tagged, select 'Update Tags' button to add tags to file(s).");
        } catch (Exception e) {
            if (e.getMessage().equals("No tags selected!") || e.getMessage().equals("Duplicate tag values found, Select a different one!")) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            } else {
                customtaggedFiles_view();
            }
        }
        
        Tag1ComboBox.setSelectedIndex(-1);
        Tag2ComboBox.setSelectedIndex(-1);
        Tag3ComboBox.setSelectedIndex(-1);
        Tag4ComboBox.setSelectedIndex(-1);
    }//GEN-LAST:event_AddTagsToSelectedFilesButtonActionPerformed


    
    private void UpdateExistingTagstoSelectedFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateExistingTagstoSelectedFilesButtonActionPerformed
        // TODO add your handling code here:
        
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                
                DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                
                int[] rows = fileTable.getSelectedRows();

                for (int i = 0; i <= rows.length; i++) {
                
                    String fileName = model.getValueAt(rows[i], 1).toString();
                    HashSet<String> tagSet = new HashSet<>();
                    String colorTag = Tag1ComboBox.getSelectedItem() != null ? Tag1ComboBox.getSelectedItem().toString() : "";
                    if (!colorTag.equals("")) {
                        if (tagSet.contains(colorTag)) {
                            JOptionPane.showMessageDialog(null, "Duplicate tag selected: " + colorTag);
                            return;
                        }
                        tagSet.add(colorTag);
                    }

                    String customTag1 = Tag2ComboBox.getSelectedItem() != null ? Tag2ComboBox.getSelectedItem().toString() : "";
                    if (!customTag1.equals("")) {
                        if (tagSet.contains(customTag1)) {
                            JOptionPane.showMessageDialog(null, "Duplicate tag selected: " + customTag1);
                            return;
                        }
                        tagSet.add(customTag1);
                    }

                    String customTag2 = Tag3ComboBox.getSelectedItem() != null ? Tag3ComboBox.getSelectedItem().toString() : "";
                    if (!customTag2.equals("")) {
                        if (tagSet.contains(customTag2)) {
                            JOptionPane.showMessageDialog(null, "Duplicate tag selected: " + customTag2);
                            return;
                        }
                        tagSet.add(customTag2);
                    }

                    String customTag3 = Tag4ComboBox.getSelectedItem() != null ? Tag4ComboBox.getSelectedItem().toString() : "";
                    if (!customTag3.equals("")) {
                        if (tagSet.contains(customTag3)) {
                            JOptionPane.showMessageDialog(null, "Duplicate tag selected: " + customTag3);
                            return;
                        }
                        tagSet.add(customTag3);
                    }
                    
                    if (tagSet.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Select at least one tag to update from comboboxes");
                        return;
                    }

                    statement.executeUpdate("UPDATE tagged_files_info SET color_tag = '" + colorTag + "', "
                            + "customtag1 = '" + customTag1 + "', " + "customtag2 = '" + customTag2 + "', "
                            + "customtag3 = '" + customTag3 + "' " + "WHERE file_name = '" + fileName + "'");

                    customtaggedFiles_view();
                }
                taggedFiles_view();
                statement.close();
                con.close();
            }
        } catch(SQLException  se) {
            JOptionPane.showMessageDialog(null, se);
        }

        
        Tag1ComboBox.setSelectedIndex(-1);
        Tag2ComboBox.setSelectedIndex(-1);
        Tag3ComboBox.setSelectedIndex(-1);
        Tag4ComboBox.setSelectedIndex(-1);
    }//GEN-LAST:event_UpdateExistingTagstoSelectedFilesButtonActionPerformed

    private void DeleteTagsToSelectedFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteTagsToSelectedFilesActionPerformed
        // TODO add your handling code here:      
        int[] rows = fileTable.getSelectedRows();
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(null, "Please select a file to delete tags for.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String message;
            if (rows.length == 1) {
                String fileName = model.getValueAt(rows[0], 1).toString();
                int count = 0;
                try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); 
                    Statement statement = con.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                    statement.close();
                con.close();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                }
                if (count == 0) {
                    JOptionPane.showMessageDialog(null, "The file " + fileName + " is not tagged, use the 'Add New Tags' button to add tags.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                message = "Are you sure you want to delete all tags for " + fileName + "?";
            } else {
                message = "Are you sure you want to delete all tags from selected files?";
            }
            int option = JOptionPane.showConfirmDialog(null, message, "Confirmation", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                try (Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); 
                    Statement statement = con.createStatement()) {
                    con.setAutoCommit(false);
                    for (int i = 0; i < rows.length; i++) {
                        String fileName = model.getValueAt(rows[i], 1).toString();
                        statement.addBatch("DELETE FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                        fileTable.setValueAt("", rows[i], 4);
                    }
                    statement.executeBatch();
                    con.commit();
                    customtaggedFiles_view();
                    refreshTagComboBoxComponent();
                    
                    statement.close();
                con.close();
                } catch (SQLException se) {
                    JOptionPane.showMessageDialog(null, "Error deleting file from database: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        Tag1ComboBox.setSelectedIndex(-1);
        Tag2ComboBox.setSelectedIndex(-1);
        Tag3ComboBox.setSelectedIndex(-1);
        Tag4ComboBox.setSelectedIndex(-1);
    }//GEN-LAST:event_DeleteTagsToSelectedFilesActionPerformed
 
    private void fileTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileTableMouseClicked
        // TODO add your handling code here:
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                String fileName = model.getValueAt(fileTable.getSelectedRow(), 1).toString();
                ResultSet resultSet = statement.executeQuery("SELECT color_tag, customtag1, customtag2, customtag3 FROM tagged_files_info WHERE file_name = '" + fileName + "'");
                String colorTag = null, customTag1 = null, customTag2 = null, customTag3 = null;
                if (resultSet.next()) {
                    colorTag = resultSet.getString("color_tag");
                    customTag1 = resultSet.getString("customtag1");
                    customTag2 = resultSet.getString("customtag2");
                    customTag3 = resultSet.getString("customtag3");
                }   
                fileNameLabel.setText(fileName);
                Tag1ComboBox.setSelectedItem(colorTag);
                Tag2ComboBox.setSelectedItem(customTag1);
                Tag3ComboBox.setSelectedItem(customTag2);
                Tag4ComboBox.setSelectedItem(customTag3);
                statement.close();
                con.close();
            }
        } 
        catch (SQLException  se) {
            JOptionPane.showMessageDialog(null, se);
        }
    }//GEN-LAST:event_fileTableMouseClicked

    private void SearchTagsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchTagsActionPerformed
        // TODO add your handling code here:
   
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                
                String selectedItems = "";
                List<String> selectedTags = jList1.getSelectedValuesList();
                
                for (String tag : selectedTags) {
                    selectedItems += "'" + tag + "', ";
                }
                
                if (!selectedItems.equals("")) {
                    selectedItems = selectedItems.substring(0, selectedItems.length() - 2);
                }
                
                String query = "SELECT file_name, file_path, file_type, color_tag, customtag1, customtag2, customtag3, " +
                        "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || " +
                        "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || " +
                        "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || " +
                        "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) || " +
                        "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || " +
                        "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || " +
                        "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags " +
                        "FROM tagged_files_info " +
                        "WHERE color_tag IN (" + selectedItems + ") OR customtag1 IN (" + selectedItems + ") OR customtag2 IN (" + selectedItems + ") OR customtag3 IN (" + selectedItems + ")";
                ResultSet resultSet = statement.executeQuery(query);
                
                DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
                model.setRowCount(0);
                
                while (resultSet.next()) {
                    String filePath = resultSet.getString("file_path");
                    Icon fileIcon = FileSystemView.getFileSystemView().getSystemIcon(new File(filePath));
                    
                    ImageIcon icon = new ImageIcon(((ImageIcon) fileIcon).getImage());
                    
                    Object[] row = {icon, resultSet.getString("file_name"), filePath, resultSet.getString("file_type"), resultSet.getString("tags")};
                    model.addRow(row);
                }
                statement.close();
                con.close();
            }
        } 
        catch (SQLException se) {
            JOptionPane.showMessageDialog(null, se);
        }

    }//GEN-LAST:event_SearchTagsActionPerformed

    private void displayTaggedFilesButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayTaggedFilesButton1ActionPerformed
        // TODO add your handling code here:
        TabbedPane.setSelectedIndex(1);
        
        DefaultTableModel model = (DefaultTableModel)TagTable.getModel();
        model.setRowCount(0);
        taggedFiles_view();
    }//GEN-LAST:event_displayTaggedFilesButton1ActionPerformed

    /* the name of the OS as given by the Java system property "os.name" */
    public final static String osname = System.getProperty("os.name");
    
    /* true if the program is running on OS X */
    public final static boolean isOSX = osname.equalsIgnoreCase("Mac OS X");
    
    /* true if the program is running on Linux */
    public final static boolean isLinux = osname.equalsIgnoreCase("Linux");
    
    /* true if the program is running on Windows OS*/
    public final static boolean isWindows = !(isOSX || isLinux);
    
    public static void main(String args[]) throws InterruptedException, JNotifyException, UnsupportedLookAndFeelException, FileNotFoundException, IOException {
        
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        
        UIManager.put("Table.showGrid", true);
        UIManager.put("Table.intercellSpacing", new Dimension(1, 1));

        UIManager.put( "ScrollBar.showButtons", true );
        UIManager.put( "ScrollBar.width", 16 );
        UIManager.put( "ScrollBar.trackInsets", new Insets( 2, 4, 2, 4 ) );
        UIManager.put( "ScrollBar.thumbInsets", new Insets( 2, 2, 2, 2 ) );
        UIManager.put( "ScrollBar.track", new Color( 0xe0e0e0 ) );
        
        UIManager.put( "TabbedPane.showTabSeparators", true );
        
        UIManager.put("CheckBox.arc", 10);

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new FM_MainFrame().setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(FM_MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        // Start JNotify
        if (isWindows) {
             // Start JNotify
            // The system being monitored
            File[] roots = File.listRoots();
            

            for (File root : roots) {
                String dir = root.getAbsolutePath();
                // Add the watch on the directory
                int mask = JNotify.FILE_CREATED | 
                           JNotify.FILE_DELETED | 
                           JNotify.FILE_MODIFIED | 
                           JNotify.FILE_RENAMED;
                boolean watchSubtree = true;
                int watchID = JNotify.addWatch(dir, mask, watchSubtree, new Listener());
            }
        } 
        else if (isLinux || isOSX) {
            // The directory being monitored
            String dir = "user.home";

            // Add the watch on the directory
            int mask = JNotify.FILE_CREATED  | 
                       JNotify.FILE_DELETED  | 
                       JNotify.FILE_MODIFIED | 
                       JNotify.FILE_RENAMED;
            boolean watchSubtree = true;
            int watchID = JNotify.addWatch(dir, mask, watchSubtree, new Listener());
        }
          
        // Wait for events every 2500 milliseconds
        while (true) {
            Thread.sleep(2500);
        }
    }
    

    // Create Database and Tables for Tagging 
    public void createDatabase(){
        // Create Database and Tables
        try {
           
            
            try ( // Establish connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                
                System.out.println("Opened database successfully");
                
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS tagged_files_info"
                        + "("
                        + "file_name TEXT UNIQUE,"
                        + "file_path TEXT UNIQUE,"
                        + "file_type TEXT,"
                        + "color_tag TEXT,"
                        + "customtag1 TEXT,"
                        + "customtag2 TEXT,"
                        + "customtag3 TEXT"
                        + ")");
                
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS customtags"
                        + "("
                        + "tags TEXT UNIQUE"
                        + ")");
                
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS colortags"
                        + "("
                        + "color TEXT UNIQUE,"
                        + "color_tag_name TEXT UNIQUE"
                        + ")");
                
            }  
        } 
        catch (SQLException  se) {  
            JOptionPane.showMessageDialog(null, se);  
        }
    }

    // Insert data into Color Tag Table
    public void insertDataInColorTagTable(){
        // Insert data into Color Tag Table 
        try {
           
            
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                
                statement.executeUpdate("INSERT INTO colortags(color, color_tag_name) VALUES"
                        + "('RED', 'RED'),"
                        + "('BLUE', 'BLUE'),"
                        + "('GREEN', 'GREEN'),"
                        + "('AZURE', 'AZURE'),"
                        + "('ORANGE', 'ORANGE'),"
                        + "('YELLOW', 'YELLOW')");
                
            }  
        } 
        catch ( SQLException e) {  
        }
    }
    
    
    // Display custom tags from SQLite database to the fileTable in File Info Panel 
    private Map<String, List<DisplayTagInfo_FileInfoPane>> customTaggedFilesMap() {
    Map<String, List<DisplayTagInfo_FileInfoPane>> map = new HashMap<>();
    try {
        try ( // Establish connection
                Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
            String query = "SELECT file_name, "
                    + "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || "
                    + "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || "
                    + "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || "
                    + "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) ||"
                    + "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || "
                    + "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || "
                    + "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags "
                    + "FROM tagged_files_info";
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                String fileName = rs.getString("file_name");
                String tags = rs.getString("tags");
                DisplayTagInfo_FileInfoPane dtf = new DisplayTagInfo_FileInfoPane(tags);
                if (!map.containsKey(fileName)) {
                    map.put(fileName, new ArrayList<>());
                }
                map.get(fileName).add(dtf);
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, e);
    }
    return map;
}

    public void customtaggedFiles_view() {
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        Map<String, List<DisplayTagInfo_FileInfoPane>> map = customTaggedFilesMap();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String fileName = model.getValueAt(i, 1).toString();
            if (map.containsKey(fileName)) {
                List<DisplayTagInfo_FileInfoPane> list = map.get(fileName);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < list.size(); j++) {
                    sb.append(list.get(j).getTags());
                    if (j < list.size() - 1) {
                        sb.append(", ");
                    }
                }
                model.setValueAt(sb.toString(), i, 4);
            }
        }
    }

    
  
    
    // Display all tagged files in Tagged Files Panel
    public ArrayList<DisplayTagInfo_TaggedFilesPane> taggedfilesList(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> taggedfilesList = new ArrayList<>();
         try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                String query1 = "SELECT file_name, file_path, file_type, "
                        + "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || "
                        + "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) ||"
                        + "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || "
                        + "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags "
                        + "FROM tagged_files_info";
                ResultSet rs = statement.executeQuery(query1);
                DisplayTagInfo_TaggedFilesPane dtf;
                
                while(rs.next()){
                    dtf = new DisplayTagInfo_TaggedFilesPane(rs.getString("file_name"), rs.getString("file_path"), rs.getString("file_type"), rs.getString("tags"));
                    taggedfilesList.add(dtf);
                }
            }
        }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }   
        return taggedfilesList;
    }
    
    public void taggedFiles_view(){
    ArrayList<DisplayTagInfo_TaggedFilesPane> list = taggedfilesList();
    DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
    Object[] row = new Object[10];
    
    for(int i=0; i<list.size(); i++){
        File file = new File(list.get(i).getFile_path());
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        JLabel iconLabel = new JLabel(icon);
        
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon) icon;
            Image image = imageIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
        }
        row[0] = icon;
        row[1] = list.get(i).getFile_name();
        row[2] = list.get(i).getFile_path();
        row[3] = list.get(i).getFile_type();
        row[4] = list.get(i).getTags();
        model.addRow(row);
    }
}


    // Display Red Tagged Files
    public ArrayList<DisplayTagInfo_TaggedFilesPane> redtaggedfilesList(){
    ArrayList<DisplayTagInfo_TaggedFilesPane> redtaggedfilesList = new ArrayList<>();
     try {
        try ( // Establish Connection
                Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
            String query1 = "SELECT file_name, file_path, file_type, "
                    + "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || "
                    + "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || "
                    + "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || "
                    + "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) ||"
                    + "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || "
                    + "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || "
                    + "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags "
                    + "FROM tagged_files_info WHERE color_tag = '" + RedTagButton.getText() + "'";
            ResultSet rs = statement.executeQuery(query1);
            DisplayTagInfo_TaggedFilesPane dtf;
            
            while(rs.next()){
                dtf = new DisplayTagInfo_TaggedFilesPane(rs.getString("file_name"), rs.getString("file_path"), rs.getString("file_type"), rs.getString("tags"));
                redtaggedfilesList.add(dtf);
            }
        }
     }
     catch (SQLException  e) {
        JOptionPane.showMessageDialog(null, e);
    }   
    return redtaggedfilesList;
}

    public void redtaggedFiles_view(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> list = redtaggedfilesList();
        DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
        Object[] row = new Object[10];
    
        for(int i=0; i<list.size(); i++){
            File file = new File(list.get(i).getFile_path());
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        
            if (icon instanceof ImageIcon) {
                ImageIcon imageIcon = (ImageIcon) icon;
                Image image = imageIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                icon = new ImageIcon(image);
            }
            row[0] = icon;
            row[1] = list.get(i).getFile_name();
            row[2] = list.get(i).getFile_path();
            row[3] = list.get(i).getFile_type();
            row[4] = list.get(i).getTags();
            model.addRow(row);
        }
    }


    // Display Blue Tagged files
    public ArrayList<DisplayTagInfo_TaggedFilesPane> bluetaggedfilesList(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> bluetaggedfilesList = new ArrayList<>();
         try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                String query1 = "SELECT file_name, file_path, file_type, "
                        + "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || "
                        + "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) ||"
                        + "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || "
                        + "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags "
                        + "FROM tagged_files_info WHERE color_tag = '" + BlueTagButton.getText() + "'";
                ResultSet rs = statement.executeQuery(query1);
                DisplayTagInfo_TaggedFilesPane dtf;
                while(rs.next()){
                    dtf = new DisplayTagInfo_TaggedFilesPane(rs.getString("file_name"), rs.getString("file_path"), rs.getString("file_type"), rs.getString("tags"));
                    bluetaggedfilesList.add(dtf);
                }
            }
        }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }   
        return bluetaggedfilesList;
    }
    
    public void bluetaggedFiles_view(){
       ArrayList<DisplayTagInfo_TaggedFilesPane> list = bluetaggedfilesList();
        DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
        Object[] row = new Object[10];
    
        for(int i=0; i<list.size(); i++){
            File file = new File(list.get(i).getFile_path());
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        
            if (icon instanceof ImageIcon) {
                ImageIcon imageIcon = (ImageIcon) icon;
                Image image = imageIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                icon = new ImageIcon(image);
            }
        
            row[0] = icon;
            row[1] = list.get(i).getFile_name();
            row[2] = list.get(i).getFile_path();
            row[3] = list.get(i).getFile_type();
            row[4] = list.get(i).getTags();
            model.addRow(row);
        }
}
    
    // Display Green Tagged files
    public ArrayList<DisplayTagInfo_TaggedFilesPane> greentaggedfilesList(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> greentaggedfilesList = new ArrayList<>();
         try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                String query1 = "SELECT file_name, file_path, file_type, "
                        + "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || "
                        + "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) ||"
                        + "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || "
                        + "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags "
                        + "FROM tagged_files_info WHERE color_tag = '" + GreenTagButton.getText() + "'";
                ResultSet rs = statement.executeQuery(query1);
                
                DisplayTagInfo_TaggedFilesPane dtf;
                while(rs.next()){
                    dtf = new DisplayTagInfo_TaggedFilesPane(rs.getString("file_name"), rs.getString("file_path"), rs.getString("file_type"), rs.getString("tags"));
                    greentaggedfilesList.add(dtf);
                }
            }
        }
        catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }   
        return greentaggedfilesList;
    }
    
    public void greentaggedFiles_view(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> list = greentaggedfilesList();
    DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
    Object[] row = new Object[10];
    
    for(int i=0; i<list.size(); i++){
        File file = new File(list.get(i).getFile_path());
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        JLabel iconLabel = new JLabel(icon);
        
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon) icon;
            Image image = imageIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
        }
        
        row[0] = icon;
        row[1] = list.get(i).getFile_name();
        row[2] = list.get(i).getFile_path();
        row[3] = list.get(i).getFile_type();
        row[4] = list.get(i).getTags();
        model.addRow(row);
    }
}
    
    // Display Azure Tagged files
    public ArrayList<DisplayTagInfo_TaggedFilesPane> azuretaggedfilesList(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> azuretaggedfilesList = new ArrayList<>();
         try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                String query1 = "SELECT file_name, file_path, file_type, "
                        + "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || "
                        + "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) ||"
                        + "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || "
                        + "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags "
                        + "FROM tagged_files_info WHERE color_tag = '" + AzureTagButton.getText() + "'";
                ResultSet rs = statement.executeQuery(query1);
                DisplayTagInfo_TaggedFilesPane dtf;
                while(rs.next()){
                    dtf = new DisplayTagInfo_TaggedFilesPane(rs.getString("file_name"), rs.getString("file_path"), rs.getString("file_type"), rs.getString("tags"));
                    azuretaggedfilesList.add(dtf);
                }
            }
         }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }   
        return azuretaggedfilesList;
    }
    
    public void azuretaggedFiles_view(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> list = azuretaggedfilesList();
    DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
    Object[] row = new Object[10];
    
    for(int i=0; i<list.size(); i++){
        File file = new File(list.get(i).getFile_path());
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        JLabel iconLabel = new JLabel(icon);
        
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon) icon;
            Image image = imageIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
        }
        row[0] = icon;
        row[1] = list.get(i).getFile_name();
        row[2] = list.get(i).getFile_path();
        row[3] = list.get(i).getFile_type();
        row[4] = list.get(i).getTags();
        model.addRow(row);
        }
    }
    
    // Display Orange Tagged files
    public ArrayList<DisplayTagInfo_TaggedFilesPane> orangetaggedfilesList(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> orangetaggedfilesList = new ArrayList<>();
         try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                String query1 = "SELECT file_name, file_path, file_type, "
                        + "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || "
                        + "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) ||"
                        + "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || "
                        + "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags "
                        + "FROM tagged_files_info WHERE color_tag = '" + OrangeTagButton.getText() + "'";
                ResultSet rs = statement.executeQuery(query1);
                DisplayTagInfo_TaggedFilesPane dtf;
                while(rs.next()){
                    dtf = new DisplayTagInfo_TaggedFilesPane(rs.getString("file_name"), rs.getString("file_path"), rs.getString("file_type"), rs.getString("tags"));
                    orangetaggedfilesList.add(dtf);
                }
            }
         }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }   
        return orangetaggedfilesList;
    }
    
    public void orangetaggedFiles_view(){
       ArrayList<DisplayTagInfo_TaggedFilesPane> list = orangetaggedfilesList();
    DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
    Object[] row = new Object[10];
    
    for(int i=0; i<list.size(); i++){
        File file = new File(list.get(i).getFile_path());
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        JLabel iconLabel = new JLabel(icon);
        
            if (icon instanceof ImageIcon) {
                ImageIcon imageIcon = (ImageIcon) icon;
                Image image = imageIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                icon = new ImageIcon(image);
            }

            row[0] = icon;
            row[1] = list.get(i).getFile_name();
            row[2] = list.get(i).getFile_path();
            row[3] = list.get(i).getFile_type();
            row[4] = list.get(i).getTags();
            model.addRow(row);
        }
    }
    
    // Display Orange Tagged files
    public ArrayList<DisplayTagInfo_TaggedFilesPane> yellowtaggedfilesList(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> yellowtaggedfilesList = new ArrayList<>();
         try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                String query1 = "SELECT file_name, file_path, file_type, "
                        + "(CASE WHEN color_tag IS NOT NULL THEN color_tag ELSE '' END) || "
                        + "(CASE WHEN color_tag IS NOT NULL AND customtag1 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL THEN customtag1 ELSE '' END) || "
                        + "(CASE WHEN customtag1 IS NOT NULL AND customtag2 IS NOT NULL THEN ', ' ELSE '' END) ||"
                        + "(CASE WHEN customtag2 IS NOT NULL THEN customtag2 ELSE '' END) || "
                        + "(CASE WHEN customtag2 IS NOT NULL AND customtag3 IS NOT NULL THEN ', ' ELSE '' END) || "
                        + "(CASE WHEN customtag3 IS NOT NULL THEN customtag3 ELSE '' END) AS tags "
                        + "FROM tagged_files_info WHERE color_tag = '" + YellowTagButton.getText() + "'";
                
                ResultSet rs = statement.executeQuery(query1);
                DisplayTagInfo_TaggedFilesPane dtf;
                
                while(rs.next()){
                    dtf = new DisplayTagInfo_TaggedFilesPane(rs.getString("file_name"), rs.getString("file_path"), rs.getString("file_type"), rs.getString("tags"));
                    yellowtaggedfilesList.add(dtf);
                }
            }
         }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }   
        return yellowtaggedfilesList;
    }
    
    public void yellowtaggedFiles_view(){
        ArrayList<DisplayTagInfo_TaggedFilesPane> list = yellowtaggedfilesList();
    DefaultTableModel model = (DefaultTableModel) TagTable.getModel();
    Object[] row = new Object[10];
    
    for(int i=0; i<list.size(); i++){
        File file = new File(list.get(i).getFile_path());
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        JLabel iconLabel = new JLabel(icon);
        
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon) icon;
            Image image = imageIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
        }
        
        row[0] = icon;
        row[1] = list.get(i).getFile_name();
        row[2] = list.get(i).getFile_path();
        row[3] = list.get(i).getFile_type();
        row[4] = list.get(i).getTags();
        model.addRow(row);
    }
    }
    
    
    // List items in JComboBox
    public void updateComboBox() {
        try {
            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                String query1 = "select * from customtags";
                ResultSet rs = statement.executeQuery(query1);
                
                DefaultListModel model = new DefaultListModel();
                
                RedComboBox.removeAllItems();
                BlueComboBox.removeAllItems();
                GreenComboBox.removeAllItems();
                AzureComboBox.removeAllItems();
                OrangeComboBox.removeAllItems();
                YellowComboBox.removeAllItems();
                RenameTagComboBox.removeAllItems();
                DeleteTagComboBox.removeAllItems();
                
                Tag1ComboBox.removeAllItems();
                Tag2ComboBox.removeAllItems();
                Tag3ComboBox.removeAllItems();
                Tag4ComboBox.removeAllItems();
                
                Tag1ComboBox.insertItemAt("", 0);
                Tag2ComboBox.insertItemAt("", 0);
                Tag3ComboBox.insertItemAt("", 0);
                Tag4ComboBox.insertItemAt("", 0);
                
                jList1.removeAll();
                
                while(rs.next()){
                    RedComboBox.addItem(rs.getString("tags"));
                    BlueComboBox.addItem(rs.getString("tags"));
                    GreenComboBox.addItem(rs.getString("tags"));
                    AzureComboBox.addItem(rs.getString("tags"));
                    OrangeComboBox.addItem(rs.getString("tags"));
                    YellowComboBox.addItem(rs.getString("tags"));
                    
                    RenameTagComboBox.addItem(rs.getString("tags"));
                    DeleteTagComboBox.addItem(rs.getString("tags"));
                    
                    Tag1ComboBox.addItem(rs.getString("tags"));
                    Tag2ComboBox.addItem(rs.getString("tags"));
                    Tag3ComboBox.addItem(rs.getString("tags"));
                    Tag4ComboBox.addItem(rs.getString("tags"));
                    
                    // Populate JList
                    model.addElement(rs.getString("tags"));
                }
                
                jList1.setModel(model);
            }
            } 
        catch (SQLException e) {
        }   
    }
    
    public void updateRedButtonName(){

            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT color_tag_name FROM colortags WHERE color='RED'");
                
                String ButtonName = "";
                if (resultSet.next()) {
                    ButtonName = resultSet.getString("color_tag_name");
                }
                RedTagButton.setText(ButtonName);
                RedComboBox.setSelectedItem(ButtonName);
            }
         catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    public void updateBlueButtonName(){
        try {
           

            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT color_tag_name FROM colortags WHERE color='BLUE'");

                String ButtonName = "";
                if (resultSet.next()) {
                    ButtonName = resultSet.getString("color_tag_name");
                }
                BlueTagButton.setText(ButtonName);
                BlueComboBox.setSelectedItem(ButtonName);
                
            }
         }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    public void updateGreenButtonName(){
        try {
           

            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT color_tag_name FROM colortags WHERE color='GREEN'");

                String ButtonName = "";
                if (resultSet.next()) {
                    ButtonName = resultSet.getString("color_tag_name");
                }
                GreenTagButton.setText(ButtonName);
                GreenComboBox.setSelectedItem(ButtonName);
                
            }
         }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    public void updateAzureButtonName(){
        try {
           

            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT color_tag_name FROM colortags WHERE color='AZURE'");
                
                String ButtonName = "";
                if (resultSet.next()) {
                    ButtonName = resultSet.getString("color_tag_name");
                }
                AzureTagButton.setText(ButtonName);
                AzureComboBox.setSelectedItem(ButtonName);
            }
         }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    public void updateOrangeButtonName(){
        try {
           

            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT color_tag_name FROM colortags WHERE color='ORANGE'");
                
                String ButtonName = "";
                if (resultSet.next()) {
                    ButtonName = resultSet.getString("color_tag_name");
                }
                OrangeTagButton.setText(ButtonName);
                OrangeComboBox.setSelectedItem(ButtonName);
            }
         }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    public void updateYellowButtonName(){
        try {
           

            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT color_tag_name FROM colortags WHERE color='YELLOW'");
                
                String ButtonName = "";
                if (resultSet.next()) {
                    ButtonName = resultSet.getString("color_tag_name");
                }
                YellowTagButton.setText(ButtonName);
                YellowComboBox.setSelectedItem(ButtonName);
            }
         }
         catch (SQLException  e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    public void refreshTagComboBoxComponent(){
         try {
           

            try ( // Establish Connection
                    Connection con = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = con.createStatement()) {
                DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                String fileName = model.getValueAt(fileTable.getSelectedRow(), 1).toString();
                try (ResultSet resultSet = statement.executeQuery("SELECT color_tag, customtag1, customtag2, customtag3 FROM tagged_files_info WHERE file_name = '" + fileName + "'")) {
                    String colorTag = null, customTag1 = null, customTag2 = null, customTag3 = null;
                    if (resultSet.next()) {
                        colorTag = resultSet.getString("color_tag");
                        customTag1 = resultSet.getString("customtag1");
                        customTag2 = resultSet.getString("customtag2");
                        customTag3 = resultSet.getString("customtag3");
                    }   fileNameLabel.setText(fileName);
                    Tag1ComboBox.setSelectedItem(colorTag);
                    Tag2ComboBox.setSelectedItem(customTag1);
                    Tag3ComboBox.setSelectedItem(customTag2);
                    Tag4ComboBox.setSelectedItem(customTag3);
                }
                statement.close();
                con.close();
            }
        } 
        catch (SQLException  se) {
            JOptionPane.showMessageDialog(null, se);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private filemanager.component.Button_Shadowed AddTagButton;
    private filemanager.component.TextField AddTagTextField;
    private filemanager.component.Button_Shadowed AddTagsToSelectedFilesButton;
    private javax.swing.JComboBox<String> AzureComboBox;
    private filemanager.component.Button_Shadowed AzureTagButton;
    private javax.swing.JComboBox<String> BlueComboBox;
    private filemanager.component.Button_Shadowed BlueTagButton;
    private filemanager.component.Button_Shadowed DeleteTagButton;
    private javax.swing.JComboBox<String> DeleteTagComboBox;
    private filemanager.component.Button_Shadowed DeleteTagsToSelectedFiles;
    private javax.swing.JLabel FilesLabel;
    private javax.swing.JComboBox<String> GreenComboBox;
    private filemanager.component.Button_Shadowed GreenTagButton;
    private javax.swing.JComboBox<String> OrangeComboBox;
    private filemanager.component.Button_Shadowed OrangeTagButton;
    private javax.swing.JComboBox<String> RedComboBox;
    private filemanager.component.Button_Shadowed RedTagButton;
    private filemanager.component.Button_Shadowed RenameTagButton;
    private javax.swing.JComboBox<String> RenameTagComboBox;
    private filemanager.component.TextField RenameTagTextField;
    private filemanager.component.Button_Shadowed SearchTags;
    private filemanager.component.Button_Shadowed SetAzureTagButton;
    private filemanager.component.Button_Shadowed SetBlueTagButton;
    private filemanager.component.Button_Shadowed SetGreenTagButton;
    private filemanager.component.Button_Shadowed SetOrangeTagButton;
    private filemanager.component.Button_Shadowed SetRedTagButton;
    private filemanager.component.Button_Shadowed SetYellowTagButton;
    private filemanager.component.Button_Shadowed SettingsButton;
    private javax.swing.JTabbedPane TabbedPane;
    private javax.swing.JComboBox<String> Tag1ComboBox;
    private javax.swing.JComboBox<String> Tag2ComboBox;
    private javax.swing.JComboBox<String> Tag3ComboBox;
    private javax.swing.JComboBox<String> Tag4ComboBox;
    private filemanager.table.swing.Table TagTable;
    private javax.swing.JLabel TagsLabel;
    private filemanager.component.Button_Shadowed UpdateExistingTagstoSelectedFilesButton;
    private javax.swing.JComboBox<String> YellowComboBox;
    private filemanager.component.Button_Shadowed YellowTagButton;
    private filemanager.component.Button_Shadowed displayTaggedFilesButton;
    private filemanager.component.Button_Shadowed displayTaggedFilesButton1;
    private javax.swing.JLabel fileNameLabel;
    private filemanager.table.swing.Table fileTable;
    private jtree.FileTree fileTree;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables
}


// JNotify Listener
class Listener implements JNotifyListener {
        private String previousFullFilePath;
        private long previousModificationTime;
        private long previousCreationTime;
     
    @Override
    public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
        String renamedFilePath = rootPath + newName;
        String oldFileName = new File(oldName).getName();
        String newFileName = new File(newName).getName();
        
        System.out.println("File renamed from: " + oldFileName + " to: " + newFileName + " at path: " + renamedFilePath);
 
        try {
            try ( // create a connection to the database
                Connection connection = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = connection.createStatement()) {
                statement.executeUpdate("UPDATE tagged_files_info SET file_name = '" + newFileName + "', file_path = '" + renamedFilePath + "' WHERE file_name = '" + oldFileName + "'");
                }
            } 
        catch (SQLException e) {
            System.out.println(e.getMessage());
        } 
        
    }

    @Override
    public void fileModified(int wd, String rootPath, String name) {
            String fullFilePath = rootPath + name;
            File file = new File(fullFilePath);
            previousFullFilePath = fullFilePath;
            previousModificationTime = file.lastModified();
            previousCreationTime = file.lastModified();          
    }
    
    @Override
    public void fileDeleted(int wd, String rootPath, String name) {
        String fullFilePath = rootPath + name;
        File file = new File(fullFilePath);
        previousFullFilePath = fullFilePath;
        previousModificationTime = file.lastModified();
        previousCreationTime = file.lastModified();
        String fileName = new File(name).getName();

        System.out.println("File deleted: " + fileName + " from " + fullFilePath);

        try {
            try ( // create a connection to the database
                Connection connection = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE from tagged_files_info WHERE file_name = '" + fileName +"'");
            }
        } 
        catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
    }

    @Override
    public void fileCreated(int wd, String rootPath, String name) {
        String fullFilePath = rootPath + name;
        String fileName = new File(name).getName();
        
        System.out.println("The file: " + fileName + " has been moved to " + fullFilePath);
        
        // SQL Query for File Moving 
        try {
            try ( // create a connection to the database
                Connection connection = DriverManager.getConnection("jdbc:sqlite:filetagsdatabase.db"); Statement statement = connection.createStatement()) {
                statement.executeUpdate("UPDATE tagged_files_info SET file_path = '" + fullFilePath + "' WHERE file_name = '" + fileName + "'");
            }
        } 
        catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
    }
}

