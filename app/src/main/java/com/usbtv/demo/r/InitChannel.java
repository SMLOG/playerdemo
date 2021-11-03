package com.usbtv.demo.r;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.usbtv.demo.App;
import com.usbtv.demo.data.Folder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class InitChannel {

    public InitChannel() {

        initChannels();
    }

    private void initChannels() {


        SharedPreferences perf = PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext());

        Integer[] channels = JSON.parseArray(perf.getString("channels", "[]")).toArray(new Integer[0]);

        for (long ch : channels) {
            MediaTVProvider.deleteChannel(App.getInstance().getApplicationContext(), ch);
        }

        LinkedHashMap<String, String> map = App.getAllTypeMap();

        List<Long> channelList = new ArrayList<>();
        int i = 0;
        for (String key : map.keySet()) {
            String val = map.get(key);
            if ("".equals(val)) continue;


            i++;
            int contentId = 0;

            List<MediaProgram> programs = new ArrayList<>();
            List<Folder> list = VideoProvider.getMovieList(val);

            if (list.size() == 0) continue;

            for (Folder p : list) {

                MediaProgram program = new MediaProgram(p.getName(), p.getName(), p.getCoverUrl(), p.getCoverUrl(),
                        "Movie", "" + p.getId(), p.getId(), Integer.toString(contentId++));
                programs.add(program);

            }
            Log.v("TAG", "add channel "+key+" size:"+programs.size());
            MediaChannel channel = new MediaChannel(key, programs, i == 1);


            long channelId = MediaTVProvider.addChannel(App.getInstance().getApplicationContext(), channel);
            channelList.add(channelId);
        }


        perf.edit().putString("channels", JSON.toJSONString(channelList)).apply();


    }

    private Uri getUSBCardImageFileUri() {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(sdPath + "/Pictures/mediachannel/usb_thumbnail.jpg");
        Uri uri = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), "com.rogera.mediaplaychannel.fileprovider", file);
        //Log.v(TAG, "uri:" + uri.toString());
        return uri;
    }

    private Uri getPVRCardImageFileUri() {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(sdPath + "/Pictures/mediachannel/pvr_thumbnail.jpg");
        Uri uri = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), "com.rogera.mediaplaychannel.fileprovider", file);
        // Log.v(TAG, "uri:" + uri.toString());
        return uri;
    }

    private void grantUriPermissionToApp(String packageName, Uri uri) {
        App.getInstance().getApplicationContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }


}
