package com.usbtv.demo.comm;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.storage.StorageManager;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONArray;
import com.usbtv.demo.data.Drive;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Utils {

    private static BTree<String, com.alibaba.fastjson.JSONObject> bTree;

    public static String getIPAddress() {

        ConnectivityManager connectivityManager = (ConnectivityManager) App.getInstance().getSystemService(CONNECTIVITY_SERVICE);//获取系统的连接服务

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if ((info.getType() == ConnectivityManager.TYPE_MOBILE) || (info.getType() == ConnectivityManager.TYPE_WIFI)) {//当前使用2G/3G/4G网络
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        } else { //当前无网络连接,请在设置中打开网络
            return null;
        }
        return null;
    }

    public static List<String> getStoragePath(Context mContext, boolean is_removale) {

        ArrayList<String> ret = new ArrayList<>();
        String path = "";
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);

            for (int i = 0; i < Array.getLength(result); i++) {
                Object storageVolumeElement = Array.get(result, i);
                path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    ret.add(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap createVideoThumbnail(String filePath, int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            // Assume this is a corrupt video file
        } catch (Throwable ex) {
            ex.printStackTrace();
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (Throwable ex) {
                // Ignore failures while cleaning up.
            }
        }
        return bitmap;
    }

    public static void saveBitmapToJPG(Bitmap bitmap, File file) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(file);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }
    // 获取最大关键帧
    /*Bitmap bmp = ThumbnailUtils.createVideoThumbnail("/sdcard/0001.mp4", MediaStore.Images.Thumbnails.MINI_KIND);
mTextView01.setBackground(new BitmapDrawable(bmp));

    // 获取第一个关键帧
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
retriever.setDataSource("/sdcard/0001.mp4");
    Bitmap bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
mTextView02.setBackground(new BitmapDrawable(bmp));*/


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            //        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static JSONObject getVideoInfo(String filePath) {
        try {
            String content = getStringFromFile(filePath);
            JSONObject obj = new JSONObject(content);
            return obj;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String exec(String cmd) {
        try {

            Process process = null;
            if (cmd.indexOf("|") > -1) {
                process = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            } else process = Runtime.getRuntime().exec(cmd);
            InputStream errorInput = process.getErrorStream();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String error = "";
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(errorInput));
            while ((line = bufferedReader.readLine()) != null) {
                error += line;
            }
            // Log.d("usb",result);
            return sb.toString().trim();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void execLocalCmdByAdb(String cmd) {
        try {
            exec("adb connect 127.0.0.1");
            exec("adb shell " + cmd);
        } finally {
            exec("adb disconnect 127.0.0.1");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getFreeBytes(Context mContext, String fsUuid) {
        try {
            UUID id;
            if (fsUuid == null) {
                id = StorageManager.UUID_DEFAULT;
            } else {
                id = UUID.fromString(fsUuid);
            }
            StorageStatsManager stats = mContext.getSystemService(StorageStatsManager.class);
            return stats.getFreeBytes(id);
        } catch (NoSuchFieldError | NoClassDefFoundError | NullPointerException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public static ArrayList<Drive> getExtendedMemoryPath(Context mContext) {


        ArrayList<Drive> ret = new ArrayList<>();

        Drive drive = null;//new Drive(App.getInstance().getCacheDir().getAbsolutePath());
        // drive.setRemoveable(false);
        // ret.add(drive);

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);

        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);

            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);


            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                String state = (String) getVolumeState.invoke(mStorageManager, path);

                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable && "mounted".equals(state)) {
                    File[] firstList = new File(path).listFiles();
                    if (firstList != null)
                        for (File file : firstList) {
                            if (file.isDirectory() && file.getName().equalsIgnoreCase("videos")) {
                                drive = new Drive(file.getAbsolutePath());
                                drive.setRemoveable(true);
                                ret.add(drive);
                                // return file.getAbsolutePath();
                            }
                            File[] secondList = file.listFiles();
                            if (secondList != null)
                                for (File file2 : secondList) {
                                    if (file2.isDirectory() && file2.getName().equalsIgnoreCase("videos")) {
                                        drive = new Drive(file.getAbsolutePath());
                                        drive.setRemoveable(true);
                                        ret.add(drive);
                                    }
                                }

                        }///storage/2067-B583/videos

                    //return ret.toArray(new String[]);
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return ret;
    }


    public static List<Drive> getSysAllDriveList() {
        ArrayList<Drive> driveList = Utils.getExtendedMemoryPath(App.getInstance().getApplicationContext());
        ArrayList<Drive> ret = new ArrayList<>();
        for (Drive d : driveList) {
            try {
                Drive d2 = (Drive) App.getHelper().getDao(Drive.class).queryBuilder().where().eq("p", d.getP()).queryForFirst();
                if (d2 != null) {
                    d.setId(d2.getId());
                    ret.add(d);
                } else {
                    App.getHelper().getDao(Drive.class).create(d);
                    ret.add(d);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
        return ret;
    }

    public static int copyFile(String fromFile, String toFile) {
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;
        } catch (Exception ex) {
            return -1;
        }
    }


    public static String join(String s, List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            sb.append(v).append(s);
        }
        return sb.toString();
    }

    public static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

    public static String get(String url,String referer) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url)
                .newBuilder();
        reqBuild.url(urlBuilder.build());
        reqBuild.addHeader("User-Agent", Utils.AGENT);
        if(referer!=null)
            reqBuild.addHeader("Referer",referer);
        Request request =reqBuild .build();

        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();

        String resp = response.body().string();
        //System.out.println(resp);
        return resp;
    }
    public static String get(String url) throws IOException {
        return get(url,null);
    }

    public static String getObject(com.alibaba.fastjson.JSONObject obj, String string) {

        if (obj == null) return null;

        if (string.indexOf(".") == -1) return obj.getString(string);
        String key = string.substring(0, string.indexOf("."));

        return getObject(obj.getJSONObject(key), string.substring(string.indexOf(".") + 1));
    }

    public static String decompress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes(StandardCharsets.ISO_8859_1));
             GZIPInputStream gunzip = new GZIPInputStream(in)) {

            byte[] buffer = new byte[1024];
            int n;
            // 从 GZIP 压缩输入流读取字节数据到 buffer 数组中
            while ((n = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }

            return out.toString(StandardCharsets.UTF_8.name());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return str;
    }

    public static synchronized String translate(String xmlUrl) throws Exception {
        String xml = Utils.get(xmlUrl);
        if (bTree == null) {


            String content = decompress(Utils.get("https://smlog.github.io/data/updateData.json"));
            //System.out.println((content));
            com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(content);
            // System.out.println(json);

            bTree = new BTree<>();
            JSONArray words = json.getJSONArray("words");

            // words.sort(Comparator.comparing(obj -> ((JSONObject) obj).getString("q")));

            for (int i = 0; i < words.size(); i++) {

                com.alibaba.fastjson.JSONObject word = words.getJSONObject(i);
                // dict.put(word.getString("q"),word);
                bTree.put(word.getString("q"), word);
            }
        }

        List<String> sections = new ArrayList<>();

        int beg = -1, len = xml.length();
        for (int j = 0; j < len; ) {
            char c = xml.charAt(j);
            if (c == '<') {
                if (beg > -1 && j > beg) {
                    sections.add(xml.substring(beg, j));
                    beg = -1;
                }
                int k = xml.indexOf('>', j);
                if (k <= j) throw new Exception("content error");
                sections.add(xml.substring(j, k + 1));

                j = k + 1;
                continue;
            } else if (beg == -1) beg = j;
            j++;
        }

        if (beg > -1) {
            sections.add(xml.substring(beg, len));
        }
        for (int i = 0; i < sections.size(); i++) {
            String s = sections.get(i);
            if (s.indexOf('<') > -1 || s.trim().equals("")) continue;
            String[] tokens = s.split("\\b");
            StringBuilder newLine = new StringBuilder();

            for (int j = 0; j < tokens.length; j++) {
                if (!tokens[j].trim().equals("") && tokens[j].length() > 3) {

                    com.alibaba.fastjson.JSONObject jsonObject = bTree.get(tokens[j]);
                    if (jsonObject != null) {

                        newLine.append("<span style=\"color:#FFFF00FF;font-size:1.5c;font-style:normal;line-height:125%;\">" + tokens[j] + jsonObject.getString("to") + "</span>");
                    } else newLine.append(tokens[j]);

                } else
                    newLine.append(tokens[j]);

            }
            sections.set(i, newLine.toString());

        }
        return String.join("", sections);
    }
}
