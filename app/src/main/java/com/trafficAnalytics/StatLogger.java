package com.trafficAnalytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by iddy on 2/10/2016.
 * Based on http://stackoverflow.com/a/6209739
 */
public class StatLogger {
    private final String filename = "sdcard/log.txt";
    private File logfile;
    private BufferedWriter buf;

    public StatLogger () {
        // open file to log
        logfile = new File(filename);
        if (!logfile.exists())
        {
            try {
                logfile.createNewFile();
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logfile, true));
            }
            catch (IOException e)
            { e.printStackTrace(); }

        }

    }

    public void log(String text) {
        try
        {
            buf.append(System.currentTimeMillis()+": "+text);
            buf.newLine();
        }
        catch (IOException e)
        { e.printStackTrace(); }
    }

    public void close() {
        try { buf.close(); }
        catch (IOException e)
        { e.printStackTrace(); }
    }
}
