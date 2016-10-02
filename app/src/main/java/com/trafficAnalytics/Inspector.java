package com.trafficAnalytics;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import xyz.hexene.localvpn.Packet;

/**
 * Created by iddy on 2/10/2016.
 */
public class Inspector {
    private static final String TAG = "Inspector";
    private HashSet<InetAddress> destinations;
    private StatLogger logger;

    public Inspector(Collection<InetAddress> destinations, Context context) {
        init(context);
        destinations.addAll(destinations);
    }

    public Inspector(Context context) {
        init(context);
    }

    private void init(Context context) {
        destinations = new HashSet<InetAddress>();
        logger = new StatLogger(context);
    }

    /**
     * Monitor and resolve IP addresses only
     * @param packet
     */
    public void monitorIPAddresses(Packet packet) {

    }

    static public List<TrafficStat> readStatsFromFile(Context context) {
        HashMap<String, Integer[]> host_stats = new HashMap<>();

        /* Populate the host records */
        for (String s : StatLogger.getRecords(context)) {
            s = s.replace("/","");
            StringTokenizer st = new StringTokenizer(s," ");
            // time proto direction src dst p q len
            String time = st.nextToken();
            String proto = st.nextToken();
            String direction = st.nextToken();
            String src_ip= st.nextToken();
            String dst_ip = st.nextToken();
            String src_port = st.nextToken();
            String dst_port = st.nextToken();
            String len = st.nextToken();
            boolean rx = direction.equals("Rx");

            if (rx) {
                if (!host_stats.containsKey(src_ip)) {
                    host_stats.put(src_ip, new Integer[]{0,0,0,0});
                }
                host_stats.get(src_ip)[1] = host_stats.get(src_ip)[1] + 1;
                host_stats.get(src_ip)[3] = host_stats.get(src_ip)[3] + Integer.parseInt(len);
            }
            else {
                if (!host_stats.containsKey(dst_ip)) {
                    host_stats.put(dst_ip, new Integer[]{0,0,0,0});
                }
                host_stats.get(dst_ip)[0] = host_stats.get(dst_ip)[0] + 1;
                host_stats.get(dst_ip)[2] = host_stats.get(dst_ip)[2] + Integer.parseInt(len);
            }
        }

        /* Convert host records into a list */
        List<TrafficStat> rv = new ArrayList<>();
        for (Map.Entry<String,Integer[]> entry: host_stats.entrySet()) {
            TrafficStat ts = new TrafficStat(entry.getKey(), entry.getValue());
            rv.add(ts);
        }
        Collections.sort(rv);
        return rv;
    }

    public void recordPacket(Packet packet, boolean rx) {
//        Log.i(TAG, "PACKET, dst:"+ packet.ip4Header.destinationAddress.getHostAddress());

        // TODO check destination against filter

        if (packet.isTCP()) {
            logger.log(
                    "TCP " + (rx ? "Rx " : "Tx ") +
                            packet.ip4Header.sourceAddress + " " +
                            packet.ip4Header.destinationAddress + " " +
                            packet.tcpHeader.sourcePort + " " +
                            packet.tcpHeader.destinationPort + " " +
                            (packet.ip4Header.totalLength - packet.ip4Header.headerLength)
            );
        }
        else if (packet.isUDP()) {
            logger.log(
                    "UDP " + (rx ? "Rx " : "Tx ") +
                            packet.ip4Header.sourceAddress + " " +
                            packet.ip4Header.destinationAddress + " " +
                            packet.udpHeader.sourcePort + " " +
                            packet.udpHeader.destinationPort + " " +
                            packet.udpHeader.length
            );
        }
        else {
            logger.log(
                    packet.ip4Header.getProtocolNum() + (rx ? "Rx " : "Tx ") +
                            packet.ip4Header.sourceAddress + " " +
                            packet.ip4Header.destinationAddress + " - - " +
                            (packet.ip4Header.totalLength - packet.ip4Header.headerLength)
                    );
        }
    }

    public static class TrafficStat implements Comparable<TrafficStat> {
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

        public String toString() {
            return address
                    +"\n    rx:"+rx+"/"+rx_bytes+"B"
                    +"\n    tx:"+tx+"/"+tx_bytes+"B";
        }

        public Integer[] addrToIntArray(String addr) {
            StringTokenizer st = new StringTokenizer(addr, ".");
            Integer[] iAddr = new Integer[]{
                    Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken())};
            return iAddr;
        }

        @Override
        public int compareTo(TrafficStat o) {
            Integer[] self  = addrToIntArray(address);
            Integer[] other = addrToIntArray(o.address);

            if (self[0].equals(other[0])) {
                if (self[1].equals(other[1])) {
                    if (self[2].equals(other[2])) {
                        return self[3].compareTo(other[3]);
                    }
                    return self[2].compareTo(other[2]);
                }
                return self[1].compareTo(other[1]);
            }
            return self[0].compareTo(other[0]);
        }
    }
}
