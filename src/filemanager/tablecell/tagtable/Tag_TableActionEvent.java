/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filemanager.tablecell.tagtable;

import filemanager.tablecell.filetable.*;

/**
 *
 * @author Kanak RT
 */
public interface Tag_TableActionEvent {
    
    public void redTag(int row);
    
    public void blueTag(int row);
    
    public void greenTag(int row);
    
    public void aruzeTag(int row);
    
    public void orangeTag(int row);
    
    public void yellowTag(int row);
    
    public void deleteTag(int row);
}
