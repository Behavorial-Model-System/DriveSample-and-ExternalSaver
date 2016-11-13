package com.wallee3535.drivesample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends Activity {

    private static final String TAG = "walter's tag";
    private EditText saveEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveEditText = (EditText) findViewById(R.id.saveEditText);
    }

    /** Runs when save button is pressed
     * Starts the DriveService which saves the data to a file in Google Drive
     * @param view
     */
    public void onSaveClicked(View view) {
        String data = saveEditText.getText().toString();
        String fileName = "BMS";

        save(data, fileName);
    }

    /** Saves the data string to a file named fileName in Google Drive folder
     * @param data string to append to file
     * @param fileName name of file to append to
     */
    public void save(String data, String fileName){
        Intent mIntent = new Intent(this, DriveService.class);
        mIntent.putExtra("data", data);
        mIntent.putExtra("fileName", fileName);
        startService(mIntent);
    }



}