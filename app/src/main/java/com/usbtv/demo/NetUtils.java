package com.usbtv.demo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetUtils {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(" +

            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +

            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");


    private static boolean isIPv4Address(String input) {

        return IPV4_PATTERN.matcher(input).matches();

    }

    //获取本机IP地址

    public static InetAddress getLocalIPAddress() {

        Enumeration<NetworkInterface> enumeration = null;

        try {

            enumeration = NetworkInterface.getNetworkInterfaces();

        } catch (SocketException e) {

            e.printStackTrace();

        }

        if (enumeration != null) {

            while (enumeration.hasMoreElements()) {

                NetworkInterface nif = enumeration.nextElement();

                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();

                if (inetAddresses != null)

                    while (inetAddresses.hasMoreElements()) {

                        InetAddress inetAddress = inetAddresses.nextElement();

                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {

                            return inetAddress;

                        }

                    }
            }

        }

        return null;

    }

    private static final int NETWORK_NONE=-1; //无网络连接
    private static final int NETWORK_WIFI=0; //wifi
    private static final int NETWORK_MOBILE=1; //数据网络

    public static int getNetWorkState(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo!=null&&activeNetworkInfo.isConnected()){
            if(activeNetworkInfo.getType()==(ConnectivityManager.TYPE_WIFI)){
                return NETWORK_WIFI;
            }else if(activeNetworkInfo.getType()==(ConnectivityManager.TYPE_MOBILE)){
                return NETWORK_MOBILE;
            }
        }else{
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }
}