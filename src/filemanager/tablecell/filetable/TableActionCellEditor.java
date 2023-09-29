/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filemanager.tablecell.filetable;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;

/**
 *
 * @author Kanak RT
 */
public class TableActionCellEditor extends DefaultCellEditor{

    private FileTableActionEvent event;
    public TableActionCellEditor(FileTableActionEvent event) {
        super(new JCheckBox());
        this.event = event;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        PanelAction action = new PanelAction();
        action.initEvent(event, row);
        
        
        
        if (isSelected) {
            action.setBackground(table.getSelectionBackground());
        }
        else {
            action.setBackground(table.getBackground());
        }
        /*if (isSelected) {
            action.setBackground(table.getSelectionBackground());
            action.setBackground(table.getSelectionForeground());
        } else {
            action.setBackground(table.getBackground());
            action.setForeground(table.getForeground());
        }*/
        return action;
    } 
}
