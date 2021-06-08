package com.usbtv.demo.comm;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.storage.StorageManager;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

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
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
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

    // 获取最大关键帧
    /*Bitmap bmp = ThumbnailUtils.createVideoThumbnail("/sdcard/0001.mp4", MediaStore.Images.Thumbnails.MINI_KIND);
mTextView01.setBackground(new BitmapDrawable(bmp));

    // 获取第一个关键帧
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
retriever.setDataSource("/sdcard/0001.mp4");
    Bitmap bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
mTextView02.setBackground(new BitmapDrawable(bmp));*/

    // 模拟键盘按键，Keycode对应Android键盘按键的的keycode
    public static void setKeyPress(int keycode) {
        try {
            String keyCommand = "input keyevent " + keycode;
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(keyCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  static void simulateKeystroke(final int KeyCode) {
        new Thread(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendCharacterSync(KeyCode);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }).start();
    }
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
    //        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        /*
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }

         */
    }

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

    public static String[] getExtendedMemoryPath(Context mContext) {

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
            ArrayList<String> ret = new ArrayList<>();
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
                            ret.add(file.getAbsolutePath());
                            // return file.getAbsolutePath();
                        }
                        File[] secondList = file.listFiles();
                        if(secondList!=null)
                        for(File file2:secondList){
                            if(file2.isDirectory() && file2.getName().equalsIgnoreCase("videos")){
                                ret.add(file2.getAbsolutePath());
                                //  return file2.getAbsolutePath();
                            }
                        }

                    }///storage/2067-B583/videos

                    //return ret.toArray(new String[]);
                }

            }
            String[]  paths= ret.toArray(new String[0 ]);
            return paths;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


}
