package com.plotagon.Drive.model;

import android.media.Image;

import com.google.android.gms.drive.DriveId;

/**
 * Created by mitra on 2017-03-01.
 */

public class Plot {
    private String id;
    private String image;
    private String name;
    private DriveId driveId;
    private boolean isLocal;

    public Plot(String id, String image, String name, DriveId driveId, boolean isLocal) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.driveId = driveId;
        this.isLocal = isLocal;
    }

    public DriveId getDriveId(){
        return driveId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getThumb() {
        return image;
    }

    public boolean isLocal () {
        return isLocal;
    }

    @Override
    public String toString() {
        return "Plot:" +
                "  name='" + name + '\'' +
                ", created='" + image + '\'';
    }
}
