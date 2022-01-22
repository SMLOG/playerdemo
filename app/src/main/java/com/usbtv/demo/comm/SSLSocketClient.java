package com.usbtv.demo.comm;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLSocketClient {

    //获取这个SSLSocketFactory
    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //获取TrustManager
    private static TrustManager[] getTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
        return trustAllCerts;
    }
    
  //获取HostnameVerifier
    public static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
        return hostnameVerifier;
    }

    public static class ServerManager {

        private Server mServer;
        public static final int port=9090;


        public static String getServerHttpAddress(){
            return "http://127.0.0.1:"+port;
        }
        /**
         * Create server.
         */
        public ServerManager(Context context) {
            mServer = AndServer.webServer(context)
                    .port(port)
                    .timeout(10, TimeUnit.SECONDS)
                    .listener(new Server.ServerListener() {
                        @Override
                        public void onStarted() {
                            // TODO The server started successfully.
                        }

                        @Override
                        public void onStopped() {
                            // TODO The server has stopped.
                        }

                        @Override
                        public void onException(Exception e) {
                            // TODO An exception occurred while the server was starting.
                        }
                    })
                    .build();
        }

        /**
         * Start server.
         */
        public void startServer() {
            if (mServer.isRunning()) {
                // TODO The server is already up.
            } else {
                mServer.startup();
            }
        }

        /**
         * Stop server.
         */
        public void stopServer() {
            if (mServer.isRunning()) {
                mServer.shutdown();
            } else {
                Log.w("AndServer", "The server has not started yet.");
            }
        }
    }

    /**
     * Created by Mr.T on 2018/4/25.
     */

    public static class SoundUtil {

        public static void playClickSound(View view) {
            if (view.isSoundEffectsEnabled()) {
                AudioManager manager = (AudioManager) view.getContext().getSystemService(Context.AUDIO_SERVICE);
                manager.playSoundEffect(AudioManager.FX_KEY_CLICK);
            }
        }
    }
}