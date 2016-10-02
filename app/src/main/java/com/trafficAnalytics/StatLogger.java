package com.trafficAnalytics;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by iddy on 2/10/2016.
 * Based on http://stackoverflow.com/a/6209739
 */
public class StatLogger {
    // Using and writing to files -- https://developer.android.com/training/basics/data-storage/files.html
    private static final String filename = "statslog.txt";
    private ConcurrentLinkedQueue<String> outputBuffer;
    private File logfile;
    private String TAG = "StatLogger";
    private final int DURATION = 1000; // time to wait between file writes/emptying queue

    public StatLogger (Context context) {
        // open file to log
        logfile = new File(context.getExternalFilesDir("testing"), filename);
        if (!logfile.exists())
        {
            try {
                Log.i(TAG, "Creating file "+filename);
                logfile.createNewFile();
            }
            catch (IOException e)
            { e.printStackTrace(); }
        }
        outputBuffer = new ConcurrentLinkedQueue<>();

        startWriterThread();
    }

    /**
     * Start the timer which executes writing the stats queue to file.
     * Instead of writing to file on main thread or for every packet.
     */
    private void startWriterThread() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    /**
                     * Write the statistics info buffer to file
                     */
                    public void run() {
                        try
                        {
                            //BufferedWriter for performance, true to set append to file flag
                            BufferedWriter buf = new BufferedWriter(new FileWriter(logfile, true));
                            while (!outputBuffer.isEmpty()) {
                                buf.append(outputBuffer.poll());
                                buf.newLine();
                            }
                            buf.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, DURATION);
    }

    public void log(String text) {
//        Log.i(TAG, "Adding to queue \""+text+"\"");
        outputBuffer.add(System.currentTimeMillis()+" "+text);
    }

    public static Queue<String> getRecords(Context context) {
        Queue<String> lines = new LinkedList<>();
        File logfile = new File(context.getExternalFilesDir("testing"), filename);
        if (logfile.exists()) {
            try {

                BufferedReader buf = new BufferedReader(new FileReader(logfile));
                while(buf.ready())
                    lines.add(buf.readLine());
                buf.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }
}
