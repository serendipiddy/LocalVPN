package com.trafficAnalytics;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import xyz.hexene.localvpn.R;

public class ConnectionTrafficGraph extends AppCompatActivity {

    /*
    Based on quickstart guide: http://androidplot.com/docs/quickstart/
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_traffic_graph);
    }
}