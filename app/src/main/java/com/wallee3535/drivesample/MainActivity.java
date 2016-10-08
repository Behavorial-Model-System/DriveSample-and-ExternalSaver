package com.wallee3535.drivesample;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static android.R.attr.id;

/*downloads a text file's contents, reads it and displays
the contents in a new activity*/
public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private DriveId driveID;
    private static final String fileName = "BMS";
    private static final int REQUEST_CODE = 102;
    private GoogleApiClient googleApiClient;
    private static final String TAG = "walter's tag";
    private static String textToSave;
    private EditText saveEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*build the api client*/
        buildGoogleApiClient();
        saveEditText = (EditText) findViewById(R.id.saveEditText);
    }

    /*connect client to Google Play Services*/
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "In onStart()");
    }

    /*close connection to Google Play Services*/
    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            Log.i(TAG, "In onStop()");
        }
    }

    /*handles onConnectionFailed callbacks*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Log.i(TAG, "In onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Log.i(TAG, "In onActivityResult() - connecting...");
            googleApiClient.connect();
        }
    }

    /*handles connection callbacks*/
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "In onConnected()");
        //query the root folder in drive to see if save file has been created already
        query();

    }

    /*handles suspended connection callbacks*/
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "In onConnectionSuspended()");
    }
    /*callback on getting the drive id, contained in result*/

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed");
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {

            Log.i(TAG, "trying to resolve the Connection failed error...");
            result.startResolutionForResult(this, REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /*build the google api client*/
    private void buildGoogleApiClient() {
        Log.i(TAG, "In buildGoogleApiClient()");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    /**
     * Runs when the file is successfully opened
     * Appends the textToSave string to the file
     */
    final private ResultCallback<DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveContentsResult>() {
                @Override
                public void onResult(DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("file cant be opened");
                        return;
                    }
                    // DriveContents object contains pointers
                    // to the actual byte stream
                    try {
                        DriveContents contents = result.getDriveContents();
//                        OutputStream outputStream = contents.getOutputStream();
//                        outputStream.write("Hello world kjlagegkjregbkjlr".getBytes());
                        ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor
                                .getFileDescriptor());
                        // Read to the end of the file.
                        fileInputStream.read(new byte[fileInputStream.available()]);

                        // Append to the file.
                        FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                                .getFileDescriptor());
                        Writer writer = new OutputStreamWriter(fileOutputStream);
                        writer.write(textToSave);
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


    /**
     * Runs when a content is created successfully
     * Creates a new file
     */
    final private ResultCallback<DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveContentsResult>() {
                @Override
                public void onResult(DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    // Perform I/O off the UI thread.
//                    new Thread() {
//                        @Override
//                        public void run() {
                    // write content to DriveContents
                    OutputStream outputStream = driveContents.getOutputStream();
                    Writer writer = new OutputStreamWriter(outputStream);
                    try {
                        writer.write("Text file created.");
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
//                    }.start();
//                }
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

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.i(TAG, message);
    }

    /** Runs when save button is pressed
     * Saves the string in the EditText
     * @param view
     */
    public void save(View view) {
        save(saveEditText.getText().toString());
    }

    /** Saves the string in the EditText
     * @param text text to save
     */
    public void save(String text) {
        Log.i(TAG, "In save()");
        //if we dont have a client, build it
        textToSave = text;
        if (googleApiClient == null) {
            buildGoogleApiClient();
        }
        googleApiClient.reconnect();
        //if successful goes to onConnected()

    }

    /** Checks if the save file has been created yet and creates it if not.
     *  Then it calls write()
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
                    Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(driveContentsCallback);
                } else if (count == 1) {
                    driveID = metadataBufferResult.getMetadataBuffer().get(0).getDriveId();
                    write();
                } else {
                    showMessage("found too many matching filenames, dont know which one to save to");
                }
                metadataBufferResult.release();
            }
        });
    }

    /** Writes to the file
     *
     */
    public void write() {
        Log.i(TAG, "In write()");
        //Log.i(TAG, "driveID: " + driveID);
        DriveFile file = driveID.asDriveFile();
        file.open(googleApiClient, DriveFile.MODE_READ_WRITE, null).setResultCallback(contentsOpenedCallback);
    }
}