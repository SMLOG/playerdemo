package com.usbtv.demo.comm;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

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
import java.util.LinkedHashMap;
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
    private int curFocusFolderIndex;
    private int curCatId;
    private Map<String, Integer> allTypeMap;
    private LinkedHashMap<Integer, String> typeIdMap;
    private RecyclerView numTabRecyclerView;
    private Folder curFolder;
    private VFile[] numFiles;
    private List<Folder> curCatList;


    private PlayerController() {
    }

    public List<Folder> getCurCatList() {
        return this.curCatList;
    }

    public void setCurCatList(List<Folder> curCatList) {
        this.curCatList = curCatList;
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
        Map<String, Integer> allMap = App.getInstance().getStoreTypeMap();
        this.allTypeMap = new LinkedHashMap<>();
        this.typeIdMap = new LinkedHashMap<>();
        for (String key : allMap.keySet()) {
            Integer value = allMap.get(key);
            if (this.catMoviesMap.get(value) != null) {
                this.cats.add(key);
                allTypeMap.put(key, value);
                typeIdMap.put(value, key);
            }
        }

        if (catsAdaper != null) {
            catsAdaper.notifyDataSetChanged();
        }


    }

    public void setRVAdapts(FolderCatsListRecycleViewAdapter catsAdaper, FolderListAdapter foldersAdapter, FolderNumListRecycleViewAdapter numAdapter) {
        this.catsAdaper = catsAdaper;
        this.foldersAdapter = foldersAdapter;
        this.numAdapter = numAdapter;
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
            Folder folder = null;

            List<Folder> catFolerList = this.getCurCatList();
            int nextPos = this.curFocusFolderIndex + 1;
            if (catFolerList.size() > 0 && catFolerList.size() > nextPos && nextPos >= 0) {
                folder = this.getCurCatList().get(nextPos);
                this.play(folder, nextPos, 0);
                return folder.getFiles().iterator().next();
            }
            if (folder == null) {
                Dao<Folder, ?> folderDao = App.getHelper().getDao(Folder.class);

                int typeId = curItem.getFolder().getTypeId();
                folder = folderDao.queryBuilder()
                        .where().eq("typeId", typeId)
                        .and().lt("orderSeq", curItem.getOrderSeq())
                        .queryBuilder()
                        .orderBy("orderSeq", false)
                        .orderBy("id", false)
                        .queryForFirst();
                if (folder == null) {

                    folder = folderDao.queryBuilder()
                            .where().eq("typeId", typeId)
                            .queryBuilder()
                            .orderBy("orderSeq", false)
                            .orderBy("id", false)
                            .queryForFirst();
                }
            }

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

        if (mode == MODE_LOOP && curItem != null) {
            play(curItem);
            return;
        }

        try {
            Dao<VFile, Integer> vfDao = App.getHelper().getDao(VFile.class);

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


    public void playByVFileId(int id) {


        VFile item = null;
        try {
            item = App.getHelper().getDao(VFile.class).queryBuilder().where().eq("id", id).queryForFirst();
            playVFile(item);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void init(long folderId) {

        try {

            if (folderId > 0) {
                Folder folder = null;

                Dao<Folder, Integer> dao = App.getHelper().getDao(Folder.class);
                folder = dao.queryForId((int) folderId);
                if (folder != null) this.curItem = folder.getFiles().iterator().next();


            }
            if (this.curItem == null) {

                Dao<VFile, Integer> vfDao = App.getHelper().getDao(VFile.class);

                SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

                int id = sp.getInt("id", 1);
                this.curItem = vfDao.queryForId(id);
                if (this.curItem != null) {
                } else {
                    this.curItem = vfDao.queryBuilder().queryForFirst();
                }

            }

            if(this.curItem!=null){
                playVFile(this.curItem);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    public void playVFile(VFile vfile) {
        String curCat = this.typeIdMap.get( vfile.getFolder().getTypeId());
        if(curCat==null){
            curCat = this.typeIdMap.values().iterator().next();
        }
        if(curCat!=null){
            this.setCurCat(curCat);
            int curFolderIndex=0;

            int curfolderId = vfile.getFolder().getId();
            for(int i=0;i< this.curCatList.size();i++){
                if( this.curCatList.get(i).getId()==curfolderId){
                    curFolderIndex=i;
                    break;
                }
            }
            this.setCurFocusFolderIndex(curFolderIndex);

        }

        this.play(vfile);
    }

    public void setUIs(TvVideoView videoView,
                       View gridView, RecyclerView numTabRecyclerView) {
        this.videoView = videoView;
        this.girdView = gridView;
        this.numTabRecyclerView = numTabRecyclerView;
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
        if (this.numAdapter != null)
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
        Integer typeId = allTypeMap.get(curCat);
        this.setCurCatId(typeId);
        this.setCurCatList(catMoviesMap.get(typeId));

        this.foldersAdapter.notifyDataSetChanged();
    }


    public String getCurCat() {
        return curCat;
    }

    public void play(Folder folder, int position, int i) {
        this.setCurFocusFolderIndex(position);
        if (folder.getFiles().size() > i) {
            this.setCurIndex(i);
            this.play(folder.getFiles().toArray(new VFile[]{})[i]);
        }

    }

    public void setCurFocusFolderIndex(int folderPosition) {
        int p = this.curFocusFolderIndex;
        this.curFocusFolderIndex = folderPosition;
        this.curFolder = this.getCurCatList().get(folderPosition);
        this.foldersAdapter.notifyItemChanged(p);
        this.foldersAdapter.notifyItemChanged(folderPosition);
        this.setNumFiles(this.curFolder != null ? this.curFolder.getFiles().toArray(new VFile[]{}) : null);
        this.numAdapter.notifyDataSetChanged();
        // this.numTabRecyclerView.setVisibility(this.curFolder!=null&& this.curFolder.getFiles().size()>1?View.VISIBLE:View.GONE);

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

        if (this.curCat != null && this.curItem != null) {
            return getCurCatList().get(position).getId() == this.curItem.getFolder().getId();
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
        return (this.curItem != null && this.curFolder != null && this.curItem.getFolder().getId() == this.curFolder.getId() && i == this.curIndex);
    }

    public void play() {
        if(this.curItem!=null)
             this.play(this.curItem);
    }


}