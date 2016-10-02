package com.trafficAnalytics;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import xyz.hexene.localvpn.Packet;

/**
 * Created by iddy on 2/10/2016.
 */
public class Inspector {
    private final String TAG = "Inspector";
    private HashSet<InetAddress> destinations;
    private HashMap<InetAddress, Integer[]> seen;
    private final int MAX_PKT_SIZE = 65535;

    public Inspector(Collection<InetAddress> destinations) {
        init();
        destinations.addAll(destinations);
    }

    public Inspector() {
        init();
    }

    private void init() {
        destinations = new HashSet<InetAddress>();
        seen = new HashMap<InetAddress, Integer[]>();
    }

    /**
     * Monitor and resolve IP addresses only
     * @param packet
     */
    public void monitorIPAddresses(Packet packet) {

    }

    public List<TrafficStat> getStats(){
        List<TrafficStat> rv = new ArrayList<>();

        for (Map.Entry<InetAddress,Integer[]> e: seen.entrySet()) {
            TrafficStat ts = new TrafficStat(e.getKey().getHostAddress(), e.getValue());
            rv.add(ts);
        }
        return rv;
    }

    public void doAnalysis(Packet packet, boolean rx) {
//        Log.i(TAG, "TCP PACKET, dst:"+ packet.ip4Header.destinationAddress.getHostAddress());

        // check destination against filter

        if (packet.isTCP()) {
            Log.d(TAG,
                    "TCP " + (rx ? "Rx " : "Tx ") +
                            packet.ip4Header.sourceAddress + " " +
                            packet.ip4Header.destinationAddress + " " +
                            packet.tcpHeader.sourcePort + " " +
                            packet.tcpHeader.destinationPort + " " +
                            packet.tcpHeader.sequenceNumber + " " +
                            (packet.ip4Header.totalLength - packet.ip4Header.headerLength)
            );
        }
        else if (packet.isUDP()) {
            Log.d(TAG,
                    "UDP " + (rx ? "Rx " : "Tx ") +
                            packet.ip4Header.sourceAddress + " " +
                            packet.ip4Header.destinationAddress + " " +
                            packet.udpHeader.sourcePort + " " +
                            packet.udpHeader.destinationPort + " " +
                            packet.udpHeader.length
            );
        }
        else {
            Log.d(TAG,
                    "Other " + (rx ? "Rx " : "Tx ") +
                            packet.ip4Header.sourceAddress + " " +
                            packet.ip4Header.destinationAddress + " " +
                            packet.ip4Header.getProtocolNum()
                    );
        }

        if (rx) {
            InetAddress src = packet.ip4Header.sourceAddress;
            if (!seen.containsKey(src)) {
                Log.i(TAG, "New source: "+src.getHostAddress());
                seen.put(src, new Integer[]{0,0,0,0});
//            seen.put(dest, new Integer[MAX_PKT_SIZE]);
            }
            seen.get(src)[1] = seen.get(src)[1] + 1;
            seen.get(src)[3] = seen.get(src)[3] + packet.ip4Header.totalLength;
        }
        else {
            InetAddress dest = packet.ip4Header.destinationAddress;
            if (!seen.containsKey(dest)) {
                Log.i(TAG, "New destination: "+dest.getHostAddress());
                seen.put(dest, new Integer[]{0,0,0,0});
            }
            seen.get(dest)[0] = seen.get(dest)[0] + 1;
            seen.get(dest)[2] = seen.get(dest)[2] + packet.ip4Header.totalLength;
        }

    }

    public class TrafficStat {
        public String address;
        public int tx;
        public int tx_bytes;
        public int rx;
        public int rx_bytes;

        public TrafficStat(String addr, Integer[] counts) {
            this.address = addr;
            this.tx = counts[0];
            this.rx = counts[1];
            this.tx_bytes = counts[2];
            this.rx_bytes = counts[3];
        }

        public String getDomainName() {
            HostRequest hs = new HostRequest();
            hs.execute(this.address);
            try {
                return hs.get();
            } catch (Exception e) {
                Log.e(TAG, "Failed to get address");
            }
            return address;
        }

        public String toString() {
            return address
                    +"\n    rx:"+rx+"/"+rx_bytes+"B"
                    +"\n    tx:"+tx+"/"+tx_bytes+"B";
        }

    }
}
