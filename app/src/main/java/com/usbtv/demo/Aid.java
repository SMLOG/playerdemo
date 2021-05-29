package com.usbtv.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.field.DatabaseField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Item {
    private String path;
    private String title;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}

public class Aid {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String typeId;
    @DatabaseField(unique = true)
    private String aid;

    private String coverUrl;
    private String title;
    private String  serverBase;

    private List<Item> items;

    public Aid() {
        super();
        this.items = new ArrayList<Item>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aid aid1 = (Aid) o;
        return aid.equals(aid1.aid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aid);
    }

    public List<Item> getItems() {
        return items;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getServerBase() {
        return serverBase;
    }

    public void setServerBase(String serverBase) {
        this.serverBase = serverBase;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    public static void main(String[] args) throws Exception {

        scan("/Users/alexwang/www/bilibili/");
    }

    public static List<Aid> scan(String root) throws Exception {

        List<Aid> aidList = new ArrayList<Aid>();

        File dir = new File(root);

        File[] aidDirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        for (File aidDir : aidDirs) {

            String divfile = aidDir.getAbsolutePath() + File.separator + aidDir.getName() + ".dvi";

            List<File> matchFiles = searchFiles(aidDir, ".*\\.(mp4|rmvb|flv|mpeg|avi|mkv)");

            String title = null;
            String coverURL = null;
            if (matchFiles.size() > 0) {
                if (new File(divfile).exists()) {
                    String content = getStringFromFile(divfile);
                    JSONObject jsonObj = JSON.parseObject(content);
                    title = (String) jsonObj.get("Title");
                    coverURL = (String) jsonObj.get("CoverURL");
                }
            }else continue;

            Aid aidItem = new Aid();
            aidItem.setAid(aidDir.getName());
            aidItem.setCoverUrl(coverURL);
            aidItem.setTitle(title);

            aidList.add(aidItem);

            for (File file : matchFiles) {

                Item item = new Item();
                item.setPath(file.getAbsolutePath().substring(aidDir.getAbsolutePath().length() + 1));
                item.setTitle(file.getName());
                aidItem.getItems().add(item);

            }

        }

        String jsonOutput = JSON.toJSONString(aidList, true);
        System.out.println(jsonOutput);

        FileWriter fw = new FileWriter(new File(dir + File.separator + "playlist.json"));
        fw.write(jsonOutput);
        fw.close();

        return aidList;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static String exec(String cmd) {
        try {
            // System.out.println(cmd);

            List<String> cmds = new ArrayList<String>();
            cmds.add("sh");
            cmds.add("-c");
            cmds.add(cmd);
            ProcessBuilder pb = new ProcessBuilder(cmds);
            Process process = pb.start();

            // Process process = Runtime.getRuntime().exec(cmd);
            InputStream errorInput = process.getErrorStream();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String error = "";
            String result = "";
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line + "\n";
            }
            bufferedReader = new BufferedReader(new InputStreamReader(errorInput));
            while ((line = bufferedReader.readLine()) != null) {
                error += line + "\n";
            }
            // Log.d("usb",result);
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

