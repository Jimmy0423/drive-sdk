package com.plotagon.Drive.calls;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResults;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.DriveEvent;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.plotagon.Drive.mock.MockPlot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.Callable;

public class CreateGoogleDriveFileCommand  {

    private final static String TAG = CreateGoogleDriveFileCommand.class.getSimpleName();
    DriveFileResult fileResult;
    private final GoogleApiClient client;
    private static String plotId;

    public CreateGoogleDriveFileCommand(GoogleApiClient client, File file) {
        this.client = client;
        Drive.DriveApi.newDriveContents(client).setResultCallback(driveContentsCallback);
    }

    final private ResultCallback<DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveContentsResult>(){
                @Override
                public void onResult(@NonNull DriveContentsResult driveContentsResult) {
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        Log.d(TAG , "Fail creating file");
                    }

                    final DriveContents driveContents = driveContentsResult.getDriveContents();

                    MockPlot plot = new MockPlot();
                    String manuscript = plot.getManuscript2();

                    Writer writer = new OutputStreamWriter(driveContentsResult.getDriveContents().getOutputStream());
                    CustomPropertyKey plotNameProperty = new CustomPropertyKey("plot-name", CustomPropertyKey.PUBLIC);
                    CustomPropertyKey createdDateProperty = new CustomPropertyKey("created-date", CustomPropertyKey.PUBLIC);

                    try {
                        writer.write(manuscript);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String plotName;
                    String plotCreatedDate;
                    try {
                        JSONObject jsonObject = new JSONObject(manuscript);
                        plotId  = jsonObject.getString("id");
                        plotName = jsonObject.getString("name");
                        plotCreatedDate = jsonObject.getString("dateCreated");
                        Log.i("CREATE", plotId );

                    } catch (JSONException e) {
                        e.printStackTrace();
                        plotId = "-1";
                        plotName = "Not set";
                        plotCreatedDate = "0000";
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(plotId + ".plotdoc")
                            .setCustomProperty(plotNameProperty, plotName)
                            .setCustomProperty(createdDateProperty, plotCreatedDate)
                            .setMimeType("text/plain")
                            .build();

                    Log.d("PROPS", changeSet.getCustomPropertyChangeMap().get(plotNameProperty));
                    Drive.DriveApi.getRootFolder(client)
                            .createFile(client, changeSet, driveContents).setResultCallback(fileCallback);
                }
            };

    final private ResultCallback<DriveFileResult> fileCallback = new
            ResultCallback<DriveFileResult>() {
                @Override
                public void onResult(DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.d(TAG, "Creating sub-folder failed");
                    }
                        Log.i("DRIVE-ID", result.getDriveFile().getDriveId().toString());
                       // createSubFolder(result, plotId);
                }
            };

    private void createSubFolder (DriveFileResult result, String plotId) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(plotId).build();
        Drive.DriveApi.getRootFolder(client).createFolder(client, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolderResult folderResult) {
                if (!folderResult.getStatus().isSuccess()) {
                    Log.d("FOLDER", "failed");
                }
            }
        });
    }



}
