package xyz.hexene.localvpn;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by iddy on 4/10/2016.
 */
public class LocalIPAddress {
    private static final String TAG = "GETIP";

    public static String get() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                if (intf.isUp()) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ip = inetAddress.getHostAddress();
//                            Log.i(TAG, "***** IP="+ ip);
                            if (ip.contains(".") && !ip.equals("0.0.0.0")) {
                                return ip;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
            ex.printStackTrace();
        }
        return null;
    }
}
