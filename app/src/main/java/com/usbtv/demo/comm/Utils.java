package com.usbtv.demo.comm;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.storage.StorageManager;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import com.usbtv.demo.App;
import com.usbtv.demo.data.Drive;

import org.json.JSONObject;

import java.io.BufferedReader;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utils {
    
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
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
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

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static JSONObject getVideoInfo(String filePath){
        try {
            String content =getStringFromFile(filePath);
            JSONObject obj = new JSONObject(content);
            return obj;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public  static String exec(String cmd){
        try {

            Process process=null;
            if(cmd.indexOf("|")>-1){
                 process = Runtime.getRuntime().exec(new String[]{"sh","-c",cmd});
            }
            else process= Runtime.getRuntime().exec(cmd);
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

    public static void execLocalCmdByAdb(String cmd){
        try{
            exec("adb connect 127.0.0.1");
            exec("adb shell "+ cmd);
        }finally {
            exec("adb disconnect 127.0.0.1");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getFreeBytes(Context mContext,String fsUuid) {
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
                if (removable && "mounted".equals(state) ) {
                    File[] firstList = new File(path).listFiles();
                    if(firstList!=null)
                    for(File file:firstList){
                        if(file.isDirectory() && file.getName().equalsIgnoreCase("videos")){
                            drive = new Drive(file.getAbsolutePath());
                            drive.setRemoveable(true);
                            ret.add(drive);
                            // return file.getAbsolutePath();
                        }
                        File[] secondList = file.listFiles();
                        if(secondList!=null)
                        for(File file2:secondList){
                            if(file2.isDirectory() && file2.getName().equalsIgnoreCase("videos")){
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
        for(Drive d:driveList){
            try {
                Drive d2=(Drive)App.getHelper().getDao(Drive.class).queryBuilder().where().eq("p",d.getP()).queryForFirst();
                if(d2!=null){
                    d.setId(d2.getId());
                    ret.add(d);
                }else{
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


}
