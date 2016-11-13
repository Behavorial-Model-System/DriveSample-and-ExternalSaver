package com.wallee3535.drivesample;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Walter on 10/3/2016.
 * "Externally" saves some text in /storage/emulated/0/aaTutorial/savedFile.txt
 * The saved file will be on the phone and accessible to other apps
 */

public class ExternalSaver {
    //location of folder to create and store data in
    public static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaTutorial";


    /** appends text to a textfile, creates the textfile if it doesnt exist
     *
     * @param text string to write to file
     * @param fileName name of textfile to create or append to
     */
    public static void save(String text, String fileName) {
        //check the directory has been created
        File dir = new File(path);
        if(!dir.exists()){
            boolean success = dir.mkdir();
            if(! success){
                Log.e("walter's tag", "In Saver.java, setup() failed to create directory");
                return;
            }
        }

        //save the text file in the directory
        File file = new File(path + "/" + fileName);
        Save(file, text);
    }

    /** appends text to a default text file, called savedFile.txt
     *
     * @param text string to write to file
     */
    public static void save(String text) {
        File file = new File(path + "/savedFile.txt");

        Save(file, text);
    }

    //helper for saving
    private static void Save(File file, String data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            try {
                fos.write(data.getBytes());
                //fos.write("\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
