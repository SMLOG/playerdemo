package com.usbtv.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.field.DatabaseField;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Aid {

    public static List<File> searchFiles(File folder, final String keyword) {
        List<File> result = new ArrayList<File>();
        if (folder.isFile())
            result.add(folder);

        File[] subFolders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (file.getName().toLowerCase().matches(keyword)) {
                    return true;
                }
                return false;
            }
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    result.add(file);
                } else {
                    result.addAll(searchFiles(file, keyword));
                }
            }
        }

        return result;
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

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;
    }


    public static void scanFolder() throws Exception {

        for (Drive root : Utils.getSysAllDriveList()) {

            File dir = new File(root.getP());

            File[] aidDirs = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            App.getHelper().getDao(Drive.class).createIfNotExists(root);

            for (File aidDir : aidDirs) {

                String divfile = aidDir.getAbsolutePath() + File.separator + aidDir.getName() + ".dvi";

                List<File> matchFiles = searchFiles(aidDir, ".*\\.(mp4|rmvb|flv|mpeg|avi|mkv)");

                String title = aidDir.getName();
                String coverURL = null;
                if (matchFiles.size() > 0) {
                    if (new File(divfile).exists()) {
                        String content = getStringFromFile(divfile);
                        JSONObject jsonObj = JSON.parseObject(content);
                        title = (String) jsonObj.get("Title");
                        coverURL = (String) jsonObj.get("CoverURL");
                    }
                } else continue;


                Folder folder = new Folder();
                folder.setName(title);

                folder.setRoot(root);

                folder.setP(aidDir.getName());

                folder.setCoverUrl(coverURL);

                App.getHelper().getDao(Folder.class).createIfNotExists(folder);

                for (File file : matchFiles) {
                    VFile vfile = new VFile();
                    vfile.setP(file.getAbsolutePath().substring(aidDir.getAbsolutePath().length() + 1));
                    vfile.setName(file.getName());
                    vfile.setFolder(folder);
                    App.getHelper().getDao(VFile.class).createIfNotExists(vfile);
                }


            }
        }


    }



}

