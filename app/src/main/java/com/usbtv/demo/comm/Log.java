package com.usbtv.demo.comm;

import java.sql.SQLException;

public class Log {

    public static String TAG="app";
    private static volatile int level = Constant.INFO;

    public static void i(CharSequence message) {
        android.util.Log.i(TAG, (String) message);
    }

    public static void d(CharSequence message) {
        android.util.Log.d(TAG, (String) message);

    }

    public static void e(CharSequence message) {
        android.util.Log.e(TAG, (String) message);

    }

    public static void setLevel(int level) {
        if (level != Constant.NONE && level != Constant.INFO && level != Constant.DEBUG && level != Constant.ERROR)
            throw new IllegalArgumentException("日志参数信息设置错误！");
        Log.level = level;
    }

    public static int getLevel() {
        return level;
    }

    public static CharSequence getStackTraceString(SQLException throwables) {
        return android.util.Log.getStackTraceString(throwables);
    }

    public static void e(Throwable e) {
        e(android.util.Log.getStackTraceString(e));
    }
}
