package com.llw.androidtvdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    public static VideoItem getVideoInfo(VideoItem item, String filePath){
        try {
            String content =getStringFromFile(filePath);
            JSONObject obj = new JSONObject(content);
            item.title=obj.getString("Title")+obj.getString("Description");
            item.thumb = obj.getString("CoverURL");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }
}
