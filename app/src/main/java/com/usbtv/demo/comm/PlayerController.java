package com.usbtv.demo.comm;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.leanback.widget.HorizontalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.nurmemet.nur.nurvideoplayer.TvVideoView;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.view.adapter.FolderCatsListRecycleViewAdapter;
import com.usbtv.demo.view.adapter.FolderListAdapter;
import com.usbtv.demo.view.adapter.FolderNumListRecycleViewAdapter;
import com.usbtv.demo.view.adapter.QtabListRecycleViewAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
    private RecyclerView qTabRecyclerView;
    private QtabListRecycleViewAdapter qAdapter;
    private RecyclerView foldersRecyclerView;
    private Timer timerCat =new Timer();
    private Timer timer2 = new Timer();


    private PlayerController() {
    }

    public List<Folder> getCurCatList() {
        return this.curCatList;
    }

    public void setCurCatList(List<Folder> curCatList) {
        this.curCatList = curCatList;
    }

    public List<Folder> loadCatFolderList(int typeId){
        List<Folder> ret = null;
        try {
            Where<Folder, ?> where = App.getHelper().getDao(Folder.class).queryBuilder().where();
            if(typeId>0)
            ret= where.eq("typeId",typeId).queryBuilder().orderBy("orderSeq",false).query();
            else  ret= where.eq("isFav",1).queryBuilder().orderBy("orderSeq",false).query();


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            ret = new ArrayList<>();
        }
        return ret;
    }

    public synchronized void refreshCats() {


        this.cats = new ArrayList<String>();
        Map<String, Integer> allMap = App.getInstance().getStoreTypeMap();
        this.allTypeMap = new LinkedHashMap<>();
        this.typeIdMap = new LinkedHashMap<>();
        for (String key : allMap.keySet()) {
            Integer value = allMap.get(key);
                this.cats.add(key);
                allTypeMap.put(key, value);
                typeIdMap.put(value, key);

        }
        if (catsAdaper != null) {
            catsAdaper.notifyDataSetChanged();
        }

    }

    public void setRVAdapts(FolderCatsListRecycleViewAdapter catsAdaper, FolderListAdapter foldersAdapter, FolderNumListRecycleViewAdapter numAdapter, QtabListRecycleViewAdapter qAdapter) {
        this.catsAdaper = catsAdaper;
        this.foldersAdapter = foldersAdapter;
        this.numAdapter = numAdapter;
        this.qAdapter=qAdapter;
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
        if(curCat==null && this.typeIdMap.values().size()>0){
            curCat = this.typeIdMap.values().iterator().next();
        }
        if(curCat!=null){
            this.setCurCat(curCat);
            int curFolderIndex=0;

            int curfolderId = vfile.getFolder().getId();
            if(this.curCatList==null || this.curCatList.size()==0){
                this.curCatList = loadCatFolderList(vfile.getFolder().getTypeId());
            }

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
                       View gridView, RecyclerView numTabRecyclerView, RecyclerView qTabRecyclerView, RecyclerView foldersRecyclerView) {
        this.videoView = videoView;
        this.girdView = gridView;
        this.numTabRecyclerView = numTabRecyclerView;
        this.qTabRecyclerView = qTabRecyclerView;
        this.foldersRecyclerView=foldersRecyclerView;
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

        if(this.curCat!=null&&this.curCat.equals(curCat)){
            return;
        }
        this.curCat = curCat;
        Integer typeId = allTypeMap.get(curCat);
        this.setCurCatId(typeId);


        this.timerCat.cancel();
        this.timerCat = new Timer();

        timerCat.schedule(new TimerTask() {
            @Override
            public void run() {

                List<Folder> catItemList = loadCatFolderList(PlayerController.this.curCatId);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PlayerController.this.setCurCatList(catItemList);
                        if(PlayerController.this.curCatList.size()>0)
                            ((HorizontalGridView)PlayerController.this.foldersRecyclerView).setSelectedPosition(0);
                        PlayerController.this.foldersAdapter.notifyDataSetChanged();
                    }
                });


            }
        },500);//延时1s执行

    }

    public Map<String, Integer> getAllTypeMap() {
        return allTypeMap;
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
        this.curFolder = curCatList.get(folderPosition);
        this.foldersAdapter.notifyItemChanged(p);
        this.foldersAdapter.notifyItemChanged(folderPosition);
        //this.timer2.purge();
        this.timer2.cancel();
        this.timer2 = new Timer();

        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                VFile[] numFiles = PlayerController.this.curFolder != null ? PlayerController.this.curFolder.getFiles().toArray(new VFile[]{}) : null;

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PlayerController.this.setNumFiles(numFiles);
                        PlayerController.this.numAdapter.notifyDataSetChanged();
                        qTabRecyclerView.setVisibility(PlayerController.this.curFolder.getTypeId()>=200&& PlayerController.this.curFolder.getTypeId() <300?View.VISIBLE:View.GONE);

                    }
                });
            }
        },500);


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

    private String[] rates = new String[]{"1080","720","540"};

    private int curRateIndex=0;
    public String[] getRates() {
        return  rates;
    }

    public boolean getRate(int position) {
        return  curRateIndex == position;
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

    public void doFav() {
       this.curFolder.setIsFav(this.curFolder.getIsFav()>0?0:1);
        try {
            App.getHelper().getDao(Folder.class).update(this.curFolder);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        this.foldersAdapter.notifyItemChanged(this.curFocusFolderIndex);

    }
}