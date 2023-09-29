
package filemanager.table.displaydata;

import javax.swing.Icon;

public class DisplayTagInfo_TaggedFilesPane {
    //byte icon;
    String file_name, file_path, file_type, tags;

    public DisplayTagInfo_TaggedFilesPane(String file_name, String file_path, String file_type, String tags) {
        //this.icon = icon;
        this.file_name = file_name;
        this.file_path = file_path;
        this.file_type = file_type;
        this.tags = tags;
    }
    
    /*public byte getIcon() {
        return icon;
    }

    public void setIcon(byte icon) {
        this.icon = icon;
    }*/

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    
}
