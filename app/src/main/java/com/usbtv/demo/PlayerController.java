package com.usbtv.demo;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.data.CatType;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.sync.VideoList;
import com.usbtv.demo.view.adapter.FolderCatsListRecycleViewAdapter;
import com.usbtv.demo.view.adapter.FolderListAdapter;
import com.usbtv.demo.view.adapter.FolderNumListRecycleViewAdapter;
import com.usbtv.demo.view.adapter.QtabListRecycleViewAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.usbtv.demo.comm.Log;

public final class PlayerController {

    final static int MODE_LOOP = 2;

    private static PlayerController instance;


    private int mode;

    private GsyTvVideoView videoView;
    private View girdView;

    private VFile curFile;
    private ArrayList<String> options;

    public void setFileIndexOfFolder(int fileIndexOfFolder) {
        this.fileIndexOfFolder = fileIndexOfFolder;
        curFile = null;
        curFile = getFile();
        options = null;
        options = getFileOptions();
    }


    private int fileIndexOfFolder;
    private String curCat;

    private FolderCatsListRecycleViewAdapter catsAdaper;
    private FolderListAdapter foldersAdapter;
    private FolderNumListRecycleViewAdapter numAdapter;
    private int curFocusFolderIndex;
    private int curCatId;

    @JSONField(serialize = false)
    private Map<String, Integer> allTypeMap;

    private LinkedHashMap<Integer, String> typeIdMap;

    @JSONField(serialize = false)
    private Folder curFolder;

    @JSONField(serialize = false)
    private VFile[] numFiles;
    private List<Folder> curCatList;
    private RecyclerView qTabRecyclerView;
    private QtabListRecycleViewAdapter qAdapter;
    private RecyclerView foldersRecyclerView;
    private Timer timerCat = new Timer();
    private Timer timer2 = new Timer();
    private List<CatType> cats;
    public ConfigStore configStore;
    private int folderIndex;


    private PlayerController() {
        configStore = ConfigStore.restore();
    }

    @JSONField(serialize = false)
    public List<Folder> getCurCatList() {
        return this.curCatList;
    }

    private List tmpList;

    @JSONField(serialize = false)
    public List<Folder> getFocusCatList() {
        if (focusCat == null) focusCat = curCat;
        if (tmpList == null && focusCat != null)
            tmpList = loadCatFolderList(allTypeMap.get(focusCat));

        if (tmpList == null) return new ArrayList<>();
        return tmpList;
    }


    public List<Folder> loadCatFolderList(int typeId) {
        List<Folder> ret = null;
        try {
            Where<Folder, ?> where = App.getHelper().getDao(Folder.class).queryBuilder().where();

            if (typeId > 0)
                ret = where.eq("typeId", typeId).queryBuilder().orderBy("orderSeq", false).query();
                //local
            else if (typeId == 0)
                ret = where.eq("isFav", 1).queryBuilder().orderBy("orderSeq", false).query();


        } catch (SQLException throwables) {
            Log.e(Log.getStackTraceString(throwables));
            ret = new ArrayList<>();
        }
        return ret;
    }

    public synchronized void refreshCats() {

        try {
            CatType fav = new CatType();
            fav.setTypeId(0);
            fav.setName("Fav");
            fav.setStatus("A");
            fav.setOrderSeq(0);
            App.getCatTypeDao().createOrUpdate(fav);

            this.cats = App.getCatTypeDao().queryBuilder().orderBy("orderSeq", true).where().eq("status", "A").query();

            allTypeMap = new LinkedHashMap<>();
            typeIdMap = new LinkedHashMap<>();
            for (CatType cat : cats) {
                allTypeMap.put(cat.getName(), cat.getTypeId());
                typeIdMap.put(cat.getTypeId(), cat.getName());
            }

        } catch (Throwable e) {
            Log.e(e);
        }


        if (catsAdaper != null) {
            catsAdaper.notifyDataSetChanged();
        }

    }

    public void bindUI(FolderCatsListRecycleViewAdapter catsAdaper, FolderListAdapter foldersAdapter, FolderNumListRecycleViewAdapter numAdapter, QtabListRecycleViewAdapter qAdapter) {
        this.catsAdaper = catsAdaper;
        this.foldersAdapter = foldersAdapter;
        this.numAdapter = numAdapter;
        this.qAdapter = qAdapter;
    }

    @JSONField(serialize = false)
    public List<CatType> getCats() {
        return this.cats;
    }


    public static PlayerController getInstance() {
        if (instance == null) instance = new PlayerController();
        return instance;
    }

    public long getDuration() {
        return videoView == null ? 0 : videoView.getDuration();
    }

    public long getCurrentPosition() {
        return videoView == null ? 0 : videoView.getCurrentPosition();

    }

    public boolean isPlaying() {
        return videoView == null ? false : videoView.isPlaying();

    }


    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void seekTo(int pos) {
        if (videoView != null) videoView.seekTo(pos);

    }

    public void pause() {
        if (videoView != null) videoView.onPause();
    }

    public void start() {
        if (videoView != null) videoView.start();

    }


    public PlayerController play() {


        VFile res = null;
        if (curFolder != null && fileIndexOfFolder >= 0 && fileIndexOfFolder < curFolder.getFiles().size()) {
            res = getFile();
            this.configStore.fileId = res.getId();
            PlayerController.getInstance().configStore.save();
            PlayerController.getInstance().setCurFileIndexInFolderAndUpdateUI();

        }

        if (res != null) {
            VFile finalRes = res;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            synchronized (videoView) {

                                videoView.setVideoURI(finalRes, PlayerController.this.fileIndexOfFolder);

                            }
                        }
                    });
                }
            }).start();
        }

        return this;
    }

    public void nextFolderFile() {
        Folder folder = null;

        List<Folder> catFolerList = this.getCurCatList();
        int nextPos = folderIndex + 1;
        if (catFolerList != null && !catFolerList.isEmpty()) {
            for (int i = nextPos > catFolerList.size() ? 0 : nextPos; i < catFolerList.size(); i++) {
                folder = catFolerList.get(i);

                if (folder != null && folder.getFiles() != null && folder.getFiles().size() > 0) {
                    selectFolder(i);
                    selectFile(0);
                    play();
                    break;
                }

            }


        }

    }

    private void selectFile(int i) {
        setFileIndexOfFolder(i);
        PlayerController.this.numAdapter.notifyDataSetChanged();
    }

    VFile getFile() {
        if (this.curFolder == null) return null;
        if (curFile != null) return curFile;
        if (fileIndexOfFolder < 0) return null;
        VFile[] files = this.curFolder.getFiles().toArray(new VFile[]{});
        if (files.length <= fileIndexOfFolder) return null;
        return files[this.fileIndexOfFolder];
    }

    public void next() {

        if (mode == MODE_LOOP) {
            play();
            return;
        }

        if (this.curFolder != null && this.curFolder.getFiles() != null) {
            if (this.fileIndexOfFolder < this.curFolder.getFiles().size()) {
                setFileIndexOfFolder(fileIndexOfFolder + 1);
                this.play();
                return;
            }
        }
        nextFolderFile();


    }

    public void prev() {

        if (mode == MODE_LOOP) {
            play();
            return;
        }

        if (this.curFolder != null && this.curFolder.getFiles() != null && this.fileIndexOfFolder > 0) {
            setFileIndexOfFolder(this.fileIndexOfFolder - 1);
            if (this.fileIndexOfFolder < this.curFolder.getFiles().size()) {
                this.play();
                return;
            }
        }
        nextFolderFile();


    }

    public void playByVFileId(int id) {


        VFile item = null;
        try {
            item = App.getHelper().getDao(VFile.class).queryBuilder().where().eq("id", id).queryForFirst();
            this.curCatList = null;
            playVFile(item);
        } catch (Throwable throwables) {
            Log.e(throwables);
        }
    }

    public void init() {

        try {

            Dao<VFile, Integer> vfDao = App.getHelper().getDao(VFile.class);

            if (configStore.fileId != null) {
                VFile curFile = vfDao.queryForId(PlayerController.getInstance().configStore.fileId);
                if (curFile != null) playVFile(curFile);

            }


        } catch (Throwable throwables) {
            Log.e(throwables);
        }


    }

    public void playVFile(VFile vfile) {
        if (vfile == null) return;
        Folder folder = vfile.getFolder();
        String curCat = this.typeIdMap.get(folder.getTypeId());
        if (curCat == null && !this.typeIdMap.values().isEmpty()) {
            curCat = this.typeIdMap.values().iterator().next();
        }
        if (curCat != null) {
            this.selectCat(curCat);
            int curFolderIndex = -1;

            int curfolderId = folder.getId();

            if (this.curCatList == null || this.curCatList.isEmpty()) {
                this.curCatList = loadCatFolderList(folder.getTypeId());
            }

            for (int i = 0; i < this.curCatList.size(); i++) {
                if (this.curCatList.get(i).getId() == curfolderId) {
                    curFolderIndex = i;
                    this.curFolder = this.curCatList.get(i);
                    int index = 0;
                    for (VFile tf : this.curFolder.getFiles()) {

                        if (tf.getId() == vfile.getId()) {
                            selectFolder(curFolderIndex);
                            setFileIndexOfFolder(index);
                            break;
                        }
                        index++;
                    }
                    break;
                }
            }


        }

        this.play();
    }

    public void setUIs(GsyTvVideoView videoView,
                       View gridView, RecyclerView numTabRecyclerView, RecyclerView qTabRecyclerView, RecyclerView foldersRecyclerView) {
        this.videoView = videoView;
        this.girdView = gridView;
        this.qTabRecyclerView = qTabRecyclerView;
        this.foldersRecyclerView = foldersRecyclerView;
    }

    public String getCoverUrl() {
        VFile file = getFile();
        if (file != null)
            return file.getFolder().getCoverUrl();
        return "";
    }

    public String getName() {
        VFile file = getFile();
        if (file != null)
            return file.getFolder().getName();
        return "";
    }


    public PlayerController hideMenu() {
        this.girdView.setVisibility(View.GONE);
        return this;
    }


    public int getFileIndexOfFolder() {
        return this.fileIndexOfFolder;
    }

    public PlayerController setCurFileIndexInFolderAndUpdateUI() {
        this.numAdapter.notifyDataSetChanged();
        return this;
    }

    public PlayerController play(Folder folder, int position) {
        if (folder != null) {
            this.curFolder = folder;
            setFileIndexOfFolder(position);
            play();
        }
        return this;
    }

    private String focusCat;

    public void focusCat(String cat) {
        focusCat = cat;

        this.timerCat.cancel();
        this.timerCat = new Timer();

        timerCat.schedule(new TimerTask() {
            @Override
            public void run() {

                tmpList = loadCatFolderList(allTypeMap.get(focusCat));
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PlayerController.this.foldersAdapter.notifyDataSetChanged();
                    }
                });


            }
        }, 500);//延时1s执行
    }

    public void selectCat(String cat) {

        if (this.curCat != null && this.curCat.equals(cat)) {
            return;
        }
        this.curCat = cat;
        Integer typeId = allTypeMap.get(cat);
        this.setCurCatId(typeId);
        curCatList = loadCatFolderList(PlayerController.this.curCatId);


    }

    public Map<String, Integer> getAllTypeMap() {
        return allTypeMap;
    }


    public String getCurCat() {
        return curCat;
    }


    public void focusFolder(int folderPosition) {
        this.curFocusFolderIndex = folderPosition;

        this.timer2.cancel();
        this.timer2 = new Timer();

        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                Folder folder = PlayerController.this.getFocusCatList().get(curFocusFolderIndex);
                VFile[] numFiles = folder.getFiles().toArray(new VFile[]{});

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PlayerController.this.setNumFiles(numFiles);
                        PlayerController.this.numAdapter.notifyDataSetChanged();
                        qTabRecyclerView.setVisibility(PlayerController.this.getFileOptions().size() > 0 ? View.VISIBLE : View.GONE);

                    }
                });
            }
        }, 500);

    }

    public ArrayList<String> getFileOptions() {
        if (options != null) return options;
        VFile file = getFile();
        ArrayList<String> list = new ArrayList();
        if (file == null) return list;
        CatType type = null;
        try {
            type = App.getCatTypeDao().queryForId(file.getFolder().getTypeId());
            if (type.getUrl() != null) {
                list.add("Sync");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (file.getCc() != null) {
            list.add("Cc");
        }
        return list;
    }

    public void toggleFileOption(int position) {
        ArrayList<String> options = getFileOptions();
        switch (options.get(position)) {
            case "Sync":
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            VFile file = getFile();
                            CatType type = App.getCatTypeDao().queryForId(file.getFolder().getTypeId());
                            if (type.getUrl() != null) {
                                VideoList.insertVideos(type.getUrl(), null, true);
                            }
                        } catch (Throwable e) {
                            android.util.Log.e("Sync", android.util.Log.getStackTraceString(e));
                        }
                    }
                }).start();

                break;
            case "Cc":
                configStore.subTitleActive = !configStore.subTitleActive;
                configStore.save();
                break;
        }
    }

    public boolean isOptionSelect(int position) {
        ArrayList<String> options = getFileOptions();
        if (options.get(position).equals("Cc")) {
            return configStore.subTitleActive;
        }
        return false;
    }

    public void selectFolder(int folderPosition) {
        folderIndex = folderPosition;
        this.curFolder = curCatList.get(folderPosition);
        this.foldersAdapter.notifyItemChanged(folderPosition);

    }

    public VFile[] getNumFiles() {
        return numFiles;
    }

    public void setNumFiles(VFile[] numFiles) {
        this.numFiles = numFiles;
    }

    public Folder getCurFolder() {

        if (this.curFolder != null) return this.curFolder;
        return null;
    }

    public PlayerController play(int position) {
        Folder folder = getCurFolder();
        this.play(folder, position);
        return this;
    }

    public boolean isFolderPositionSelected(int position) {

        if (this.focusCat != null && this.curFolder != null) {
            return getFocusCatList().get(position).getId() == this.curFolder.getId();
        }
        return false;
    }

    public int getCurCatId() {
        return curCatId;
    }

    public void setCurCatId(int curCatId) {
        this.curCatId = curCatId;
    }


    public boolean isNumberSelect(int i) {
        if (this.curFolder == null) return false;
        VFile curFile = getFile();
        return (curFile != null && this.curFolder != null && curFile.getFolder().getId() == this.curFolder.getId() && i == this.fileIndexOfFolder);
    }


    @JSONField(serialize = false)
    private String[] rates = new String[]{"1080", "720", "540"};

    private int curRateIndex = 0;

    public String[] getRates() {
        return rates;
    }

    public boolean getRate(int position) {
        return curRateIndex == position;
    }

    public PlayerController setCurRateIndex(int position) {
        this.curRateIndex = position;
        qAdapter.notifyDataSetChanged();
        this.play();
        return this;
    }

    public String getRate() {
        return rates[curRateIndex];
    }

    public void toggleFav() {
        this.curFolder.setIsFav(this.curFolder.getIsFav() > 0 ? 0 : 1);
        try {
            App.getHelper().getDao(Folder.class).update(this.curFolder);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        this.foldersAdapter.notifyItemChanged(this.curFocusFolderIndex);

    }


    public String getFallBackUrl() {
        VFile file = getFile();
        if (this.configStore.fallback != null && !this.configStore.fallback.trim().equals("")

        ) {
            if (file.getdLink() != null && !file.getdLink().trim().equals("")) {
                try {
                    return this.configStore.fallback + "?url=" + URLEncoder.encode(file.getdLink(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

            } else if (file.getBvid() != null) {
                return this.configStore.fallback + "?bvid=" + file.getBvid();

            }

        }
        return null;
    }

    public void play(Folder folder, int folderIndex, int fileIndexOfFolder) {
        this.curCat = this.focusCat;
        this.curCatList = this.tmpList;

        this.selectFolder(folderIndex);
        this.play(folder, fileIndexOfFolder);

    }

    public PlayerController play(VFile numFile, int position) {
        play(numFile.getFolder(), curFocusFolderIndex, position);
        return this;
    }


}