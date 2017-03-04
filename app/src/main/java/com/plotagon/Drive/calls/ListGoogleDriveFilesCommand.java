package com.plotagon.Drive.calls;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.plotagon.Drive.model.Plot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class ListGoogleDriveFilesCommand implements Callable<List<Plot>> {

    private final GoogleApiClient client;
    private CustomPropertyKey keyName = new CustomPropertyKey("plot-name", CustomPropertyKey.PUBLIC);
    private CustomPropertyKey keyCreatedDate= new CustomPropertyKey("created-date", CustomPropertyKey.PUBLIC);

    public ListGoogleDriveFilesCommand(GoogleApiClient client) {
        this.client = client;
    }
    @Override
    public List<Plot> call() throws Exception{

        final DriveFolder folder = Drive.DriveApi.getRootFolder(client);

        MetadataBuffer buffer = folder.listChildren(client).await().getMetadataBuffer();
        List<Plot>plotList = new ArrayList<>();

        for (Metadata metadata : buffer) {
            if (!metadata.isTrashed() && !metadata.isInAppFolder()) {
                Log.d("METADATA", metadata.toString());
                String title = metadata.getTitle();
                DriveId driveId = metadata.getDriveId();
                String name  = metadata.getCustomProperties().get(keyName);
                String date = metadata.getCustomProperties().get(keyCreatedDate);
                Plot plot = new Plot(title,date, name, driveId, false);
                plotList.add(plot);
            }
        }

        buffer.release();
        return plotList;
        }
    }

