/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filemanager.tablecell.tagtable;

import filemanager.tablecell.filetable.*;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Kanak RT
 */
public class Tag_TableActionCellRender extends DefaultTableCellRenderer{

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component com =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        PanelAction action = new PanelAction();
        
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
