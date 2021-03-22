package com.llw.androidtvdemo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Bili {

    public static String step2(String param) throws IOException {

        String urlPath = new String("http://bilibili.applinzi.com/index.php");

        URL url = new URL(urlPath);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        try {
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");

            httpConn.connect();
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(param);
            dos.flush();
            dos.close();
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(
                        new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                return sb.toString();
            }
        } finally {
            httpConn.disconnect();
        }
        return null;

    }
    static {
        TrustManager[] trustAllCertificates = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null; // Not relevant.
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing. Just allow them all.
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing. Just allow them all.
                    }
                }
        };

        HostnameVerifier trustAllHostnames = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true; // Just allow them all.
            }
        };

        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCertificates, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
        }
        catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    } // static initializer to allow SSL access to URLs that have no valid security certificates



    public static JSONObject getParams(String biliUrl)
            throws MalformedURLException, IOException, ProtocolException, UnsupportedEncodingException {
        String urlPath = biliUrl.replace("bilibili", "ibilibili");
        URL url = new URL(urlPath);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        try {
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("GET");

            httpConn.connect();
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.flush();
            dos.close();
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(
                        new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();

                String body = sb.toString();
                Matcher titleMatches = Pattern.compile("<h4>(.*?)</h4>").matcher(body);
                if(titleMatches.find()){
                    String title = titleMatches.group(1);
                    Pattern pattern = Pattern.compile("data: \\{(.*?)\\}", Pattern.DOTALL | Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(body);
                    if (matcher.find()) {
                        String params = matcher.group(1).replaceAll("\"", "").replaceAll("\\s+", "").replace(":", "=")
                                .replaceAll(",", "&");
                        String json = step2(params);
                        if(json!=null){
                         JSONObject jsonObj =  new JSONObject(json);
                         jsonObj.put("title",title);
                         return  jsonObj;
                        }

                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            httpConn.disconnect();
        }
        return null;
    }

}
