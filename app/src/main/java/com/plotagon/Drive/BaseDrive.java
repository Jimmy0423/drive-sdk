package com.plotagon.Drive;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.plotagon.Drive.calls.CreateGoogleDriveFileCommand;
import com.plotagon.Drive.calls.ListGoogleDriveFilesCommand;
import com.plotagon.Drive.calls.OpenGoogleDriveFileCommand;
import com.plotagon.Drive.mock.MockPlot;
import com.plotagon.Drive.model.Plot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;

public class BaseDrive extends AppCompatActivity implements OnConnectionFailedListener, ConnectionCallbacks{

    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static String TAG1 = "GOOGLE-DRIVE";
    private GoogleApiClient mGoogleApiClient;
    private TextView plotText;
    private Button pushDrive;
    private Button connectApi;
    private Button fetch;
    private OnFragmentInteractionListener listener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_drive);

        connectApi = (Button) findViewById(R.id.button_connect);
        pushDrive = (Button) findViewById(R.id.button_push_drive);
        fetch = (Button) findViewById(R.id.button_sync);
        plotText = (TextView) findViewById(R.id.view_plot);


        if (mGoogleApiClient == null) {
            pushDrive.setEnabled(false);
            fetch.setEnabled(false);
        }

        pushDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   /*MockPlot plot = new MockPlot();
                    String manuscript = plot.getManuscript();
                    String plotId;
                try {
                    JSONObject jsonObject = new JSONObject(manuscript);
                    plotId  = jsonObject.getString("id");
                    Log.i("CREATE", plotId );

                } catch (JSONException e) {
                    e.printStackTrace();
                    plotId = "-1";
                }

                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(plotId+".plotdoc", BaseDrive.this.MODE_PRIVATE));
                    outputStreamWriter.write(manuscript);
                    outputStreamWriter.close();
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
*/
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                File[] external_AND_removable_storage_m1_Args = getExternalFilesDirs(null);
                        Log.d("PATHG", path);
                        Log.d("test", external_AND_removable_storage_m1_Args.toString());
                File f = new File(currDir);
                File[] files = f.listFiles();
                if (files != null) {
                    if (files.length > 0) {
                        rootDir.clear();
                        for (File inFile : files) {
                            if (inFile.isDirectory()) { //return true if it's directory
                                // is directory
                                DirectoryModel dir = new DirectoryModel();
                                dir.setDirName(inFile.toString().replace("/storage/emulated/0", ""));
                                dir.setDirType(0); // set 0 for directory
                                rootDir.add(dir);
                            } else if (inFile.isFile()) { // return true if it's file
                                //is file
                                DirectoryModel dir = new DirectoryModel();
                                dir.setDirName(inFile.toString().replace("/storage/emulated/0", ""));
                                dir.setDirType(1); // set 1 for file
                                rootDir.add(dir);
                            }
                        }
                    }
                    printDirectoryList();
                }

                //CreateGoogleDriveFileCommand create = new CreateGoogleDriveFileCommand(mGoogleApiClient,  );
                //Log.i("OnCreate", create.toString());
            }
        });

        connectApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG1, "Trying to connect");

                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(BaseDrive.this)
                            .addApi(Drive.API)
                            .addScope(Drive.SCOPE_FILE)
                            .addScope(Drive.SCOPE_APPFOLDER)
                            .addConnectionCallbacks(BaseDrive.this)
                            .addOnConnectionFailedListener(BaseDrive.this)
                            .build();
                }
                mGoogleApiClient.connect();
                Log.d(TAG1, "Should be connected");
            }
        });

        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchPlots();
            }
        });
    }

    public void setListener(OnFragmentInteractionListener listener){
        this.listener = listener;
    }

    public void fetchPlots() {
        ListGoogleDriveFilesCommand googleFiles = new ListGoogleDriveFilesCommand(mGoogleApiClient);

        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<List<Plot>> future = service.submit(googleFiles);

        List<Plot> plots = null;
        try {
            plots = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        listener.onFragmentInteraction(plots);
    }


    public void openPlot( DriveId driveId) {

            ImageView image = (ImageView) findViewById(R.id.img_google);
            OpenGoogleDriveFileCommand openFile = new OpenGoogleDriveFileCommand(mGoogleApiClient, driveId);
            ExecutorService service = Executors.newSingleThreadExecutor();
            Future<JSONObject> future = service.submit(openFile);

            JSONObject plot = null;
            try {
                plot = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            image.setVisibility(View.INVISIBLE);
            plotText.setText(plot.toString());


    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.i(TAG1, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG1, "Exception while starting resolution activity", e);
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        pushDrive.setEnabled(true);
        fetch.setEnabled(true);
    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG1, "GoogleApiClient connection suspended");
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d(TAG1, requestCode + " Activity Result " + requestCode + " " + data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();

    }

    interface OnFragmentInteractionListener {
        public void onFragmentInteraction(List<Plot> plots);
    }
}
