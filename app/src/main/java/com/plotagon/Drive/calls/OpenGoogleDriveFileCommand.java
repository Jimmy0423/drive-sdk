package com.plotagon.Drive.calls;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Callable;

public class OpenGoogleDriveFileCommand implements Callable<JSONObject> {

    private final static String TAG = OpenGoogleDriveFileCommand.class.getSimpleName();
    private final GoogleApiClient client;
    private final DriveId driveId;

    public OpenGoogleDriveFileCommand(GoogleApiClient client, DriveId driveId) {
        this.client = client;
        this.driveId = driveId;
    }


    @Override
    public JSONObject call() throws Exception {

        DriveFile file = driveId.asDriveFile();
        DriveContentsResult result = file.open(client, DriveFile.MODE_READ_ONLY, null).await();
        DriveContents contents = result.getDriveContents();
        BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        String contentsAsString = builder.toString();
        reader.close();
        JSONObject json = new JSONObject(contentsAsString);
        return json;
    }


}