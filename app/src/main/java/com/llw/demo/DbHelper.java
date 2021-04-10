package com.llw.demo;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 5;
    private static final String DATABASE_NAME = "data.db";
    private static SQLiteDatabase mSqLiteDatabase;

    /**
     * 在构造方法中，尽量少的向外提供参数接口，防止其他类修改数据库名称、
     * 版本号。
     */
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);

    }

    public static SQLiteDatabase getDBInstance() {
        if (mSqLiteDatabase == null)
            mSqLiteDatabase = new DbHelper(App.getInstance().getApplicationContext()).getWritableDatabase();
        return mSqLiteDatabase;
    }

    public static boolean insert(VideoItem item) {

        getDBInstance().execSQL("insert into " + VideoItem.Table.NAME + "(" +
                        VideoItem.Table.Cols.URL + "," +
                        VideoItem.Table.Cols.TITLE + "," +
                        VideoItem.Table.Cols.CREATE_DATE + "," +
                        VideoItem.Table.Cols.CAT + "," +
                        VideoItem.Table.Cols.LAST_UPATE + "," +
                        VideoItem.Table.Cols.TIMES + "," +
                        VideoItem.Table.Cols.STATUS + "," +
                        VideoItem.Table.Cols.SEQ +
                        ") values(?,?,?,?,?,?,?,?)",
                new Object[]{item.url,
                        item.title,
                        item.createDate,
                        item.cat,
                        item.lastUpdate,
                        item.times,
                        item.status,
                        item.seq
                });

        return true;
    }

    public static boolean delete(Integer id) {

        getDBInstance().execSQL("delete from  " + VideoItem.Table.NAME
                        + " where " + VideoItem.Table.Cols.ID + " =?",
                new Integer[]{id
                });
        return true;
    }

    public static ArrayList<VideoItem> getList() {

        ArrayList<VideoItem> result = new ArrayList<VideoItem>();

        Cursor cursor = getDBInstance().rawQuery("select "
                + VideoItem.Table.Cols.ID + "," +
                VideoItem.Table.Cols.URL + "," +
                VideoItem.Table.Cols.TITLE + "," +
                VideoItem.Table.Cols.CREATE_DATE + "," +
                VideoItem.Table.Cols.CAT + "," +
                VideoItem.Table.Cols.LAST_UPATE + "," +
                VideoItem.Table.Cols.TIMES + "," +
                VideoItem.Table.Cols.STATUS + "," +
                VideoItem.Table.Cols.SEQ
                + " from " + VideoItem.Table.NAME
                + " order by " + VideoItem.Table.Cols.LAST_UPATE + " asc", null);


        while (cursor.moveToNext()) {
            VideoItem item = new VideoItem();
            int i = 0;
            item.id = cursor.getInt(i++);
            item.url = cursor.getString(i++);
            item.title = cursor.getString(i++);
            item.createDate = cursor.getLong(i++);
            item.cat = cursor.getString(i++);
            item.lastUpdate = cursor.getLong(i++);
            item.times = cursor.getInt(i++);
            item.status = cursor.getInt(i++);
            item.seq = cursor.getInt(i++);
            result.add(item);

        }
        return result;
    }

    public static ArrayList<VideoItem> getActiveList() {

        ArrayList<VideoItem> result = new ArrayList<VideoItem>();

        Cursor cursor = getDBInstance().rawQuery("select "
                + VideoItem.Table.Cols.ID + "," +
                VideoItem.Table.Cols.URL + "," +
                VideoItem.Table.Cols.TITLE + "," +
                VideoItem.Table.Cols.CREATE_DATE + "," +
                VideoItem.Table.Cols.CAT + "," +
                VideoItem.Table.Cols.LAST_UPATE + "," +
                VideoItem.Table.Cols.TIMES + "," +
                VideoItem.Table.Cols.STATUS + "," +
                VideoItem.Table.Cols.SEQ
                + " from " + VideoItem.Table.NAME
                + " where " + VideoItem.Table.Cols.STATUS + "=0 order by " + VideoItem.Table.Cols.LAST_UPATE + " asc", null);


        while (cursor.moveToNext()) {
            VideoItem item = new VideoItem();
            int i = 0;
            item.id = cursor.getInt(i++);
            item.url = cursor.getString(i++);
            item.title = cursor.getString(i++);
            item.createDate = cursor.getLong(i++);
            item.cat = cursor.getString(i++);
            item.lastUpdate = cursor.getLong(i++);
            item.times = cursor.getInt(i++);
            item.status = cursor.getInt(i++);
            item.seq = cursor.getInt(i++);
            result.add(item);

        }
        return result;
    }

    public static void update(VideoItem item) {

        getDBInstance().execSQL("update " + VideoItem.Table.NAME + " set " +
                        VideoItem.Table.Cols.URL + "=?," +
                        VideoItem.Table.Cols.TITLE + "=?," +
                        VideoItem.Table.Cols.CREATE_DATE + "=?," +
                        VideoItem.Table.Cols.CAT + "=?," +
                        VideoItem.Table.Cols.LAST_UPATE + "=?," +
                        VideoItem.Table.Cols.TIMES + "=?," +
                        VideoItem.Table.Cols.STATUS + "=?," +
                        VideoItem.Table.Cols.SEQ + "=?"
                        + " where  " + VideoItem.Table.Cols.ID + "=?"
                , new Object[]{
                        item.url,
                        item.title,
                        item.createDate,
                        item.cat,
                        item.lastUpdate,
                        item.times,
                        item.status,
                        item.seq,
                        item.id
                });

    }

    public static VideoItem UpdateOrInsert(String videoUrl, String title) {
        VideoItem item = findByTitle(videoUrl);
        if(item!=null){
            item.title = title;
            update(item);

        }
        else{
            item = new VideoItem();
            item.url = videoUrl;
            item.title=title;
            insert(item);
            return findByTitle(videoUrl);
        }

        return item;
    }

    private static VideoItem findByTitle(String videoUrl) {
        Cursor cursor = getDBInstance().rawQuery("select "
                + VideoItem.Table.Cols.ID + "," +
                VideoItem.Table.Cols.URL + "," +
                VideoItem.Table.Cols.TITLE + "," +
                VideoItem.Table.Cols.CREATE_DATE + "," +
                VideoItem.Table.Cols.CAT + "," +
                VideoItem.Table.Cols.LAST_UPATE + "," +
                VideoItem.Table.Cols.TIMES + "," +
                VideoItem.Table.Cols.STATUS + "," +
                VideoItem.Table.Cols.SEQ
                + " from " + VideoItem.Table.NAME
                + " where " + VideoItem.Table.Cols.URL + "=?", new String[]{videoUrl});


        VideoItem item = null;

        while (cursor.moveToNext()) {
            item = new VideoItem();
            int i = 0;
            item.id = cursor.getInt(i++);
            item.url = cursor.getString(i++);
            item.title = cursor.getString(i++);
            item.createDate = cursor.getLong(i++);
            item.cat = cursor.getString(i++);
            item.lastUpdate = cursor.getLong(i++);
            item.times = cursor.getInt(i++);
            item.status = cursor.getInt(i++);
            item.seq = cursor.getInt(i++);
            break;
        }
        cursor.close();
        return item;
    }

    /**
     * 负责数据库的创建和初始化，只在第一次生成数据库的时候
     * 会被调用
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + VideoItem.Table.NAME + "(" +
                VideoItem.Table.Cols.ID + " integer primary key autoincrement, " +
                VideoItem.Table.Cols.URL + " NOT NULL UNIQUE," +
                VideoItem.Table.Cols.TITLE + "," +
                VideoItem.Table.Cols.CREATE_DATE + "," +
                VideoItem.Table.Cols.CAT + "," +
                VideoItem.Table.Cols.LAST_UPATE + "," +
                VideoItem.Table.Cols.TIMES + "," +
                VideoItem.Table.Cols.STATUS + "," +
                VideoItem.Table.Cols.SEQ +
                ")"
        );
    }

    /**
     * 数据库升级的时候才会调用
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("drop table " + VideoItem.Table.NAME);
        onCreate(sqLiteDatabase);

    }
}