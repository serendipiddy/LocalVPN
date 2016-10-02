package com.trafficAnalytics;

import android.os.AsyncTask;
import android.util.Log;

import java.net.InetAddress;

/**
 * Created by iddy on 2/10/2016.
 */
public class HostRequest extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        String ipaddr = params[0];
        try {
            return InetAddress.getByName(ipaddr).getHostAddress();
        }catch (Exception e) {
            Log.d("Request_doInBG", "Error: (" + e.getClass() + ") -- " + e.getMessage());
        }
        return ipaddr;
    }
}
