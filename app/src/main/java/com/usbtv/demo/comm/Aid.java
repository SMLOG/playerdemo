package com.usbtv.demo.comm;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


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


    public static void scanAllDrive(Map<String, Integer> typesMap, Map<Integer, Boolean> validFoldersMap, Map<String, Boolean> validAidsMap) throws Exception {

        for (Drive root : Utils.getSysAllDriveList()) {

            File rootDir = new File(root.getP());

            File[] aidDirs = rootDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            App.getHelper().getDao(Drive.class).createOrUpdate(root);

            typesMap.put("本地",0);
            for (File aidDir : aidDirs) {
                scanFolder(0,root, aidDir,validFoldersMap,validAidsMap);
            }
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void scanFolder(Integer typeId, Drive root, File aidDir, Map<Integer, Boolean> validFoldersMap, Map<String, Boolean> validAidsMap) throws Exception {

        if(validAidsMap.get(aidDir.getName())!=null)return;

        String divfile = aidDir.getAbsolutePath() + File.separator + aidDir.getName() + ".dvi";

        List<File> matchFiles = searchFiles(aidDir, ".*\\.(mp4|rmvb|flv|mpeg|avi|mkv)");

        String title = aidDir.getName();
        String coverURL = null;
        String bvid = null;
        if (matchFiles.size() > 0) {
            if (new File(divfile).exists()) {
                String content = getStringFromFile(divfile);
                JSONObject jsonObj = JSON.parseObject(content);
                title = (String) jsonObj.get("Title");
                coverURL = (String) jsonObj.get("CoverURL");
                bvid = (String) jsonObj.get("Bid");
            }
        } else return;

        matchFiles.sort(new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {

                String n1=f1.getName();
                String n2=f2.getName();
                int maxLen=n1.length()>n2.length()?n1.length():n2.length();

                while (n1.length()<maxLen){
                    n1=" "+n1;
                }
                while (n2.length()<maxLen){
                    n2=" "+n2;
                }
                return n1.compareTo(n2);

            }
        });


        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);

        Folder folder = folderDao.queryBuilder().where().eq("aid", aidDir.getName()).and().eq("root_id", root.getId()).queryForFirst();

        if (folder == null) {

            folder = new Folder();
            folder.setName(title);
            folder.setRoot(root);
            folder.setP(aidDir.getName());
            folder.setCoverUrl(coverURL);
            folder.setAid(aidDir.getName());
            folder.setBvid(bvid);
            folder.setTypeId(typeId);
            folderDao.createOrUpdate(folder);

        }
        validFoldersMap.put(folder.getId(),true);
        validAidsMap.put(folder.getAid(),true);

        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);

        int orderN=0;
        for (File file : matchFiles) {

            String path = file.getAbsolutePath().substring(aidDir.getAbsolutePath().length() + 1);

            VFile vfile = vFileDao.queryBuilder().where().eq("p", path).and()
                    .eq("folder_id", folder.getId()).queryForFirst();

            if (vfile == null) {

                try {
                    vfile = new VFile();
                    vfile.setP(path);
                    vfile.setName(file.getName());
                    vfile.setFolder(folder);

                    try{
                        String num = path.split(File.separator)[0];
                        vfile.setPage(Integer.parseInt(num));
                    }catch (Exception e){

                    }



                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
            vfile.setOrderSeq(orderN++);
            vFileDao.createOrUpdate(vfile);


        }
    }


}

