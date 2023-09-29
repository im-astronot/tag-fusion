
package filemanager.table.displaydata;

public class DisplayTagInfo_FileInfoPane {
    
    String file_name, Tags;
    
    public DisplayTagInfo_FileInfoPane(String Tags) {
        this.Tags = Tags;
    }

    /*public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }*/

    public String getTags() {
        return Tags;
    }

    public void setTags(String Tags) {
        this.Tags = Tags;
    }    
}
