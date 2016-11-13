package com.wallee3535.drivesample;

import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by Walter on 11/12/2016.
 */

public class DriveService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "walter's tag";
    private GoogleApiClient googleApiClient;
    private String fileName;
    private String data;
    private DriveId driveID;

    public DriveService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showMessage("DriveService onStartCommand");

        data = intent.getStringExtra("data");
        fileName = intent.getStringExtra("fileName");

        buildGoogleApiClient();
        googleApiClient.connect();

        return super.onStartCommand(intent, flags, startId);
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.i(TAG, message);
    }

    private void buildGoogleApiClient() {
        Log.i(TAG, "In buildGoogleApiClient()");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /*handles connection callbacks*/
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "In onConnected()");
        //query the root folder in drive to see if save file has been created already
        new Thread() {
            @Override
            public void run() {
                query();
            }
        }.start();

    }

    /*handles suspended connection callbacks*/
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "In onConnectionSuspended()");
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "In onConnectionFailed()");
        Intent i = new Intent(this, ResolverActivity.class);
        i.putExtra(ResolverActivity.CONNECT_RESULT_KEY, result);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    /**
     * Checks if the save file has been created yet and creates it if not.
     * Then it calls write()
     */
    public void query() {
        Log.i(TAG, "In query()");
        Query query = new Query.Builder().addFilter(
                Filters.and(
                        Filters.eq(SearchableField.TITLE, fileName),
                        Filters.eq(SearchableField.TRASHED, false))
        ).build();
        DriveFolder root = Drive.DriveApi.getRootFolder(googleApiClient);
        root.queryChildren(googleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                int count = metadataBufferResult.getMetadataBuffer().getCount();
                showMessage("found " + count + " file(s) matching the name of the save file");
                if (count == 0) {
                    //if 0 matching filenames were found, create a new file
                    Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(driveContentsCallback);
                } else if (count == 1) {
                    //if 1 matching filename was found, write to that file
                    driveID = metadataBufferResult.getMetadataBuffer().get(0).getDriveId();
                    write();
                } else {
                    showMessage("found too many matching filenames, dont know which one to save to");
                }
                metadataBufferResult.release();
            }
        });
    }

    /**
     * Writes to the file
     */
    public void write() {
        Log.i(TAG, "In write()");
        //Log.i(TAG, "driveID: " + driveID);
        DriveFile file = driveID.asDriveFile();
        file.open(googleApiClient, DriveFile.MODE_READ_WRITE, null).setResultCallback(contentsOpenedCallback);
    }

    /**
     * Runs when a content is created successfully
     * Creates a new file
     */
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    // Perform I/O off the UI thread.
                    new Thread() {
                        @Override
                        public void run() {
                            // write content to DriveContents
                            OutputStream outputStream = driveContents.getOutputStream();
                            Writer writer = new OutputStreamWriter(outputStream);
                            try {
                                writer.write("Text file created.\n");
                                writer.close();
                                outputStream.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle(fileName)
                                    .setMimeType("text/plain")
                                    .setStarred(true).build();

                            // create a file on root folder
                            Drive.DriveApi.getRootFolder(googleApiClient)
                                    .createFile(googleApiClient, changeSet, driveContents)
                                    .setResultCallback(fileCallback);
                        }
                    }.start();
                }
            };

    /**
     * Runs when file is created successfully
     * Saves the file's drive id to driveID
     */
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
                    driveID = result.getDriveFile().getDriveId();
                    write();
                }
            };

    /**
     * Runs when the file is successfully opened
     * Appends the textToSave string to the file
     */
    final private ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("file cant be opened");
                        return;
                    }
                    // DriveContents object contains pointers
                    // to the actual byte stream
                    try {
                        DriveContents contents = result.getDriveContents();
                        ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor
                                .getFileDescriptor());
                        // Read to the end of the file.
                        fileInputStream.read(new byte[fileInputStream.available()]);

                        // Append to the file.
                        FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                                .getFileDescriptor());
                        Writer writer = new OutputStreamWriter(fileOutputStream);
                        writer.write(data);
                        writer.flush();
                        writer.close();
                        fileOutputStream.close();
                        fileInputStream.close();
                        contents.commit(googleApiClient, null);
                        showMessage("write successful");

                    } catch (IOException e) {
                        showMessage("IOException while appending to the output stream" + e);
                    }
                }
            };
}
