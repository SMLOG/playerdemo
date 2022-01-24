package com.usbtv.demo.comm;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.j256.ormlite.dao.Dao;
import com.nurmemet.nur.nurvideoplayer.TvVideoView;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.view.adapter.FolderCatsListRecycleViewAdapter;
import com.usbtv.demo.view.adapter.FolderListAdapter;
import com.usbtv.demo.view.adapter.FolderNumListRecycleViewAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class PlayerController {

    final static int MODE_RANDOM = 1;
    final static int MODE_SEQ = 0;
    final static int MODE_LOOP = 2;

    private static PlayerController instance;
    private VFile curItem;

    private int mode;

    private TvVideoView videoView;
    private Uri videoUrl;
    private View girdView;
    private int curIndex;
    private String curCat;

    private List<Folder> allMovies;
    private HashMap<Integer, List<Folder>> catMoviesMap;
    private FolderCatsListRecycleViewAdapter catsAdaper;
    private FolderListAdapter foldersAdapter;
    private FolderNumListRecycleViewAdapter numAdapter;
    private List<String> cats;
    private int folderPosition;
    private int curCatId;


    private PlayerController() {
    }

    public List<Folder> getCurCatList() {

        if (this.curCat == null) return new ArrayList<>();
        Integer typeId = App.getAllTypeMap().get(this.curCat);
        if (catMoviesMap.get(typeId) == null) return new ArrayList<>();
        return catMoviesMap.get(typeId);
    }

    public synchronized void reloadMoviesList() {

        this.allMovies = App.getAllMovies();
        this.catMoviesMap = new HashMap<Integer, List<Folder>>();
        List<Folder> list = null;

        int lastType = -1;
        for (Folder folder : allMovies) {
            if (folder.getTypeId() != lastType) {
                lastType = folder.getTypeId();
                list = new LinkedList<>();
                catMoviesMap.put(lastType, list);
            }
            list.add(folder);
        }

        this.cats = new ArrayList<String>();
        Map<String, Integer> allMap = App.getInstance().getAllTypeMap();
        for (String key : allMap.keySet()) {
            Integer value = allMap.get(key);
            if (this.catMoviesMap.get(value) != null) {
                this.cats.add(key);

            }
        }

        if (catsAdaper != null) {
            catsAdaper.notifyDataSetChanged();
        }
        if(this.curItem == null && allMovies.size()>0){
            play(allMovies.iterator().next(),0);
        }

    }

    public void setRVAdapts(FolderCatsListRecycleViewAdapter catsAdaper, FolderListAdapter foldersAdapter, FolderNumListRecycleViewAdapter numAdapter) {
        this.catsAdaper = catsAdaper;
        this.foldersAdapter = foldersAdapter;
        this.numAdapter = numAdapter;
    }

    public void init() {
        if (this.curCat == null && this.cats.size() > 0) {
            setCurCat(this.cats.get(0));
        }
    }

    public List<String> getCats() {

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

    public void seekTo(int pos) {
        if (videoView != null) videoView.seekTo(pos);

    }

    public void pause() {
        if (videoView != null) videoView.pause();
    }

    public void start() {
        if (videoView != null) videoView.start();

    }

    public void prepare() {
    }


    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }


    public PlayerController play(VFile res) {
        this.curItem = res;
        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("id", res.getId());
        editor.apply();
        sp.edit().commit();

        Iterator<VFile> it = res.getFolder().getFiles().iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().getId() == res.getId()) {
                break;
            }
            i++;
        }
        PlayerController.getInstance().setCurIndex(i);

        new Thread(new Runnable() {
            @Override
            public void run() {

                String title = "";
                PlayerController.this.videoUrl = null;
                videoUrl = App.getUri(res);
                title = res.getName();

                Handler handler = new Handler(Looper.getMainLooper());
                String finalTitle = title;
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        synchronized (videoView) {


                            videoView.pause();
                            videoView.setVideoURI(PlayerController.this.videoUrl, finalTitle);
                            videoView.resume();

                        }
                    }
                });
            }
        }).start();

        return this;
    }


    public void prev() {
    }

    public VFile playNextFolder() {

        if (curItem == null) {
            return null;
        }
        try {
            Dao<Folder, ?> folderDao = App.getHelper().getDao(Folder.class);

            Folder folder = folderDao.queryBuilder()
                    .where().eq("typeId", curItem.getFolder().getTypeId()).and().lt("orderSeq", curItem.getOrderSeq())
                    .queryBuilder()
                    .orderBy("orderSeq", false)
                    .queryForFirst();
            if (folder == null)
                folder = folderDao.queryBuilder()
                        .where().eq("typeId", curItem.getFolder().getTypeId())
                        .queryBuilder()
                        .orderBy("orderSeq", false)
                        .orderBy("id", false)
                        .queryForFirst();

            if (folder != null) {
                curItem = folder.getFiles().iterator().next();
                play(curItem);
                return curItem;
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;

    }

    public void next() {
        try {
            Dao<VFile, Integer> vfDao = App.getHelper().getDao(VFile.class);

            if (curItem == null) {

                firstPlay();
                return;
            }


            VFile vf = curItem;


            int vfId = vf.getId();

            vf = vfDao.queryForId(vfId);

            VFile nextVf = null;

            if (vf != null) {
                Iterator<VFile> fileListIterator = vf.getFolder().getFiles().iterator();
                while (fileListIterator.hasNext()) {
                    VFile file = fileListIterator.next();
                    if (file.getId() == vfId) {
                        if (fileListIterator.hasNext()) {
                            nextVf = fileListIterator.next();
                        }
                        break;
                    }
                }

                if (nextVf != null) {
                    play(nextVf);
                    return;
                }
            }

            playNextFolder();


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean firstPlay() throws SQLException {

        Dao<VFile, Integer> vfDao = App.getHelper().getDao(VFile.class);

        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

        int id = sp.getInt("id", 1);
        VFile file = vfDao.queryForId(id);
        if (file != null) {
        } else {
            file = vfDao.queryBuilder().queryForFirst();
        }
        if (file != null) {
            play(file);
            return true;
        }
        return false;
    }

    public void playNext() {

        if (mode == MODE_LOOP && curItem != null) {
            play(curItem);
            return;
        }
        next();

    }

    public void setUIs(TvVideoView videoView,
                       View gridView) {
        this.videoView = videoView;
        this.girdView = gridView;
    }

    public String getCoverUrl() {
        if (this.curItem instanceof VFile)
            return ((VFile) (this.curItem)).getFolder().getCoverUrl();
        return "";
    }

    public String getName() {
        if (this.curItem instanceof VFile)
            return ((VFile) (this.curItem)).getFolder().getName();
        return "";
    }


    public PlayerController hideMenu() {
        this.girdView.setVisibility(View.GONE);
        return this;
    }


    public int getCurIndex() {
        return this.curIndex;
    }

    public PlayerController setCurIndex(int i) {
        this.curIndex = i;
        if(this.numAdapter!=null)
        this.numAdapter.notifyDataSetChanged();
        return this;
    }

    public PlayerController play(Folder folder, int position) {
        if (folder != null) {
            this.setCurIndex(position);
            PlayerController.getInstance().play(folder.getFiles().toArray(new VFile[]{})[position]);
        }
        return this;
    }

    public void setCurCat(String curCat) {

        this.curCat = curCat;
        Integer typeId = App.getAllTypeMap().get(curCat);
        this.setCurCatId(typeId);
        this.foldersAdapter.notifyDataSetChanged();
    }

    public String getCurCat() {
        return curCat;
    }

    public void play(Folder folder, int position, int i) {
        this.setFolderPosition(position);
        this.setCurIndex(i);
        this.play(folder.getFiles().toArray(new VFile[]{})[i]);
    }

    public int getFolderPosition() {
        return folderPosition;
    }

    public void setFolderPosition(int folderPosition) {
        int p = this.folderPosition;
        this.folderPosition = folderPosition;
        this.foldersAdapter.notifyItemChanged(p);
        this.foldersAdapter.notifyItemChanged(folderPosition);
    }

    public Folder getCurFolder() {
        if (this.curItem != null) return this.curItem.getFolder();
        return null;
    }

    public PlayerController play(int position) {
        Folder folder = getCurFolder();
        this.play(folder, position);
        return this;
    }

    public boolean isFolderPositionSelected(int position) {

        if (this.curCat != null && this.curItem != null) {
            int typeId = this.getCurCatId();
            if (typeId == this.curItem.getFolder().getTypeId()) {
                return position == folderPosition;
            }


        }
        return false;
    }

    public int getCurCatId() {
        return curCatId;
    }

    public void setCurCatId(int curCatId) {
        this.curCatId = curCatId;
    }
}