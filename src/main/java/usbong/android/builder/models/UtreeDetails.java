package usbong.android.builder.models;

import android.provider.BaseColumns;
import com.activeandroid.Model;

import java.io.Serializable;

/**
 * Created by talusan on 7/26/2015.
 */
public class UtreeDetails extends Model implements BaseColumns, Serializable {
    private String FILENAME = "";
    private String FILEPATH = "";
    private String UPLOADER = "";
    private String DESCRIPTION = "";
    private String YOUTUBELINK = "";
    private String YOUTUBELINK2 = "";
    private String SCREENSHOT = "";
    private String SCREENSHOT3 = "";
    private String SCREENSHOT4 = "";
    private String DATEUPLOADED = "";

    public UtreeDetails(String UPLOADER,
                        String FILEPATH,
                        String FILENAME,
                        String DESCRIPTION,
                        String YOUTUBELINK,
                        String YOUTUBELINK2,
                        String SCREENSHOT,
                        String SCREENSHOT4,
                        String SCREENSHOT3,
                        String DATEUPLOADED) {
        this.DESCRIPTION = DESCRIPTION;
        this.UPLOADER = UPLOADER;
        this.FILEPATH = FILEPATH;
        this.FILENAME = FILENAME;
        this.YOUTUBELINK = YOUTUBELINK;
        this.YOUTUBELINK2 = YOUTUBELINK2;
        this.SCREENSHOT = SCREENSHOT;
        this.SCREENSHOT4 = SCREENSHOT4;
        this.SCREENSHOT3 = SCREENSHOT3;
        this.DATEUPLOADED = DATEUPLOADED;
    }

    public String getFILENAME() {
        return FILENAME;
    }

    public String getFILEPATH() {
        return FILEPATH;
    }

    public String getUPLOADER() {
        return UPLOADER;
    }

    public String getDESCRIPTION() {
        return DESCRIPTION;
    }

    public String getYOUTUBELINK() {
        return YOUTUBELINK;
    }

    public String getYOUTUBELINK2() {
        return YOUTUBELINK2;
    }

    public String getSCREENSHOT() {
        return SCREENSHOT;
    }

    public String getSCREENSHOT3() {
        return SCREENSHOT3;
    }

    public String getSCREENSHOT4() {
        return SCREENSHOT4;
    }

    public String getDATEUPLOADED() {
        return DATEUPLOADED;
    }

    @Override
    public String toString() {
        return "UtreeDetails{" +
                "FILENAME='" + FILENAME + '\'' +
                ", FILEPATH='" + FILEPATH + '\'' +
                ", UPLOADER='" + UPLOADER + '\'' +
                ", DESCRIPTION='" + DESCRIPTION + '\'' +
                ", YOUTUBELINK='" + YOUTUBELINK + '\'' +
                ", YOUTUBELINK2='" + YOUTUBELINK2 + '\'' +
                ", SCREENSHOT='" + SCREENSHOT + '\'' +
                ", SCREENSHOT3='" + SCREENSHOT3 + '\'' +
                ", SCREENSHOT4='" + SCREENSHOT4 + '\'' +
                ", DATEUPLOADED='" + DATEUPLOADED + '\'' +
                '}';
    }
}